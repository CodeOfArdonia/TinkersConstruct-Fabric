package slimeknights.tconstruct.common.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import slimeknights.mantle.datagen.MantleTags;
import slimeknights.mantle.registration.object.FluidObject;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.fluids.TinkerFluids;

import java.util.concurrent.CompletableFuture;

@SuppressWarnings("unchecked")
public class FluidTagProvider extends FabricTagProvider.FluidTagProvider {

  public FluidTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
    super(output, registriesFuture);
  }

  @Override
  public void addTags(HolderLookup.Provider provider) {
    // first, register common tags
    // slime
    this.tagLocal(TinkerFluids.blood);
    this.tagAll(TinkerFluids.earthSlime);
    this.tagLocal(TinkerFluids.skySlime);
    this.tagLocal(TinkerFluids.enderSlime);
    this.tagAll(TinkerFluids.magma);
    this.tagLocal(TinkerFluids.venom);
    // basic molten
    this.tagLocal(TinkerFluids.searedStone);
    this.tagLocal(TinkerFluids.scorchedStone);
    this.tagLocal(TinkerFluids.moltenClay);
    this.tagLocal(TinkerFluids.moltenGlass);
    this.tagLocal(TinkerFluids.liquidSoul);
    this.tagLocal(TinkerFluids.moltenPorcelain);
    // fancy molten
    this.tagLocal(TinkerFluids.moltenObsidian);
    this.tagLocal(TinkerFluids.moltenEmerald);
    this.tagLocal(TinkerFluids.moltenQuartz);
    this.tagLocal(TinkerFluids.moltenDiamond);
    this.tagLocal(TinkerFluids.moltenAmethyst);
    this.tagAll(TinkerFluids.moltenEnder);
    this.tagLocal(TinkerFluids.blazingBlood);
    // ores
    this.tagAll(TinkerFluids.moltenIron);
    this.tagAll(TinkerFluids.moltenGold);
    this.tagAll(TinkerFluids.moltenCopper);
    this.tagAll(TinkerFluids.moltenCobalt);
    this.tagLocal(TinkerFluids.moltenDebris);
    // alloys
    this.tagLocal(TinkerFluids.moltenSlimesteel);
    this.tagAll(TinkerFluids.moltenAmethystBronze);
    this.tagAll(TinkerFluids.moltenRoseGold);
    this.tagLocal(TinkerFluids.moltenPigIron);
    // nether alloys
    this.tagAll(TinkerFluids.moltenManyullyn);
    this.tagAll(TinkerFluids.moltenHepatizon);
    this.tagLocal(TinkerFluids.moltenQueensSlime);
    this.tagLocal(TinkerFluids.moltenSoulsteel);
    this.tagAll(TinkerFluids.moltenNetherite);
    // end alloys
    this.tagLocal(TinkerFluids.moltenKnightslime);
    // compat ores
    this.tagAll(TinkerFluids.moltenTin);
    this.tagAll(TinkerFluids.moltenAluminum);
    this.tagAll(TinkerFluids.moltenLead);
    this.tagAll(TinkerFluids.moltenSilver);
    this.tagAll(TinkerFluids.moltenNickel);
    this.tagAll(TinkerFluids.moltenZinc);
    this.tagAll(TinkerFluids.moltenPlatinum);
    this.tagAll(TinkerFluids.moltenTungsten);
    this.tagAll(TinkerFluids.moltenOsmium);
    this.tagAll(TinkerFluids.moltenUranium);
    // compat alloys
    this.tagAll(TinkerFluids.moltenBronze);
    this.tagAll(TinkerFluids.moltenBrass);
    this.tagAll(TinkerFluids.moltenElectrum);
    this.tagAll(TinkerFluids.moltenInvar);
    this.tagAll(TinkerFluids.moltenConstantan);
    this.tagAll(TinkerFluids.moltenPewter);
    this.tagAll(TinkerFluids.moltenSteel);
    // thermal compat alloys
    this.tagAll(TinkerFluids.moltenEnderium);
    this.tagAll(TinkerFluids.moltenLumium);
    this.tagAll(TinkerFluids.moltenSignalum);
    // mekanism compat alloys
    this.tagAll(TinkerFluids.moltenRefinedGlowstone);
    this.tagAll(TinkerFluids.moltenRefinedObsidian);
    // unplacable fluids
    this.tagAll(TinkerFluids.honey);
    this.tagAll(TinkerFluids.beetrootSoup);
    this.tagAll(TinkerFluids.mushroomStew);
    this.tagAll(TinkerFluids.rabbitStew);

    /* Normal tags */
    this.tag(TinkerTags.Fluids.SLIME)
      .addTag(TinkerFluids.earthSlime.getForgeTag())
      .addTag(TinkerFluids.skySlime.getLocalTag())
      .addTag(TinkerFluids.enderSlime.getLocalTag());

    this.getOrCreateTagBuilder(TinkerTags.Fluids.POTION).add(TinkerFluids.potion.get());

    // tooltips //
    this.tag(TinkerTags.Fluids.GLASS_TOOLTIPS).addTag(TinkerFluids.moltenGlass.getLocalTag()).addTag(TinkerFluids.liquidSoul.getLocalTag()).addTag(TinkerFluids.moltenObsidian.getLocalTag());
    this.tag(TinkerTags.Fluids.SLIME_TOOLTIPS).addTag(TinkerFluids.magma.getForgeTag()).addTag(TinkerFluids.blood.getLocalTag()).addTag(TinkerFluids.moltenEnder.getForgeTag()).addTag(TinkerTags.Fluids.SLIME);
    this.tag(TinkerTags.Fluids.CLAY_TOOLTIPS).addTag(TinkerFluids.moltenClay.getLocalTag()).addTag(TinkerFluids.moltenPorcelain.getLocalTag()).addTag(TinkerFluids.searedStone.getLocalTag()).addTag(TinkerFluids.scorchedStone.getLocalTag());
    this.tag(TinkerTags.Fluids.METAL_TOOLTIPS).addTag(
      // vanilla ores
      TinkerFluids.moltenIron.getForgeTag()).addTag(TinkerFluids.moltenGold.getForgeTag()).addTag(TinkerFluids.moltenCopper.getForgeTag()).addTag(TinkerFluids.moltenCobalt.getForgeTag()).addTag(TinkerFluids.moltenDebris.getLocalTag()).addTag(
      // base alloys
      TinkerFluids.moltenSlimesteel.getLocalTag()).addTag(TinkerFluids.moltenAmethystBronze.getLocalTag()).addTag(TinkerFluids.moltenRoseGold.getForgeTag()).addTag(TinkerFluids.moltenPigIron.getLocalTag()).addTag(
      TinkerFluids.moltenManyullyn.getForgeTag()).addTag(TinkerFluids.moltenHepatizon.getForgeTag()).addTag(TinkerFluids.moltenQueensSlime.getLocalTag()).addTag(TinkerFluids.moltenNetherite.getForgeTag()).addTag(
      TinkerFluids.moltenSoulsteel.getLocalTag()).addTag(TinkerFluids.moltenKnightslime.getLocalTag()).addTag(
      // compat ores
      TinkerFluids.moltenTin.getForgeTag()).addTag(TinkerFluids.moltenAluminum.getForgeTag()).addTag(TinkerFluids.moltenLead.getForgeTag()).addTag(TinkerFluids.moltenSilver.getForgeTag()).addTag(
      TinkerFluids.moltenNickel.getForgeTag()).addTag(TinkerFluids.moltenZinc.getForgeTag()).addTag(TinkerFluids.moltenPlatinum.getForgeTag()).addTag(
      TinkerFluids.moltenTungsten.getForgeTag()).addTag(TinkerFluids.moltenOsmium.getForgeTag()).addTag(TinkerFluids.moltenUranium.getForgeTag()).addTag(
      // compat alloys
      TinkerFluids.moltenBronze.getForgeTag()).addTag(TinkerFluids.moltenBrass.getForgeTag()).addTag(TinkerFluids.moltenElectrum.getForgeTag()).addTag(
      TinkerFluids.moltenInvar.getForgeTag()).addTag(TinkerFluids.moltenConstantan.getForgeTag()).addTag(TinkerFluids.moltenPewter.getForgeTag()).addTag(TinkerFluids.moltenSteel.getForgeTag()).addTag(
      // thermal alloys
      TinkerFluids.moltenEnderium.getForgeTag()).addTag(TinkerFluids.moltenLumium.getForgeTag()).addTag(TinkerFluids.moltenSignalum.getForgeTag()).addTag(
      // mekanism alloys
      TinkerFluids.moltenRefinedGlowstone.getForgeTag()).addTag(TinkerFluids.moltenRefinedObsidian.getForgeTag());

    this.tag(TinkerTags.Fluids.LARGE_GEM_TOOLTIPS).addTags(TinkerFluids.moltenEmerald.getLocalTag(), TinkerFluids.moltenDiamond.getLocalTag());
    this.tag(TinkerTags.Fluids.SMALL_GEM_TOOLTIPS).addTags(TinkerFluids.moltenQuartz.getLocalTag(), TinkerFluids.moltenAmethyst.getLocalTag());
    this.tag(TinkerTags.Fluids.SOUP_TOOLTIPS).addTags(TinkerFluids.beetrootSoup.getLocalTag(), TinkerFluids.mushroomStew.getLocalTag(), TinkerFluids.rabbitStew.getLocalTag());
    this.getOrCreateTagBuilder(TinkerTags.Fluids.WATER_TOOLTIPS).forceAddTag(MantleTags.Fluids.WATER);

    // spilling tags - reduces the number of recipes generated //
    this.tag(TinkerTags.Fluids.CLAY_SPILLING)
      .addTag(TinkerFluids.moltenClay.getLocalTag())
      .addTag(TinkerFluids.moltenPorcelain.getLocalTag())
      .addTag(TinkerFluids.searedStone.getLocalTag())
      .addTag(TinkerFluids.scorchedStone.getLocalTag());
    this.tag(TinkerTags.Fluids.GLASS_SPILLING)
      .addTag(TinkerFluids.moltenGlass.getLocalTag())
      .addTag(TinkerFluids.moltenObsidian.getLocalTag());
    this.tag(TinkerTags.Fluids.CHEAP_METAL_SPILLING)
      .addTag(TinkerFluids.moltenPlatinum.getForgeTag())
      .addTag(TinkerFluids.moltenTungsten.getForgeTag())
      .addTag(TinkerFluids.moltenOsmium.getForgeTag())
      .addTag(TinkerFluids.moltenAmethyst.getLocalTag());
    this.tag(TinkerTags.Fluids.AVERAGE_METAL_SPILLING)
      .addTag(TinkerFluids.moltenQuartz.getLocalTag())
      .addTag(TinkerFluids.moltenEmerald.getLocalTag())
      .addTag(TinkerFluids.moltenRefinedGlowstone.getForgeTag());
    this.tag(TinkerTags.Fluids.EXPENSIVE_METAL_SPILLING)
      .addTag(TinkerFluids.moltenDiamond.getLocalTag())
      .addTag(TinkerFluids.moltenDebris.getLocalTag())
      .addTag(TinkerFluids.moltenEnderium.getForgeTag())
      .addTag(TinkerFluids.moltenLumium.getForgeTag())
      .addTag(TinkerFluids.moltenSignalum.getForgeTag())
      .addTag(TinkerFluids.moltenRefinedObsidian.getForgeTag());
  }

  @Override
  public String getName() {
    return "Tinkers Construct Fluid TinkerTags";
  }

  /**
   * Tags this fluid using local tags
   */
  private void tagLocal(FluidObject<?> fluid) {
    this.getOrCreateTagBuilder(fluid.getLocalTag()).add(fluid.getStill(), fluid.getFlowing());
  }

  /**
   * Tags this fluid with local and forge tags
   */
  private void tagAll(FluidObject<?> fluid) {
    this.tagLocal(fluid);
    this.tag(fluid.getForgeTag()).addTag(fluid.getLocalTag());
  }
}
