package slimeknights.tconstruct.tables.block.entity.inventory;

import com.google.common.base.Preconditions;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

/**
 * Extension of {@link TransientCraftingContainer} to use instead wrap an existing {@link Container}
 */
public class CraftingContainerWrapper extends TransientCraftingContainer {

  private final Container crafter;

  public CraftingContainerWrapper(Container crafter, int width, int height) {
    //noinspection ConstantConditions
    super(null, width, height);
    Preconditions.checkArgument(crafter.getContainerSize() == width * height, "Invalid width and height for inventroy size");
    this.crafter = crafter;
  }

  /**
   * Inventory redirection
   */

  @Override
  public ItemStack getItem(int index) {
    return this.crafter.getItem(index);
  }

  @Override
  public int getContainerSize() {
    return this.crafter.getContainerSize();
  }

  @Override
  public boolean isEmpty() {
    return this.crafter.isEmpty();
  }

  @Override
  public ItemStack removeItemNoUpdate(int index) {
    return this.crafter.removeItemNoUpdate(index);
  }

  @Override
  public ItemStack removeItem(int index, int count) {
    return this.crafter.removeItem(index, count);
  }

  @Override
  public void setItem(int index, ItemStack stack) {
    this.crafter.setItem(index, stack);
  }

  @Override
  public void setChanged() {
    this.crafter.setChanged();
  }

  @Override
  public void clearContent() {
    this.crafter.clearContent();
  }

  @Override
  public void fillStackedContents(StackedContents helper) {
    for (int i = 0; i < this.crafter.getContainerSize(); i++) {
      helper.accountSimpleStack(this.crafter.getItem(i));
    }
  }
}
