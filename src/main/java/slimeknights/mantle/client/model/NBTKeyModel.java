package slimeknights.mantle.client.model;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.math.Transformation;
import io.github.fabricators_of_create.porting_lib.models.UnbakedGeometryHelper;
import io.github.fabricators_of_create.porting_lib.models.geometry.IGeometryLoader;
import io.github.fabricators_of_create.porting_lib.models.geometry.IUnbakedGeometry;
import io.github.fabricators_of_create.porting_lib.models.geometry.SimpleModelState;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.mesh.MeshBuilder;
import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.client.model.util.BakedItemModel;
import slimeknights.mantle.client.model.util.ModelTextureIteratable;
import slimeknights.mantle.util.JsonHelper;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

@RequiredArgsConstructor
public class NBTKeyModel implements IUnbakedGeometry<NBTKeyModel> {
  /** Model loader instance */
  public static final Loader LOADER = new Loader();

  private static final ItemModelGenerator ITEM_MODEL_GENERATOR = new ItemModelGenerator();

  /** Map of statically registered extra textures, used for addon mods */
  private static final Multimap<ResourceLocation,Pair<String,ResourceLocation>> EXTRA_TEXTURES = HashMultimap.create();

  /**
   * Registers an extra variant texture for the model with the given key. Note that resource packs can override the extra texture
   * @param key          Model key, should be defined in the model JSON if supported
   * @param textureName  Name of the texture defined, corresponds to a possible value of the NBT key
   * @param texture      Texture to use, same format as in resource packs
   */
  public static void registerExtraTexture(ResourceLocation key, String textureName, ResourceLocation texture) {
    EXTRA_TEXTURES.put(key, Pair.of(textureName, texture));
  }

  /** Key to check in item NBT */
  private final String nbtKey;
  /** Key denoting which extra textures to fetch from the map */
  @Nullable
  private final ResourceLocation extraTexturesKey;

  /** Map of textures for the model */
  private Map<String,Material> textures = Collections.emptyMap();

  /** Bakes a model for the given texture */
  private static BakedModel bakeModel(BlockModel owner, Material texture, Function<Material,TextureAtlasSprite> spriteGetter, ItemOverrides overrides) {
    TextureAtlasSprite sprite = spriteGetter.apply(texture);
    List<BakedQuad> quads = UnbakedGeometryHelper.bakeElements(ITEM_MODEL_GENERATOR.processFrames(-1, sprite.contents().name().toString(), sprite.contents()), spriteGetter, new SimpleModelState(Transformation.identity()), sprite.contents().name());
    MeshBuilder meshBuilder = RendererAccess.INSTANCE.getRenderer().meshBuilder();
    QuadEmitter emitter = meshBuilder.getEmitter();
    for (BakedQuad quad : quads) {
      emitter.fromVanilla(quad, RendererAccess.INSTANCE.getRenderer().materialFinder().find(), null);
      emitter.emit();
    }
    return new BakedItemModel(meshBuilder.build(), quad -> true, sprite, owner.getTransforms(), overrides, true, owner.getGuiLight().lightLikeBlock());
  }

  @Override
  public BakedModel bake(BlockModel owner, ModelBaker baker, Function<Material,TextureAtlasSprite> spriteGetter, ModelState modelTransform, ItemOverrides overrides, ResourceLocation modelLocation, boolean isGui3d) {
    textures = new HashMap<>();
    // must have a default
    Material defaultTexture = owner.getMaterial("default");
    textures.put("default", defaultTexture);
    // fetch others
    ModelTextureIteratable iterable = new ModelTextureIteratable(null, owner);
    for (Map<String,Either<Material,String>> map : iterable) {
      for (String key : map.keySet()) {
        if (!textures.containsKey(key) && owner.hasTexture(key)) {
          textures.put(key, owner.getMaterial(key));
        }
      }
    }
    // fetch extra textures
    if (extraTexturesKey != null) {
      for (Pair<String,ResourceLocation> extra : EXTRA_TEXTURES.get(extraTexturesKey)) {
        String key = extra.getFirst();
        if (!textures.containsKey(key)) {
          textures.put(key, new Material(TextureAtlas.LOCATION_BLOCKS, extra.getSecond()));
        }
      }
    }
    ImmutableMap.Builder<String, BakedModel> variants = ImmutableMap.builder();
    for (Entry<String,Material> entry : textures.entrySet()) {
      String key = entry.getKey();
      if (!key.equals("default")) {
        variants.put(key, bakeModel(owner, entry.getValue(), spriteGetter, ItemOverrides.EMPTY));
      }
    }
    return bakeModel(owner, textures.get("default"), spriteGetter, new Overrides(nbtKey, textures, variants.build()));
  }

  /** Overrides list for a tool slot item model */
  @RequiredArgsConstructor
  public static class Overrides extends ItemOverrides {
    private final String nbtKey;
    private final Map<String,Material> textures;
    private final Map<String,BakedModel> variants;

    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity livingEntity, int pSeed) {
      CompoundTag nbt = stack.getTag();
      if (nbt != null && nbt.contains(nbtKey)) {
        return variants.getOrDefault(nbt.getString(nbtKey), model);
      }
      return model;
    }

    /** Gets the given texture from the model */
    public Material getTexture(String name) {
      Material texture = textures.get(name);
      return texture != null ? texture : textures.get("default");
    }
  }

  /** Loader logic */
  private static class Loader implements IGeometryLoader<NBTKeyModel> {
    @Override
    public NBTKeyModel read(JsonObject modelContents, JsonDeserializationContext deserializationContext) {
      String key = GsonHelper.getAsString(modelContents, "nbt_key");
      ResourceLocation extraTexturesKey = null;
      if (modelContents.has("extra_textures_key")) {
        extraTexturesKey = JsonHelper.getResourceLocation(modelContents, "extra_textures_key");
      }
      return new NBTKeyModel(key, extraTexturesKey);
    }
  }
}
