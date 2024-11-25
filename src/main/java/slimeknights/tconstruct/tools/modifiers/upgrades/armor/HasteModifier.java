package slimeknights.tconstruct.tools.modifiers.upgrades.armor;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.modifiers.impl.IncrementalArmorLevelModifier;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay;
import slimeknights.tconstruct.library.modifiers.util.ModifierLevelDisplay.UniqueForLevels;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;
import slimeknights.tconstruct.library.tools.stat.ToolStats;
import slimeknights.tconstruct.library.utils.TooltipKey;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nullable;
import java.util.List;

public class HasteModifier extends IncrementalArmorLevelModifier {

  private static final Component MINING_SPEED = TConstruct.makeTranslation("modifier", "fake_attribute.mining_speed");
  /**
   * Player modifier data key for haste
   */
  public static final TinkerDataKey<Float> HASTE = TConstruct.createKey("haste");

  private static final ModifierLevelDisplay NAME = new UniqueForLevels(5);

  public HasteModifier() {
    super(HASTE);
  }

  @Override
  public Component getDisplayName(int level) {
    return NAME.nameForLevel(this, level);
  }

  @Override
  public void addToolStats(ToolRebuildContext context, int level, ModifierStatsBuilder builder) {
    ToolStats.MINING_SPEED.add(builder, 4 * this.getEffectiveLevel(context, level));
  }

  @Override
  public void addInformation(IToolStackView tool, int level, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    if (tool.hasTag(TinkerTags.Items.ARMOR)) {
      double boost = 0.1 * this.getScaledLevel(tool, level);
      if (boost != 0) {
        tooltip.add(this.applyStyle(Component.literal(Util.PERCENT_BOOST_FORMAT.format(boost)).append(" ").append(MINING_SPEED)));
      }
    }
  }
}
