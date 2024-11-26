package slimeknights.mantle.client.model.util;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.math.Transformation;
import io.github.fabricators_of_create.porting_lib.models.ItemLayerModel;
import io.github.fabricators_of_create.porting_lib.models.QuadTransformers;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.MaterialFinder;
import net.fabricmc.fabric.api.renderer.v1.mesh.Mesh;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.MutableQuadView;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import slimeknights.mantle.util.ItemLayerPixels;
import slimeknights.mantle.util.JsonHelper;
import slimeknights.mantle.util.ReversedListBuilder;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Function;

/**
 * Clone of {@link ItemLayerModel} to propagate a hardcoded color in, allows reducing rendering time by bypassing item colors for a static color.
 * Also supports luminosity, and when used as a model loader supports telling a layer to not use a tint index
 */
@RequiredArgsConstructor
public class MantleItemLayerModel implements IUnbakedGeometry<MantleItemLayerModel> {
  /** Model loader instance */
  public static final Loader LOADER = new Loader();

  private static final Direction[] HORIZONTALS = {Direction.UP, Direction.DOWN};
  private static final Direction[] VERTICALS = {Direction.WEST, Direction.EAST};

  /** Layers in the model */
  private final List<LayerData> layers;

  /** Gets the layer at the given index */
  private LayerData getLayer(int index) {
    if (index < 0 || index >= layers.size()) {
      return LayerData.DEFAULT;
    }
    return layers.get(index);
  }

  @Override
  public BakedModel bake(BlockModel owner, ModelBaker baker, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation, boolean isGui3d) {
    ImmutableList.Builder<Material> materialBuilder = ImmutableList.builder();
    for (int i = 0; owner.hasTexture("layer" + i); i++) {
      materialBuilder.add(owner.getMaterial("layer" + i));
    }
    List<Material> textures = materialBuilder.build();
    // determine particle texture
    TextureAtlasSprite particle = spriteGetter.apply(owner.hasTexture("particle") ? owner.getMaterial("particle") : textures.get(0));
    // bake in special properties
    ReversedListBuilder<Mesh> builder = new ReversedListBuilder<>();
    // skip the pixel tracking if using a single texture only
    ItemLayerPixels pixels = textures.size() == 1 ? null : new ItemLayerPixels();
    Transformation transform = modelTransform.getRotation();
    for (int i = textures.size() - 1; i >= 0; i--) {
      TextureAtlasSprite sprite = spriteGetter.apply(textures.get(i));
      LayerData data = getLayer(i);
      builder.add(getQuadsForSprite(data.color(), data.noTint() ? -1 : i, sprite, transform, data.luminosity(), pixels));
    }
    // transform data
    MeshBuilder finalMesh = RendererAccess.INSTANCE.getRenderer().meshBuilder();
    QuadEmitter emitter = finalMesh.getEmitter();
    for (Mesh mesh : builder.build()) {
      mesh.outputTo(emitter);
    }
    return new BakedItemModel(finalMesh.build(), quad -> true, particle, owner.getTransforms(), overrides, true, owner.getGuiLight().lightLikeBlock());
  }

  /**
   * Gets all quads for an item layer for the given sprite
   * @param color       Color for the sprite in AARRGGBB format.
   * @param tint        Tint index for {@link net.minecraft.client.color.block.BlockColors} and {@link net.minecraft.client.color.item.ItemColors}. Generally unused
   * @param sprite      Sprite to convert into quads
   * @param transform   Transforms to apply
   * @param luminosity  Extra light to add to the quad from 0-15, makes it appear to glow a bit
   * @return  List of baked quads
   */
  public static Mesh getQuadsForSprite(int color, int tint, TextureAtlasSprite sprite, Transformation transform, int luminosity) {
    return getQuadsForSprite(color, tint, sprite, transform, luminosity, null);
  }

  /**
   * Gets all quads for an item layer for the given sprite
   * @param color       Color for the sprite in AARRGGBB format.
   * @param tint        Tint index for {@link net.minecraft.client.color.block.BlockColors} and {@link net.minecraft.client.color.item.ItemColors}. Generally unused
   * @param sprite      Sprite to convert into quads
   * @param transform   Transforms to apply
   * @param luminosity  Extra light to add to the quad from 0-15, makes it appear to glow a bit
   * @param pixels      Object to keep track of used pixels across multiple layers to help prevent z-fighting. To effective use, sprites must be built in reverse order. Use null to skip this logic
   * @return  List of baked quads
   */
  public static Mesh getQuadsForSprite(int color, int tint, TextureAtlasSprite sprite, Transformation transform, int luminosity, @Nullable ItemLayerPixels pixels) {
    MeshBuilder mesh = RendererAccess.INSTANCE.getRenderer().meshBuilder();

    int uMax = sprite.contents().width();
    int vMax = sprite.contents().height();
    FaceData faceData = new FaceData(uMax, vMax);
    boolean translucent = false;

    for(int f = 0; f < sprite.contents().getFrameCount(); f++) {
      boolean ptu;
      boolean[] ptv = new boolean[uMax];
      Arrays.fill(ptv, true);
      for(int v = 0; v < vMax; v++) {
        ptu = true;
        for(int u = 0; u < uMax; u++) {
          int alpha = sprite.contents().getPixelRGBA(f, u, vMax - v - 1) >> 24 & 0xFF;
          boolean t = alpha / 255f <= 0.1f;

          if (!t && alpha < 255) {
            translucent = true;
          }

          if(ptu && !t) { // left - transparent, right - opaque
            faceData.set(Direction.WEST, u, v);
          }
          if(!ptu && t) { // left - opaque, right - transparent
            faceData.set(Direction.EAST, u-1, v);
          }
          if(ptv[u] && !t) { // up - transparent, down - opaque
            faceData.set(Direction.UP, u, v);
          }
          if(!ptv[u] && t) { // up - opaque, down - transparent
            faceData.set(Direction.DOWN, u, v-1);
          }

          ptu = t;
          ptv[u] = t;
        }
        if(!ptu) { // last - opaque
          faceData.set(Direction.EAST, uMax-1, v);
        }
      }
      // last line
      for(int u = 0; u < uMax; u++) {
        if(!ptv[u]) {
          faceData.set(Direction.DOWN, u, vMax-1);
        }
      }
    }

    // horizontal quads
    for (Direction facing : HORIZONTALS) {
      for (int v = 0; v < vMax; v++) {
        int uStart = 0, uEnd = uMax;
        boolean building = false;
        for (int u = 0; u < uMax; u++) {
          boolean canDraw = pixels == null || !pixels.get(u, v, uMax, vMax);
          boolean face = canDraw && faceData.get(facing, u, v);
          // set the end for translucent to draw right after this pixel
          if (face) {
            uEnd = u + 1;
            // if not currently building and we have data, start new quad
            if (!building) {
              building = true;
              uStart = u;
            }
          }
          // make quad [uStart, u]
          else if (building) {
            // finish current quad if translucent (minimize overdraw) or we are forbidden from touching this pixel (previous layer drew here)
            if (!canDraw || translucent) {
              int off = facing == Direction.DOWN ? 1 : 0;
              buildSideQuad(mesh, transform, facing, color, tint, sprite, uStart, v + off, uEnd - uStart, luminosity);
              building = false;
            }
          }
        }
        if (building) { // build remaining quad
          // make quad [uStart, uEnd]
          int off = facing == Direction.DOWN ? 1 : 0;
          buildSideQuad(mesh, transform, facing, color, tint, sprite, uStart, v+off, uEnd-uStart, luminosity);
        }
      }
    }

    // vertical quads
    for (Direction facing : VERTICALS) {
      for (int u = 0; u < uMax; u++) {
        int vStart = 0, vEnd = vMax;
        boolean building = false;
        for (int v = 0; v < vMax; v++) {
          boolean canDraw = pixels == null || !pixels.get(u, v, uMax, vMax);
          boolean face = canDraw && faceData.get(facing, u, v);
          // set the end for translucent to draw right after this pixel
          if (face) {
            vEnd = v + 1;
            // if not currently building and we have data, start new quad
            if (!building) {
              building = true;
              vStart = v;
            }
          }
          // make quad [vStart, v]
          else if (building) {
            // finish current quad if translucent (minimize overdraw) or we are forbidden from touching this pixel (future layer drew here)
            if (!canDraw || translucent) {
              int off = facing == Direction.EAST ? 1 : 0;
              buildSideQuad(mesh, transform, facing, color, tint, sprite, u + off, vStart, vEnd - vStart, luminosity);
              building = false;
            }
          }
        }
        if (building) { // build remaining quad
          // make quad [vStart, vEnd]
          int off = facing == Direction.EAST ? 1 : 0;
          buildSideQuad(mesh, transform, facing, color, tint, sprite, u+off, vStart, vEnd-vStart, luminosity);
        }
      }
    }

    // front
    buildQuad(mesh, transform, Direction.NORTH, sprite, color, tint, luminosity,
      0, 0, 7.5f / 16f, sprite.getU0(), sprite.getV1(),
      0, 1, 7.5f / 16f, sprite.getU0(), sprite.getV0(),
      1, 1, 7.5f / 16f, sprite.getU1(), sprite.getV0(),
      1, 0, 7.5f / 16f, sprite.getU1(), sprite.getV1()
    );
    // back
    buildQuad(mesh, transform, Direction.SOUTH, sprite, color, tint, luminosity,
      0, 0, 8.5f / 16f, sprite.getU0(), sprite.getV1(),
      1, 0, 8.5f / 16f, sprite.getU1(), sprite.getV1(),
      1, 1, 8.5f / 16f, sprite.getU1(), sprite.getV0(),
      0, 1, 8.5f / 16f, sprite.getU0(), sprite.getV0()
    );

    // fill in the pixel map with new pixels from the sprite
    if (pixels != null) {
      // animated textures are tricky, as we have three choices:
      //  1. if a pixel is only potentially there, don't draw lower layers - leads to gaps
      //  2. if a pixel is only potentially there, always draw lower layers - leads to z-fighting
      //  3. only use the first frame
      // of these, 2 would give the most accurate result. However, its also the hardest to calculate
      // of the remaining methods, 3 is both more accurate and easier to calculate than 1, so I opted for that approach
      if (sprite.contents().getFrameCount() > 0) {
        for(int v = 0; v < vMax; v++) {
          for(int u = 0; u < uMax; u++) {
            int alpha = sprite.contents().getPixelRGBA(0, u, vMax - v - 1) >> 24 & 0xFF;
            if (alpha / 255f > 0.1f) {
              pixels.set(u, v, uMax, vMax);
            }
          }
        }
      }
    }

    return mesh.build();
  }

  /**
   * Builds a single quad on the side of the sprite
   * @param transform  Transforms to apply
   * @param side       Side to build
   * @param color      Color for the sprite
   * @param tint       Tint index for {@link net.minecraft.client.color.block.BlockColors} and {@link net.minecraft.client.color.item.ItemColors}
   * @param sprite     Sprite to render
   * @param u          Sprite U
   * @param v          Sprite V
   * @param size       Size of the quad in the correct direction (depth is always 1 pixel)
   * @param luminosity Extra light to add to the quad between 0 and 15
   * @return  Baked quad
   */
  private static void buildSideQuad(MeshBuilder builder, Transformation transform, Direction side, int color, int tint, TextureAtlasSprite sprite, int u, int v, int size, int luminosity) {
    final float eps = 1e-2f;
    int width = sprite.contents().width();
    int height = sprite.contents().height();
    float x0 = (float) u / width;
    float y0 = (float) v / height;
    float x1 = x0, y1 = y0;
    float z0 = 7.5f / 16f, z1 = 8.5f / 16f;
    switch(side) {
      case WEST:
        z0 = 8.5f / 16f;
        z1 = 7.5f / 16f;
        // continue into EAST
      case EAST:
        y1 = (float) (v + size) / height;
        break;
      case DOWN:
        z0 = 8.5f / 16f;
        z1 = 7.5f / 16f;
        // continue into UP
      case UP:
        x1 = (float) (u + size) / width;
        break;
      default:
        throw new IllegalArgumentException("can't handle z-oriented side");
    }

    // for the side, Y axis's use of getOpposite is related to the swapping of V direction
    float dx = side.getNormal().getX() * eps / width;
    float dy = side.getNormal().getY() * eps / height;
    float u0 = 16f * (x0 - dx);
    float u1 = 16f * (x1 - dx);
    float v0 = 16f * (1f - y0 - dy);
    float v1 = 16f * (1f - y1 - dy);
    buildQuad(
      builder, transform, (side.getAxis() == Axis.Y ? side.getOpposite() : side),
      sprite, color, tint, luminosity,
      x0, y0, z0, sprite.getU(u0), sprite.getV(v0),
      x1, y1, z0, sprite.getU(u1), sprite.getV(v1),
      x1, y1, z1, sprite.getU(u1), sprite.getV(v1),
      x0, y0, z1, sprite.getU(u0), sprite.getV(v0));
  }

  /**
   * Builds a single quad in the model, based on the method in {@link ItemLayerModel} but with color added
   * @param transform    Model transforms
   * @param side         Quad side
   * @param sprite       Sprite to use in the quad
   * @param color        Color for the sprite in AARRGGBB format
   * @param tint         Tint index for {@link net.minecraft.client.color.block.BlockColors} and {@link net.minecraft.client.color.item.ItemColors}
   * @param luminosity Extra light to add to the quad between 0 and 15
   * @return  Final quad
   */
  protected static void buildQuad(MeshBuilder builder, Transformation transform, Direction side, TextureAtlasSprite sprite, int color, int tint, int luminosity,
                                       float x0, float y0, float z0, float u0, float v0,
                                       float x1, float y1, float z1, float u1, float v1,
                                       float x2, float y2, float z2, float u2, float v2,
                                       float x3, float y3, float z3, float u3, float v3) {
    QuadEmitter emitter = builder.getEmitter();
    MaterialFinder material = RendererAccess.INSTANCE.getRenderer().materialFinder();
    emitter.spriteBake(sprite, MutableQuadView.BAKE_ROTATE_NONE);
    emitter.colorIndex(tint);
    emitter.nominalFace(side);
    emitter.material(material.disableDiffuse(true).find());

    putVertex(emitter, side, x0, y0, z0, u0, v0, color, luminosity, 0);
    putVertex(emitter, side, x1, y1, z1, u1, v1, color, luminosity, 1);
    putVertex(emitter, side, x2, y2, z2, u2, v2, color, luminosity, 2);
    putVertex(emitter, side, x3, y3, z3, u3, v3, color, luminosity, 3);
    QuadTransformers.applying(transform).transform(emitter);
    emitter.emit();
  }

  /**
   * Clone of the method in {@link ItemLayerModel} with the color parameter added
   * @param consumer   Vertex consumer
   * @param side       Side for the quad
   * @param x          Quad X position
   * @param y          Quad Y position
   * @param z          Quad Z position
   * @param u          Quad texture U
   * @param v          Quad texture V
   * @param color      Quad color in AARRGGBB format
   * @param luminosity Extra light to add to the quad between 0 and 15
   */
  private static void putVertex(QuadEmitter consumer, Direction side, float x, float y, float z, float u, float v, int color, int luminosity, int vertexIndex) {
    consumer.pos(vertexIndex, x, y, z);

    float r = ((color >> 16) & 0xFF) / 255f;
    float g = ((color >>  8) & 0xFF) / 255f;
    float b = ((color      ) & 0xFF) / 255f;
    float a = ((color >> 24) & 0xFF) / 255f;
    consumer.spriteColor(vertexIndex, 0, encodeQuadColor(r, g, b, a));

    consumer.sprite(vertexIndex, 0, u, v);

    consumer.lightmap(vertexIndex, LightTexture.pack(luminosity, luminosity));

    float offX = (float) side.getStepX();
    float offY = (float) side.getStepY();
    float offZ = (float) side.getStepZ();
    consumer.normal(vertexIndex, offX, offY, offZ);
  }

  private static int encodeQuadColor(float x, float y, float z, float w) {
    int r = (int) (x * 255.0F);
    int g = (int) (y * 255.0F);
    int b = (int) (z * 255.0F);
    int a = (int) (w * 255.0F);

    return ((a & 0xFF) << 24) |
      ((b & 0xFF) << 16) |
      ((g & 0xFF) << 8) |
      (r & 0xFF);
  }

  /** Cloned from {@link ItemLayerModel}'s FaceData subclass */
  private static class FaceData {
    private final EnumMap<Direction,BitSet> data = new EnumMap<>(Direction.class);
    private final int vMax;

    FaceData(int uMax, int vMax) {
      this.vMax = vMax;

      data.put(Direction.WEST, new BitSet(uMax * vMax));
      data.put(Direction.EAST, new BitSet(uMax * vMax));
      data.put(Direction.UP,   new BitSet(uMax * vMax));
      data.put(Direction.DOWN, new BitSet(uMax * vMax));
    }

    public void set(Direction facing, int u, int v) {
      data.get(facing).set(getIndex(u, v));
    }

    public boolean get(Direction facing, int u, int v) {
      return data.get(facing).get(getIndex(u, v));
    }

    private int getIndex(int u, int v) {
      return v * vMax + u;
    }
  }

  /**
   * Class holding details about a single layer in the model
   */
  private record LayerData(int color, int luminosity, boolean noTint) {
    private static final LayerData DEFAULT = new LayerData(-1, 0, false);

    /**
     * Parses the layer data from JSON
     */
    public static LayerData fromJson(JsonObject json) {
      int color = JsonHelper.parseColor(GsonHelper.getAsString(json, "color", ""));
      int luminosity = GsonHelper.getAsInt(json, "luminosity");
      boolean noTint = GsonHelper.getAsBoolean(json, "no_tint", false);
      return new LayerData(color, luminosity, noTint);
    }
  }

  private static class Loader implements IGeometryLoader<MantleItemLayerModel> {
    @Override
    public MantleItemLayerModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
      List<LayerData> layers = JsonHelper.parseList(modelContents, "layers", LayerData::fromJson);
      return new MantleItemLayerModel(layers);
    }
  }
}
