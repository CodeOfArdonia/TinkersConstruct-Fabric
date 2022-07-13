package slimeknights.tconstruct.smeltery.block.entity.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public class DuctTankWrapper extends WrappedStorage<FluidVariant> {
  private final DuctItemHandler itemHandler;

  public DuctTankWrapper(Storage<FluidVariant> wrapped, DuctItemHandler itemHandler) {
    super(wrapped);
    this.itemHandler = itemHandler;
  }

  /* Properties */

  /* Interactions */

  @Override
  public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
    if (resource.isBlank() || !itemHandler.getFluid().isFluidEqual(resource)) {
      return 0;
    }
    return super.insert(resource, maxAmount, transaction);
  }

  @Override
  public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
    if (resource.isBlank() || !itemHandler.getFluid().isFluidEqual(resource)) {
      return 0;
    }
    return super.extract(resource, maxAmount, transaction);
  }
}
