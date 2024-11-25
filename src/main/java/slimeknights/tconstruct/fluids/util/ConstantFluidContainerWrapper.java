package slimeknights.tconstruct.fluids.util;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.Getter;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * Represents a capability handler for a container with a constant fluid
 */
public class ConstantFluidContainerWrapper extends SnapshotParticipant<Boolean> implements SingleSlotStorage<FluidVariant>/*, ICapabilityProvider*/ {

  /**
   * Contained fluid
   */
  private final FluidStack fluid;
  /**
   * If true, the container is now empty
   */
  private boolean empty = false;
  /**
   * Item stack representing the current state
   */
  @Getter
  @Nonnull
  protected ItemStack container;
  @Getter
  @Nonnull
  protected ContainerItemContext context;

  public ConstantFluidContainerWrapper(FluidStack fluid, ItemStack container, ContainerItemContext context) {
    this.fluid = fluid;
    this.container = container;
    this.context = context;
  }

  @Override
  public long getCapacity() {
    return this.fluid.getAmount();
  }

  @Override
  public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
    return 0;
  }

  @Override
  public long extract(FluidVariant resource, long maxDrain, TransactionContext transaction) {
    // cannot drain if: already drained, requested the wrong type, or requested too little
    if (this.empty || maxDrain < this.fluid.getAmount()) {
      return 0;
    }
    this.updateSnapshots(transaction);
    if (this.context.exchange(ItemVariant.of(this.container.getRecipeRemainder()), 1, transaction) == 1) {
      this.empty = true;
      return this.fluid.getAmount();
    }
    return 0;
  }

  @Override
  public boolean isResourceBlank() {
    return this.empty || this.fluid.getType().isBlank();
  }

  @Override
  public FluidVariant getResource() {
    return this.empty ? FluidVariant.blank() : this.fluid.getType();
  }

  @Override
  public long getAmount() {
    return this.empty ? 0 : this.fluid.getAmount();
  }

  @Override
  protected Boolean createSnapshot() {
    return this.empty;
  }

  @Override
  protected void readSnapshot(Boolean snapshot) {
    this.empty = snapshot;
  }
}
