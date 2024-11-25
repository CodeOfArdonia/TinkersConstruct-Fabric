package slimeknights.tconstruct.tools.data;

import io.github.fabricators_of_create.porting_lib.tags.Tags;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.resource.conditions.v1.DefaultResourceConditions;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import slimeknights.mantle.recipe.data.FluidNameIngredient;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.library.data.tinkering.AbstractSpillingFluidProvider;
import slimeknights.tconstruct.library.json.predicate.entity.LivingEntityPredicate;
import slimeknights.tconstruct.library.json.predicate.entity.MobTypePredicate;
import slimeknights.tconstruct.library.modifiers.spilling.effects.AddBreathSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.AddInsomniaSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.CureEffectsSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.DamageSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.DamageSpillingEffect.DamageType;
import slimeknights.tconstruct.library.modifiers.spilling.effects.EffectSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.ExtinguishSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.PotionFluidEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.RemoveEffectSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.RestoreHungerSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.SetFireSpillingEffect;
import slimeknights.tconstruct.library.modifiers.spilling.effects.TeleportSpillingEffect;
import slimeknights.tconstruct.library.recipe.FluidValues;
import slimeknights.tconstruct.library.recipe.TagPredicate;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.modifiers.traits.skull.StrongBonesModifier;

import java.util.function.Function;

@SuppressWarnings("removal")
public class SpillingFluidProvider extends AbstractSpillingFluidProvider {

  public SpillingFluidProvider(FabricDataOutput output) {
    super(output, TConstruct.MOD_ID);
  }

  @Override
  protected void addFluids() {
    // vanilla
    this.addFluid(Fluids.WATER, FluidConstants.BUCKET / 20)
      .addEffect(LivingEntityPredicate.WATER_SENSITIVE, new DamageSpillingEffect(DamageType.PIERCING, 2f))
      .addEffect(ExtinguishSpillingEffect.INSTANCE);
    this.addFluid(Fluids.LAVA, FluidConstants.BUCKET / 20)
      .addEffect(LivingEntityPredicate.FIRE_IMMUNE.inverted(), new DamageSpillingEffect(DamageType.FIRE, 2f))
      .addEffect(new SetFireSpillingEffect(10));
    this.addFluid(Tags.Fluids.MILK, FluidConstants.BUCKET / 10)
      .addEffect(new CureEffectsSpillingEffect(new ItemStack(Items.MILK_BUCKET)))
      .addEffect(StrongBonesModifier.SPILLING_EFFECT);

    // blaze - more damage, less fire
    this.burningFluid("blazing_blood", TinkerFluids.blazingBlood.getLocalTag(), FluidConstants.BUCKET / 20, 3f, 5);

    // slime
    int slimeballPiece = FluidValues.SLIMEBALL / 5;
    // earth - lucky
    this.addFluid(TinkerFluids.earthSlime.getForgeTag(), slimeballPiece)
      .addEffect(new EffectSpillingEffect(MobEffects.LUCK, 15, 1))
      .addEffect(new EffectSpillingEffect(MobEffects.MOVEMENT_SLOWDOWN, 15, 1));
    // sky - jump boost
    this.addFluid(TinkerFluids.skySlime.getLocalTag(), slimeballPiece)
      .addEffect(new EffectSpillingEffect(MobEffects.JUMP, 20, 1))
      .addEffect(new EffectSpillingEffect(MobEffects.MOVEMENT_SLOWDOWN, 15, 1));
    // ender - levitation
    this.addFluid(TinkerFluids.enderSlime.getLocalTag(), slimeballPiece)
      .addEffect(new EffectSpillingEffect(MobEffects.LEVITATION, 5, 1))
      .addEffect(new EffectSpillingEffect(MobEffects.MOVEMENT_SLOWDOWN, 15, 1));
    // slimelike
    // blood - food
    this.addFluid(TinkerFluids.blood.getLocalTag(), slimeballPiece)
      .addEffect(new RestoreHungerSpillingEffect(1, 0.2f))
      .addEffect(new EffectSpillingEffect(MobEffects.DIG_SLOWDOWN, 10, 1));
    // venom - poison & strength
    this.addFluid(TinkerFluids.venom.getLocalTag(), slimeballPiece)
      .addEffect(new EffectSpillingEffect(MobEffects.POISON, 5, 1))
      .addEffect(new EffectSpillingEffect(MobEffects.DAMAGE_BOOST, 10, 1));
    // magma - fire resistance
    this.addFluid(TinkerFluids.magma.getForgeTag(), slimeballPiece)
      .addEffect(new EffectSpillingEffect(MobEffects.FIRE_RESISTANCE, 25, 1));
    // soul - slowness and blindness
    this.addFluid(TinkerFluids.liquidSoul.getLocalTag(), FluidConstants.BUCKET / 20)
      .addEffect(new EffectSpillingEffect(MobEffects.MOVEMENT_SLOWDOWN, 25, 2))
      .addEffect(new EffectSpillingEffect(MobEffects.BLINDNESS, 5, 1));
    // ender - teleporting
    this.addFluid(TinkerFluids.moltenEnder.getForgeTag(), FluidConstants.BUCKET / 20)
      .addEffect(new DamageSpillingEffect(DamageType.MAGIC, 1f))
      .addEffect(TeleportSpillingEffect.INSTANCE);

    // foods
    this.addFluid(TinkerFluids.honey.getForgeTag(), slimeballPiece)
      .addEffect(new RestoreHungerSpillingEffect(1, 0.02f))
      .addEffect(new RemoveEffectSpillingEffect(MobEffects.POISON));
    this.addFluid(TinkerFluids.beetrootSoup.getForgeTag(), slimeballPiece)
      .addEffect(new RestoreHungerSpillingEffect(1, 0.15f));
    this.addFluid(TinkerFluids.mushroomStew.getForgeTag(), slimeballPiece)
      .addEffect(new RestoreHungerSpillingEffect(1, 0.15f));
    this.addFluid(TinkerFluids.rabbitStew.getForgeTag(), slimeballPiece)
      .addEffect(new RestoreHungerSpillingEffect(2, 0.10f));
    // pig iron fills you up food, but still hurts
    this.addFluid(TinkerFluids.moltenPigIron.getLocalTag(), FluidValues.NUGGET)
      .addEffect(new RestoreHungerSpillingEffect(2, 0.3f))
      .addEffect(new SetFireSpillingEffect(2));

    // metals, lose reference to mistborn (though a true fan would probably get angry at how much I stray from the source)
    this.metalborn(TinkerFluids.moltenIron.getForgeTag(), 2f).addEffect(new EffectSpillingEffect(TinkerModifiers.magneticEffect.get(), 4, 2));
    this.metalborn(TinkerFluids.moltenSteel.getForgeTag(), 2f).addEffect(new EffectSpillingEffect(TinkerModifiers.repulsiveEffect.get(), 4, 2));
    this.metalborn(TinkerFluids.moltenCopper.getForgeTag(), 1.5f).addEffect(new AddBreathSpillingEffect(80));
    this.metalborn(TinkerFluids.moltenBronze.getForgeTag(), 2f).addEffect(new AddInsomniaSpillingEffect(-2000));
    this.metalborn(TinkerFluids.moltenAmethystBronze.getLocalTag(), 1.5f).addEffect(new AddInsomniaSpillingEffect(2000));
    this.metalborn(TinkerFluids.moltenZinc.getForgeTag(), 1.5f).addEffect(new EffectSpillingEffect(MobEffects.MOVEMENT_SPEED, 10, 1));
    this.metalborn(TinkerFluids.moltenBrass.getForgeTag(), 2f).addEffect(new EffectSpillingEffect(MobEffects.FIRE_RESISTANCE, 8, 1));
    this.metalborn(TinkerFluids.moltenTin.getForgeTag(), 1.5f).addEffect(new EffectSpillingEffect(MobEffects.NIGHT_VISION, 8, 1));
    this.metalborn(TinkerFluids.moltenPewter.getForgeTag(), 2f).addEffect(new EffectSpillingEffect(MobEffects.DAMAGE_BOOST, 7, 1));
    this.addFluid(TinkerFluids.moltenGold.getForgeTag(), FluidValues.NUGGET)
      .addEffect(new MobTypePredicate(MobType.UNDEAD), new DamageSpillingEffect(DamageType.MAGIC, 2f))
      .addEffect(new EffectSpillingEffect(MobEffects.REGENERATION, 6, 1));
    this.addFluid(TinkerFluids.moltenElectrum.getForgeTag(), FluidValues.NUGGET)
      .addEffect(new MobTypePredicate(MobType.UNDEAD), new DamageSpillingEffect(DamageType.MAGIC, 2f))
      .addEffect(new EffectSpillingEffect(MobEffects.DIG_SPEED, 8, 1));
    this.addFluid(TinkerFluids.moltenRoseGold.getForgeTag(), FluidValues.NUGGET)
      .addEffect(new MobTypePredicate(MobType.UNDEAD), new DamageSpillingEffect(DamageType.MAGIC, 2f))
      .addEffect(new EffectSpillingEffect(MobEffects.HEALTH_BOOST, 15, 1));
    this.metalborn(TinkerFluids.moltenAluminum.getForgeTag(), 1f).addEffect(new CureEffectsSpillingEffect(new ItemStack(Items.MILK_BUCKET)));
    this.addFluid(TinkerFluids.moltenSilver.getForgeTag(), FluidValues.NUGGET)
      .addEffect(new MobTypePredicate(MobType.UNDEAD), new DamageSpillingEffect(DamageType.MAGIC, 2f))
      .addEffect(new RemoveEffectSpillingEffect(MobEffects.WITHER));

    this.metalborn(TinkerFluids.moltenLead.getForgeTag(), 1.5f).addEffect(new EffectSpillingEffect(MobEffects.MOVEMENT_SLOWDOWN, 6, 1));
    this.metalborn(TinkerFluids.moltenNickel.getForgeTag(), 1.5f).addEffect(new EffectSpillingEffect(MobEffects.WEAKNESS, 7, 1));
    this.metalborn(TinkerFluids.moltenInvar.getForgeTag(), 2f).addEffect(new EffectSpillingEffect(MobEffects.HUNGER, 10, 1));
    this.metalborn(TinkerFluids.moltenConstantan.getForgeTag(), 2f).addEffect(new EffectSpillingEffect(MobEffects.HUNGER, 10, 1));
    this.burningFluid(TinkerFluids.moltenUranium.getForgeTag(), 1.5f, 3).addEffect(new EffectSpillingEffect(MobEffects.POISON, 10, 1));

    this.metalborn(TinkerFluids.moltenCobalt.getForgeTag(), 1f)
      .addEffect(new EffectSpillingEffect(MobEffects.DIG_SPEED, 7, 1))
      .addEffect(new EffectSpillingEffect(MobEffects.MOVEMENT_SPEED, 7, 1));
    this.metalborn(TinkerFluids.moltenManyullyn.getForgeTag(), 3f).addEffect(new EffectSpillingEffect(MobEffects.DAMAGE_RESISTANCE, 15, 1));
    this.metalborn(TinkerFluids.moltenHepatizon.getForgeTag(), 2.5f).addEffect(new EffectSpillingEffect(MobEffects.DAMAGE_RESISTANCE, 10, 1));
    this.burningFluid(TinkerFluids.moltenNetherite.getForgeTag(), 3.5f, 4).addEffect(new EffectSpillingEffect(MobEffects.BLINDNESS, 15, 1));

    this.metalborn(TinkerFluids.moltenSlimesteel.getLocalTag(), 1f).addEffect(new EffectSpillingEffect(MobEffects.SLOW_FALLING, 5, 1));
    this.metalborn(TinkerFluids.moltenQueensSlime.getLocalTag(), 1f).addEffect(new EffectSpillingEffect(MobEffects.LEVITATION, 5, 1));

    // multi-recipes
    this.burningFluid("glass", TinkerTags.Fluids.GLASS_SPILLING, FluidConstants.BUCKET / 10, 1f, 3);
    this.burningFluid("clay", TinkerTags.Fluids.CLAY_SPILLING, FluidValues.BRICK / 5, 1.5f, 3);
    this.burningFluid("metal_cheap", TinkerTags.Fluids.CHEAP_METAL_SPILLING, FluidValues.NUGGET, 1.5f, 7);
    this.burningFluid("metal_average", TinkerTags.Fluids.AVERAGE_METAL_SPILLING, FluidValues.NUGGET, 2f, 7);
    this.burningFluid("metal_expensive", TinkerTags.Fluids.EXPENSIVE_METAL_SPILLING, FluidValues.NUGGET, 3f, 7);

    // potion fluid compat
    // standard potion is 250 mb, but we want a smaller number. divide into 5 pieces at 25% a piece (so healing is 1 health), means you gain 25% per potion
    long bottleSip = FluidValues.BOTTLE / 5;
    this.addFluid("potion_fluid", TinkerTags.Fluids.POTION, bottleSip).addEffect(new PotionFluidEffect(0.25f, TagPredicate.ANY));

    // create has three types of bottles stored on their fluid, react to it to boost
    Function<String, TagPredicate> createBottle = value -> {
      CompoundTag compound = new CompoundTag();
      compound.putString("Bottle", value);
      return new TagPredicate(compound);
    };
    String create = "create";
    this.addFluid("potion_create", FluidNameIngredient.of(new ResourceLocation(create, "potion"), bottleSip))
      .condition(DefaultResourceConditions.allModsLoaded(create))
      .addEffect(new PotionFluidEffect(0.25f, createBottle.apply("REGULAR")))
      .addEffect(new PotionFluidEffect(0.5f, createBottle.apply("SPLASH")))
      .addEffect(new PotionFluidEffect(0.75f, createBottle.apply("LINGERING")));

  }

  /**
   * Builder for an effect based metal
   */
  private Builder metalborn(TagKey<Fluid> tag, float damage) {
    return this.burningFluid(tag.location().getPath(), tag, FluidValues.NUGGET, damage, 0);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Spilling Fluid Provider";
  }
}
