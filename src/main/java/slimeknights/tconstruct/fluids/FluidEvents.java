package slimeknights.tconstruct.fluids;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.world.item.Items;
import slimeknights.tconstruct.fluids.util.ConstantFluidContainerWrapper;

/**
 * Event subscriber for modifier events
 * Note the way the subscribers are set up, technically works on anything that has the tic_modifiers tag
 */
@SuppressWarnings("unused")
public class FluidEvents {
  public static void init() {
    onFurnaceFuel();
    FluidStorage.ITEM.registerForItems((itemStack, context) -> new ConstantFluidContainerWrapper(new FluidStack(TinkerFluids.powderedSnow.get(), FluidConstants.BUCKET), Items.BUCKET.getDefaultInstance(), context), Items.POWDER_SNOW_BUCKET);
  }

  public static void onFurnaceFuel() {
//    if (event.getItemStack().getItem() == TinkerFluids.blazingBlood.asItem()) {
//      // 150% efficiency compared to lava bucket, compare to casting blaze rods, which cast into 120%
//      event.setBurnTime(30000);
//    }
    FuelRegistry.INSTANCE.add(TinkerFluids.blazingBlood.asItem(), 30000);
  }
}
