package slimeknights.tconstruct.shared.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.WrappedStorage;
import net.fabricmc.fabric.api.transfer.v1.item.InventoryStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.Container;

import javax.annotation.Nonnull;

public class ConfigurableInvWrapperCapability extends WrappedStorage<ItemVariant> {

  private final boolean canInsert;
  private final boolean canExtract;

  public ConfigurableInvWrapperCapability(Container inv, boolean canInsert, boolean canExtract) {
    super(InventoryStorage.of(inv, null));
    this.canInsert = canInsert;
    this.canExtract = canExtract;
  }

  @Nonnull
  @Override
  public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
    if (!this.canInsert) {
      return 0;
    }
    return super.insert(resource, maxAmount, transaction);
  }

  @Nonnull
  @Override
  public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
    if (!this.canExtract) {
      return 0;
    }
    return super.extract(resource, maxAmount, transaction);
  }
}
