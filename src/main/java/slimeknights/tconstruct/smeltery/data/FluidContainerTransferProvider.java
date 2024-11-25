package slimeknights.tconstruct.smeltery.data;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;
import slimeknights.mantle.fluid.transfer.AbstractFluidContainerTransferProvider;
import slimeknights.mantle.fluid.transfer.FillFluidContainerTransfer;
import slimeknights.mantle.fluid.transfer.FillFluidWithNBTTransfer;
import slimeknights.mantle.recipe.helper.ItemOutput;
import slimeknights.mantle.recipe.ingredient.FluidIngredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.fluids.item.EmptyPotionTransfer;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.shared.block.SlimeType;

import javax.annotation.Nullable;

public class FluidContainerTransferProvider extends AbstractFluidContainerTransferProvider {

  public FluidContainerTransferProvider(FabricDataOutput output) {
    super(output, TConstruct.MOD_ID);
  }

  @Override
  protected void addTransfers() {
    this.addFillEmpty("honey_bottle_", Items.HONEY_BOTTLE, Items.GLASS_BOTTLE, TinkerFluids.honey.get(), TinkerFluids.honey.getForgeTag(), FluidValues.BOTTLE);
    this.addFillEmpty("beetroot_soup_", Items.BEETROOT_SOUP, Items.BOWL, TinkerFluids.beetrootSoup.get(), TinkerFluids.beetrootSoup.getForgeTag(), FluidValues.BOWL);
    this.addFillEmpty("mushroom_stew_", Items.MUSHROOM_STEW, Items.BOWL, TinkerFluids.mushroomStew.get(), TinkerFluids.mushroomStew.getForgeTag(), FluidValues.BOWL);
    this.addFillEmpty("rabbit_stew_", Items.RABBIT_STEW, Items.BOWL, TinkerFluids.rabbitStew.get(), TinkerFluids.rabbitStew.getForgeTag(), FluidValues.BOWL);
    // potions
    this.addPotion("potion_", Items.POTION, Items.GLASS_BOTTLE, null);
    this.addPotion("potion_splash_", Items.SPLASH_POTION, TinkerFluids.splashBottle, TinkerTags.Items.SPLASH_BOTTLE);
    this.addPotion("potion_lingering_", Items.LINGERING_POTION, TinkerFluids.lingeringBottle, TinkerTags.Items.LINGERING_BOTTLE);
    // these bottles are fluid handlers, but glass bottles are not
    this.addBottleFill("venom_bottle_fill", TinkerFluids.venomBottle, TinkerFluids.venom.getLocalTag());
    this.addBottleFill("earth_slime_bottle_fill", TinkerFluids.slimeBottle.get(SlimeType.EARTH), TinkerFluids.earthSlime.getForgeTag());
    this.addBottleFill("sky_slime_bottle_fill", TinkerFluids.slimeBottle.get(SlimeType.SKY), TinkerFluids.skySlime.getLocalTag());
    this.addBottleFill("ender_slime_bottle_fill", TinkerFluids.slimeBottle.get(SlimeType.ENDER), TinkerFluids.enderSlime.getLocalTag());
    this.addBottleFill("blood_bottle_fill", TinkerFluids.slimeBottle.get(SlimeType.BLOOD), TinkerFluids.blood.getLocalTag());
    this.addBottleFill("magma_bottle_fill", TinkerFluids.magmaBottle, TinkerFluids.magma.getForgeTag());
  }

  /**
   * Adds generic fill and empty for a container
   */
  protected void addPotion(String prefix, ItemLike filled, ItemLike containerItem, @Nullable TagKey<Item> containerTag) {
    // water bottles are 1/3 of a bucket, to prevent water dupes we round up on fill and down on empty
    this.addTransfer(prefix + "empty", new EmptyPotionTransfer(Ingredient.of(filled), ItemOutput.fromItem(containerItem), new FluidStack(TinkerFluids.potion.get(), FluidValues.BOTTLE)));
    Ingredient container = containerTag == null ? Ingredient.of(containerItem) : Ingredient.of(containerTag);
    this.addTransfer(prefix + "fill", new FillFluidWithNBTTransfer(container, ItemOutput.fromItem(filled), FluidIngredient.of(TinkerTags.Fluids.POTION, FluidValues.BOTTLE)));
    this.addTransfer(prefix + "water", new FillFluidContainerTransfer(
      container,
      ItemOutput.fromStack(PotionUtils.setPotion(new ItemStack(filled), Potions.WATER)),
      FluidIngredient.of(FluidTags.WATER, FluidValues.BOTTLE * 2)));
  }

  /**
   * Adds a recipe for a bottle that fills with 250mb of fluid, emptying is assumed handled
   */
  protected void addBottleFill(String name, ItemLike output, TagKey<Fluid> tag) {
    this.addTransfer(name, new FillFluidContainerTransfer(Ingredient.of(Items.GLASS_BOTTLE), ItemOutput.fromItem(output), FluidIngredient.of(tag, FluidValues.BOTTLE)));
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Fluid Container Transfer";
  }
}
