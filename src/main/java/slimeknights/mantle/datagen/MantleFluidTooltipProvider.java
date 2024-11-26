package slimeknights.mantle.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.tooltip.AbstractFluidTooltipProvider;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;

/**
 * Mantle datagen for fluid tooltips. For mods, don't use this, use {@link AbstractFluidTooltipProvider}
 */
public class MantleFluidTooltipProvider extends AbstractFluidTooltipProvider {

  public MantleFluidTooltipProvider(FabricDataOutput generator) {
    super(generator, Mantle.modId);
  }

  @Override
  protected void addFluids() {
    this.add("buckets")
      .addUnit("kilobucket", FluidConstants.BUCKET * 1000)
      .addUnit("bucket", FluidConstants.BUCKET)
      .addUnit("droplet", 1);
    this.addRedirect(FluidTooltipHandler.DEFAULT_ID, this.id("buckets"));
  }

  @Override
  public String getName() {
    return "Mantle Fluid Tooltip Provider";
  }
}
