package slimeknights.tconstruct.fluids.item;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.fluid.transfer.EmptyFluidWithNBTTransfer;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.fluid.transfer.IFluidContainerTransfer;

/**
 * Fluid transfer info that empties a fluid from an item, copying the fluid's NBT to the stack
 */
public class EmptyPotionTransfer extends EmptyFluidWithNBTTransfer implements IFluidContainerTransfer {

  public static final ResourceLocation ID = TConstruct.getResource("empty_potion");

  public EmptyPotionTransfer(Ingredient input, ItemOutput filled, FluidStack fluid) {
    super(input, filled, fluid);
  }

  @Override
  protected FluidStack getFluid(ItemStack stack) {
    if (PotionUtils.getPotion(stack) == Potions.WATER) {
      return new FluidStack(Fluids.WATER, this.fluid.getAmount());
    }
    return new FluidStack(this.fluid.getFluid(), this.fluid.getAmount(), stack.getTag());
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = super.serialize(context);
    json.addProperty("type", ID.toString());
    return json;
  }

  /**
   * Unique loader instance
   */
  public static final JsonDeserializer<EmptyPotionTransfer> DESERIALIZER = new Deserializer<>(EmptyPotionTransfer::new);
}
