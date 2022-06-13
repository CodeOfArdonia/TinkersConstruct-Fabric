package slimeknights.tconstruct.smeltery.block.entity.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.WrappedStorage;
import lombok.AllArgsConstructor;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.IFluidHandler;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;

@AllArgsConstructor
public class DuctTankWrapper extends WrappedStorage<FluidVariant> {
  private final IFluidHandler parent;
  private final DuctItemHandler itemHandler;


  /* Properties */

  @Override
  public boolean isFluidValid(int tank, FluidStack stack) {
    return itemHandler.getFluid().isFluidEqual(stack);
  }


  /* Interactions */

  @Override
  public long fill(FluidStack resource, boolean sim) {
    if (resource.isEmpty() || !itemHandler.getFluid().isFluidEqual(resource)) {
      return 0;
    }
    return parent.fill(resource, sim);
  }

  @Override
  public FluidStack drain(long maxDrain, boolean sim) {
    FluidStack fluid = itemHandler.getFluid();
    if (fluid.isEmpty()) {
      return FluidStack.EMPTY;
    }
    return parent.drain(new FluidStack(fluid, maxDrain), sim);
  }

  @Override
  public FluidStack drain(FluidStack resource, boolean sim) {
    if (resource.isEmpty() || !itemHandler.getFluid().isFluidEqual(resource)) {
      return FluidStack.EMPTY;
    }
    return parent.drain(resource, sim);
  }
}
