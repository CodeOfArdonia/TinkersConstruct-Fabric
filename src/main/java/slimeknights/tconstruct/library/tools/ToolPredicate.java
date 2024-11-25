package slimeknights.tconstruct.library.tools;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.recipe.helper.RecipeHelper;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags.Items;
import slimeknights.tconstruct.library.materials.definition.MaterialId;
import slimeknights.tconstruct.library.materials.definition.MaterialVariant;
import slimeknights.tconstruct.library.recipe.modifiers.ModifierMatch;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.tools.stat.StatPredicate;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/**
 * Variant of ItemPredicate for matching Tinker tools
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ToolPredicate extends ItemPredicate {

  public static final ResourceLocation ID = TConstruct.getResource("tool");

  @Nullable
  protected final Item item;
  @Nullable
  protected final TagKey<Item> tag;
  protected final List<MaterialId> materials;
  protected final boolean hasUpgrades;
  protected final ModifierMatch upgrades;
  protected final ModifierMatch modifiers;
  protected final List<StatPredicate> stats;

  @Override
  public boolean matches(ItemStack stack) {
    // first validate item and tag
    if (this.tag != null && !stack.is(this.tag)) {
      return false;
    }
    if (this.item != null && stack.getItem() != this.item) {
      return false;
    }
    // prevent changing NBT for non-tools
    if (!stack.is(Items.MODIFIABLE)) {
      return false;
    }
    ToolStack tool = ToolStack.from(stack);

    // materials
    matLoop:
    for (MaterialId check : this.materials) {
      for (MaterialVariant mat : tool.getMaterials().getList()) {
        if (mat.getId().equals(check)) {
          continue matLoop;
        }
      }
      return false;
    }

    // modifiers
    if (this.hasUpgrades && tool.getUpgrades().isEmpty()) {
      return false;
    }
    if (this.upgrades != ModifierMatch.ALWAYS && !this.upgrades.test(tool.getUpgrades().getModifiers())) {
      return false;
    }
    if (this.modifiers != ModifierMatch.ALWAYS && !this.modifiers.test(tool.getModifierList())) {
      return false;
    }
    // stats
    if (!this.stats.isEmpty()) {
      StatsNBT toolStats = tool.getStats();
      for (StatPredicate predicate : this.stats) {
        if (!predicate.test(toolStats)) {
          return false;
        }
      }
    }

    return true;
  }

  /**
   * Converts the given list to a json array
   */
  private static <D> JsonArray toArray(List<D> list, Function<D, JsonElement> mapper) {
    JsonArray array = new JsonArray();
    for (D data : list) {
      array.add(mapper.apply(data));
    }
    return array;
  }

  @Override
  public JsonElement serializeToJson() {
    JsonObject json = new JsonObject();
    json.addProperty("type", ID.toString());
    if (this.item != null) {
      json.addProperty("item", Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(this.item)).toString());
    }
    if (this.tag != null) {
      json.addProperty("tag", this.tag.location().toString());
    }
    if (!this.materials.isEmpty()) {
      json.add("materials", toArray(this.materials, mat -> new JsonPrimitive(mat.toString())));
    }
    if (this.hasUpgrades) {
      json.addProperty("has_upgrades", true);
    }
    if (this.upgrades != ModifierMatch.ALWAYS) {
      json.add("upgrades", this.upgrades.serialize());
    }
    if (this.modifiers != ModifierMatch.ALWAYS) {
      json.add("modifiers", this.modifiers.serialize());
    }
    if (!this.stats.isEmpty()) {
      json.add("stats", toArray(this.stats, StatPredicate::serialize));
    }
    return json;
  }

  /**
   * Deserializes the tool predicate from JSON
   */
  public static ToolPredicate deserialize(JsonObject json) {
    // item
    Item item = null;
    if (json.has("item")) {
      item = RecipeHelper.deserializeItem(GsonHelper.getAsString(json, "item"), "item", Item.class);
    }
    // tag
    TagKey<Item> tag = null;
    if (json.has("tag")) {
      tag = TagKey.create(Registries.ITEM, JsonHelper.getResourceLocation(json, "tag"));
    }
    // materials
    List<MaterialId> materials = Collections.emptyList();
    if (json.has("materials")) {
      materials = JsonHelper.parseList(json, "materials", (element, key) -> new MaterialId(GsonHelper.convertToString(element, key)));
    }
    // upgrades
    boolean hasUpgrades = GsonHelper.getAsBoolean(json, "has_upgrades", false);
    ModifierMatch upgrades = ModifierMatch.ALWAYS;
    if (json.has("upgrades")) {
      upgrades = ModifierMatch.deserialize(GsonHelper.getAsJsonObject(json, "upgrades"));
    }
    // modifiers
    ModifierMatch modifiers = ModifierMatch.ALWAYS;
    if (json.has("modifiers")) {
      modifiers = ModifierMatch.deserialize(GsonHelper.getAsJsonObject(json, "modifiers"));
    }
    // stats
    List<StatPredicate> stats = Collections.emptyList();
    if (json.has("stats")) {
      stats = JsonHelper.parseList(json, "stats", StatPredicate::deserialize);
    }
    return new ToolPredicate(item, tag, materials, hasUpgrades, upgrades, modifiers, stats);
  }

  /**
   * Creates a new builder instance for an item
   */
  public static Builder builder(Item item) {
    return new Builder(item, null);
  }

  /**
   * Creates a new builder instance for a tag
   */
  public static Builder builder(TagKey<Item> tag) {
    return new Builder(null, tag);
  }

  /**
   * Creates a new builder instance for any item
   */
  public static Builder builder() {
    return new Builder(null, null);
  }

  /**
   * Builder for data generators
   */
  @SuppressWarnings("unused")
  @Setter
  @Accessors(fluent = true)
  public static class Builder {

    /**
     * Item that must match
     */
    @Nullable
    protected final Item item;
    /**
     * Tag that must match
     */
    @Nullable
    protected final TagKey<Item> tag;
    /**
     * Materials that must be contained in the tool
     */
    protected final List<MaterialId> materials = new ArrayList<>();
    /**
     * If true, the tool must have at least 1 upgrade
     */
    protected boolean hasUpgrades = false;
    /**
     * List of upgrades that must exist in the tool
     */
    protected ModifierMatch upgrades = ModifierMatch.ALWAYS;
    /**
     * List of modifiers that must exist in the tool
     */
    protected ModifierMatch modifiers = ModifierMatch.ALWAYS;
    protected final List<StatPredicate> stats = new ArrayList<>();

    protected Builder(@Nullable Item item, @Nullable TagKey<Item> tag) {
      this.item = item;
      this.tag = tag;
    }

    /**
     * Adds the given material as a requirement
     */
    public Builder withMaterial(MaterialId material) {
      this.materials.add(material);
      return this;
    }

    /**
     * Adds the given stat predicate as a requirement
     */
    public Builder withStat(StatPredicate predicate) {
      this.stats.add(predicate);
      return this;
    }

    /**
     * Creates the predicate
     */
    public ToolPredicate build() {
      return new ToolPredicate(this.item, this.tag, this.materials, this.hasUpgrades, this.upgrades, this.modifiers, this.stats);
    }
  }
}
