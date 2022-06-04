package slimeknights.tconstruct.smeltery.item;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.IFluidHandlerItem;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.smeltery.block.entity.component.TankBlockEntity;

import javax.annotation.Nonnull;

/**
 * Handler that works with a tank item to adjust its tank in NBT
 */
@RequiredArgsConstructor
public class TankItemFluidHandler implements IFluidHandlerItem {
  private final LazyOptional<IFluidHandlerItem> holder = LazyOptional.of(() -> this);
  @Getter
  private final ItemStack container;

  /** Gets the tank on the stack */
  private FluidTank getTank() {
    return TankItem.getFluidTank(container);
  }

  /** Updates the container from the given tank */
  private void updateContainer(FluidTank tank) {
    TankItem.setTank(container, tank);
  }

//  @Override
//  public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
//    return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.orEmpty(cap, holder);
//  }

  @Override
  public int getTanks() {
    return 1;
  }

  @Nonnull
  @Override
  public FluidStack getFluidInTank(int tank) {
    return getTank().getFluidInTank(tank);
  }

  @Override
  public long getTankCapacity(int tank) {
    return TankBlockEntity.getCapacity(container.getItem());
  }

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return true;
  }

  @Override
  public long fill(FluidStack resource, boolean sim) {
    FluidTank tank = getTank();
    long didFill = tank.fill(resource, sim);
    if (didFill > 0 && !sim) {
      updateContainer(tank);
    }
    return didFill;
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, boolean sim) {
    FluidTank tank = getTank();
    FluidStack didDrain = tank.drain(resource, sim);
    if (!didDrain.isEmpty() && !sim) {
      updateContainer(tank);
    }
    return didDrain;
  }

  @Nonnull
  @Override
  public FluidStack drain(long maxDrain, boolean sim) {
    FluidTank tank = getTank();
    FluidStack didDrain = tank.drain(maxDrain, sim);
    if (!didDrain.isEmpty() && !sim) {
      updateContainer(tank);
    }
    return didDrain;
  }
}
