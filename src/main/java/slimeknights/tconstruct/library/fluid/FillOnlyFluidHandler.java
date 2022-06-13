package slimeknights.tconstruct.library.fluid;

import io.github.fabricators_of_create.porting_lib.transfer.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

/**
 * Fluid handler wrapper that only allows filling
 */
public class FillOnlyFluidHandler extends WrappedStorage<FluidVariant> {

  public FillOnlyFluidHandler(Storage<FluidVariant> wrapped) {
    super(wrapped);
  }

  @Override
  public boolean supportsExtraction() {
    return false;
  }

  @Override
  public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
    return 0;
  }
}
