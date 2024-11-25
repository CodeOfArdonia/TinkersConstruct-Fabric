package slimeknights.tconstruct.tools.modifiers.ability.armor;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.library.modifiers.impl.TankModifier;
import slimeknights.tconstruct.library.modifiers.spilling.SpillingFluid;
import slimeknights.tconstruct.library.modifiers.spilling.SpillingFluidManager;
import slimeknights.tconstruct.library.tools.context.EquipmentContext;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.particle.FluidParticleData;

import javax.annotation.Nullable;

/**
 * Modifier to handle spilling recipes onto self when attacked
 */
@SuppressWarnings("removal")
public class WettingModifier extends TankModifier {

  public WettingModifier() {
    super(FluidConstants.BUCKET);
  }

  /**
   * Spawns particles at the given entity
   */
  public static void spawnParticles(Entity target, FluidStack fluid) {
    if (target.level() instanceof ServerLevel serverLevel) {
      serverLevel.sendParticles(new FluidParticleData(TinkerCommons.fluidParticle.get(), fluid), target.getX(), target.getY(0.5), target.getZ(), 10, 0.1, 0.2, 0.1, 0.2);
    }
  }

  /**
   * Overridable method to create the attack context and spawn particles
   */
  public ToolAttackContext createContext(LivingEntity self, @Nullable Player player, @Nullable Entity attacker, FluidStack fluid) {
    spawnParticles(self, fluid);
    return new ToolAttackContext(self, player, InteractionHand.MAIN_HAND, self, self, false, 1.0f, false);
  }

  /**
   * Checks if the modifier triggers
   */
  protected boolean doesTrigger(DamageSource source, boolean isDirectDamage) {
    return !source.is(DamageTypeTags.BYPASSES_EFFECTS) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY);
  }

  @Override
  public void onAttacked(IToolStackView tool, int level, EquipmentContext context, EquipmentSlot slotType, DamageSource source, float amount, boolean isDirectDamage) {
    Entity attacker = source.getEntity();
    if (this.doesTrigger(source, isDirectDamage)) {
      // 25% chance of working per level, 50% per level on shields
      if (RANDOM.nextInt(slotType.getType() == Type.HAND ? 2 : 4) < level) {
        FluidStack fluid = this.getFluid(tool);
        if (!fluid.isEmpty()) {
          LivingEntity self = context.getEntity();
          Player player = self instanceof Player p ? p : null;
          SpillingFluid recipe = SpillingFluidManager.INSTANCE.find(fluid.getFluid());
          if (recipe.hasEffects()) {
            FluidStack remaining = recipe.applyEffects(fluid, level, this.createContext(self, player, attacker, fluid));
            if (player == null || !player.isCreative()) {
              this.setFluid(tool, remaining);
            }
          }
        }
      }
    }
  }
}
