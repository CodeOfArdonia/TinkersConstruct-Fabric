package slimeknights.mantle.inventory;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlotItemHandler;
import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.minecraft.world.item.ItemStack;

/** Forge still uses dumb vanilla logic for determining slot limits instead of their own method */
public class SmartItemHandlerSlot extends SlotItemHandler {
	public SmartItemHandlerSlot(SlottedStorage<ItemVariant> itemHandler, int index, int xPosition, int yPosition) {
		super(itemHandler, index, xPosition, yPosition);
	}

	@Override
	public int getMaxStackSize(ItemStack stack) {
    var storage = getItemHandler();
		return (int) Math.min(stack.getMaxStackSize(), storage instanceof SlottedStackStorage slottedStackStorage ? slottedStackStorage.getSlotLimit(getSlotIndex()): storage.getSlot(getSlotIndex()).getCapacity());
	}
}
