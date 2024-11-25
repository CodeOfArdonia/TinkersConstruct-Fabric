package slimeknights.tconstruct.library.recipe.melting;

import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.recipe.helper.LoggingRecipeSerializer;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.recipe.casting.material.MaterialCastingLookup;
import slimeknights.tconstruct.library.recipe.ingredient.MaterialIngredient;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Recipe to melt all castable tool parts of a given material
 */
public class MaterialMeltingRecipe implements IMeltingRecipe, IMultiRecipe<MeltingRecipe> {

  @Getter
  private final ResourceLocation id;
  private final MaterialVariant input;
  private final int temperature;
  private final FluidStack result;

  public MaterialMeltingRecipe(ResourceLocation id, MaterialVariantId input, int temperature, FluidStack result) {
    this.id = id;
    this.input = MaterialVariant.of(input);
    this.temperature = temperature;
    this.result = result;
  }

  @Override
  public boolean matches(IMeltingContainer inv, Level worldIn) {
    if (this.input.isUnknown()) {
      return false;
    }
    ItemStack stack = inv.getStack();
    if (stack.isEmpty() || MaterialCastingLookup.getItemCost(stack.getItem()) == 0) {
      return false;
    }
    return this.input.matchesVariant(stack);
  }

  @Override
  public int getTemperature(IMeltingContainer inv) {
    return this.temperature;
  }

  @Override
  public int getTime(IMeltingContainer inv) {
    int cost = MaterialCastingLookup.getItemCost(inv.getStack().getItem());
    return IMeltingRecipe.calcTimeForAmount(this.temperature, this.result.getAmount() * cost);
  }

  @Override
  public FluidStack getOutput(IMeltingContainer inv) {
    int cost = MaterialCastingLookup.getItemCost(inv.getStack().getItem());
    return new FluidStack(this.result, this.result.getAmount() * cost);
  }

  @Override
  public RecipeSerializer<?> getSerializer() {
    return TinkerSmeltery.materialMeltingSerializer.get();
  }


  /* JEI display */
  private List<MeltingRecipe> multiRecipes = null;

  @Override
  public List<MeltingRecipe> getRecipes() {
    if (this.multiRecipes == null) {
      if (this.input.get().isHidden()) {
        this.multiRecipes = Collections.emptyList();
      } else {
        // 1 recipe for each part
        MaterialId inputId = this.input.getId();
        this.multiRecipes = MaterialCastingLookup
          .getAllItemCosts().stream()
          .filter(entry -> entry.getKey().canUseMaterial(inputId))
          .map(entry -> {
            FluidStack output = this.result;
            if (entry.getIntValue() != 1) {
              output = new FluidStack(output, output.getAmount() * entry.getIntValue());
            }
            return new MeltingRecipe(this.id, "", MaterialIngredient.fromItem(entry.getKey(), inputId), output, this.temperature,
              IMeltingRecipe.calcTimeForAmount(this.temperature, output.getAmount()), Collections.emptyList());
          }).collect(Collectors.toList());
      }
    }
    return this.multiRecipes;
  }

  public static class Serializer extends LoggingRecipeSerializer<MaterialMeltingRecipe> {

    @Override
    public MaterialMeltingRecipe fromJson(ResourceLocation id, JsonObject json) {
      MaterialVariantId inputId = MaterialVariantId.fromJson(json, "input");
      int temperature = GsonHelper.getAsInt(json, "temperature");
      FluidStack output = RecipeHelper.deserializeFluidStack(GsonHelper.getAsJsonObject(json, "result"));
      return new MaterialMeltingRecipe(id, inputId, temperature, output);
    }

    @Nullable
    @Override
    protected MaterialMeltingRecipe fromNetworkSafe(ResourceLocation id, FriendlyByteBuf buffer) {
      MaterialVariantId inputId = MaterialVariantId.parse(buffer.readUtf(Short.MAX_VALUE));
      int temperature = buffer.readInt();
      FluidStack output = FluidStack.readFromPacket(buffer);
      return new MaterialMeltingRecipe(id, inputId, temperature, output);
    }

    @Override
    protected void toNetworkSafe(FriendlyByteBuf buffer, MaterialMeltingRecipe recipe) {
      buffer.writeUtf(recipe.input.getVariant().toString());
      buffer.writeInt(recipe.temperature);
      recipe.result.writeToPacket(buffer);
    }
  }
}
