package slimeknights.tconstruct.smeltery.block.entity.module;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.library.recipe.melting.IMeltingContainer.IOreRate;
import slimeknights.tconstruct.library.recipe.melting.IMeltingRecipe;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Inventory composite made of a set of melting module inventories
 */
public class MeltingModuleInventory implements SlottedStackStorage, TransactionContext.CloseCallback {

  private static final String TAG_SLOT = "slot";
  private static final String TAG_ITEMS = "items";
  private static final String TAG_SIZE = "size";

  /**
   * Parent tile entity
   */
  private final MantleBlockEntity parent;
  /**
   * Fluid handler for outputs
   */
  protected final SlottedStorage<FluidVariant> fluidHandler;
  /**
   * Array of modules containing each slot
   */
  private MeltingModule[] modules;
  /**
   * If true, module cannot be resized
   */
  private final boolean strictSize;
  /**
   * Number of nuggets to produce when melting an ore
   */
  private final IOreRate oreRate;

  /**
   * Creates a new inventory with a fixed size
   *
   * @param parent       Parent tile
   * @param fluidHandler Tank for output
   * @param oreRate      Ore rate
   * @param size         Size
   */
  public MeltingModuleInventory(MantleBlockEntity parent, SlottedStorage<FluidVariant> fluidHandler, IOreRate oreRate, int size) {
    this.parent = parent;
    this.fluidHandler = fluidHandler;
    this.modules = new MeltingModule[size];
    this.oreRate = oreRate;
    this.strictSize = size != 0;
  }

  /**
   * Creates a new inventory with a variable size
   *
   * @param parent       Parent tile
   * @param fluidHandler Tank for output
   * @param oreRate      Ore rate
   */
  public MeltingModuleInventory(MantleBlockEntity parent, SlottedStorage<FluidVariant> fluidHandler, IOreRate oreRate) {
    this(parent, fluidHandler, oreRate, 0);
  }

  /* Properties */

  @Override
  public int getSlotCount() {
    return this.modules.length;
  }

  /**
   * Checks if the given slot index is valid
   *
   * @param slot Slot index to check
   * @return True if valid
   */
  public boolean validSlot(int slot) {
    return slot >= 0 && slot < this.getSlotCount();
  }

  @Override
  public int getSlotLimit(int slot) {
    return 1;
  }

  @Override
  public boolean isItemValid(int slot, ItemVariant stack, int count) {
    return true;
  }

  /**
   * Returns true if a slot is defined in the array
   */
  private boolean hasModule(int slot) {
    return this.validSlot(slot) && this.modules[slot] != null;
  }

  /**
   * Gets the current time of a slot
   *
   * @param slot Slot index
   * @return Slot temperature
   */
  public int getCurrentTime(int slot) {
    return this.hasModule(slot) ? this.modules[slot].getCurrentTime() : 0;
  }

  /**
   * Gets the required time for a slot
   *
   * @param slot Slot index
   * @return Required time
   */
  public int getRequiredTime(int slot) {
    return this.hasModule(slot) ? this.modules[slot].getRequiredTime() : 0;
  }

  /**
   * Gets the required temperature for a slot
   *
   * @param slot Slot index
   * @return Required temperature
   */
  public int getRequiredTemp(int slot) {
    return this.hasModule(slot) ? this.modules[slot].getRequiredTemp() : 0;
  }


  /* Sub modules */

  /**
   * Gets the module for the given index
   *
   * @param slot Index
   * @return Module for index
   * @throws IndexOutOfBoundsException index is invalid
   */
  public MeltingModule getModule(int slot) {
    if (!this.validSlot(slot)) {
      throw new IndexOutOfBoundsException();
    }
    if (this.modules[slot] == null) {
      this.modules[slot] = new MeltingModule(this.parent, recipe -> this.tryFillTank(slot, recipe), this.oreRate, slot);
    }
    return this.modules[slot];
  }

  /**
   * Resizes the module to a new size
   *
   * @param newSize       New size
   * @param stackConsumer Consumer for any stacks that no longer fit
   * @throws IllegalStateException If this inventory cannot be resized
   */
  public void resize(int newSize, Consumer<ItemStack> stackConsumer) {
    if (this.strictSize) {
      throw new IllegalStateException("Cannot resize this melting module inventory");
    }
    // nothing to do
    if (newSize == this.modules.length) {
      return;
    }
    // if shrinking, drop extra items
    if (newSize < this.modules.length) {
      for (int i = newSize; i < this.modules.length; i++) {
        if (this.modules[i] != null && !this.modules[i].getStack().isEmpty()) {
          stackConsumer.accept(this.modules[i].getStack());
        }
      }
    }

    // resize the module array
    this.modules = Arrays.copyOf(this.modules, newSize);
    this.parent.setChangedFast();
  }


  /* Item handling */

  @Nonnull
  @Override
  public ItemStack getStackInSlot(int slot) {
    if (this.validSlot(slot)) {
      // don't create the slot, just reading
      if (this.modules[slot] != null) {
        return this.modules[slot].getStack();
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void setStackInSlot(int slot, ItemStack stack) {
    // actually set the stack
    if (this.validSlot(slot)) {
      if (stack.isEmpty()) {
        if (this.modules[slot] != null) {
          this.modules[slot].setStack(ItemStack.EMPTY);
        }
      } else {
        // validate size
        if (stack.getCount() > 1) {
          stack.setCount(1);
        }
        this.getModule(slot).setStack(stack);
      }
    }
  }

  @Override
  public long insertSlot(int slot, ItemVariant resource, long amount, TransactionContext transaction) {
    if (resource.isBlank()) {
      return 0;
    }
    if (slot < 0 || slot >= this.getSlotCount()) {
      return 0;
    }

    // if the slot is empty, we can insert. Ignores stack sizes at this time, assuming always 1
    MeltingModule module = this.getModule(slot);
    boolean canInsert = module.getStack().isEmpty();
    if (canInsert) {
      this.updateSnapshots(slot, transaction);
      this.setStackInSlot(slot, resource.toStack((int) amount));
    }
    return canInsert ? amount : 0;
  }

  @Override
  public long extractSlot(int slot, ItemVariant resource, long amount, TransactionContext transaction) {
    if (amount == 0) {
      return 0;
    }
    if (!this.validSlot(slot)) {
      return 0;
    }

    ItemStack existing = this.getStackInSlot(slot);
    if (existing.isEmpty()) {
      return 0;
    }

    this.updateSnapshots(slot, transaction);
    this.setStackInSlot(slot, ItemStack.EMPTY);
    return existing.getCount();
  }

  @Override
  public SingleSlotStorage<ItemVariant> getSlot(int slot) {
    return this.getModule(slot);
  }

  /* Heating */

  /**
   * Checks if any slot can heat
   *
   * @param temperature Temperature to try
   * @return True if a slot can heat
   */
  public boolean canHeat(int temperature) {
    for (MeltingModule module : this.modules) {
      if (module != null && module.canHeatItem(temperature)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Tries to fill the fluid handler with the given fluid
   *
   * @param index  Index of the module being filled
   * @param recipe Recipe to add
   * @return True if filled, false if not enough space for the whole fluid
   */
  protected boolean tryFillTank(int index, IMeltingRecipe recipe) {
    FluidStack fluid = recipe.getOutput(this.getModule(index));
    if (StorageUtil.simulateInsert(this.fluidHandler, fluid.getType(), fluid.getAmount(), null) == fluid.getAmount()) {
      try (Transaction tx = TransferUtil.getTransaction()) {
        this.fluidHandler.insert(fluid.getType(), fluid.getAmount(), tx);
        tx.commit();
      }
      return true;
    }
    return false;
  }

  /**
   * Heats all items in the inventory
   *
   * @param temperature Heating structure temperature
   */
  public void heatItems(int temperature) {
    for (MeltingModule module : this.modules) {
      if (module != null) {
        module.heatItem(temperature);
      }
    }
  }

  /**
   * Cools down all items in the inventory, used when there is no fuel
   */
  public void coolItems() {
    for (MeltingModule module : this.modules) {
      if (module != null) {
        module.coolItem();
      }
    }
  }

  /**
   * Writes this module to Tag
   *
   * @return Module in Tag
   */
  public CompoundTag writeToTag() {
    CompoundTag nbt = new CompoundTag();
    ListTag list = new ListTag();
    for (int i = 0; i < this.modules.length; i++) {
      if (this.modules[i] != null && !this.modules[i].getStack().isEmpty()) {
        CompoundTag moduleTag = this.modules[i].writeToTag();
        moduleTag.putByte(TAG_SLOT, (byte) i);
        list.add(moduleTag);
      }
    }
    if (!list.isEmpty()) {
      nbt.put(TAG_ITEMS, list);
    }
    nbt.putByte(TAG_SIZE, (byte) this.modules.length);
    return nbt;
  }

  /**
   * Reads this inventory from Tag
   *
   * @param nbt Tag compound
   */
  public void readFromTag(CompoundTag nbt) {
    if (!this.strictSize) {
      int newSize = nbt.getByte(TAG_SIZE) & 255;
      if (newSize != this.modules.length) {
        this.modules = Arrays.copyOf(this.modules, newSize);
      }
    }
    // remove old data
    for (MeltingModule module : this.modules) {
      if (module != null) {
        module.setStack(ItemStack.EMPTY);
      }
    }

    ListTag list = nbt.getList(TAG_ITEMS, Tag.TAG_COMPOUND);
    for (int i = 0; i < list.size(); i++) {
      CompoundTag item = list.getCompound(i);
      if (item.contains(TAG_SLOT, Tag.TAG_BYTE)) {
        int slot = item.getByte(TAG_SLOT) & 255;
        if (this.validSlot(slot)) {
          this.getModule(slot).readFromTag(item);
        }
      }
    }
  }


  /* Container sync */

  /**
   * Sets up all sub slots for tracking
   *
   * @param consumer IIntArray consumer
   */
  public void trackInts(Consumer<ContainerData> consumer) {
    for (int i = 0; i < this.getSlotCount(); i++) {
      consumer.accept(this.getModule(i));
    }
  }

  @Override
  public long insert(ItemVariant resource, long maxAmount, TransactionContext transaction) {
    long totalInserted = 0;
    for (int i = 0; i < this.getSlotCount(); i++) {
      long inserted = this.insertSlot(i, resource, maxAmount, transaction);
      totalInserted += inserted;
      maxAmount -= inserted;
    }
    return totalInserted;
  }

  @Override
  public long extract(ItemVariant resource, long maxAmount, TransactionContext transaction) {
    long totalExtracted = 0;
    for (int i = 0; i < this.getSlotCount(); i++) {
      long extracted = this.extractSlot(i, resource, maxAmount, transaction);
      totalExtracted += extracted;
      maxAmount -= extracted;
    }
    return totalExtracted;
  }

  protected SnapshotData createSnapshot(int slot) {
    return new SnapshotData(slot, this.getStackInSlot(slot));
  }

  protected void readSnapshot(SnapshotData snapshot) {
    this.setStackInSlot(snapshot.slot(), snapshot.stack());
  }

  public void updateSnapshots(int slot, TransactionContext transaction) {
    // Make sure we have enough storage for snapshots
    while (this.snapshots.size() <= transaction.nestingDepth()) {
      this.snapshots.add(null);
    }

    // If the snapshot is null, we need to create it, and we need to register a callback.
    if (this.snapshots.get(transaction.nestingDepth()) == null) {
      SnapshotData snapshot = this.createSnapshot(slot);
      Objects.requireNonNull(snapshot, "Snapshot may not be null!");

      this.snapshots.set(transaction.nestingDepth(), snapshot);
      transaction.addCloseCallback(this);
    }
  }

  private final List<SnapshotData> snapshots = new ArrayList<>();

  @Override
  public void onClose(TransactionContext transaction, TransactionContext.Result result) {
    // Get and remove the relevant snapshot.
    SnapshotData snapshot = this.snapshots.set(transaction.nestingDepth(), null);

    if (result.wasAborted()) {
      // If the transaction was aborted, we just revert to the state of the snapshot.
      this.readSnapshot(snapshot);
    } else if (transaction.nestingDepth() > 0) {
      if (this.snapshots.get(transaction.nestingDepth() - 1) == null) {
        // No snapshot yet, so move the snapshot one nesting level up.
        this.snapshots.set(transaction.nestingDepth() - 1, snapshot);
        // This is the first snapshot at this level: we need to call addCloseCallback.
        transaction.getOpenTransaction(transaction.nestingDepth() - 1).addCloseCallback(this);
      }
    }
  }
}
