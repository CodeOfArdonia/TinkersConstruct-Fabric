package slimeknights.tconstruct.fluids.item;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.item.FluidBucketWrapper;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import slimeknights.tconstruct.library.utils.Util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

/**
 * Implements filling a bucket with an NBT fluid
 */
public class PotionBucketItem extends PotionItem {

  private final Supplier<? extends Fluid> supplier;

  public PotionBucketItem(Supplier<? extends Fluid> supplier, Properties builder) {
    super(builder);
    this.supplier = supplier;
    FluidStorage.ITEM.registerForItems((itemStack, context) -> new PotionBucketWrapper(context), this);
  }

  public Fluid getFluid() {
    return this.supplier.get();
  }

  @Override
  public String getDescriptionId(ItemStack stack) {
    String bucketKey = PotionUtils.getPotion(stack.getTag()).getName(this.getDescriptionId() + ".effect.");
    if (Util.canTranslate(bucketKey)) {
      return bucketKey;
    }
    return super.getDescriptionId();
  }

  @Override
  public Component getName(ItemStack stack) {
    Potion potion = PotionUtils.getPotion(stack.getTag());
    String bucketKey = potion.getName(this.getDescriptionId() + ".effect.");
    if (Util.canTranslate(bucketKey)) {
      return Component.translatable(bucketKey);
    }
    // default to filling with the contents
    return Component.translatable(this.getDescriptionId() + ".contents", Component.translatable(potion.getName("item.minecraft.potion.effect.")));
  }

  @Override
  public ItemStack getDefaultInstance() {
    return PotionUtils.setPotion(super.getDefaultInstance(), Potions.WATER);
  }

  @Override
  public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity living) {
    Player player = living instanceof Player p ? p : null;
    if (player instanceof ServerPlayer serverPlayer) {
      CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
    }

    // effects are 2x duration
    if (!level.isClientSide) {
      for (MobEffectInstance effect : PotionUtils.getMobEffects(stack)) {
        if (effect.getEffect().isInstantenous()) {
          effect.getEffect().applyInstantenousEffect(player, player, living, effect.getAmplifier(), 2.5D);
        } else {
          MobEffectInstance newEffect = new MobEffectInstance(effect);
          newEffect.duration = newEffect.duration * 5 / 2;
          living.addEffect(newEffect);
        }
      }
    }

    if (player != null) {
      player.awardStat(Stats.ITEM_USED.get(this));
      if (!player.getAbilities().instabuild) {
        stack.shrink(1);
      }
    }

    if (player == null || !player.getAbilities().instabuild) {
      if (stack.isEmpty()) {
        return new ItemStack(Items.BUCKET);
      }
      if (player != null) {
        player.getInventory().add(new ItemStack(Items.BUCKET));
      }
    }
    living.gameEvent(GameEvent.DRINK);
    return stack;
  }

  @Override
  public void appendHoverText(ItemStack pStack, @Nullable Level pLevel, List<Component> pTooltip, TooltipFlag pFlag) {
    PotionUtils.addPotionTooltip(pStack, pTooltip, 2.5f);
  }

  @Override
  public int getUseDuration(ItemStack pStack) {
    return 96; // 3x duration of potion bottles
  }

  public static class PotionBucketWrapper extends FluidBucketWrapper {

    public PotionBucketWrapper(ContainerItemContext container) {
      super(container);
    }

    @Nonnull
    @Override
    public FluidStack getFluid() {
      return new FluidStack(((PotionBucketItem) this.context.getItemVariant().getItem()).getFluid(),
        FluidConstants.BUCKET, this.context.getItemVariant().getNbt());
    }
  }
}
