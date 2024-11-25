package slimeknights.tconstruct.tools.modifiers.traits;

import io.github.fabricators_of_create.porting_lib.common.util.Lazy;
import io.github.fabricators_of_create.porting_lib.entity.events.PlayerEvents;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.TooltipKey;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Shared logic for jagged and stonebound. Trait boosts attack damage as it lowers mining speed.
 */
public class DamageSpeedTradeModifier extends Modifier {

  private static final Component MINING_SPEED = TConstruct.makeTranslation("modifier", "fake_attribute.mining_speed");
  private final float multiplier;
  private final Lazy<UUID> uuid = Lazy.of(() -> UUID.nameUUIDFromBytes(this.getId().toString().getBytes()));
  private final Lazy<String> attributeName = Lazy.of(() -> {
    ResourceLocation id = this.getId();
    return id.getPath() + "." + id.getNamespace() + ".attack_damage";
  });

  /**
   * Creates a new instance of
   *
   * @param multiplier Multiplier. Positive boosts damage, negative boosts mining speed
   */
  public DamageSpeedTradeModifier(float multiplier) {
    this.multiplier = multiplier;
  }

  /**
   * Gets the multiplier for this modifier at the current durability and level
   */
  private double getMultiplier(IToolStackView tool, int level) {
    return Math.sqrt(tool.getDamage() * level / tool.getMultiplier(ToolStats.DURABILITY)) * this.multiplier;
  }

  @Override
  public void addInformation(IToolStackView tool, int level, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    double boost = this.getMultiplier(tool, level);
    if (boost != 0 && tool.hasTag(TinkerTags.Items.HARVEST)) {
      tooltip.add(this.applyStyle(Component.literal(Util.PERCENT_BOOST_FORMAT.format(-boost)).append(" ").append(MINING_SPEED)));
    }
  }

  @Override
  public void addAttributes(IToolStackView tool, int level, EquipmentSlot slot, BiConsumer<Attribute, AttributeModifier> consumer) {
    if (slot == EquipmentSlot.MAINHAND) {
      double boost = this.getMultiplier(tool, level);
      if (boost != 0) {
        // half boost for attack speed, its
        consumer.accept(Attributes.ATTACK_DAMAGE, new AttributeModifier(this.uuid.get(), this.attributeName.get(), boost / 2, Operation.MULTIPLY_TOTAL));
      }
    }
  }

  @Override
  public void onBreakSpeed(IToolStackView tool, int level, PlayerEvents.BreakSpeed event, Direction sideHit, boolean isEffective, float miningSpeedModifier) {
    if (isEffective) {
      event.setNewSpeed((float) (event.getNewSpeed() * (1 - this.getMultiplier(tool, level))));
    }
  }
}
