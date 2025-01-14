package slimeknights.mantle.recipe.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Ingredient for a non-NBT sensitive item from another mod, should never be used outside datagen
 */
public class ItemNameIngredient extends Ingredient {

  private final List<ResourceLocation> names;

  protected ItemNameIngredient(List<ResourceLocation> names) {
    super(names.stream().map(NamedValue::new));
    this.names = names;
  }

  /**
   * Creates a new ingredient from a list of names
   */
  public static ItemNameIngredient from(List<ResourceLocation> names) {
    return new ItemNameIngredient(names);
  }

  /**
   * Creates a new ingredient from a list of names
   */
  public static ItemNameIngredient from(ResourceLocation... names) {
    return from(Arrays.asList(names));
  }

  @Override
  public boolean test(@Nullable ItemStack stack) {
    throw new UnsupportedOperationException();
  }

  /**
   * Creates a JSON object for a name
   */
  private static JsonObject forName(ResourceLocation name) {
    JsonObject json = new JsonObject();
    json.addProperty("item", name.toString());
    return json;
  }

  @Override
  public JsonElement toJson() {
    if (this.names.size() == 1) {
      return forName(this.names.get(0));
    }
    JsonArray array = new JsonArray();
    for (ResourceLocation name : this.names) {
      array.add(forName(name));
    }
    return array;
  }

  @RequiredArgsConstructor
  public static class NamedValue implements Value {

    private final ResourceLocation name;

    @Override
    public Collection<ItemStack> getItems() {
      return Collections.emptyList();
    }

    @Override
    public JsonObject serialize() {
      JsonObject json = new JsonObject();
      json.addProperty("item", this.name.toString());
      return json;
    }
  }
}
