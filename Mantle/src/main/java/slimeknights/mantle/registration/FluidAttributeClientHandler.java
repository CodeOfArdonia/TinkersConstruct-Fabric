package slimeknights.mantle.registration;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandler;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;
import slimeknights.mantle.fluid.attributes.FluidAttributes;

public class FluidAttributeClientHandler implements FluidRenderHandler {
  protected final FluidAttributes attributes;

  protected TextureAtlasSprite[] sprites;

  public FluidAttributeClientHandler(FluidAttributes attributes) {
    this.attributes = attributes;
  }

  @Override
  public TextureAtlasSprite[] getFluidSprites(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
    return sprites;
  }

  @Override
  public int getFluidColor(@Nullable BlockAndTintGetter view, @Nullable BlockPos pos, FluidState state) {
    return attributes.getColor(view, pos);
  }

  @Override
  public void reloadTextures(TextureAtlas textureAtlas) {
    ResourceLocation overlayTexture = attributes.getOverlayTexture();
    this.sprites = new TextureAtlasSprite[overlayTexture == null ? 2 : 3];
    sprites[0] = textureAtlas.getSprite(attributes.getStillTexture());
    sprites[1] = textureAtlas.getSprite(attributes.getFlowingTexture());


    if (overlayTexture != null) {
      sprites[2] = textureAtlas.getSprite(overlayTexture);
    }
  }
}
