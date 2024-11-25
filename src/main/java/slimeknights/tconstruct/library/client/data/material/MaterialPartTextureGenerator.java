package slimeknights.tconstruct.library.client.data.material;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import slimeknights.tconstruct.library.client.data.GenericTextureGenerator;
import slimeknights.tconstruct.library.client.data.material.AbstractMaterialSpriteProvider.MaterialSpriteInfo;
import slimeknights.tconstruct.library.client.data.material.AbstractPartSpriteProvider.PartSpriteInfo;
import slimeknights.tconstruct.library.client.data.util.AbstractSpriteReader;
import slimeknights.tconstruct.library.client.data.util.DataGenSpriteReader;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

/**
 * Texture generator to generate textures for materials, supports adding a set of sprites to recolor, alongside a set of materials
 * <p>
 * Note this only supports generating the crossproduct of materials and textures. If your addon adds both materials and tools, the best setup is having two generators:
 * <ul>
 *   <li>A generator adding all TiC and custom materials for your new sprites</li>
 *   <li>A generator adding all custom materials for TiC sprites</li>
 * </ul>
 * In case you need to divide into more than those two, it will be most efficient if each sprite is handled by only a single generator, so always split over sets of materials.
 */
public class MaterialPartTextureGenerator extends GenericTextureGenerator {

  /**
   * Path to textures outputted by this generator
   */
  public static final String FOLDER = "textures";
  private final DataGenSpriteReader spriteReader;
  private final ExistingFileHelper existingFileHelper;
  /**
   * Sprite provider
   */
  private final AbstractPartSpriteProvider partProvider;
  /**
   * Materials to provide
   */
  private final AbstractMaterialSpriteProvider[] materialProviders;

  public MaterialPartTextureGenerator(FabricDataOutput output, ExistingFileHelper existingFileHelper, AbstractPartSpriteProvider spriteProvider, AbstractMaterialSpriteProvider... materialProviders) {
    super(output, FOLDER);
    this.spriteReader = new DataGenSpriteReader(existingFileHelper, FOLDER);
    this.existingFileHelper = existingFileHelper;
    this.partProvider = spriteProvider;
    this.materialProviders = materialProviders;
  }

  @Override
  public String getName() {
    StringBuilder name = new StringBuilder();
    name.append("Material Part Generator - ");
    name.append(this.partProvider.getName());
    name.append(" - ");
    name.append(this.materialProviders[0].getName());
    for (int i = 1; i < this.materialProviders.length; i++) {
      name.append(", ").append(this.materialProviders[i].getName());
    }
    return name.toString();
  }


  @Override
  public CompletableFuture<?> run(CachedOutput cache) {
    List<CompletableFuture<?>> futures = new ArrayList<>();
    runCallbacks(this.existingFileHelper, null);

    // ensure we have parts
    List<PartSpriteInfo> parts = this.partProvider.getSprites();
    if (parts.isEmpty()) {
      throw new IllegalStateException(this.partProvider.getName() + " has no parts, must have at least one part to generate");
    }

    // for each material list, generate sprites
    for (AbstractMaterialSpriteProvider materialProvider : this.materialProviders) {
      Collection<MaterialSpriteInfo> materials = materialProvider.getMaterials().values();
      if (materials.isEmpty()) {
        throw new IllegalStateException(materialProvider.getName() + " has no materials, must have at least one material to generate");
      }
      // want cross product of textures
      BiConsumer<ResourceLocation, NativeImage> saver = (path, image) -> this.saveImage(cache, path, image);
      Predicate<ResourceLocation> shouldGenerate = path -> !this.spriteReader.exists(path);
      for (MaterialSpriteInfo material : materials) {
        for (PartSpriteInfo part : parts) {
          if (material.supportStatType(part.getStatType())) {
            generateSprite(this.spriteReader, material, part, shouldGenerate, saver);
          }
        }
      }
    }
    this.spriteReader.closeAll();
    this.partProvider.cleanCache();
    runCallbacks(null, null);
    return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
  }

  /**
   * Generates the given sprite
   *
   * @param spriteReader   Reader to find existing sprites
   * @param material       Material for the sprite
   * @param part           Part for the sprites
   * @param shouldGenerate Predicate to determine if the sprite should generate, given the local path to the sprite
   * @param saver          Function to save the file
   */
  public static void generateSprite(AbstractSpriteReader spriteReader, MaterialSpriteInfo material, PartSpriteInfo part, Predicate<ResourceLocation> shouldGenerate, BiConsumer<ResourceLocation, NativeImage> saver) {
    // first step: see if this sprite has already been generated, if so nothing to do
    // path format: pNamespace:pPath_mNamespace_mPath
    ResourceLocation partPath = part.getPath();
    ResourceLocation materialTexture = material.getTexture();
    ResourceLocation spritePath = new ResourceLocation(partPath.getNamespace(),
      partPath.getPath() + "_" + materialTexture.getNamespace() + "_" + materialTexture.getPath());

    // image does not exist? first step is to find a base image
    if (shouldGenerate.test(spritePath)) {
      NativeImage base = null;
      for (String fallback : material.getFallbacks()) {
        base = part.getTexture(spriteReader, fallback);
        if (base != null) {
          break;
        }
      }
      // no fallback existed, try the main one
      if (base == null) {
        base = part.getTexture(spriteReader, "");
      }
      if (base == null) {
        throw new IllegalStateException("Missing sprite at " + partPath + ".png, cannot generate textures");
      }
      // successfully found a texture, now transform and save
      NativeImage transformed = material.getTransformer().transformCopy(base);
      spriteReader.track(transformed);
      saver.accept(spritePath, transformed);
    }
  }


  /* Static callbacks, handled this way as the event bus is a pain to use during datagen */

  /**
   * List of callbacks
   */
  private static final List<IPartTextureCallback> TEXTURE_CALLBACKS = new ArrayList<>();

  /**
   * Registers a callback to run whenever sprites are generated.
   */
  public static void registerCallback(IPartTextureCallback callback) {
    TEXTURE_CALLBACKS.add(callback);
  }

  /**
   * Runs all callbacks
   */
  public static void runCallbacks(@Nullable ExistingFileHelper existingFileHelper, @Nullable ResourceManager manager) {
    for (IPartTextureCallback callback : TEXTURE_CALLBACKS) {
      callback.accept(existingFileHelper, manager);
    }
  }

  public interface IPartTextureCallback {

    /**
     * Tells the given callback that texture generating is either starting or ending. Both parameters being null means texture generating is ending
     *
     * @param existingFileHelper If nonnull, datagenerators are starting
     * @param manager            If nonnull, command is starting
     */
    void accept(@Nullable ExistingFileHelper existingFileHelper, @Nullable ResourceManager manager);
  }
}
