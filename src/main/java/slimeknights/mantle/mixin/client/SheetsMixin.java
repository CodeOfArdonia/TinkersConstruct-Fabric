package slimeknights.mantle.mixin.client;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.WoodType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import slimeknights.mantle.registration.RegistrationHelper;

@Mixin(Sheets.class)
public class SheetsMixin {

  @Shadow
  @Final
  public static ResourceLocation SIGN_SHEET;

  @Inject(method = "createSignMaterial", at = @At("HEAD"), cancellable = true)
  private static void fixMantleSigns(WoodType oldWoodType, CallbackInfoReturnable<Material> cir) {
    RegistrationHelper.forEachWoodType(woodType -> {
      if(woodType == oldWoodType) {
        ResourceLocation location = new ResourceLocation(woodType.name());
        cir.setReturnValue(new Material(SIGN_SHEET, new ResourceLocation(location.getNamespace(), "entity/signs/" + location.getPath())));
      }
    });
  }
}
