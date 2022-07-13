package slimeknights.tconstruct.smeltery.block.entity.component;

import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTransferable;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemTransferable;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Shared logic between drains and ducts
 */
public abstract class SmelteryInputOutputBlockEntity<T> extends SmelteryComponentBlockEntity {
  /** Capability this TE watches */
  private final Class<T> capability;
  /** Empty capability for in case the valid capability becomes invalid without invalidating */
  protected final Storage<T> emptyInstance;

  protected SmelteryInputOutputBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Class<T> capability, Storage<T> emptyInstance) {
    super(type, pos, state);
    this.capability = capability;
    this.emptyInstance = emptyInstance;
  }

  @Override
  protected void setMaster(@Nullable BlockPos master, @Nullable Block block) {
    assert level != null;

    // if we have a new master, invalidate handlers
    boolean masterChanged = false;
    if (!Objects.equals(getMasterPos(), master)) {
      masterChanged = true;
    }
    super.setMaster(master, block);
    // notify neighbors of the change (state change skips the notify flag)
    if (masterChanged) {
      level.blockUpdated(worldPosition, getBlockState().getBlock());
    }
  }

  /**
   * Gets the capability to store in this IO block. Capability parent should have the proper listeners attached
   * @param parent  Parent tile entity
   * @return  Capability from parent, or empty if absent
   */
  protected Storage<T> getCapability(BlockEntity parent) {
    return TransferUtil.getStorage(parent, null, capability);
  }

  /**
   * Fetches the capability handlers if missing
   */
  protected Storage<T> getCachedCapability() {
    if (validateMaster()) {
      BlockPos master = getMasterPos();
      if (master != null && this.level != null) {
        BlockEntity te = level.getBlockEntity(master);
        if (te != null) {
          return getCapability(te);
        }
      }
    }
    return null;
  }
  
//  @Nonnull
//  @Override
//  public <C> LazyOptional<C> getCapability(Capability<C> capability, @Nullable Direction facing) {
//    if (capability == this.capability) {
//      return getCachedCapability().cast();
//    }
//    return super.getCapability(capability, facing);
//  }

  /** Fluid implementation of smeltery IO */
  public static abstract class SmelteryFluidIO extends SmelteryInputOutputBlockEntity<FluidVariant> implements FluidTransferable {
    protected SmelteryFluidIO(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, FluidVariant.class, Storage.empty());
    }

    /** Wraps the given capability */
    protected Storage<FluidVariant> makeWrapper(Storage<FluidVariant> capability) {
      if (capability != null)
        return capability;
      return emptyInstance;
    }

    @Override
    protected Storage<FluidVariant> getCapability(BlockEntity parent) {
      // fluid capability is not exposed directly in the smeltery
      if (parent instanceof ISmelteryTankHandler) {
        Storage<FluidVariant> capability = ((ISmelteryTankHandler) parent).getTank();
        if (capability != null) {
          return makeWrapper(capability);
        }
      }
      return null;
    }
  
    @Nullable
    @Override
    public Storage<FluidVariant> getFluidStorage(@Nullable Direction direction) {
      return getCachedCapability();
    }
  }

  /** Item implementation of smeltery IO */
  public static class ChuteBlockEntity extends SmelteryInputOutputBlockEntity<ItemVariant> implements ItemTransferable {
    public ChuteBlockEntity(BlockPos pos, BlockState state) {
      this(TinkerSmeltery.chute.get(), pos, state);
    }

    protected ChuteBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
      super(type, pos, state, ItemVariant.class, Storage.empty());
    }
  
    @Nullable
    @Override
    public Storage<ItemVariant> getItemStorage(@Nullable Direction direction) {
      return getCachedCapability();
    }
  }

}
