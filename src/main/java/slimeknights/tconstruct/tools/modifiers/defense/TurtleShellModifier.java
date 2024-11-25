package slimeknights.tconstruct.tools.modifiers.defense;

import io.github.fabricators_of_create.porting_lib.attributes.PortingLibAttributes;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.tconstruct.library.modifiers.impl.IncrementalModifier;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.utils.TooltipKey;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class TurtleShellModifier extends IncrementalModifier {

  private static final UUID[] UUIDS = {
    UUID.fromString("62a1c224-50e5-11ec-bf63-0242ac130002"),
    UUID.fromString("62a1c4a4-50e5-11ec-bf63-0242ac130002"),
    UUID.fromString("62a1c5e4-50e5-11ec-bf63-0242ac130002"),
    UUID.fromString("62a1c6e8-50e5-11ec-bf63-0242ac130002")
  };

  @Override
  public void addAttributes(IToolStackView tool, int level, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
    consumer.accept(PortingLibAttributes.SWIM_SPEED, new AttributeModifier(UUIDS[slot.getIndex()], "tconstruct.modifier.armor_power." + slot.getName(), 0.05f * this.getScaledLevel(tool, level), Operation.MULTIPLY_TOTAL));
  }

  @Override
  public float getProtectionModifier(IToolStackView tool, int level, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float modifierValue) {
    if (!source.is(DamageTypeTags.BYPASSES_EFFECTS) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
      LivingEntity entity = context.getEntity();
      // helmet/chest boost if eyes in water, legs/boots boost if feet in water
      if ((slotType == EquipmentSlot.HEAD || slotType == EquipmentSlot.CHEST) ? entity.wasEyeInWater : entity.isInWater()) {
        modifierValue += this.getScaledLevel(tool, level) * 2.5f;
      }
    }
    return modifierValue;
  }

  @Override
  public void addInformation(IToolStackView tool, int level, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    AbstractProtectionModifier.addResistanceTooltip(this, tool, level, 2.5f, tooltip);
  }
}
