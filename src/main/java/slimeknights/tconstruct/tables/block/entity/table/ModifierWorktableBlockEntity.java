package slimeknights.tconstruct.tables.block.entity.table;

import io.github.fabricators_of_create.porting_lib.event.common.ItemCraftedCallback;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.recipe.RecipeResult;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.worktable.IModifierWorktableRecipe;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.shared.inventory.ConfigurableInvWrapperCapability;
import slimeknights.tconstruct.tables.TinkerTables;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer;
import slimeknights.tconstruct.tables.block.entity.inventory.LazyResultContainer.ILazyCrafter;
import slimeknights.tconstruct.tables.block.entity.inventory.ModifierWorktableContainerWrapper;
import slimeknights.tconstruct.tables.menu.ModifierWorktableContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

// TODO: spend some time planning out data flow, its not currently doing it
public class ModifierWorktableBlockEntity extends RetexturedTableBlockEntity implements ILazyCrafter {

  /**
   * Index containing the tool
   */
  public static final int TINKER_SLOT = 0;
  /**
   * First input slot index
   */
  public static final int INPUT_START = 1;
  /**
   * Number of input slots
   */
  public static final int INPUT_COUNT = 2;
  /**
   * Title for the GUI
   */
  private static final Component NAME = TConstruct.makeTranslation("gui", "modifier_worktable");

  /**
   * Result inventory, lazy loads results
   */
  @Getter
  private final LazyResultContainer craftingResult;
  /**
   * Crafting inventory for the recipe calls
   */
  @Getter
  private final ModifierWorktableContainerWrapper inventoryWrapper;

  /**
   * If true, the last recipe is the current recipe. If false, no recipe was found. If null, have not tried recipe lookup
   */
  private Boolean recipeValid;
  /**
   * Cache of the last recipe, may not be the current one
   */
  @Nullable
  private IModifierWorktableRecipe lastRecipe;
  /* Current buttons to display */
  @Nonnull
  private List<ModifierEntry> buttons = Collections.emptyList();
  /**
   * Index of the currently selected modifier
   */
  private int selectedModifierIndex = -1;

  /**
   * Current result, may be modified again later
   */
  @Nullable
  @Getter
  private ToolStack result = null;
  /**
   * Current message displayed on the screen
   */
  @Getter
  private Component currentMessage = Component.empty();

  public ModifierWorktableBlockEntity(BlockPos pos, BlockState state) {
    super(TinkerTables.modifierWorktableTile.get(), pos, state, NAME, 3);
    this.itemHandler = new ConfigurableInvWrapperCapability(this, false, false);
    this.inventoryWrapper = new ModifierWorktableContainerWrapper(this);
    this.craftingResult = new LazyResultContainer(this);
  }

  /**
   * Selects a modifier by index. Will fetch the buttons list if the index is non-negative
   *
   * @param index New index
   */
  public void selectModifier(int index) {
    this.result = null;
    this.craftingResult.clearContent();
    if (index >= 0) {
      List<ModifierEntry> list = this.getCurrentButtons();
      if (index < list.size()) {
        this.selectedModifierIndex = index;
        ModifierEntry entry = list.get(index);

        // last recipe must be nonnull for list to be non-empty
        assert this.lastRecipe != null;
        RecipeResult<ToolStack> recipeResult = this.lastRecipe.getResult(this.inventoryWrapper, entry);
        if (recipeResult.isSuccess()) {
          this.result = recipeResult.getResult();
          this.currentMessage = Component.empty();
        } else if (recipeResult.hasError()) {
          this.currentMessage = recipeResult.getMessage();
        } else {
          this.currentMessage = this.lastRecipe.getDescription(this.inventoryWrapper);
        }
        return;
      }
    }
    // index is either not valid or the list is empty, so just clear
    this.selectedModifierIndex = -1;
    this.currentMessage = this.recipeValid == Boolean.TRUE && this.lastRecipe != null
      ? this.lastRecipe.getDescription(this.inventoryWrapper)
      : Component.empty();
  }

  /**
   * Gets the index of the selected pattern
   */
  public int getSelectedIndex() {
    return this.selectedModifierIndex;
  }

  private void syncRecipe() {
    if (this.level != null && !this.level.isClientSide) {
      this.syncToRelevantPlayers(this::syncScreen);
    }
  }

  /**
   * Updates the current recipe
   */
  public IModifierWorktableRecipe updateRecipe(IModifierWorktableRecipe recipe) {
    this.lastRecipe = recipe;
    this.recipeValid = true;
    this.currentMessage = this.lastRecipe.getDescription(this.inventoryWrapper);
    this.buttons = recipe.getModifierOptions(this.inventoryWrapper);
    //        if (!level.isClientSide) {
    //          syncToRelevantPlayers(this::syncScreen);
    //        }

    // clear the active modifier
    this.selectModifier(-1);
    return recipe;
  }

  /**
   * Gets the currently active recipe
   */
  @Nullable
  public IModifierWorktableRecipe getCurrentRecipe() {
    if (this.recipeValid == Boolean.TRUE) {
      return this.lastRecipe;
    }
    if (this.recipeValid == null && this.level != null) {
      // if the previous recipe matches, flip state to use that again
      if (this.lastRecipe != null && this.lastRecipe.matches(this.inventoryWrapper, this.level)) {
        return this.updateRecipe(this.lastRecipe);
      }
      // look for a new recipe, if it matches cache it
      Optional<IModifierWorktableRecipe> recipe = this.level.getRecipeManager().getRecipeFor(TinkerRecipeTypes.MODIFIER_WORKTABLE.get(), this.inventoryWrapper, this.level);
      if (recipe.isPresent()) {
        return this.updateRecipe(recipe.get());
      }
      this.recipeValid = false;
      this.currentMessage = Component.empty();
      this.buttons = Collections.emptyList();
      this.selectModifier(-1);
    }
    // level null or no recipe found
    return null;
  }

  /**
   * Gets a map of all recipes for the current inputs
   *
   * @return List of recipes for the current inputs
   */
  public List<ModifierEntry> getCurrentButtons() {
    if (this.level == null) {
      return Collections.emptyList();
    }
    // if last recipe is not fetched, the buttons may be outdated
    this.getCurrentRecipe();
    return this.buttons;
  }

  /**
   * Called when a slot changes to clear the current result
   */
  public void onSlotChanged(int slot) {
    this.inventoryWrapper.refreshInput(slot);
    this.recipeValid = null;
    this.buttons = Collections.emptyList();
    this.selectModifier(-1);
  }

  @Override
  public void setItem(int slot, ItemStack stack) {
    ItemStack original = this.getItem(slot);
    super.setItem(slot, stack);
    // if the stack changed, clear everything
    if (original.getCount() != stack.getCount() || !ItemStack.isSameItemSameTags(original, stack)) {
      this.onSlotChanged(slot);
    }
  }

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int menuId, Inventory playerInventory, Player playerEntity) {
    return new ModifierWorktableContainerMenu(menuId, playerInventory, this);
  }

  @Override
  public ItemStack calcResult(@Nullable Player player) {
    if (this.selectedModifierIndex != -1) {
      IModifierWorktableRecipe recipe = this.getCurrentRecipe();
      if (recipe != null && this.result != null) {
        return this.result.createStack(recipe.toolResultSize(this.inventoryWrapper, this.getCurrentButtons().get(this.selectedModifierIndex)));
      }
    }
    return ItemStack.EMPTY;
  }

  @Override
  public void onCraft(Player player, ItemStack resultItem, int amount) {
    // the recipe should match if we got this far, but being null is a problem
    if (amount == 0 || this.level == null || this.lastRecipe == null || this.result == null) {
      return;
    }

    // we are definitely crafting at this point
    resultItem.onCraftedBy(this.level, player, amount);
    ItemCraftedCallback.EVENT.invoker().onCraft(player, resultItem, this.inventoryWrapper);
    this.playCraftSound(player);

    // run the recipe, will shrink inputs
    // run both sides for the sake of shift clicking
    this.inventoryWrapper.setPlayer(player);
    this.lastRecipe.updateInputs(this.result, this.inventoryWrapper, this.getCurrentButtons().get(this.selectedModifierIndex), !this.level.isClientSide);
    this.inventoryWrapper.setPlayer(null);

    ItemStack tinkerable = this.getItem(TINKER_SLOT);
    if (!tinkerable.isEmpty()) {
      int shrinkToolSlot = this.lastRecipe.toolResultSize();
      if (tinkerable.getCount() <= shrinkToolSlot) {
        this.setItem(TINKER_SLOT, ItemStack.EMPTY);
      } else {
        this.setItem(TINKER_SLOT, ItemHandlerHelper.copyStackWithSize(tinkerable, tinkerable.getCount() - shrinkToolSlot));
      }
    }
    // screen should reset back to empty now that we crafted
//    syncRecipe();
  }
}
