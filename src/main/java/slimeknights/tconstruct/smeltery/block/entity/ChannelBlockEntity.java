package slimeknights.tconstruct.smeltery.block.entity;

import io.github.fabricators_of_create.porting_lib.block.CustomRenderBoundingBoxBlockEntity;
import io.github.fabricators_of_create.porting_lib.common.util.NonNullConsumer;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.util.StorageProvider;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.library.fluid.FillOnlyFluidHandler;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.ChannelBlock;
import slimeknights.tconstruct.smeltery.block.ChannelBlock.ChannelConnection;
import slimeknights.tconstruct.smeltery.block.entity.tank.ChannelSideTank;
import slimeknights.tconstruct.smeltery.block.entity.tank.ChannelTank;
import slimeknights.tconstruct.smeltery.network.ChannelFlowPacket;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket;
import slimeknights.tconstruct.smeltery.network.FluidUpdatePacket.IFluidPacketReceiver;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Logic for channel fluid transfer
 */
@SuppressWarnings("UnstableApiUsage")
public class ChannelBlockEntity extends MantleBlockEntity implements IFluidPacketReceiver, SidedStorageBlockEntity, CustomRenderBoundingBoxBlockEntity {

  /**
   * Channel internal tank
   */
  private final ChannelTank tank = new ChannelTank(FaucetBlockEntity.DROPLETS_PER_TICK * 3, this);
  /**
   * Handler to return from channel top
   */
  private final Storage<FluidVariant> topHandler = new FillOnlyFluidHandler(this.tank);
  /**
   * Tanks for inserting on each side
   */
  private final Map<Direction, Storage<FluidVariant>> sideTanks = Util.make(new EnumMap<>(Direction.class), map -> {
    for (Direction direction : Plane.HORIZONTAL) {
      map.put(direction, new ChannelSideTank(this, this.tank, direction));
    }
  });
  /**
   * Tanks for inserting on each side
   */
  private final Map<Direction, Storage<FluidVariant>> sideHandlers = new EnumMap<>(Direction.class);
  /**
   * Tanks for alerting neighbors the given side is present
   */
  private final Map<Direction, Storage<FluidVariant>> emptySideHandler = new EnumMap<>(Direction.class);

  /**
   * Cache of tanks on all neighboring sides
   */
  private final Map<Direction, StorageProvider<FluidVariant>> neighborTanks = new EnumMap<>(Direction.class);
  /**
   * Consumers to attach to each of the neighbors
   */
  private final Map<Direction, NonNullConsumer<Storage<FluidVariant>>> neighborConsumers = new EnumMap<>(Direction.class);

  /**
   * Ticker instance for this TE, serverside only
   */
  public static final BlockEntityTicker<ChannelBlockEntity> SERVER_TICKER = (level, pos, state, self) -> self.tick(state);

  /**
   * Stores if the channel is currently flowing, set to 2 to allow a small buffer
   */
  private final byte[] isFlowing = new byte[5];

  public ChannelBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.channel.get(), pos, state);
  }

  protected ChannelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  /**
   * Gets the central fluid tank of this channel
   *
   * @return Central tank
   */
  public FluidStack getFluid() {
    return this.tank.getFluid();
  }

  @Override
  public AABB getRenderBoundingBox() {
    return new AABB(this.worldPosition.getX(), this.worldPosition.getY() - 1, this.worldPosition.getZ(), this.worldPosition.getX() + 1, this.worldPosition.getY() + 1, this.worldPosition.getZ() + 1);
  }

  /* Fluid handlers */

  @Override
  public Storage<FluidVariant> getFluidStorage(@Nullable Direction side) {
    if (side == null || side == Direction.UP) {
      return this.topHandler;
    }
    // side tanks keep track of which side inserts
    if (side != Direction.DOWN) {
      ChannelConnection connection = this.getBlockState().getValue(ChannelBlock.DIRECTION_MAP.get(side));
      if (connection == ChannelConnection.IN) {
        return this.sideHandlers.computeIfAbsent(side, this.sideTanks::get);
      }
      // for out, return an empty fluid handler so the block we are pouring into knows we support fluids, even though we disallow any interaction
      // this will get invalidated when the connection goes back to in later
      if (connection == ChannelConnection.OUT) {
        return this.emptySideHandler.computeIfAbsent(side, s -> Storage.empty());
      }
    }
    return null;
  }

  /**
   * Gets the fluid handler directly from a neighbor, skipping the cache
   *
   * @param side Side of the neighbor to fetch
   * @return Fluid handler, or empty
   */
  private StorageProvider<FluidVariant> getNeighborHandlerUncached(Direction side) {
    assert this.level != null;
    return StorageProvider.createForFluids(this.level, this.worldPosition.relative(side));
  }

  /**
   * Gets the fluid handler from a neighbor
   *
   * @param side Side of the neighbor to fetch
   * @return Fluid handler, or empty
   */
  protected StorageProvider<FluidVariant> getNeighborHandler(Direction side) {
    return this.neighborTanks.computeIfAbsent(side, this::getNeighborHandlerUncached);
  }

  /**
   * Removes a cached handler from the given neighbor as the block changed
   *
   * @param side Side to remove
   */
  public void removeCachedNeighbor(Direction side) {
    this.neighborTanks.remove(side);
  }

  /**
   * Refreshes a neighbor based on the new connection
   *
   * @param state The state that will later be put in the world, may not be the state currently in the world
   * @param side  Side to update
   */
  public void refreshNeighbor(BlockState state, Direction side) {
    // for below, only thing that needs to invalidate is if we are no longer connected down, remove the listener below
    if (side == Direction.DOWN) {
      if (!state.getValue(ChannelBlock.DOWN)) {
        this.neighborTanks.remove(Direction.DOWN);
      }
    } else if (side != Direction.UP) {
      ChannelConnection connection = state.getValue(ChannelBlock.DIRECTION_MAP.get(side));
      // if no longer flowing out, remove the neighbor tank
      if (connection != ChannelConnection.OUT) {
        this.neighborTanks.remove(Direction.DOWN);
        // remove the empty handler, mostly so the neighbor knows to update
        Storage<FluidVariant> handler = this.emptySideHandler.remove(side);
        if (handler != null) {
//					handler.invalidate();
        }
      }
      // remove the side handler, if we changed from out or from in the handler is no longer correct
      if (connection != ChannelConnection.IN) {
        Storage<FluidVariant> handler = this.sideHandlers.remove(side);
        if (handler != null) {
//					handler.invalidate();
        }
      }
    }
  }

//	@Override
//	public void invalidateCaps() {
//		super.invalidateCaps();
//		topHandler.invalidate();
//		for (LazyOptional<IFluidHandler> handler : sideHandlers.values()) {
//			if (handler != null) {
//				handler.invalidate();
//			}
//		}
//		for (LazyOptional<IFluidHandler> handler : emptySideHandler.values()) {
//			if (handler != null) {
//				handler.invalidate();
//			}
//		}
//	}


  /* Flowing property */

  /**
   * Gets the index for the given side for flowing. Same as regular index but without up
   *
   * @param side Side to index
   * @return Flow index
   */
  private int getFlowIndex(Direction side) {
    if (side.getAxis().isVertical()) {
      return 0;
    }
    return side.get3DDataValue() - 1;
  }

  /**
   * Marks the given side as flowing for the sake of rendering
   *
   * @param side    Side to set
   * @param flowing True to mark it as flowing
   */
  public void setFlow(Direction side, boolean flowing) {
    if (side == Direction.UP) {
      return;
    }
    // update flowing state
    int index = this.getFlowIndex(side);
    boolean wasFlowing = this.isFlowing[index] > 0;
    this.isFlowing[index] = (byte) (flowing ? 2 : 0);

    // send packet to client if it changed
    if (wasFlowing != flowing && this.level != null && !this.level.isClientSide) {
      this.syncFlowToClient(side, flowing);
    }
  }

  /**
   * Checks if the given side is flowing
   *
   * @param side Side to check
   * @return True if flowing
   */
  public boolean isFlowing(Direction side) {
    if (side == Direction.UP) {
      return false;
    }

    return this.isFlowing[this.getFlowIndex(side)] > 0;
  }


  /* Utilities */

  /**
   * Gets the connection for a side
   *
   * @param side Side to query
   * @return Connection on the specified side
   */
  protected boolean isOutput(Direction side) {
    // just always return in for up, thats fine
    if (side == Direction.UP) {
      return false;
    }
    // down is boolean, sides is multistate
    if (side == Direction.DOWN) {
      return this.getBlockState().getValue(ChannelBlock.DOWN);
    }
    return this.getBlockState().getValue(ChannelBlock.DIRECTION_MAP.get(side)) == ChannelConnection.OUT;
  }

  /**
   * Counts the number of side outputs on the given side
   *
   * @param state State to check
   * @return Number of outputs
   */
  private static int countOutputs(BlockState state) {
    int count = 0;
    for (Direction direction : Plane.HORIZONTAL) {
      if (state.getValue(ChannelBlock.DIRECTION_MAP.get(direction)) == ChannelConnection.OUT) {
        count++;
      }
    }
    return count;
  }

  /**
   * Syncs the given flowing state to the client side
   *
   * @param side    Side to sync
   * @param flowing Flowing state to sync
   */
  private void syncFlowToClient(Direction side, boolean flowing) {
    TinkerNetwork.getInstance().sendToClientsAround(new ChannelFlowPacket(this.worldPosition, side, flowing), this.level, this.worldPosition);
  }

  public static long clampL(long f, long g, long h) {
    return f < g ? g : Math.min(f, h);
  }

  /* Flow */

  /**
   * Server ticking logic
   */
  private void tick(BlockState state) {
    // must have fluid first
    FluidStack fluid = this.tank.getFluid();
    if (!fluid.isEmpty()) {
      // if we have down and can flow, skip sides
      boolean hasFlown = false;
      if (state.getValue(ChannelBlock.DOWN)) {
        hasFlown = this.trySide(Direction.DOWN, FaucetBlockEntity.DROPLETS_PER_TICK);
      }
      // try sides if we have any sides
      int outputs = countOutputs(state);
      if (!hasFlown && outputs > 0) {
        // split the fluid evenly between sides
        long flowRate = clampL(this.tank.getMaxUsable() / outputs, 1, FaucetBlockEntity.DROPLETS_PER_TICK);
        // then transfer on each side
        for (Direction side : Plane.HORIZONTAL) {
          this.trySide(side, flowRate);
        }
      }
    }

    // clear flowing if we should no longer flow on a side
    for (int i = 0; i < 5; i++) {
      if (this.isFlowing[i] > 0) {
        this.isFlowing[i]--;
        if (this.isFlowing[i] == 0) {
          Direction direction;
          if (i == 0) {
            direction = Direction.DOWN;
          } else {
            direction = Direction.from3DDataValue(i + 1);
          }
          this.syncFlowToClient(direction, false);
        }
      }
    }

    this.tank.freeFluid();
  }

  /**
   * Tries transferring fluid on a single side of the channel
   *
   * @param side     Side to transfer from
   * @param flowRate Maximum amount to output
   * @return True if the side transferred fluid
   */
  protected boolean trySide(Direction side, long flowRate) {
    if (this.tank.isEmpty() || !this.isOutput(side)) {
      return false;
    }

    // get the handler on the side, try filling
    // TODO: handle the case of no fluid handler on the side that may later become a handler
    return Optional.ofNullable(this.getNeighborHandler(side).get(side.getOpposite())).filter(handler -> this.fill(side, handler, flowRate))
      .isPresent();
  }

  /**
   * Fill the fluid handler on the given side
   *
   * @param side    Side to fill
   * @param handler Handler to fill
   * @param amount  Amount to fill
   * @return True if the side successfully filled something
   */
  protected boolean fill(Direction side, Storage<FluidVariant> handler, long amount) {
    // make sure we do not allow more than the fluid allows, should not happen but just in case
    long usable = Math.min(this.tank.getMaxUsable(), amount);
    if (usable > 0) {
      // see how much works
      Transaction sim = TransferUtil.getTransaction();
      long fluid = StorageUtil.simulateExtract(this.tank, this.tank.getResource(), usable, sim);
      long filled = StorageUtil.simulateInsert(handler, this.tank.getResource(), fluid, sim);
      sim.close();
      if (filled > 0) {
        // drain the amount that worked
        try (Transaction tx = TransferUtil.getTransaction()) {
          var resource = this.tank.getResource();
          fluid = this.tank.extract(resource, filled, tx);
          handler.insert(resource, fluid, tx);
          tx.commit();
        }

        // mark that the side is flowing
        this.setFlow(side, true);
        return true;
      }
    }

    // failed to flow, mark side as not flowing
    this.setFlow(side, false);
    return false;
  }


  /* NBT and sync */
  private static final String TAG_IS_FLOWING = "is_flowing";
  private static final String TAG_TANK = "tank";

  /**
   * Sends a fluid update to the client with the current fluid
   */
  public void sendFluidUpdate() {
    if (this.level != null && !this.level.isClientSide) {
      TinkerNetwork.getInstance().sendToClientsAround(new FluidUpdatePacket(this.worldPosition, this.getFluid()), this.level, this.worldPosition);
    }
  }

  @Override
  public void updateFluidTo(FluidStack fluid) {
    this.tank.setFluid(fluid);
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag nbt) {
    super.saveSynced(nbt);
    nbt.putByteArray(TAG_IS_FLOWING, this.isFlowing);
    nbt.put(TAG_TANK, this.tank.writeToNBT(new CompoundTag()));
  }

  @Override
  public void load(CompoundTag nbt) {
    super.load(nbt);

    // isFlowing
    if (nbt.contains(TAG_IS_FLOWING)) {
      byte[] nbtFlowing = nbt.getByteArray(TAG_IS_FLOWING);
      int max = Math.min(5, nbtFlowing.length);
      for (int i = 0; i < max; i++) {
        byte b = nbtFlowing[i];
        if (b > 2) {
          this.isFlowing[i] = 2;
        } else if (b < 0) {
          this.isFlowing[i] = 0;
        } else {
          this.isFlowing[i] = b;
        }
      }
    }

    // tank
    CompoundTag tankTag = nbt.getCompound(TAG_TANK);
    this.tank.readFromNBT(tankTag);
  }
}
