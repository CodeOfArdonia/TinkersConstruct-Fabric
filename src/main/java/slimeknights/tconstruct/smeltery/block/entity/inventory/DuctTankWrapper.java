package slimeknights.tconstruct.smeltery.block.entity.inventory;

import lombok.AllArgsConstructor;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;

@AllArgsConstructor
public class DuctTankWrapper implements SlottedStorage<FluidVariant> {

  private final SlottedStorage<FluidVariant> parent;
  private final DuctItemHandler itemHandler;


  /* Properties */

  @Override
  public int getSlotCount() {
    return this.parent.getSlotCount();
  }

  @Override
  public SingleSlotStorage<FluidVariant> getSlot(int tank) {
    return this.parent.getSlot(tank);
  }

  @Override
  public Iterator<StorageView<FluidVariant>> iterator() {
    return this.parent.iterator();
  }


  /* Interactions */

  @Override
  public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
    if ((maxAmount <= 0 || resource.isBlank()) || !this.itemHandler.getFluid().isFluidEqual(resource)) {
      return 0;
    }
    return this.parent.insert(resource, maxAmount, transaction);
  }

  @Override
  public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
    if ((maxAmount <= 0 || resource.isBlank()) || !this.itemHandler.getFluid().isFluidEqual(resource)) {
      return 0;
    }
    return this.parent.extract(resource, maxAmount, transaction);
  }
}
