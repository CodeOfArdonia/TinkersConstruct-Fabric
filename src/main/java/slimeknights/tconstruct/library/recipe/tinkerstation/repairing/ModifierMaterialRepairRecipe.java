package slimeknights.tconstruct.library.recipe.tinkerstation.repairing;

import lombok.Getter;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.materials.definition.LazyMaterial;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.recipe.tinkerstation.ITinkerStationContainer;
import slimeknights.tconstruct.library.recipe.tinkerstation.repairing.ModifierMaterialRepairSerializer.IModifierMaterialRepairRecipe;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tables.recipe.TinkerStationRepairRecipe;
import slimeknights.tconstruct.tools.TinkerModifiers;

/**
 * Recipe to repair a specialized tool in the tinker station
 */
public class ModifierMaterialRepairRecipe extends TinkerStationRepairRecipe implements IModifierMaterialRepairRecipe {

  /**
   * Tool that can be repaired with this recipe
   */
  @Getter
  private final ModifierId modifier;
  /**
   * ID of material used in repairing
   */
  private final LazyMaterial repairMaterial;

  public ModifierMaterialRepairRecipe(ResourceLocation id, ModifierId modifier, MaterialId repairMaterialID) {
    super(id);
    this.modifier = modifier;
    this.repairMaterial = LazyMaterial.of(repairMaterialID);
  }

  @Override
  public MaterialId getRepairMaterial() {
    return this.repairMaterial.getId();
  }

  @Override
  protected MaterialId getPrimaryMaterial(IToolStackView tool) {
    return this.repairMaterial.getId();
  }

  @Override
  public boolean matches(ITinkerStationContainer inv, Level world) {
    if (this.repairMaterial.isUnknown()) {
      return false;
    }
    // must have the modifier
    ItemStack tinkerable = inv.getTinkerableStack();
    if (!tinkerable.is(TinkerTags.Items.MODIFIABLE) || ModifierUtil.getModifierLevel(tinkerable, this.modifier) == 0) {
      return false;
    }
    return SpecializedRepairRecipe.findMaterialItem(inv, this.repairMaterial);
  }

  @Override
  protected float getRepairPerItem(ToolStack tool, ITinkerStationContainer inv, int slot, MaterialId repairMaterial) {
    return super.getRepairPerItem(tool, inv, slot, repairMaterial) * tool.getModifierLevel(this.modifier);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerModifiers.modifierMaterialRepair.get();
  }
}
