package slimeknights.tconstruct.library.client.modifiers;

import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.client.model.util.MantleItemLayerModel;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nullable;
import java.util.function.Function;

/**
 * Default modifier model loader, loads a single texture from the standard path
 */
public class NormalModifierModel implements IBakedModifierModel {

  /**
   * Constant unbaked model instance, as they are all the same
   */
  public static final IUnbakedModifierModel UNBAKED_INSTANCE = new Unbaked(-1, 0);

  /**
   * Textures to show
   */
  private final Material[] textures;
  /**
   * Color to apply to the texture
   */
  private final int color;
  /**
   * Luminosity to apply to the texture
   */
  private final int luminosity;

  public NormalModifierModel(@Nullable Material smallTexture, @Nullable Material largeTexture, int color, int luminosity) {
    this.color = color;
    this.luminosity = luminosity;
    this.textures = new Material[]{smallTexture, largeTexture};
  }

  public NormalModifierModel(@Nullable Material smallTexture, @Nullable Material largeTexture) {
    this(smallTexture, largeTexture, -1, 0);
  }

  @Override
  public Mesh getQuads(IToolStackView tool, ModifierEntry entry, Function<Material, TextureAtlasSprite> spriteGetter, Transformation transforms, boolean isLarge, int startTintIndex, @Nullable ItemLayerPixels pixels) {
    int index = isLarge ? 1 : 0;
    return MantleItemLayerModel.getQuadsForSprite(this.color, -1, spriteGetter.apply(this.textures[index]), transforms, this.luminosity, pixels);
  }

  private record Unbaked(int color, int luminosity) implements IUnbakedModifierModel {

    @Nullable
    @Override
    public IBakedModifierModel forTool(Function<String, Material> smallGetter, Function<String, Material> largeGetter) {
      Material smallTexture = smallGetter.apply("");
      Material largeTexture = largeGetter.apply("");
      if (smallTexture != null || largeTexture != null) {
        return new NormalModifierModel(smallTexture, largeTexture, this.color, this.luminosity);
      }
      return null;
    }

    @Override
    public IUnbakedModifierModel configure(JsonObject data) {
      // parse the two keys, if we ended up with something new create an instance
      int color = JsonHelper.parseColor(GsonHelper.getAsString(data, "color", ""));
      int luminosity = GsonHelper.getAsInt(data, "luminosity");
      if (color != this.color || luminosity != this.luminosity) {
        return new Unbaked(color, luminosity);
      }
      return this;
    }
  }
}
