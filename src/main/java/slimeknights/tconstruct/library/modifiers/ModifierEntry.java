package slimeknights.tconstruct.library.modifiers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.With;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import slimeknights.tconstruct.library.modifiers.util.LazyModifier;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;

import java.lang.reflect.Type;
import java.util.Objects;

/**
 * Data class holding a modifier with a level
 */
@RequiredArgsConstructor
public class ModifierEntry implements Comparable<ModifierEntry> {

  /**
   * JSON serializer instance for GSON
   */
  public static final Serializer SERIALIZER = new Serializer();

  /**
   * Modifier instance
   */
  private final LazyModifier modifier;
  /**
   * Current level
   */
  @Getter
  @With
  private final int level;

  public ModifierEntry(ModifierId id, int level) {
    this(new LazyModifier(id), level);
  }

  public ModifierEntry(Modifier modifier, int level) {
    this(new LazyModifier(modifier), level);
  }

  /**
   * Checks if the given modifier is bound
   */
  public boolean isBound() {
    return this.modifier.isBound();
  }

  /**
   * Gets the contained modifier ID, prevents resolving the lazy modifier if not needed
   */
  public ModifierId getId() {
    return this.modifier.getId();
  }

  /**
   * Gets the contained modifier
   */
  public Modifier getModifier() {
    return this.modifier.get();
  }

  /**
   * Helper for efficiency, returns the lazy modifier instance directly, which can then be copied along
   */
  public LazyModifier getLazyModifier() {
    return this.modifier;
  }

  /**
   * Gets the level scaled based on attributes of modifier data. Used mainly for incremental modifiers.
   *
   * @param tool Tool context
   * @return Entry level, possibly adjusted by tool properties
   */
  public float getEffectiveLevel(IToolContext tool) {
    return this.modifier.get().getEffectiveLevel(tool, this.level);
  }

  /**
   * Gets the given hook from the modifier, returning default instance if not present
   */
  public final <T> T getHook(ModifierHook<T> hook) {
    return this.modifier.get().getHook(hook);
  }

  /**
   * Checks if this entry matches the given modifier
   */
  public boolean matches(ModifierId id) {
    return this.modifier.getId().equals(id);
  }

  /**
   * Checks if this entry matches the given modifier
   */
  public boolean matches(Modifier modifier) {
    return this.matches(modifier.getId());
  }

  /**
   * Checks if the modifier is in the given tag
   */
  public boolean matches(TagKey<Modifier> tag) {
    return this.modifier.is(tag);
  }

  @Override
  public int compareTo(ModifierEntry other) {
    Modifier mod1 = this.getModifier(), mod2 = other.getModifier();
    int priority1 = mod1.getPriority(), priority2 = mod2.getPriority();
    // sort by priority first if different
    if (priority1 != priority2) {
      // reversed order so higher goes first
      return Integer.compare(priority2, priority1);
    }
    // fallback to ID path, approximates localized name so we get mostly alphabetical sort in the tooltip
    return mod1.getId().getPath().compareTo(mod2.getId().getPath());
  }

  /**
   * Parses a modifier entry from JSON
   *
   * @param json JSON object
   * @return Parsed JSON
   */
  public static ModifierEntry fromJson(JsonObject json) {
    return new ModifierEntry(ModifierId.getFromJson(json, "name"), GsonHelper.getAsInt(json, "level", 1));
  }

  /**
   * Converts this entry to JSON
   *
   * @return Json object of entry
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    json.addProperty("name", this.getId().toString());
    json.addProperty("level", this.level);
    return json;
  }

  /**
   * Converts this entry to Nbt
   *
   * @return Compound tag of entry
   */
  public CompoundTag toNbt() {
    CompoundTag tag = new CompoundTag();
    tag.putString("name", this.getId().toString());
    tag.putInt("level", this.level);
    return tag;
  }

  /**
   * Parses a modifier entry from JSON
   *
   * @param tag Compound object
   * @return Parsed Compound
   */
  public static ModifierEntry fromNbt(CompoundTag tag) {
    return new ModifierEntry(ModifierId.getFromNbt(tag, "name"), tag.getInt("level"));
  }

  /**
   * Reads this modifier entry from the packet buffer
   *
   * @param buffer Buffer instance
   * @return Read entry
   */
  public static ModifierEntry read(FriendlyByteBuf buffer) {
    return new ModifierEntry(ModifierId.fromNetwork(buffer), buffer.readVarInt());
  }

  /**
   * Writes this modifier entry to the packet buffer
   *
   * @param buffer Buffer instance
   */
  public void write(FriendlyByteBuf buffer) {
    this.getId().toNetwork(buffer);
    buffer.writeVarInt(this.level);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || this.getClass() != o.getClass()) return false;
    ModifierEntry entry = (ModifierEntry) o;
    return this.matches(entry.getId()) && this.level == entry.level;
  }

  @Override
  public int hashCode() {
    return 31 * this.modifier.hashCode() + Objects.hash(this.level);
  }

  @Override
  public String toString() {
    return "ModifierEntry{" + this.modifier.getId() + ",level=" + this.level + '}';
  }

  private static class Serializer implements JsonDeserializer<ModifierEntry>, JsonSerializer<ModifierEntry> {

    @Override
    public ModifierEntry deserialize(JsonElement json, Type type, JsonDeserializationContext context) throws JsonParseException {
      return fromJson(GsonHelper.convertToJsonObject(json, "modifier"));
    }

    @Override
    public JsonElement serialize(ModifierEntry entry, Type type, JsonSerializationContext context) {
      return entry.toJson();
    }
  }
}
