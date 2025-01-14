package slimeknights.mantle.recipe.data;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.RequiredArgsConstructor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.helper.ItemOutput;

import javax.annotation.Nullable;

/**
 * Extension of {@link ItemOutput} for datagen of recipes for compat. Should never be used in an actual recipe
 */
@RequiredArgsConstructor(staticName = "fromName")
public class ItemNameOutput extends ItemOutput {

  private final ResourceLocation name;
  private final int count;
  @Nullable
  private final CompoundTag nbt;

  /**
   * Creates an output for the given item with no NBT
   *
   * @param name  Item name
   * @param count Count
   * @return Output
   */
  public static ItemNameOutput fromName(ResourceLocation name, int count) {
    return fromName(name, count, null);
  }

  /**
   * Creates an output for the given item with a count of 1
   *
   * @param name Item name
   * @return Output
   */
  public static ItemNameOutput fromName(ResourceLocation name) {
    return fromName(name, 1);
  }

  @Override
  public ItemStack get() {
    throw new UnsupportedOperationException("Cannot get the item stack from a item name output");
  }

  @Override
  public JsonElement serialize() {
    String itemName = this.name.toString();
    if (this.nbt == null && this.count <= 1) {
      return new JsonPrimitive(itemName);
    } else {
      JsonObject jsonResult = new JsonObject();
      jsonResult.addProperty("item", itemName);
      if (this.count > 1) {
        jsonResult.addProperty("count", this.count);
      }

      if (this.nbt != null) {
        jsonResult.addProperty("nbt", this.nbt.toString());
      }

      return jsonResult;
    }
  }
}
