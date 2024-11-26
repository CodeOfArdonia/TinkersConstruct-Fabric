package slimeknights.mantle.recipe.container;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;

import java.util.Set;

/**
 * {@link Recipe} extension for an inventory wrapper containing a single item. Primarily used for furnace like recipes.
 */
public interface ISingleStackContainer extends IRecipeContainer {

  /**
   * Gets the relevant item in this inventory
   *
   * @return Contained item
   */
  ItemStack getStack();

  /* Multistack methods, redundant now */

  /**
   * @deprecated use {{@link #getStack()}}
   */
  @Deprecated
  @Override
  default ItemStack getItem(int index) {
    return index == 0 ? this.getStack() : ItemStack.EMPTY;
  }

  @Override
  default boolean isEmpty() {
    return this.getStack().isEmpty();
  }

  /**
   * @deprecated always 1, not useful
   */
  @Deprecated
  @Override
  default int getContainerSize() {
    return 1;
  }


  /* Utils made more efficient */

  @Override
  default int countItem(Item itemIn) {
    ItemStack stack = this.getStack();
    if (stack.getItem() == itemIn) {
      return stack.getCount();
    }
    return 0;
  }

  @Override
  default boolean hasAnyOf(Set<Item> set) {
    ItemStack stack = this.getStack();
    return !stack.isEmpty() && set.contains(this.getStack().getItem());
  }
}
