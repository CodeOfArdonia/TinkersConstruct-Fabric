package slimeknights.tconstruct.tools.data.material;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialRenderInfoProvider;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialSpriteProvider;
import slimeknights.tconstruct.library.tools.helper.ToolBuildHandler;

public class MaterialRenderInfoProvider extends AbstractMaterialRenderInfoProvider {

  public MaterialRenderInfoProvider(FabricDataOutput output, AbstractMaterialSpriteProvider spriteProvider) {
    super(output, spriteProvider);
  }

  @Override
  protected void addMaterialRenderInfo() {
    // tier 1
    this.buildRenderInfo(MaterialIds.flint).color(0x3D3C3C).fallbacks("crystal", "rock", "stick");
    this.buildRenderInfo(MaterialIds.basalt);
    this.buildRenderInfo(MaterialIds.bone).color(0xE8E5D2).fallbacks("bone", "rock");
    this.buildRenderInfo(MaterialIds.chorus);
    this.buildRenderInfo(MaterialIds.string).color(0xFFFFFF);
    this.buildRenderInfo(MaterialIds.leather).color(0xC65C35);
    this.buildRenderInfo(MaterialIds.vine).color(0x48B518).fallbacks("vine");
    // tier 1 - wood
    this.buildRenderInfo(MaterialIds.wood).color(0x876627).fallbacks("wood", "stick", "primitive");
    this.buildRenderInfo(MaterialIds.oak);
    this.buildRenderInfo(MaterialIds.spruce);
    this.buildRenderInfo(MaterialIds.birch);
    this.buildRenderInfo(MaterialIds.jungle);
    this.buildRenderInfo(MaterialIds.darkOak);
    this.buildRenderInfo(MaterialIds.acacia);
    this.buildRenderInfo(MaterialIds.crimson);
    this.buildRenderInfo(MaterialIds.warped);
    this.buildRenderInfo(MaterialIds.bamboo);
    // tier 1 - stone
    this.buildRenderInfo(MaterialIds.rock).materialTexture(MaterialIds.stone).color(0xB1AFAD).fallbacks("rock");
    this.buildRenderInfo(MaterialIds.stone).color(0xB1AFAD);
    this.buildRenderInfo(MaterialIds.andesite);
    this.buildRenderInfo(MaterialIds.diorite);
    this.buildRenderInfo(MaterialIds.granite);
    this.buildRenderInfo(MaterialIds.deepslate);
    this.buildRenderInfo(MaterialIds.blackstone);

    // tier 2
    this.buildRenderInfo(MaterialIds.iron).color(0xD8D8D8).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.oxidizedIron).color(0xE9C8B1).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.copper).color(0xE77C56).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.oxidizedCopper).color(0x4FAB90).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.searedStone).color(0x4F4A47).fallbacks("rock");
    this.buildRenderInfo(MaterialIds.scorchedStone).color(0x5B4C43).fallbacks("rock");
    this.buildRenderInfo(MaterialIds.bloodbone).color(0xE52323).fallbacks("bone", "rock");
    this.buildRenderInfo(MaterialIds.necroticBone).color(0x2A2A2A).fallbacks("bone", "rock");
    this.buildRenderInfo(MaterialIds.endstone);
    this.buildRenderInfo(MaterialIds.chain).color(0x3E4453).fallbacks("chain", "metal");
    this.buildRenderInfo(MaterialIds.skyslimeVine).color(0x00F4DA).fallbacks("vine");
    // slimewood
    this.buildRenderInfo(MaterialIds.slimewood).materialTexture(MaterialIds.greenheart).color(0x82c873).fallbacks("wood", "primitive");
    this.buildRenderInfo(MaterialIds.greenheart);
    this.buildRenderInfo(MaterialIds.skyroot);
    this.buildRenderInfo(MaterialIds.bloodshroom);

    // tier 3
    this.buildRenderInfo(MaterialIds.slimesteel).color(0x46ECE7).fallbacks("slime_metal", "metal");
    // default texture is tin even though silicon is the one we provide, as it makes the names cleaner
    this.buildRenderInfo(MaterialIds.amethystBronze).color(0xD9A2D0).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.nahuatl).color(0x3B2754).fallbacks("wood", "stick");
    this.buildRenderInfo(MaterialIds.pigIron).color(0xF0A8A4).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.roseGold).color(0xF7CDBB).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.cobalt).color(0x2376dd).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.darkthread);

    // tier 4
    this.buildRenderInfo(MaterialIds.queensSlime).color(0x809912).fallbacks("slime_metal", "metal").luminosity(9);
    this.buildRenderInfo(MaterialIds.hepatizon).color(0x60496b).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.manyullyn).color(0x9261cc).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.blazingBone).color(0xF2D500).fallbacks("bone", "rock").luminosity(15);
    this.buildRenderInfo(MaterialIds.ancientHide);
    this.buildRenderInfo(MaterialIds.enderslimeVine).color(0xa92dff).fallbacks("vine");

    // tier 2 compat
    this.buildRenderInfo(MaterialIds.osmium).color(0xC1E6F4).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.tungsten).color(0x6F6F62).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.platinum).color(0xA3E7FE).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.silver).color(0xDAF3ED).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.lead).color(0x696579).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.whitestone).color(0xE0E9EC).fallbacks("rock");
    this.buildRenderInfo(MaterialIds.aluminum);

    // tier 3 compat
    this.buildRenderInfo(MaterialIds.steel).color(0x959595).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.bronze).color(0xD49765).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.constantan).color(0xFF8B70).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.invar).color(0xCADBD0).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.necronium).color(0x9CBD89).fallbacks("bone", "metal");
    this.buildRenderInfo(MaterialIds.electrum).color(0xFFEA65).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.platedSlimewood).color(0xFFE170).fallbacks("slime_metal", "metal");

    // plate
    this.buildRenderInfo(MaterialIds.gold).color(0xFDF55F).fallbacks("metal");
    this.buildRenderInfo(MaterialIds.obsidian);
    this.buildRenderInfo(MaterialIds.debris);
    this.buildRenderInfo(MaterialIds.netherite).color(0x4C4143).fallbacks("metal");
    // compat plate
    this.buildRenderInfo(MaterialIds.nickel);
    this.buildRenderInfo(MaterialIds.tin);
    this.buildRenderInfo(MaterialIds.zinc);
    this.buildRenderInfo(MaterialIds.brass);
    this.buildRenderInfo(MaterialIds.uranium);
    // slimeskull
    this.buildRenderInfo(MaterialIds.glass);
    this.buildRenderInfo(MaterialIds.enderPearl);
    this.buildRenderInfo(MaterialIds.rottenFlesh);
    // slimesuit
    this.buildRenderInfo(MaterialIds.earthslime);
    this.buildRenderInfo(MaterialIds.skyslime);
    this.buildRenderInfo(MaterialIds.blood);
    this.buildRenderInfo(MaterialIds.ichor);
    this.buildRenderInfo(MaterialIds.enderslime);
    this.buildRenderInfo(MaterialIds.clay);
    this.buildRenderInfo(MaterialIds.honey);
    //buildRenderInfo(MaterialIds.venom);

    this.buildRenderInfo(MaterialIds.phantom);

    // UI internal
    this.buildRenderInfo(ToolBuildHandler.getRenderMaterial(0)).color(0xD8D8D8).texture(MaterialIds.iron).fallbacks("metal");
    this.buildRenderInfo(ToolBuildHandler.getRenderMaterial(1)).color(0x745f38).texture(MaterialIds.wood).fallbacks("wood", "stick");
    this.buildRenderInfo(ToolBuildHandler.getRenderMaterial(2)).color(0x2376dd).texture(MaterialIds.cobalt).fallbacks("metal");
    this.buildRenderInfo(ToolBuildHandler.getRenderMaterial(3)).color(0x9261cc).texture(MaterialIds.manyullyn).fallbacks("metal");
    this.buildRenderInfo(ToolBuildHandler.getRenderMaterial(4)).color(0xF98648).texture(MaterialIds.copper).fallbacks("metal");
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Material Render Info Provider";
  }
}
