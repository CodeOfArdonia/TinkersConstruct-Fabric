package slimeknights.tconstruct.tables.block.entity.inventory;

import lombok.Setter;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.container.ISingleStackContainer;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.material.MaterialRecipe;
import slimeknights.tconstruct.library.recipe.tinkerstation.IMutableTinkerStationContainer;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;

import static slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity.INPUT_SLOT;
import static slimeknights.tconstruct.tables.block.entity.table.TinkerStationBlockEntity.TINKER_SLOT;

public class TinkerStationContainerWrapper implements IMutableTinkerStationContainer {

  private final TinkerStationBlockEntity station;
  /**
   * Cache of the material recipes found in each slot
   */
  private MaterialRecipe[] materials;
  /**
   * Cache of whether each slot has been searched for a material
   */
  private boolean[] searchedMaterial;

  /**
   * Cached tool instance to save lookup effort
   */
  @Nullable
  private ToolStack tool;

  private MaterialRecipe lastMaterialRecipe;
  @Nullable
  @Setter
  private Player player;

  /**
   * Creates a new wrapper instance for the station
   *
   * @param station Station instance
   */
  public TinkerStationContainerWrapper(TinkerStationBlockEntity station) {
    this.station = station;
    int count = station.getInputCount();
    this.materials = new MaterialRecipe[count];
    this.searchedMaterial = new boolean[count];
  }

  /**
   * Finds a material recipe for the given slot
   *
   * @param stack Stack in slot
   * @return Material recipe found, or null if missing
   */
  @Nullable
  private MaterialRecipe findMaterialRecipe(ItemStack stack) {
    // must have world
    Level world = this.station.getLevel();
    if (world == null) {
      return null;
    }
    // try last recipe
    ISingleStackContainer inv = () -> stack;
    if (this.lastMaterialRecipe != null && this.lastMaterialRecipe.matches(inv, world)) {
      return this.lastMaterialRecipe;
    }
    // try to find a new recipe
    Optional<MaterialRecipe> newRecipe = world.getRecipeManager().getRecipeFor(TinkerRecipeTypes.MATERIAL.get(), inv, world);
    if (newRecipe.isPresent()) {
      this.lastMaterialRecipe = newRecipe.get();
      return this.lastMaterialRecipe;
    }
    // if none found, return null
    return null;
  }

  /**
   * Clears the cached inputs
   */
  public void refreshInput(int slot) {
    if (slot == TINKER_SLOT) {
      this.tool = null;
    } else if (slot >= INPUT_SLOT && slot < this.station.getInputCount() + INPUT_SLOT) {
      this.materials[slot - 1] = null;
      this.searchedMaterial[slot - 1] = false;
    }
  }

  /**
   * Refreshes the size of this based on the size of the tinker station
   */
  public void resize() {
    int count = this.station.getInputCount();
    if (count != this.materials.length) {
      this.materials = new MaterialRecipe[count];
      this.searchedMaterial = new boolean[count];
    }
  }

  @Override
  public ItemStack getTinkerableStack() {
    return this.station.getItem(TINKER_SLOT);
  }

  @Override
  public ToolStack getTinkerable() {
    if (this.tool == null) {
      this.tool = ToolStack.from(this.getTinkerableStack());
    }
    return this.tool;
  }

  @Override
  public ItemStack getInput(int index) {
    if (index < 0 || index >= this.station.getInputCount()) {
      return ItemStack.EMPTY;
    }
    return this.station.getItem(index + TinkerStationBlockEntity.INPUT_SLOT);
  }

  @Override
  public int getInputCount() {
    return this.station.getInputCount();
  }

  @Nullable
  @Override
  public MaterialRecipe getInputMaterial(int index) {
    if (index < 0 || index >= this.station.getInputCount()) {
      return null;
    }
    if (!this.searchedMaterial[index]) {
      this.materials[index] = this.findMaterialRecipe(this.getInput(index));
      this.searchedMaterial[index] = true;
    }
    return this.materials[index];
  }

  @Override
  public void setInput(int index, ItemStack stack) {
    if (index >= 0 && index < this.station.getInputCount()) {
      this.station.setItem(index + TinkerStationBlockEntity.INPUT_SLOT, stack);
    }
  }

  @Override
  public void giveItem(ItemStack stack) {
    if (this.player != null) {
      this.player.getInventory().placeItemBackInInventory(stack);
    }
  }
}
