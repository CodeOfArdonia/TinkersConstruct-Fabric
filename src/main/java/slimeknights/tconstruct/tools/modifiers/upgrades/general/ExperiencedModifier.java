package slimeknights.tconstruct.tools.modifiers.upgrades.general;

import io.github.fabricators_of_create.porting_lib.entity.events.LivingDeathEvent;
import io.github.fabricators_of_create.porting_lib.event.common.BlockEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.LivingEntityEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.tools.capability.EntityModifierCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability;
import slimeknights.tconstruct.library.tools.capability.TinkerDataCapability.TinkerDataKey;
import slimeknights.tconstruct.library.tools.helper.ModifierLootingHandler;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;

public class ExperiencedModifier extends Modifier {

  private static final TinkerDataKey<Integer> EXPERIENCED = TConstruct.createKey("experienced");

  public ExperiencedModifier() {
    LivingEntityEvents.EXPERIENCE_DROP.register(this::onExperienceDrop);
    LivingDeathEvent.DEATH.register(this::onEntityKilled);
    BlockEvents.BLOCK_BREAK.register(this::beforeBlockBreak);
  }

  /**
   * Boosts the original based on the level
   *
   * @param original Original amount
   * @param level    Modifier level
   * @return Boosted XP
   */
  private static int boost(int original, int level) {
    return (int) (original * (1 + (0.5 * level)));
  }

  /**
   * Used to modify the XP dropped, regular hook is just for canceling
   *
   * @param event Event
   */
  private void beforeBlockBreak(BlockEvents.BreakEvent event) {
    // only support main hand block breaking currently
    int level = 0;
    ToolStack tool = getHeldTool(event.getPlayer(), InteractionHand.MAIN_HAND);
    if (tool != null) {
      level = tool.getModifierLevel(this);
    }
    // bonus from experienced pants
    tool = getHeldTool(event.getPlayer(), EquipmentSlot.LEGS);
    if (tool != null) {
      level += tool.getModifierLevel(this);
    }
    if (level > 0) {
      event.setExpToDrop(boost(event.getExpToDrop(), level));
    }
  }

  /**
   * Mark entities killed by our arrows
   */
  private void onEntityKilled(LivingDeathEvent event) {
    DamageSource source = event.getSource();
    if (source != null && source.getDirectEntity() instanceof Projectile projectile) {
      ModifierNBT modifiers = EntityModifierCapability.getOrEmpty(projectile);
      // it is very unlikely that we fire an arrow on a bow with no modifiers, if that ever happens though we will not be able to identify its our arrow
      if (!modifiers.isEmpty()) {
        TinkerDataCapability.CAPABILITY.maybeGet(event.getEntity()).ifPresent(data -> data.put(EXPERIENCED, modifiers.getLevel(this.getId())));
      }
    }
  }

  /**
   * Event handled locally as its pretty specialized
   *
   * @param amount amount of xp
   * @param player current player
   */
  private int onExperienceDrop(int amount, Player player, LivingEntity entity) {
    // if the entity was killed by one of our arrows, boost the experience from that
    int experienced = TinkerDataCapability.CAPABILITY.maybeGet(entity).map(data -> data.get(EXPERIENCED)).orElse(-1);
    if (experienced > 0) {
      return boost(amount, experienced);
      // experienced being zero means it was our arrow but it was not modified, do not check the held item in that case
    } else if (experienced != 0) {
      if (player != null) {
        int level = 0;
        // held tool
        ToolStack tool = getHeldTool(player, ModifierLootingHandler.getLootingSlot(player));
        if (tool != null) level = tool.getModifierLevel(this);
        // bonus from experienced pants
        tool = getHeldTool(player, EquipmentSlot.LEGS);
        if (tool != null) level += tool.getModifierLevel(this);
        if (level > 0) {
          return boost(amount, level);
        }
      }
    }
    return amount;
  }
}
