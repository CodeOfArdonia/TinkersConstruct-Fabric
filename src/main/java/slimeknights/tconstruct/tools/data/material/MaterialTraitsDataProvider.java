package slimeknights.tconstruct.tools.data.material;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.data.material.AbstractMaterialTraitDataProvider;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.stats.SkullStats;

import static slimeknights.tconstruct.library.materials.MaterialRegistry.MELEE_HARVEST;
import static slimeknights.tconstruct.library.materials.MaterialRegistry.RANGED;

public class MaterialTraitsDataProvider extends AbstractMaterialTraitDataProvider {

  public MaterialTraitsDataProvider(FabricDataOutput output, AbstractMaterialDataProvider materials) {
    super(output, materials);
  }

  @Override
  public String getName() {
    return "Tinker's Construct Material Traits";
  }

  @Override
  protected void addMaterialTraits() {
    // tier 1
    this.addDefaultTraits(MaterialIds.wood, ModifierIds.cultivated);
    this.addDefaultTraits(MaterialIds.rock, TinkerModifiers.stonebound);
    this.addDefaultTraits(MaterialIds.flint, TinkerModifiers.jagged);
    this.addDefaultTraits(MaterialIds.bone, TinkerModifiers.piercing);
    this.addTraits(MaterialIds.bamboo, RANGED, ModifierIds.flexible);
    // tier 1 - end
    this.addDefaultTraits(MaterialIds.chorus, TinkerModifiers.enderference);
    // tier 1 - binding
    this.addDefaultTraits(MaterialIds.string, ModifierIds.stringy);
    this.addDefaultTraits(MaterialIds.leather, TinkerModifiers.tanned);
    this.addDefaultTraits(MaterialIds.vine, TinkerModifiers.solarPowered);

    // tier 2
    this.addDefaultTraits(MaterialIds.iron, TinkerModifiers.magnetic);
    this.addDefaultTraits(MaterialIds.copper, TinkerModifiers.dwarven);
    this.addDefaultTraits(MaterialIds.searedStone, TinkerModifiers.searing);
    this.addDefaultTraits(MaterialIds.slimewood, TinkerModifiers.overgrowth, TinkerModifiers.overslime);
    this.addDefaultTraits(MaterialIds.bloodbone, TinkerModifiers.raging);
    this.addTraits(MaterialIds.aluminum, RANGED, ModifierIds.featherweight);
    // tier 2 - nether
    this.addDefaultTraits(MaterialIds.necroticBone, TinkerModifiers.necrotic);
    this.addDefaultTraits(MaterialIds.scorchedStone, ModifierIds.scorching);
    // tier 2 - end
    this.addDefaultTraits(MaterialIds.whitestone, TinkerModifiers.stoneshield);
    // tier 2 - binding
    this.addDefaultTraits(MaterialIds.chain, TinkerModifiers.reinforced);
    this.addDefaultTraits(MaterialIds.skyslimeVine, TinkerModifiers.airborne);

    // tier 3
    this.addDefaultTraits(MaterialIds.slimesteel, TinkerModifiers.overcast, TinkerModifiers.overslime);
    this.addTraits(MaterialIds.amethystBronze, MELEE_HARVEST, ModifierIds.crumbling);
    this.addTraits(MaterialIds.amethystBronze, RANGED, TinkerModifiers.crystalbound);
    this.addDefaultTraits(MaterialIds.nahuatl, TinkerModifiers.lacerating);
    this.addDefaultTraits(MaterialIds.roseGold, ModifierIds.enhanced);
    this.addDefaultTraits(MaterialIds.pigIron, TinkerModifiers.tasty);
    // tier 3 - nether
    this.addDefaultTraits(MaterialIds.cobalt, ModifierIds.lightweight);
    // tier 3 - binding
    this.addDefaultTraits(MaterialIds.darkthread, ModifierIds.looting);

    // tier 4
    this.addDefaultTraits(MaterialIds.queensSlime, TinkerModifiers.overlord, TinkerModifiers.overslime);
    this.addDefaultTraits(MaterialIds.hepatizon, TinkerModifiers.momentum);
    this.addDefaultTraits(MaterialIds.manyullyn, TinkerModifiers.insatiable);
    this.addDefaultTraits(MaterialIds.blazingBone, TinkerModifiers.conducting);
    // tier 4 - binding
    this.addDefaultTraits(MaterialIds.ancientHide, ModifierIds.fortune);

    // tier 5
    this.addDefaultTraits(MaterialIds.enderslimeVine, TinkerModifiers.enderporting);

    // tier 2 - mod compat
    this.addDefaultTraits(MaterialIds.osmium, TinkerModifiers.dense);
    this.addDefaultTraits(MaterialIds.tungsten, ModifierIds.sharpweight);
    this.addTraits(MaterialIds.platinum, MELEE_HARVEST, ModifierIds.lustrous);
    this.addTraits(MaterialIds.platinum, RANGED, TinkerModifiers.olympic);
    this.addDefaultTraits(MaterialIds.lead, ModifierIds.heavy);
    this.addTraits(MaterialIds.silver, MELEE_HARVEST, ModifierIds.smite);
    this.addTraits(MaterialIds.silver, RANGED, TinkerModifiers.holy);
    // tier 3 - mod compat
    this.addDefaultTraits(MaterialIds.steel, ModifierIds.ductile);
    this.addDefaultTraits(MaterialIds.bronze, TinkerModifiers.maintained);
    this.addDefaultTraits(MaterialIds.constantan, TinkerModifiers.temperate);
    this.addDefaultTraits(MaterialIds.invar, TinkerModifiers.invariant);
    this.addDefaultTraits(MaterialIds.necronium, TinkerModifiers.decay);
    this.addDefaultTraits(MaterialIds.electrum, TinkerModifiers.experienced);
    this.addDefaultTraits(MaterialIds.platedSlimewood, TinkerModifiers.overworked, TinkerModifiers.overslime);

    // slimeskull
    this.addTraits(MaterialIds.glass, SkullStats.ID, TinkerModifiers.selfDestructive.getId(), ModifierIds.creeperDisguise);
    this.addTraits(MaterialIds.enderPearl, SkullStats.ID, TinkerModifiers.enderdodging.getId(), ModifierIds.endermanDisguise);
    this.addTraits(MaterialIds.bone, SkullStats.ID, TinkerModifiers.strongBones.getId(), ModifierIds.skeletonDisguise);
    this.addTraits(MaterialIds.bloodbone, SkullStats.ID, TinkerModifiers.frosttouch.getId(), ModifierIds.strayDisguise);
    this.addTraits(MaterialIds.necroticBone, SkullStats.ID, TinkerModifiers.withered.getId(), ModifierIds.witherSkeletonDisguise);
    this.addTraits(MaterialIds.string, SkullStats.ID, TinkerModifiers.boonOfSssss.getId(), ModifierIds.spiderDisguise);
    this.addTraits(MaterialIds.darkthread, SkullStats.ID, TinkerModifiers.mithridatism.getId(), ModifierIds.caveSpiderDisguise);
    this.addTraits(MaterialIds.rottenFlesh, SkullStats.ID, TinkerModifiers.wildfire.getId(), ModifierIds.zombieDisguise);
    this.addTraits(MaterialIds.iron, SkullStats.ID, TinkerModifiers.plague.getId(), ModifierIds.huskDisguise);
    this.addTraits(MaterialIds.copper, SkullStats.ID, TinkerModifiers.breathtaking.getId(), ModifierIds.drownedDisguise);
    this.addTraits(MaterialIds.blazingBone, SkullStats.ID, TinkerModifiers.firebreath.getId(), ModifierIds.blazeDisguise);
    this.addTraits(MaterialIds.gold, SkullStats.ID, TinkerModifiers.chrysophilite.getId(), ModifierIds.piglinDisguise);
    this.addTraits(MaterialIds.roseGold, SkullStats.ID, TinkerModifiers.goldGuard.getId(), ModifierIds.piglinBruteDisguise);
    this.addTraits(MaterialIds.pigIron, SkullStats.ID, TinkerModifiers.revenge.getId(), ModifierIds.zombifiedPiglinDisguise);
    // plate
    this.noTraits(MaterialIds.obsidian);
    this.noTraits(MaterialIds.debris);
    this.noTraits(MaterialIds.netherite);
    // slimesuit
    this.noTraits(MaterialIds.earthslime);
    this.noTraits(MaterialIds.skyslime);
    this.noTraits(MaterialIds.blood);
    this.noTraits(MaterialIds.ichor);
    this.noTraits(MaterialIds.enderslime);
    this.noTraits(MaterialIds.clay);
    this.noTraits(MaterialIds.honey);
    this.noTraits(MaterialIds.phantom);
    // compat plate
    this.noTraits(MaterialIds.nickel);
    this.noTraits(MaterialIds.tin);
    this.noTraits(MaterialIds.zinc);
    this.noTraits(MaterialIds.brass);
    this.noTraits(MaterialIds.uranium);
  }
}
