package slimeknights.tconstruct.fluids;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import slimeknights.mantle.fluid.tooltip.AbstractFluidTooltipProvider;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.menu.AlloyerContainerMenu;
import slimeknights.tconstruct.smeltery.menu.MelterContainerMenu;

import static slimeknights.tconstruct.common.TinkerTags.Fluids.CLAY_TOOLTIPS;
import static slimeknights.tconstruct.common.TinkerTags.Fluids.GLASS_TOOLTIPS;
import static slimeknights.tconstruct.common.TinkerTags.Fluids.LARGE_GEM_TOOLTIPS;
import static slimeknights.tconstruct.common.TinkerTags.Fluids.METAL_TOOLTIPS;
import static slimeknights.tconstruct.common.TinkerTags.Fluids.SLIME_TOOLTIPS;
import static slimeknights.tconstruct.common.TinkerTags.Fluids.SMALL_GEM_TOOLTIPS;
import static slimeknights.tconstruct.common.TinkerTags.Fluids.SOUP_TOOLTIPS;
import static slimeknights.tconstruct.common.TinkerTags.Fluids.WATER_TOOLTIPS;

@SuppressWarnings("removal")
public class FluidTooltipProvider extends AbstractFluidTooltipProvider {

  public FluidTooltipProvider(FabricDataOutput output) {
    super(output, TConstruct.MOD_ID);
  }

  @Override
  protected void addFluids() {
    // screen capacities
    this.add("ingots").addUnit("ingot", FluidValues.INGOT);
    this.addRedirect(AlloyerContainerMenu.TOOLTIP_FORMAT, this.id("ingots"));
    this.addRedirect(MelterContainerMenu.TOOLTIP_FORMAT, this.id("ingots"));
    this.addRedirect(TinkerSmeltery.smeltery.getId(), this.id("ingots"));
    this.addRedirect(TinkerSmeltery.foundry.getId(), this.id("ingots"));

    // standard fluids
    this.add("metals", METAL_TOOLTIPS)
      .addUnit("block", FluidValues.METAL_BLOCK)
      .addUnit("ingot", FluidValues.INGOT)
      .addUnit("nugget", FluidValues.NUGGET);
    this.add("large_gems", LARGE_GEM_TOOLTIPS)
      .addUnit("block", FluidValues.LARGE_GEM_BLOCK)
      .addUnit("gem", FluidValues.GEM)
      .addUnit("shard", FluidValues.GEM_SHARD);
    this.add("small_gems", SMALL_GEM_TOOLTIPS)
      .addUnit("block", FluidValues.SMALL_GEM_BLOCK)
      .addUnit("gem", FluidValues.GEM)
      .addUnit("shard", FluidValues.GEM_SHARD);

    this.add("clay", CLAY_TOOLTIPS)
      .addUnit("block", FluidValues.BRICK_BLOCK)
      .addUnit("brick", FluidValues.BRICK);
    this.add("slime", SLIME_TOOLTIPS)
      .addUnit("block", FluidValues.SLIME_BLOCK)
      .addUnit("slimeball", FluidValues.SLIMEBALL);
    this.add("glass", GLASS_TOOLTIPS)
      .addUnit("block", FluidValues.GLASS_BLOCK)
      .addUnit("pane", FluidValues.GLASS_PANE);

    this.add("water", WATER_TOOLTIPS)
      .addUnit("kilobucket", "mantle", FluidConstants.BUCKET * 1000)
      .addUnit("bucket", "mantle", FluidConstants.BUCKET)
      .addUnit("bottle", FluidValues.BOTTLE);
    this.add("venom", TinkerFluids.venom.getLocalTag())
      .addUnit("kilobucket", "mantle", FluidConstants.BUCKET * 1000)
      .addUnit("bucket", "mantle", FluidConstants.BUCKET)
      .addUnit("bottle", FluidValues.BOTTLE);
    this.add("honey", TinkerFluids.honey.getForgeTag())
      .addUnit("block", FluidValues.BOTTLE * 4)
      .addUnit("bottle", FluidValues.BOTTLE);
    this.add("soup", SOUP_TOOLTIPS)
      .addUnit("bowl", FluidValues.BOWL);

    this.add("potion", TinkerTags.Fluids.POTION)
      .addUnit("bottle", FluidValues.BOTTLE);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Fluid Tooltip Provider";
  }
}
