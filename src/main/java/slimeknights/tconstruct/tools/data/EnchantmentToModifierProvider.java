package slimeknights.tconstruct.tools.data;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.world.item.enchantment.Enchantments;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.data.tinkering.AbstractEnchantmentToModifierProvider;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.tools.TinkerModifiers;

public class EnchantmentToModifierProvider extends AbstractEnchantmentToModifierProvider {

  public EnchantmentToModifierProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  protected void addEnchantmentMappings() {
    // general
    this.add(Enchantments.UNBREAKING, TinkerModifiers.reinforced.getId());

    // protection
    this.add(Enchantments.ALL_DAMAGE_PROTECTION, TinkerModifiers.protection.getId());
    this.add(Enchantments.FIRE_PROTECTION, TinkerModifiers.fireProtection.getId());
    this.add(Enchantments.BLAST_PROTECTION, TinkerModifiers.blastProtection.getId());
    this.add(Enchantments.PROJECTILE_PROTECTION, TinkerModifiers.projectileProtection.getId());
    this.add(Enchantments.FALL_PROTECTION, TinkerModifiers.featherFalling.getId());
    // misc armor
    this.add(Enchantments.RESPIRATION, ModifierIds.respiration);
    this.add(Enchantments.AQUA_AFFINITY, TinkerModifiers.aquaAffinity.getId());
    this.add(Enchantments.THORNS, TinkerModifiers.thorns.getId());
    this.add(Enchantments.DEPTH_STRIDER, ModifierIds.depthStrider);
    this.add(Enchantments.FROST_WALKER, TinkerModifiers.frostWalker.getId());
    this.add(Enchantments.SOUL_SPEED, TinkerModifiers.soulspeed.getId());

    // melee
    this.add(Enchantments.SHARPNESS, ModifierIds.sharpness);
    this.add(Enchantments.SMITE, ModifierIds.smite);
    this.add(Enchantments.BANE_OF_ARTHROPODS, ModifierIds.baneOfSssss);
    this.add(Enchantments.KNOCKBACK, TinkerModifiers.knockback.getId());
    this.add(Enchantments.FIRE_ASPECT, TinkerModifiers.fiery.getId());
    this.add(Enchantments.MOB_LOOTING, ModifierIds.luck);
    this.add(Enchantments.SWEEPING_EDGE, TinkerModifiers.sweeping.getId());
    this.add(Enchantments.IMPALING, ModifierIds.antiaquatic);

    // harvest
    this.add(Enchantments.BLOCK_EFFICIENCY, TinkerModifiers.haste.getId());
    this.add(Enchantments.SILK_TOUCH, TinkerModifiers.silky.getId());
    this.add(Enchantments.BLOCK_FORTUNE, ModifierIds.luck);

    // ranged
    this.add(Enchantments.POWER_ARROWS, ModifierIds.power);
    this.add(Enchantments.PUNCH_ARROWS, TinkerModifiers.punch.getId());
    this.add(Enchantments.FLAMING_ARROWS, TinkerModifiers.fiery.getId());
    this.add(Enchantments.INFINITY_ARROWS, TinkerModifiers.crystalshot.getId());
    this.add(Enchantments.MULTISHOT, TinkerModifiers.multishot.getId());
    this.add(Enchantments.QUICK_CHARGE, ModifierIds.quickCharge);
    this.add(Enchantments.PIERCING, TinkerModifiers.impaling.getId());

    // tag compat
    // upgrade
    this.addCompat(TinkerModifiers.experienced.getId());
    this.addCompat(ModifierIds.killager);
    this.addCompat(TinkerModifiers.magnetic.getId());
    this.addCompat(TinkerModifiers.necrotic.getId());
    this.addCompat(TinkerModifiers.severing.getId());
    this.addCompat(ModifierIds.stepUp);
    this.addCompat(TinkerModifiers.soulbound.getId());
    this.addCompat(ModifierIds.trueshot);

    // defense
    this.addCompat(ModifierIds.knockbackResistance);
    this.addCompat(TinkerModifiers.magicProtection.getId());
    this.addCompat(ModifierIds.revitalizing);

    // ability
    this.addCompat(TinkerModifiers.autosmelt.getId());
    this.addCompat(TinkerModifiers.doubleJump.getId());
    this.addCompat(TinkerModifiers.expanded.getId());
    this.addCompat(ModifierIds.luck);
    this.addCompat(TinkerModifiers.multishot.getId());
    this.addCompat(ModifierIds.reach);
    this.addCompat(TinkerModifiers.tilling.getId());
    this.addCompat(TinkerModifiers.reflecting.getId());
  }

  /**
   * Adds a compat enchantment
   */
  private void addCompat(ModifierId modifier) {
    this.add(TConstruct.getResource("modifier_like/" + modifier.getPath()), modifier);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Enchantment to Modifier Mapping";
  }
}
