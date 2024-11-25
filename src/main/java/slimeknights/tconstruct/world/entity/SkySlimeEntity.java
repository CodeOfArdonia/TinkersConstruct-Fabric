package slimeknights.tconstruct.world.entity;

import io.github.fabricators_of_create.porting_lib.entity.events.LivingEntityEvents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ModifierNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.TinkerTools;
import slimeknights.tconstruct.tools.data.ModifierIds;
import slimeknights.tconstruct.tools.item.ArmorSlotType;
import slimeknights.tconstruct.world.TinkerWorld;

public class SkySlimeEntity extends ArmoredSlimeEntity {

  private double bounceAmount = 0f;

  public SkySlimeEntity(EntityType<? extends SkySlimeEntity> type, Level worldIn) {
    super(type, worldIn);
  }

  @Override
  protected float getJumpPower() {
    return (float) Math.sqrt(this.getSize()) * this.getBlockJumpFactor() / 2;
  }

  @Override
  protected ParticleOptions getParticleType() {
    return TinkerWorld.skySlimeParticle.get();
  }

  @Override
  public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource source) {
    if (this.isSuppressingBounce()) {
      return super.causeFallDamage(distance, damageMultiplier * 0.2f, source);
    }
    LivingEntityEvents.Fall.FallEvent fallEvent = new LivingEntityEvents.Fall.FallEvent(this, source, distance, damageMultiplier);
//    float[] ret = LivingEntityEvents.FALL.invoker().onFall(fallEvent);
    fallEvent.sendEvent();
//    if (ret == null) {
//      return false;
//    }
    distance = fallEvent.getDistance();//ret[0];
    if (distance > 2) {
      // invert Y motion, boost X and Z slightly
      Vec3 motion = this.getDeltaMovement();
      this.setDeltaMovement(motion.x / 0.95f, motion.y * -0.9, motion.z / 0.95f);
      this.bounceAmount = this.getDeltaMovement().y;
      this.fallDistance = 0f;
      this.hasImpulse = true;
      this.setOnGround(false);
      this.playSound(Sounds.SLIMY_BOUNCE.getSound(), 1f, 1f);
    }
    return false;
  }

  @Override
  public void move(MoverType typeIn, Vec3 pos) {
    super.move(typeIn, pos);
    if (this.bounceAmount > 0) {
      Vec3 motion = this.getDeltaMovement();
      this.setDeltaMovement(motion.x, this.bounceAmount, motion.z);
      this.bounceAmount = 0;
    }
  }

  @Override
  protected void populateDefaultEquipmentSlots(RandomSource randomSource, DifficultyInstance difficulty) {
    // sky slime spawns with tinkers armor, high chance of travelers, low chance of plate
    // vanilla logic but simplified down to just helmets
    float multiplier = difficulty.getSpecialMultiplier();
    if (this.random.nextFloat() < 0.15F * multiplier) {
      // 2.5% chance of plate
      boolean isPlate = this.random.nextFloat() < (0.05f * multiplier);
      ItemStack helmet = new ItemStack((isPlate ? TinkerTools.plateArmor : TinkerTools.travelersGear).get(ArmorSlotType.HELMET));

      // for plate, just init stats
      ToolStack tool = ToolStack.from(helmet);
      tool.ensureSlotsBuilt();
      ModifierNBT modifiers = tool.getUpgrades();
      ModDataNBT persistentData = tool.getPersistentData();
      if (!isPlate) {
        // travelers dyes a random color
        persistentData.putInt(TinkerModifiers.dyed.getId(), this.random.nextInt(0xFFFFFF + 1));
        modifiers = modifiers.withModifier(TinkerModifiers.dyed.getId(), 1);
      }

      // TODO: make this less hardcoded?
      // add some random defense modifiers
      int max = tool.getFreeSlots(SlotType.DEFENSE);
      for (int i = 0; i < max; i++) {
        if (this.random.nextFloat() > 0.5f * multiplier) {
          break;
        }
        persistentData.addSlots(SlotType.DEFENSE, -1);
        modifiers = modifiers.withModifier(randomDefense(this.random.nextInt(6)), 1);
      }
      // chance of diamond or emerald
      if (tool.getFreeSlots(SlotType.UPGRADE) > 0 && this.random.nextFloat() < 0.5f * multiplier) {
        persistentData.addSlots(SlotType.UPGRADE, -1);
        modifiers = modifiers.withModifier(this.random.nextBoolean() ? ModifierIds.emerald : ModifierIds.diamond, 1);
      }

      tool.setUpgrades(modifiers);

      // finally, give the slime the helmet
      this.setItemSlot(EquipmentSlot.HEAD, helmet);
    }
  }

  private static ModifierId randomDefense(int index) {
    return switch (index) {
      case 1 -> TinkerModifiers.projectileProtection.getId();
      case 2 -> TinkerModifiers.fireProtection.getId();
      case 3 -> TinkerModifiers.magicProtection.getId();
      case 4 -> TinkerModifiers.blastProtection.getId();
      case 5 -> TinkerModifiers.golden.getId();
      default -> TinkerModifiers.meleeProtection.getId();
    };
  }

  @Override
  protected void populateDefaultEquipmentEnchantments(RandomSource randomSource, DifficultyInstance difficulty) {}
}
