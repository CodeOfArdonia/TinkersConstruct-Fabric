package slimeknights.tconstruct.tools.data.material;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.data.material.AbstractMaterialDataProvider;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

public class MaterialDataProvider extends AbstractMaterialDataProvider {

  public MaterialDataProvider(FabricDataOutput output) {
    super(output);
  }

  @Override
  public String getName() {
    return "Tinker's Construct Materials";
  }

  @Override
  protected void addMaterials() {
    // tier 1
    this.addMaterial(MaterialIds.wood, 1, ORDER_GENERAL, true);
    this.addMaterial(MaterialIds.rock, 1, ORDER_HARVEST, true);
    this.addMaterial(MaterialIds.flint, 1, ORDER_WEAPON, true);
    this.addMaterial(MaterialIds.copper, 1, ORDER_SPECIAL, true);
    this.addMaterial(MaterialIds.bone, 1, ORDER_SPECIAL, true);
    this.addMaterial(MaterialIds.bamboo, 1, ORDER_RANGED, true);
    // tier 1 - end
    this.addMaterial(MaterialIds.chorus, 1, ORDER_END, true);
    // tier 1 - binding
    this.addMaterial(MaterialIds.string, 1, ORDER_BINDING, true);
    this.addMaterial(MaterialIds.leather, 1, ORDER_BINDING, true);
    this.addMaterial(MaterialIds.vine, 1, ORDER_BINDING, true);

    // tier 2
    this.addMaterial(MaterialIds.iron, 2, ORDER_GENERAL, false);
    this.addMaterial(MaterialIds.searedStone, 2, ORDER_HARVEST, false);
    this.addMaterial(MaterialIds.bloodbone, 2, ORDER_WEAPON, true);
    this.addMaterial(MaterialIds.slimewood, 2, ORDER_SPECIAL, true);
    // tier 2 - nether
    this.addMaterial(MaterialIds.scorchedStone, 2, ORDER_NETHER, false);
    this.addMaterial(MaterialIds.necroticBone, 2, ORDER_NETHER, true);
    // tier 2 - end
    this.addMaterial(MaterialIds.whitestone, 2, ORDER_END, true);
    // tier 2 - binding
    this.addMaterial(MaterialIds.chain, 2, ORDER_BINDING, true);
    this.addMaterial(MaterialIds.skyslimeVine, 2, ORDER_BINDING, true);

    // tier 3
    this.addMaterial(MaterialIds.slimesteel, 3, ORDER_GENERAL, false);
    this.addMaterial(MaterialIds.amethystBronze, 3, ORDER_HARVEST, false);
    this.addMaterial(MaterialIds.nahuatl, 3, ORDER_WEAPON, false);
    this.addMaterial(MaterialIds.roseGold, 3, ORDER_SPECIAL, false);
    this.addMaterial(MaterialIds.pigIron, 3, ORDER_SPECIAL, false);
    // tier 3 (nether)
    this.addMaterial(MaterialIds.cobalt, 3, ORDER_NETHER, false);
    // tier 3 - binding
    this.addMaterial(MaterialIds.darkthread, 3, ORDER_BINDING, false);

    // tier 4
    this.addMaterial(MaterialIds.queensSlime, 4, ORDER_GENERAL, false);
    this.addMaterial(MaterialIds.hepatizon, 4, ORDER_HARVEST, false);
    this.addMaterial(MaterialIds.manyullyn, 4, ORDER_WEAPON, false);
    this.addMaterial(MaterialIds.blazingBone, 4, ORDER_SPECIAL, true);
    //addMetalMaterial(MaterialIds.soulsteel, 4, ORDER_SPECIAL, false, 0x6a5244);
    // tier 4 - binding
    this.addMaterial(MaterialIds.ancientHide, 4, ORDER_BINDING, false);

    // tier 5 binding, temporarily in book 4
    this.addMaterial(MaterialIds.enderslimeVine, 4, ORDER_BINDING, true);

    // tier 2 (end)
    //addMaterialNoFluid(MaterialIds.endstone, 2, ORDER_END, true, 0xe0d890);

    // tier 2 (mod integration)
    this.addCompatMetalMaterial(MaterialIds.osmium, 2, ORDER_COMPAT + ORDER_GENERAL);
    this.addCompatMetalMaterial(MaterialIds.tungsten, 2, ORDER_COMPAT + ORDER_HARVEST);
    this.addCompatMetalMaterial(MaterialIds.platinum, 2, ORDER_COMPAT + ORDER_HARVEST);
    this.addCompatMetalMaterial(MaterialIds.silver, 2, ORDER_COMPAT + ORDER_WEAPON);
    this.addCompatMetalMaterial(MaterialIds.lead, 2, ORDER_COMPAT + ORDER_WEAPON);
    this.addCompatMetalMaterial(MaterialIds.aluminum, 2, ORDER_COMPAT + ORDER_RANGED);
    // tier 3 (mod integration)
    this.addCompatMetalMaterial(MaterialIds.steel, 3, ORDER_COMPAT + ORDER_GENERAL);
    this.addCompatMetalMaterial(MaterialIds.bronze, 3, ORDER_COMPAT + ORDER_HARVEST);
    this.addCompatMetalMaterial(MaterialIds.constantan, 3, ORDER_COMPAT + ORDER_HARVEST);
    this.addCompatMetalMaterial(MaterialIds.invar, 3, ORDER_COMPAT + ORDER_WEAPON);
    this.addCompatMaterial(MaterialIds.necronium, 3, ORDER_COMPAT + ORDER_WEAPON, "uranium_ingots", true);
    this.addCompatMetalMaterial(MaterialIds.electrum, 3, ORDER_COMPAT + ORDER_SPECIAL);
    this.addCompatMetalMaterial(MaterialIds.platedSlimewood, 3, ORDER_COMPAT + ORDER_SPECIAL, "brass");

    // plate
    this.addMaterial(MaterialIds.obsidian, 3, ORDER_REPAIR, false);
    this.addMaterial(MaterialIds.debris, 3, ORDER_REPAIR, false);
    this.addMaterial(MaterialIds.netherite, 4, ORDER_REPAIR, false);
    this.addCompatMetalMaterial(MaterialIds.nickel, 2, ORDER_REPAIR);
    this.addCompatMetalMaterial(MaterialIds.tin, 2, ORDER_REPAIR);
    this.addCompatMetalMaterial(MaterialIds.zinc, 2, ORDER_REPAIR);
    this.addCompatMetalMaterial(MaterialIds.brass, 3, ORDER_REPAIR);
    this.addCompatMetalMaterial(MaterialIds.uranium, 2, ORDER_REPAIR);
    // slimeskull - put in the most appropriate tier
    this.addMaterial(MaterialIds.gold, 2, ORDER_REPAIR, false);
    this.addMaterial(MaterialIds.glass, 2, ORDER_REPAIR, false);
    this.addMaterial(MaterialIds.rottenFlesh, 1, ORDER_REPAIR, true);
    this.addMaterial(MaterialIds.enderPearl, 2, ORDER_REPAIR, false);
    // slimesuit - textures
    this.addMaterial(MaterialIds.earthslime, 1, ORDER_REPAIR, true);
    this.addMaterial(MaterialIds.skyslime, 1, ORDER_REPAIR, true);
    this.addMaterial(MaterialIds.blood, 2, ORDER_REPAIR, true);
    this.addMaterial(MaterialIds.ichor, 3, ORDER_REPAIR, true);
    this.addMaterial(MaterialIds.enderslime, 4, ORDER_REPAIR, true);
    this.addMaterial(MaterialIds.clay, 1, ORDER_REPAIR, true);
    this.addMaterial(MaterialIds.honey, 1, ORDER_REPAIR, true);
    //addMaterial(MaterialIds.venom,      3, ORDER_REPAIR, true);
    // slimesuit - repair
    this.addMaterial(MaterialIds.phantom, 1, ORDER_REPAIR, true);

    // legacy
    this.addRedirect(new MaterialId(TConstruct.MOD_ID, "stone"), this.redirect(MaterialIds.rock));
    this.addRedirect(new MaterialId(TConstruct.MOD_ID, "gunpowder"), this.redirect(MaterialIds.glass));
    this.addRedirect(new MaterialId(TConstruct.MOD_ID, "spider"), this.redirect(MaterialIds.string));
    this.addRedirect(new MaterialId(TConstruct.MOD_ID, "venom"), this.redirect(MaterialIds.darkthread));
    this.addRedirect(new MaterialId(TConstruct.MOD_ID, "rabbit"), this.redirect(MaterialIds.leather));
    this.addRedirect(new MaterialId(TConstruct.MOD_ID, "tinkers_bronze"), this.conditionalRedirect(MaterialIds.bronze, tagExistsCondition("bronze_ingots")), this.redirect(MaterialIds.copper));
  }
}
