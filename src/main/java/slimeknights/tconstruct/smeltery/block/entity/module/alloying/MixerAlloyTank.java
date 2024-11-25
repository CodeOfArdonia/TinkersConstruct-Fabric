package slimeknights.tconstruct.smeltery.block.entity.module.alloying;

import io.github.fabricators_of_create.porting_lib.common.util.NonNullConsumer;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.alloying.IMutableAlloyTank;
import slimeknights.tconstruct.smeltery.block.entity.tank.EmptyFluidStorage;

import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Alloy tank that takes inputs from neighboring blocks
 */
@RequiredArgsConstructor
public class MixerAlloyTank implements IMutableAlloyTank {
  // parameters
  /**
   * Handler parent
   */
  private final MantleBlockEntity parent;
  /**
   * Tank for outputs
   */
  private final SlottedStorage<FluidVariant> outputTank;

  /**
   * Current temperature. Provided as a getter and setter as there are a few contexts with different source for temperature
   */
  @Getter
  @Setter
  private int temperature = 0;

  // side tank cache
  /**
   * Cache of tanks for each of the sides
   */
  private final Map<Direction, SlottedStorage<FluidVariant>> inputs = new EnumMap<>(Direction.class);
  /**
   * Map of invalidation listeners for each side
   */
  private final Map<Direction, NonNullConsumer<SlottedStorage<FluidVariant>>> listeners = new EnumMap<>(Direction.class);
  /**
   * Map of tank index to tank on the side
   */
  @Nullable
  private SlottedStorage<FluidVariant>[] indexedList = null;

  // state
  /**
   * If true, tanks are marked for refresh later
   */
  private boolean needsRefresh = true;
  /**
   * Number of currently held tanks
   */
  private int currentTanks = 0;

  @Override
  public int getTanks() {
    this.checkTanks();
    return this.currentTanks;
  }

  /**
   * Gets the map of index to direction
   */
  private SlottedStorage<FluidVariant>[] indexTanks() {
    // convert map into indexed list of fluid handlers, will be cleared next time a side updates
    if (this.indexedList == null) {
      this.indexedList = new SlottedStorage[this.currentTanks];
      if (this.currentTanks > 0) {
        int nextTank = 0;
        for (Direction direction : Direction.values()) {
          if (direction != Direction.DOWN) {
            SlottedStorage<FluidVariant> handler = this.inputs.getOrDefault(direction, null);
            if (handler != null) {
              this.indexedList[nextTank] = handler;
              nextTank++;
            }
          }
        }
      }
    }
    return this.indexedList;
  }

  /**
   * Gets the fluid handler for the given tank index
   */
  public SlottedStorage<FluidVariant> getFluidHandler(int tank) {
    this.checkTanks();
    // invalid index, nothing
    if (tank >= this.currentTanks || tank < 0) {
      return EmptyFluidStorage.INSTANCE;
    }
    return this.indexTanks()[tank];
  }

  @Override
  public FluidStack getFluidInTank(int tank) {
    this.checkTanks();
    // invalid index, nothing
    if (tank >= this.currentTanks || tank < 0) {
      return FluidStack.EMPTY;
    }
    // get the first fluid from the proper tank, we do not support multiple fluids on a side
    return new FluidStack(this.indexTanks()[tank].getSlot(0));
  }

  @Override
  public FluidStack drain(int tank, FluidStack fluidStack) {
    this.checkTanks();
    // invalid index, nothing
    if (tank >= this.currentTanks || tank < 0) {
      return FluidStack.EMPTY;
    }
    return new FluidStack(fluidStack.getType(), TransferUtil.extractFluid(this.indexTanks()[tank], fluidStack));
  }

  @Override
  public boolean canFit(FluidStack fluid, int removed) {
    this.checkTanks();
    return StorageUtil.simulateInsert(this.outputTank, fluid.getType(), fluid.getAmount(), null) == fluid.getAmount();
  }

  @Override
  public long fill(FluidStack fluidStack) {
    return TransferUtil.insertFluid(this.outputTank, fluidStack);
  }

  /**
   * Refreshes the cached tanks if needed
   * After calling this method, all five tank sides will have been fetched
   */
  private void checkTanks() {
    // need world to do anything
    Level world = this.parent.getLevel();
    if (world == null) {
      return;
    }
    if (this.needsRefresh) {
      for (Direction direction : Direction.values()) {
        // update each direction we are missing
        if (direction != Direction.DOWN && !this.inputs.containsKey(direction)) {
          BlockPos target = this.parent.getBlockPos().relative(direction);
          // limit by blocks as that gives the modpack more control, say they want to allow only scorched tanks
          if (world.getBlockState(target).is(TinkerTags.Blocks.ALLOYER_TANKS)) {
            // if we found a tank, increment the number of tanks
            Storage<FluidVariant> capability = FluidStorage.SIDED.find(world, target, direction.getOpposite());
            if (capability != null && capability instanceof SlottedStorage<FluidVariant> storage) {
              // attach a listener so we know when the side invalidates
//              capability.addListener(listeners.computeIfAbsent(direction, dir -> new WeakConsumerWrapper<>(this, (self, handler) -> {
//                if (handler == self.inputs.get(dir)) { TODO: PORT replacement?
//                  refresh(dir, false);
//                }
//              })));
              this.inputs.put(direction, storage);
              this.currentTanks++;
            } else {
              this.inputs.put(direction, null);
            }
          }
        }
      }
      this.needsRefresh = false;
    }
  }

  /**
   * Called on block update or when a capability invalidates to mark that a direction needs updates
   *
   * @param direction  Side updating
   * @param checkInput If true, validates that the side contains an input before reducing tank count. False when invalidated through the capability
   */
  public void refresh(Direction direction, boolean checkInput) {
    if (direction == Direction.DOWN) {
      return;
    }
    if (!checkInput || (this.inputs.containsKey(direction) && this.inputs.get(direction) != null)) {
      this.currentTanks--;
    }
    this.inputs.remove(direction);
    this.needsRefresh = true;
    this.indexedList = null;
  }
}
