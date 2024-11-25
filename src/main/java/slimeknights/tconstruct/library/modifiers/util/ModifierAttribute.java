package slimeknights.tconstruct.library.modifiers.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Represents an attribute in a modifier
 * TODO 1.19: merge into {@link slimeknights.tconstruct.library.modifiers.modules.AttributeModule}
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ModifierAttribute {

  private final String name;
  private final Attribute attribute;
  private final Operation operation;
  private final float amount;
  private final UUID[] slotUUIDs;

  public ModifierAttribute(String name, Attribute attribute, Operation operation, float amount, List<EquipmentSlot> slots) {
    this.name = name;
    this.attribute = attribute;
    this.operation = operation;
    this.amount = amount;
    this.slotUUIDs = new UUID[6];
    for (EquipmentSlot slot : slots) {
      this.slotUUIDs[slot.getFilterFlag()] = getUUID(name, slot);
    }
  }

  public ModifierAttribute(String name, Attribute attribute, Operation operation, float amount, EquipmentSlot... slots) {
    this.name = name;
    this.attribute = attribute;
    this.operation = operation;
    this.amount = amount;
    this.slotUUIDs = new UUID[6];
    for (EquipmentSlot slot : slots) {
      this.slotUUIDs[slot.getFilterFlag()] = getUUID(name, slot);
    }
  }

  /**
   * Applies this attribute boost
   *
   * @param tool     Tool receiving the boost
   * @param level    Modifier level
   * @param slot     Slot receiving the boost
   * @param consumer Consumer accepting attributes
   */
  public void apply(IToolStackView tool, float level, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
    // TODO: tag condition?
    UUID uuid = this.slotUUIDs[slot.getFilterFlag()];
    if (uuid != null) {
      consumer.accept(this.attribute, new AttributeModifier(uuid, this.name + "." + slot.getName(), this.amount * level, this.operation));
    }
  }

  /**
   * Converts this to JSON
   */
  public JsonObject toJson(JsonObject json) {
    json.addProperty("unique", this.name);
    json.addProperty("attribute", Objects.requireNonNull(BuiltInRegistries.ATTRIBUTE.getKey(this.attribute)).toString());
    json.addProperty("operation", this.operation.name().toLowerCase(Locale.ROOT));
    json.addProperty("amount", this.amount);
    JsonArray array = new JsonArray();
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      if (this.slotUUIDs[slot.getFilterFlag()] != null) {
        array.add(slot.getName());
      }
    }
    json.add("slots", array);
    return json;
  }

  /**
   * Converts this to JSON
   */
  public JsonObject toJson() {
    return this.toJson(new JsonObject());
  }

  /**
   * Parses the modifier attribute from JSON
   */
  public static ModifierAttribute fromJson(JsonObject json) {
    String unique = GsonHelper.getAsString(json, "unique");
    Attribute attribute = JsonHelper.getAsEntry(BuiltInRegistries.ATTRIBUTE, json, "attribute");
    Operation op = JsonHelper.getAsEnum(json, "operation", Operation.class);
    float amount = GsonHelper.getAsFloat(json, "amount");
    List<EquipmentSlot> slots = JsonHelper.parseList(json, "slots", (element, string) -> EquipmentSlot.byName(GsonHelper.convertToString(element, string)));
    return new ModifierAttribute(unique, attribute, op, amount, slots);
  }

  /**
   * Writes this to the network
   */
  public void toNetwork(FriendlyByteBuf buffer) {
    buffer.writeUtf(this.name);
    buffer.writeResourceLocation(BuiltInRegistries.ATTRIBUTE.getKey(this.attribute));
    buffer.writeEnum(this.operation);
    buffer.writeFloat(this.amount);
    int packed = 0;
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      if (this.slotUUIDs[slot.getFilterFlag()] != null) {
        packed |= (1 << slot.getFilterFlag());
      }
    }
    buffer.writeInt(packed);
  }

  /**
   * Reads this from the network
   */
  public static ModifierAttribute fromNetwork(FriendlyByteBuf buffer) {
    String name = buffer.readUtf(Short.MAX_VALUE);
    Attribute attribute = BuiltInRegistries.ATTRIBUTE.get(buffer.readResourceLocation());
    Operation operation = buffer.readEnum(Operation.class);
    float amount = buffer.readFloat();
    int packed = buffer.readInt();
    UUID[] slotUUIDs = new UUID[6];
    for (EquipmentSlot slot : EquipmentSlot.values()) {
      if ((packed & (1 << slot.getFilterFlag())) > 0) {
        slotUUIDs[slot.getFilterFlag()] = getUUID(name, slot);
      }
    }
    return new ModifierAttribute(name, attribute, operation, amount, slotUUIDs);
  }

  /**
   * Gets the UUID from a name
   */
  public static UUID getUUID(String name, EquipmentSlot slot) {
    return UUID.nameUUIDFromBytes((name + "." + slot.getName()).getBytes());
  }
}
