package slimeknights.tconstruct.library.modifiers.spilling.effects;

import com.google.gson.JsonDeserializer;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.spilling.ISpillingEffect;
import slimeknights.tconstruct.library.tools.context.ToolAttackContext;
import slimeknights.tconstruct.library.utils.JsonUtils;

/**
 * Effect to restore hunger to the target
 */
public record RestoreHungerSpillingEffect(int hunger, float saturation) implements ISpillingEffect {

  public static final ResourceLocation ID = TConstruct.getResource("restore_hunger");

  @Override
  public void applyEffects(FluidStack fluid, float scale, ToolAttackContext context) {
    LivingEntity target = context.getLivingTarget();
    if (target instanceof Player player) {
      if (player.canEat(false)) {
        player.getFoodData().eat((int) (this.hunger * scale), this.saturation * scale);
      }
    }
  }

  @Override
  public JsonObject serialize(JsonSerializationContext context) {
    JsonObject json = JsonUtils.withType(ID);
    json.addProperty("hunger", this.hunger);
    json.addProperty("saturation", this.saturation);
    return json;
  }

  public static final JsonDeserializer<RestoreHungerSpillingEffect> LOADER = (element, type, context) -> {
    JsonObject json = element.getAsJsonObject();
    int hunger = GsonHelper.getAsInt(json, "hunger");
    float saturation = GsonHelper.getAsFloat(json, "saturation");
    return new RestoreHungerSpillingEffect(hunger, saturation);
  };
}
