package slimeknights.tconstruct.library.recipe.casting.material;

import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import slimeknights.mantle.recipe.IMultiRecipe;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.casting.AbstractCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.DisplayCastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.ICastingContainer;
import slimeknights.tconstruct.library.recipe.casting.ICastingRecipe;
import slimeknights.tconstruct.library.recipe.casting.IDisplayableCastingRecipe;
import slimeknights.tconstruct.library.tools.part.IMaterialItem;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Casting recipe that takes an arbitrary fluid of a given amount and set the material on the output based on that fluid
 */
public abstract class MaterialCastingRecipe extends AbstractCastingRecipe implements IMultiRecipe<IDisplayableCastingRecipe> {

  protected final int itemCost;
  protected final IMaterialItem result;
  @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
  protected Optional<MaterialFluidRecipe> cachedFluidRecipe = Optional.empty();

  public MaterialCastingRecipe(RecipeType<?> type, ResourceLocation id, String group, Ingredient cast, int itemCost, IMaterialItem result, boolean consumed, boolean switchSlots) {
    super(type, id, group, cast, consumed, switchSlots);
    this.itemCost = itemCost;
    this.result = result;
    MaterialCastingLookup.registerItemCost(result, itemCost);
  }

  /**
   * Gets the material fluid recipe for the given recipe
   */
  protected Optional<MaterialFluidRecipe> getMaterialFluid(ICastingContainer inv) {
    return MaterialCastingLookup.getCastingFluid(inv);
  }

  /**
   * Gets the cached fluid recipe if it still matches, refetches if not
   */
  protected Optional<MaterialFluidRecipe> getCachedMaterialFluid(ICastingContainer inv) {
    Optional<MaterialFluidRecipe> fluidRecipe = this.cachedFluidRecipe;
    if (fluidRecipe.filter(recipe -> recipe.matches(inv)).isEmpty()) {
      fluidRecipe = this.getMaterialFluid(inv);
      if (fluidRecipe.isPresent()) {
        this.cachedFluidRecipe = fluidRecipe;
      }
    }
    return fluidRecipe;
  }

  @Override
  public boolean matches(ICastingContainer inv, Level worldIn) {
    if (!this.cast.test(inv.getStack())) {
      return false;
    }
    return this.getCachedMaterialFluid(inv).filter(recipe -> this.result.canUseMaterial(recipe.getOutput().getId())).isPresent();
  }

  @Override
  public int getCoolingTime(ICastingContainer inv) {
    return this.getCachedMaterialFluid(inv)
      .map(recipe -> ICastingRecipe.calcCoolingTime(recipe.getTemperature(), recipe.getFluidAmount(inv.getFluid()) * this.itemCost))
      .orElse(1);
  }

  @Override
  public long getFluidAmount(ICastingContainer inv) {
    return this.getCachedMaterialFluid(inv)
      .map(recipe -> recipe.getFluidAmount(inv.getFluid()))
      .orElse(1L) * this.itemCost;
  }

  @Override
  public ItemStack getResultItem(RegistryAccess registryAccess) {
    return new ItemStack(this.result);
  }

  @Override
  public ItemStack assemble(ICastingContainer inv, RegistryAccess registryAccess) {
    MaterialVariant material = this.getCachedMaterialFluid(inv).map(MaterialFluidRecipe::getOutput).orElse(MaterialVariant.UNKNOWN);
    return this.result.withMaterial(material.getVariant());
  }

  /* JEI display */
  protected List<IDisplayableCastingRecipe> multiRecipes;

  /**
   * Resizes the list of the fluids with respect to the item cost
   */
  protected List<FluidStack> resizeFluids(List<FluidStack> fluids) {
    if (this.itemCost != 1) {
      return fluids.stream()
        .map(fluid -> new FluidStack(fluid, fluid.getAmount() * this.itemCost))
        .collect(Collectors.toList());
    }
    return fluids;
  }

  @Override
  public List<IDisplayableCastingRecipe> getRecipes() {
    if (this.multiRecipes == null) {
      RecipeType<?> type = this.getType();
      List<ItemStack> castItems = Arrays.asList(this.cast.getItems());
      this.multiRecipes = MaterialCastingLookup
        .getAllCastingFluids().stream()
        .filter(recipe -> {
          MaterialVariant output = recipe.getOutput();
          return !output.isUnknown() && !output.get().isHidden() && this.result.canUseMaterial(output.getId());
        })
        .map(recipe -> {
          List<FluidStack> fluids = this.resizeFluids(recipe.getFluids());
          long fluidAmount = fluids.stream().mapToLong(FluidStack::getAmount).max().orElse(0);
          return new DisplayCastingRecipe(type, castItems, fluids, this.result.withMaterial(recipe.getOutput().getVariant()),
            ICastingRecipe.calcCoolingTime(recipe.getTemperature(), this.itemCost * fluidAmount), this.consumed);
        })
        .collect(Collectors.toList());
    }
    return this.multiRecipes;
  }

  /**
   * Basin implementation
   */
  public static class Basin extends MaterialCastingRecipe {

    public Basin(ResourceLocation id, String group, Ingredient cast, int itemCost, IMaterialItem result, boolean consumed, boolean switchSlots) {
      super(TinkerRecipeTypes.CASTING_BASIN.get(), id, group, cast, itemCost, result, consumed, switchSlots);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
      return TinkerSmeltery.basinMaterialSerializer.get();
    }
  }

  /**
   * Table implementation
   */
  public static class Table extends MaterialCastingRecipe {

    public Table(ResourceLocation id, String group, Ingredient cast, int itemCost, IMaterialItem result, boolean consumed, boolean switchSlots) {
      super(TinkerRecipeTypes.CASTING_TABLE.get(), id, group, cast, itemCost, result, consumed, switchSlots);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
      return TinkerSmeltery.tableMaterialSerializer.get();
    }
  }

  /**
   * Interface representing a material casting recipe constructor
   *
   * @param <T> Recipe class type
   */
  public interface IFactory<T extends MaterialCastingRecipe> {

    T create(ResourceLocation id, String group, @Nullable Ingredient cast, int itemCost, IMaterialItem result,
             boolean consumed, boolean switchSlots);
  }

  @RequiredArgsConstructor
  public static class Serializer<T extends MaterialCastingRecipe> extends AbstractCastingRecipe.Serializer<T> {

    private final IFactory<T> factory;

    @Override
    protected T create(ResourceLocation idIn, String groupIn, @Nullable Ingredient cast, boolean consumed, boolean switchSlots, JsonObject json) {
      int itemCost = GsonHelper.getAsInt(json, "item_cost");
      IMaterialItem result = RecipeHelper.deserializeItem(GsonHelper.getAsString(json, "result"), "result", IMaterialItem.class);
      return this.factory.create(idIn, groupIn, cast, itemCost, result, consumed, switchSlots);
    }

    @Override
    protected T create(ResourceLocation idIn, String groupIn, @Nullable Ingredient cast, boolean consumed, boolean switchSlots, FriendlyByteBuf buffer) {
      int fluidAmount = buffer.readInt();
      IMaterialItem result = RecipeHelper.readItem(buffer, IMaterialItem.class);
      return this.factory.create(idIn, groupIn, cast, fluidAmount, result, consumed, switchSlots);
    }

    @Override
    protected void writeExtra(FriendlyByteBuf buffer, MaterialCastingRecipe recipe) {
      buffer.writeInt(recipe.itemCost);
      RecipeHelper.writeItem(buffer, recipe.result);
    }
  }
}
