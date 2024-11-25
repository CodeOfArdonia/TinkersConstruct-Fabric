package slimeknights.tconstruct.smeltery.block.entity;

import io.github.fabricators_of_create.porting_lib.block.CustomRenderBoundingBoxBlockEntity;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.StorageProvider;
import lombok.Getter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.network.FaucetActivationPacket;

import static slimeknights.tconstruct.smeltery.block.FaucetBlock.FACING;

@SuppressWarnings("removal")
public class FaucetBlockEntity extends MantleBlockEntity implements CustomRenderBoundingBoxBlockEntity {

  /**
   * amount of MB to extract from the input at a time
   */
  public static final long PACKET_SIZE = FluidValues.INGOT;
  /**
   * Transfer rate of the faucet
   */
  public static final int DROPLETS_PER_TICK = 810;

  public static final BlockEntityTicker<FaucetBlockEntity> SERVER_TICKER = (level, pos, world, self) -> self.tick();

  private static final String TAG_DRAINED = "drained";
  private static final String TAG_RENDER_FLUID = "render_fluid";
  private static final String TAG_STOP = "stop";
  private static final String TAG_STATE = "state";
  private static final String TAG_LAST_REDSTONE = "lastRedstone";

  /**
   * If true, faucet is currently pouring
   */
  private FaucetState faucetState = FaucetState.OFF;
  /**
   * If true, redstone told this faucet to stop, so stop when ready
   */
  private boolean stopPouring = false;
  /**
   * Current fluid in the faucet
   */
  private FluidStack drained = FluidStack.EMPTY;
  /**
   * Fluid for rendering, used to reduce the number of packets. There is a brief moment where {@link this#drained} is empty but we should be rendering something
   */
  @Getter
  private FluidStack renderFluid = FluidStack.EMPTY;
  /**
   * Used for pulse detection
   */
  private boolean lastRedstoneState = false;

  /**
   * Fluid handler of the input to the faucet
   */
  private StorageProvider<FluidVariant> inputHandler;
  /**
   * Fluid handler of the output from the faucet
   */
  private StorageProvider<FluidVariant> outputHandler;

  public FaucetBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.faucet.get(), pos, state);
  }

  @SuppressWarnings("WeakerAccess")
  protected FaucetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }


  /* Fluid handler */

  /**
   * Finds the fluid handler on the given side
   *
   * @param side Side to check
   * @return Fluid handler
   */
  @Nullable
  private StorageProvider<FluidVariant> findFluidHandler(Direction side) {
    assert this.level != null;
    return StorageProvider.createForFluids(this.level, this.worldPosition.relative(side));
  }

  /**
   * Gets the input fluid handler
   *
   * @return Input fluid handler
   */
  @Nullable
  private Storage<FluidVariant> getInputHandler() {
    if (this.inputHandler == null) {
      this.inputHandler = this.findFluidHandler(this.getBlockState().getValue(FACING).getOpposite());
    }
    return this.inputHandler.get(this.getBlockState().getValue(FACING));
  }

  /**
   * Gets the output fluid handler
   *
   * @return Output fluid handler
   */
  @Nullable
  private Storage<FluidVariant> getOutputHandler() {
    if (this.outputHandler == null) {
      this.outputHandler = this.findFluidHandler(Direction.DOWN);
    }
    return this.outputHandler.get(Direction.UP);
  }

  /**
   * Called when a neighbor changes to invalidate the cached fluid handler
   *
   * @param neighbor Neighbor position that changed
   */
  public void neighborChanged(BlockPos neighbor) {
    // if the neighbor was below us, remove output
    if (this.worldPosition.equals(neighbor.above())) {
      this.outputHandler = null;
      // neighbor behind us
    } else if (this.worldPosition.equals(neighbor.relative(this.getBlockState().getValue(FACING)))) {
      this.inputHandler = null;
    }
  }


  /* Data */

  /**
   * Gets whether the faucet is pouring
   *
   * @return True if pouring
   */
  public boolean isPouring() {
    return this.faucetState != FaucetState.OFF;
  }

  /* Activation */

  /**
   * Toggles pouring state and initiates transfer if appropriate. Called on right click and from redstone
   */
  public void activate() {
    // don't run on client
    if (this.level == null || this.level.isClientSide) {
      return;
    }
    // already pouring? we want to start
    switch (this.faucetState) {
      // off activates the faucet
      case OFF -> {
        this.stopPouring = false;
        this.doTransfer(true);
      }
      // powered deactivates the faucet, sync to client
      case POWERED -> {
        this.faucetState = FaucetState.OFF;
        this.syncToClient(FluidStack.EMPTY, false);
      }
      // pouring means we stop pouring as soon as possible
      case POURING -> this.stopPouring = true;
    }
  }

  /**
   * Flips hasSignal and schedules a tick if appropriate.
   *
   * @param hasSignal New signal state
   */
  public void handleRedstone(boolean hasSignal) {
    if (hasSignal != this.lastRedstoneState) {
      this.lastRedstoneState = hasSignal;
      if (hasSignal) {
        if (this.level != null) {
          this.level.scheduleTick(this.worldPosition, this.getBlockState().getBlock(), 2);
        }
      } else if (this.faucetState == FaucetState.POWERED) {
        this.faucetState = FaucetState.OFF;
        this.syncToClient(FluidStack.EMPTY, false);
      }
    }
  }


  /* Pouring */

  /**
   * Handles server ticks
   */
  private void tick() {
    // nothing to do if not pouring
    if (this.faucetState == FaucetState.OFF) {
      return;
      // if powered and we can transfer, schedule transfer for next tick
    } else if (this.faucetState == FaucetState.POWERED && this.doTransfer(false)) {
      this.faucetState = FaucetState.POURING;
      return;
    }

    // continue current stack
    if (!this.drained.isEmpty()) {
      this.pour();
      // stop if told to stop once done
    } else if (this.stopPouring) {
      this.reset();
      // otherwise keep going
    } else {
      this.doTransfer(true);
    }
  }

  /**
   * Initiate fluid transfer
   */
  private boolean doTransfer(boolean execute) {
    // still got content left
    Storage<FluidVariant> input = this.getInputHandler();
    Storage<FluidVariant> output = this.getOutputHandler();
    if (input != null && output != null) {
      // can we drain?
      FluidStack drained;
      try (Transaction tx = TransferUtil.getTransaction()) {
        drained = TransferUtil.extractAnyFluid(input, PACKET_SIZE, tx);
      }
      if (!drained.isEmpty() && !FluidVariantAttributes.isLighterThanAir(drained.getType())) {
        // can we fill
        Transaction sim = TransferUtil.getTransaction();
        long filled = StorageUtil.simulateInsert(output, drained.getType(), drained.getAmount(), sim);
        sim.close();
        if (filled > 0) {
          // fill if requested
          if (execute) {
            // drain the liquid and transfer it, buffer the amount for delay
            try (Transaction tx = TransferUtil.getTransaction()) {
              this.drained = new FluidStack(drained.getType(), input.extract(drained.getType(), filled, tx));
              tx.commit();
            }

            // sync to clients if we have changes
            if (this.faucetState == FaucetState.OFF || !this.renderFluid.isFluidEqual(drained)) {
              this.syncToClient(this.drained, true);
            }
            this.faucetState = FaucetState.POURING;
            // pour after initial packet, in case we end up resetting later
            this.pour();
          }
          return true;
        }
      }

      // if powered, keep faucet running
      if (this.lastRedstoneState) {
        // sync if either we were not pouring before (particle effects), or if the client thinks we have fluid
        if (execute && (this.faucetState == FaucetState.OFF || !this.renderFluid.isFluidEqual(FluidStack.EMPTY))) {
          this.syncToClient(FluidStack.EMPTY, true);
        }
        this.faucetState = FaucetState.POWERED;
        return false;
      }
    }
    // reset if not powered, or if nothing to do
    if (execute) {
      this.reset();
    }
    return false;
  }

  /**
   * Takes the liquid inside and executes one pouring step.
   */
  private void pour() {
    if (this.drained.isEmpty()) {
      return;
    }

    // ensure we have an output
    Storage<FluidVariant> output = this.getOutputHandler();
    if (output != null) {
      FluidStack fillStack = this.drained.copy();
      fillStack.setAmount(Math.min(this.drained.getAmount(), DROPLETS_PER_TICK));

      // can we fill?
      Transaction sim = TransferUtil.getTransaction();
      long filled = StorageUtil.simulateInsert(output, fillStack.getType(), fillStack.getAmount(), sim);
      sim.close();
      if (filled > 0) {
        // update client if they do not think we have fluid
        if (!this.renderFluid.isFluidEqual(this.drained)) {
          this.syncToClient(this.drained, true);
        }

        // transfer it
        this.drained.shrink(filled);
        fillStack.setAmount(filled);
        try (Transaction tx = TransferUtil.getTransaction()) {
          output.insert(fillStack.getType(), fillStack.getAmount(), tx);
          tx.commit();
        }
      }
    } else {
      // output got lost. all liquid buffered is lost.
      this.reset();
    }
  }

  /**
   * Resets TE to default state.
   */
  private void reset() {
    this.stopPouring = false;
    this.drained = FluidStack.EMPTY;
    if (this.faucetState != FaucetState.OFF || !this.renderFluid.isFluidEqual(this.drained)) {
      this.faucetState = FaucetState.OFF;
      this.syncToClient(FluidStack.EMPTY, false);
    }
  }

  @Override
  public AABB getRenderBoundingBox() {
    return new AABB(this.worldPosition.getX(), this.worldPosition.getY() - 1, this.worldPosition.getZ(), this.worldPosition.getX() + 1, this.worldPosition.getY() + 1, this.worldPosition.getZ() + 1);
  }


  /* NBT and networking */

  /**
   * Sends an update to the client with the most recent
   *
   * @param fluid     New fluid
   * @param isPouring New isPouring status
   */
  private void syncToClient(FluidStack fluid, boolean isPouring) {
    this.renderFluid = fluid.copy();
    if (this.level instanceof ServerLevel) {
      TinkerNetwork.getInstance().sendToClientsAround(new FaucetActivationPacket(this.worldPosition, fluid, isPouring), (ServerLevel) this.level, this.getBlockPos());
    }
  }

  /**
   * Sets draining fluid to specified stack.
   *
   * @param fluid new FluidStack
   */
  public void onActivationPacket(FluidStack fluid, boolean isPouring) {
    // pouring and powered are interchangable on the client
    this.faucetState = isPouring ? FaucetState.POURING : FaucetState.OFF;
    this.renderFluid = fluid;
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag compound) {
    super.saveSynced(compound);
    compound.putByte(TAG_STATE, (byte) this.faucetState.ordinal());
    if (!this.renderFluid.isEmpty()) {
      compound.put(TAG_RENDER_FLUID, this.renderFluid.writeToNBT(new CompoundTag()));
    }
  }

  @Override
  public void saveAdditional(CompoundTag compound) {
    super.saveAdditional(compound);
    compound.putBoolean(TAG_STOP, this.stopPouring);
    compound.putBoolean(TAG_LAST_REDSTONE, this.lastRedstoneState);
    if (!this.drained.isEmpty()) {
      compound.put(TAG_DRAINED, this.drained.writeToNBT(new CompoundTag()));
    }
  }

  @Override
  public void load(CompoundTag compound) {
    super.load(compound);

    this.faucetState = FaucetState.fromIndex(compound.getByte(TAG_STATE));
    this.stopPouring = compound.getBoolean(TAG_STOP);
    this.lastRedstoneState = compound.getBoolean(TAG_LAST_REDSTONE);
    // fluids
    if (compound.contains(TAG_DRAINED, Tag.TAG_COMPOUND)) {
      this.drained = FluidStack.loadFluidStackFromNBT(compound.getCompound(TAG_DRAINED));
    } else {
      this.drained = FluidStack.EMPTY;
    }
    if (compound.contains(TAG_RENDER_FLUID, Tag.TAG_COMPOUND)) {
      this.renderFluid = FluidStack.loadFluidStackFromNBT(compound.getCompound(TAG_RENDER_FLUID));
    } else {
      this.renderFluid = FluidStack.EMPTY;
    }
  }

  private enum FaucetState {
    OFF,
    POURING,
    POWERED;

    /**
     * Gets the state for the given index
     */
    public static FaucetState fromIndex(int index) {
      switch (index) {
        case 1:
          return POURING;
        case 2:
          return POWERED;
      }
      return OFF;
    }
  }
}
