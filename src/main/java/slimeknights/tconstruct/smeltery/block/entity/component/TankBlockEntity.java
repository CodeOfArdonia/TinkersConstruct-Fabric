package slimeknights.tconstruct.smeltery.block.entity.component;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.client.model.data.SinglePropertyData;
import slimeknights.tconstruct.library.client.model.ModelProperties;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.item.TankItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class TankBlockEntity extends SmelteryComponentBlockEntity implements ITankBlockEntity, SidedStorageBlockEntity {

  /**
   * Max capacity for the tank
   */
  public static final long DEFAULT_CAPACITY = FluidConstants.BUCKET * 4;

  /**
   * Gets the capacity for the given block
   *
   * @param block block
   * @return Capacity
   */
  public static long getCapacity(Block block) {
    if (block instanceof ITankBlock) {
      return ((ITankBlock) block).getCapacity();
    }
    return DEFAULT_CAPACITY;
  }

  /**
   * Gets the capacity for the given item
   *
   * @param item item
   * @return Capacity
   */
  public static long getCapacity(Item item) {
    if (item instanceof BlockItem) {
      return getCapacity(((BlockItem) item).getBlock());
    }
    return DEFAULT_CAPACITY;
  }

  /**
   * Internal fluid tank instance
   */
  @Getter
  protected final FluidTankAnimated tank;
  /**
   * Tank data for the model
   */
  private final SinglePropertyData<FluidTank> modelData;
  /**
   * Last comparator strength to reduce block updates
   */
  @Getter
  @Setter
  private int lastStrength = -1;

  public TankBlockEntity(BlockPos pos, BlockState state) {
    this(pos, state, state.getBlock() instanceof ITankBlock tank
      ? tank
      : TinkerSmeltery.searedTank.get(TankType.FUEL_TANK));
  }

  /**
   * Main constructor
   */
  public TankBlockEntity(BlockPos pos, BlockState state, ITankBlock block) {
    this(TinkerSmeltery.tank.get(), pos, state, block);
  }

  /**
   * Extendable constructor
   */
  @SuppressWarnings("WeakerAccess")
  protected TankBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, ITankBlock block) {
    super(type, pos, state);
    this.tank = new FluidTankAnimated(block.getCapacity(), this);
    this.modelData = new SinglePropertyData<>(ModelProperties.FLUID_TANK, this.tank);
  }


  /*
   * Tank methods
   */

  @Override
  @Nonnull
  public Storage<FluidVariant> getFluidStorage(@Nullable Direction direction) {
    return this.tank;
  }

  @Nonnull
  @Override
  public Object getRenderData() {
    return this.modelData;
  }

  @Override
  public void onTankContentsChanged() {
    ITankBlockEntity.super.onTankContentsChanged();
    if (this.level != null) {
      this.level.getLightEngine().checkBlock(this.worldPosition);
    }
  }

  @Override
  public void updateFluidTo(FluidStack fluid) {
    ITankBlockEntity.super.updateFluidTo(fluid);
    // update light if the fluid changes
    if (this.level != null) {
      this.level.getLightEngine().checkBlock(this.worldPosition);
    }
  }


  /*
   * NBT
   */

  /**
   * Sets the tag on the stack based on the contained tank
   *
   * @param stack Stack
   */
  public void setTankTag(ItemStack stack) {
    TankItem.setTank(stack, this.tank);
  }

  /**
   * Updates the tank from an NBT tag, used in the block
   *
   * @param nbt tank NBT
   */
  public void updateTank(CompoundTag nbt) {
    if (nbt.isEmpty()) {
      this.tank.setFluid(FluidStack.EMPTY);
    } else {
      this.tank.readFromNBT(nbt);
      if (this.level != null) {
        this.level.getLightEngine().checkBlock(this.worldPosition);
      }
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void load(CompoundTag tag) {
    this.tank.setCapacity(getCapacity(this.getBlockState().getBlock()));
    this.updateTank(tag.getCompound(NBTTags.TANK));
    super.load(tag);
  }

  @Override
  public void saveSynced(CompoundTag tag) {
    super.saveSynced(tag);
    // want tank on the client on world load
    if (!this.tank.isEmpty()) {
      tag.put(NBTTags.TANK, this.tank.writeToNBT(new CompoundTag()));
    }
  }

  /**
   * Interface for blocks to return their capacity
   */
  public interface ITankBlock {

    /**
     * Gets the capacity for this tank
     */
    long getCapacity();
  }
}
