package slimeknights.tconstruct.tools.modifiers.ability.tool;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.common.recipe.RecipeCacheInvalidator;
import slimeknights.tconstruct.library.modifiers.impl.NoLevelsModifier;
import slimeknights.tconstruct.library.recipe.SingleItemContainer;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AutosmeltModifier extends NoLevelsModifier {

  /**
   * Cache of relevant smelting recipes
   */
  private final Cache<Item, Optional<SmeltingRecipe>> recipeCache = CacheBuilder
    .newBuilder()
    .maximumSize(64)
    .build();
  /**
   * Inventory instance to use for recipe search
   */
  private final SingleItemContainer inventory = new SingleItemContainer();

  public AutosmeltModifier() {
    RecipeCacheInvalidator.addReloadListener(client -> {
      if (!client) {
        this.recipeCache.invalidateAll();
      }
    });
  }

  /**
   * Gets a furnace recipe without using the cache
   *
   * @param stack Stack to try
   * @param world World instance
   * @return Furnace recipe
   */
  private Optional<SmeltingRecipe> findRecipe(ItemStack stack, Level world) {
    this.inventory.setStack(stack);
    return world.getRecipeManager().getRecipeFor(RecipeType.SMELTING, this.inventory, world);
  }

  /**
   * Gets a cached furnace recipe
   *
   * @param stack Stack for recipe
   * @param world World instance
   * @return Cached recipe
   */
  @Nullable
  private SmeltingRecipe findCachedRecipe(ItemStack stack, Level world) {
    // don't use the cache if there is a tag, prevent breaking NBT sensitive recipes
    if (stack.hasTag()) {
      return this.findRecipe(stack, world).orElse(null);
    }
    try {
      return this.recipeCache.get(stack.getItem(), () -> this.findRecipe(stack, world)).orElse(null);
    } catch (ExecutionException e) {
      return null;
    }
  }

  /**
   * Smelts an item using the relevant furnace recipe
   *
   * @param stack Stack to smelt
   * @param world World instance
   * @return Smelted item, or original if no recipe
   */
  private ItemStack smeltItem(ItemStack stack, Level world) {
    // skip blacklisted entries
    if (stack.is(TinkerTags.Items.AUTOSMELT_BLACKLIST)) {
      return stack;
    }
    SmeltingRecipe recipe = this.findCachedRecipe(stack, world);
    if (recipe != null) {
      this.inventory.setStack(stack);
      ItemStack output = recipe.assemble(this.inventory, world.registryAccess());
      if (stack.getCount() > 1) {
        // recipe output is a copy, safe to modify
        output.setCount(output.getCount() * stack.getCount());
      }
      return output;
    }
    return stack;
  }

  @Override
  public ObjectArrayList<ItemStack> processLoot(IToolStackView tool, int level, ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
    Level world = context.getLevel();
    if (!generatedLoot.isEmpty()) {
      return new ObjectArrayList<>(generatedLoot.stream()
        .map(stack -> this.smeltItem(stack, world))
        .filter(stack -> !stack.isEmpty())
        .collect(Collectors.toList()));
    }
    return generatedLoot;
  }
}
