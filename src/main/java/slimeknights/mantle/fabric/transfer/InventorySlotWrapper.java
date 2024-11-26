package slimeknights.mantle.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.item.base.SingleStackStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.impl.transfer.DebugMessages;
import net.fabricmc.fabric.impl.transfer.item.ItemVariantImpl;
import net.fabricmc.fabric.impl.transfer.item.SpecialLogicInventory;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;

/**
 * A wrapper around a single slot of an inventory.
 * We must ensure that only one instance of this class exists for every inventory slot,
 * or the transaction logic will not work correctly.
 * This is handled by the Map in InventoryStorageImpl.
 */
class InventorySlotWrapper extends SingleStackStorage {

  /**
   * The strong reference to the InventoryStorageImpl ensures that the weak value doesn't get GC'ed when individual slots are still being accessed.
   */
  private final InventoryStorage storage;
  final int slot;
  private final SpecialLogicInventory specialInv;
  private ItemStack lastReleasedSnapshot = null;

  InventorySlotWrapper(InventoryStorage storage, int slot) {
    this.storage = storage;
    this.slot = slot;
    this.specialInv = storage.inventory instanceof SpecialLogicInventory specialInv ? specialInv : null;
  }

  @Override
  protected ItemStack getStack() {
    return this.storage.inventory.getItem(this.slot);
  }

  @Override
  protected void setStack(ItemStack stack) {
    if (this.specialInv == null) {
      this.storage.inventory.setItem(this.slot, stack);
    } else {
      this.specialInv.fabric_setSuppress(true);

      try {
        this.storage.inventory.setItem(this.slot, stack);
      } finally {
        this.specialInv.fabric_setSuppress(false);
      }
    }
  }

  @Override
  public long insert(ItemVariant insertedVariant, long maxAmount, TransactionContext transaction) {
    if (!this.canInsert(this.slot, ((ItemVariantImpl) insertedVariant).getCachedStack())) {
      return 0;
    }

    long ret = super.insert(insertedVariant, maxAmount, transaction);
    if (this.specialInv != null && ret > 0) this.specialInv.fabric_onTransfer(this.slot, transaction);
    return ret;
  }

  private boolean canInsert(int slot, ItemStack stack) {
    if (this.storage.inventory instanceof ShulkerBoxBlockEntity shulker) {
      // Shulkers override canInsert but not isValid.
      return shulker.canPlaceItemThroughFace(slot, stack, null);
    } else {
      return this.storage.inventory.canPlaceItem(slot, stack);
    }
  }

  @Override
  public long extract(ItemVariant variant, long maxAmount, TransactionContext transaction) {
    long ret = super.extract(variant, maxAmount, transaction);
    if (this.specialInv != null && ret > 0) this.specialInv.fabric_onTransfer(this.slot, transaction);
    return ret;
  }

  /**
   * Special cases because vanilla checks the current stack in the following functions (which it shouldn't):
   * <ul>
   *     <li>{@link AbstractFurnaceBlockEntity#canPlaceItem(int, ItemStack)}.</li>
   *     <li>{@link BrewingStandBlockEntity#canPlaceItem(int, ItemStack)}.</li>
   * </ul>
   */
  @Override
  public int getCapacity(ItemVariant variant) {
    // Special case to limit buckets to 1 in furnace fuel inputs.
    if (this.storage.inventory instanceof AbstractFurnaceBlockEntity && this.slot == 1 && variant.isOf(Items.BUCKET)) {
      return 1;
    }

    // Special case to limit brewing stand "bottle inputs" to 1.
    if (this.storage.inventory instanceof BrewingStandBlockEntity && this.slot < 3) {
      return 1;
    }

    return Math.min(this.storage.inventory.getMaxStackSize(), variant.getItem().getMaxStackSize());
  }

  // We override updateSnapshots to also schedule a markDirty call for the backing inventory.
  @Override
  public void updateSnapshots(TransactionContext transaction) {
    this.storage.markDirtyParticipant.updateSnapshots(transaction);
    super.updateSnapshots(transaction);

    // For chests: also schedule a markDirty call for the other half
    if (this.storage.inventory instanceof ChestBlockEntity chest && chest.getBlockState().getValue(ChestBlock.TYPE) != ChestType.SINGLE) {
      BlockPos otherChestPos = chest.getBlockPos().relative(ChestBlock.getConnectedDirection(chest.getBlockState()));

      if (chest.getLevel().getBlockEntity(otherChestPos) instanceof ChestBlockEntity otherChest) {
        ((InventoryStorage) InventoryStorage.of(otherChest, null)).markDirtyParticipant.updateSnapshots(transaction);
      }
    }
  }

  @Override
  protected void releaseSnapshot(ItemStack snapshot) {
    this.lastReleasedSnapshot = snapshot;
  }

  @Override
  protected void onFinalCommit() {
    // Try to apply the change to the original stack
    ItemStack original = this.lastReleasedSnapshot;
    ItemStack currentStack = this.getStack();

    if (this.storage.inventory instanceof SpecialLogicInventory specialLogicInv) {
      specialLogicInv.fabric_onFinalCommit(this.slot, original, currentStack);
    }

    if (!original.isEmpty() && original.getItem() == currentStack.getItem()) {
      // None is empty and the items match: just update the amount and NBT, and reuse the original stack.
      original.setCount(currentStack.getCount());
      original.setTag(currentStack.hasTag() ? currentStack.getTag().copy() : null);
      this.setStack(original);
    } else {
      // Otherwise assume everything was taken from original so empty it.
      original.setCount(0);
    }
  }

  @Override
  public String toString() {
    return "InventorySlotWrapper[%s#%d]".formatted(DebugMessages.forInventory(this.storage.inventory), this.slot);
  }
}
