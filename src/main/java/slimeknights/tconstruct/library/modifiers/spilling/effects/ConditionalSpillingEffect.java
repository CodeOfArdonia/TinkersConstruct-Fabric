package slimeknights.tconstruct.library.modifiers.spilling.effects;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.json.predicate.IJsonPredicate;
import slimeknights.tconstruct.library.json.predicate.entity.LivingEntityPredicate;
import slimeknights.tconstruct.library.modifiers.spilling.ISpillingEffect;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.utils.JsonUtils;

/**
 * Spilling effect that conditions on the entity targeted
 */
public record ConditionalSpillingEffect(IJsonPredicate<LivingEntity> predicate,
                                        ISpillingEffect effect) implements ISpillingEffect {

  public static final ResourceLocation ID = TConstruct.getResource("conditional");

  @Override
  public void applyEffects(FluidStack fluid, float scale, ToolAttackContext context) {
    LivingEntity target = context.getLivingTarget();
    if (target != null && this.predicate.matches(target)) {
      this.effect.applyEffects(fluid, scale, context);
    }
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = JsonUtils.withType(ID);
    json.add("entity", LivingEntityPredicate.LOADER.serialize(this.predicate));
    json.add("effect", this.effect.serialize(context));
    return json;
  }

  /**
   * Loader instance
   */
  public static final JsonDeserializer<ConditionalSpillingEffect> LOADER = (element, type, context) -> {
    JsonObject json = element.getAsJsonObject();
    IJsonPredicate<LivingEntity> predicate = LivingEntityPredicate.LOADER.getAndDeserialize(json, "entity");
    ISpillingEffect effect = context.deserialize(GsonHelper.getAsJsonObject(json, "effect"), ISpillingEffect.class);
    return new ConditionalSpillingEffect(predicate, effect);
  };
}
