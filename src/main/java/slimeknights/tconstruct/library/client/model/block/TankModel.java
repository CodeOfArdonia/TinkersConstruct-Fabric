package slimeknights.tconstruct.library.client.model.block;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.fabricators_of_create.porting_lib.extensions.FluidExtensions;
import io.github.fabricators_of_create.porting_lib.model.IModelConfiguration;
import io.github.fabricators_of_create.porting_lib.model.IModelData;
import io.github.fabricators_of_create.porting_lib.model.IModelGeometry;
import io.github.fabricators_of_create.porting_lib.model.IModelLoader;
import io.github.fabricators_of_create.porting_lib.render.TransformTypeDependentItemBakedModel;
import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import io.github.fabricators_of_create.porting_lib.util.FluidAttributes;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.fabricmc.fabric.api.transfer.v1.client.fluid.FluidVariantRendering;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariantAttributes;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.SimpleBakedModel;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.client.model.util.ColoredBlockModel;
import slimeknights.mantle.client.model.util.ExtraTextureConfiguration;
import slimeknights.mantle.client.model.util.SimpleBlockModel;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.config.Config;
import slimeknights.tconstruct.library.client.model.ModelProperties;
import slimeknights.tconstruct.smeltery.item.TankItem;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This model contains a single scalable fluid that can either be statically rendered or rendered in the TESR. It also supports rendering fluids in the item model
 */
@Log4j2
@AllArgsConstructor
public class TankModel implements IModelGeometry<TankModel> {
  protected static final ResourceLocation BAKE_LOCATION = TConstruct.getResource("dynamic_model_baking");

  /** Shared loader instance */
  public static final Loader LOADER = new Loader();

  protected final SimpleBlockModel model;
  @Nullable
  protected final SimpleBlockModel gui;
  protected final IncrementalFluidCuboid fluid;
  protected final boolean forceModelFluid;

  @Override
  public Collection<Material> getTextures(IModelConfiguration owner, Function<ResourceLocation,UnbakedModel> modelGetter, Set<Pair<String,String>> missingTextureErrors) {
    Collection<Material> textures = new HashSet<>(model.getTextures(owner, modelGetter, missingTextureErrors));
    if (gui != null) {
      textures.addAll(gui.getTextures(owner, modelGetter, missingTextureErrors));
    }
    return textures;
  }

  @Override
  public BakedModel bake(IModelConfiguration owner, ModelBakery bakery, Function<Material,TextureAtlasSprite> spriteGetter, ModelState transform, ItemOverrides overrides, ResourceLocation location) {
    BakedModel baked = model.bakeModel(owner, transform, overrides, spriteGetter, location);
    // bake the GUI model if present
    BakedModel bakedGui = baked;
    if (gui != null) {
      bakedGui = gui.bakeModel(owner, transform, overrides, spriteGetter, location);
    }
    return new Baked<>(owner, transform, baked, bakedGui, this);
  }

  /** Override to add the fluid part to the item model */
  private static class FluidPartOverride extends ItemOverrides {
    /** Shared override instance, since the logic is not model dependent */
    public static final FluidPartOverride INSTANCE = new FluidPartOverride();

    @Override
    public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel world, @Nullable LivingEntity entity, int seed) {
      // ensure we have a fluid
      if (stack.isEmpty() || !stack.hasTag()) {
        return model;
      }
      // determine fluid
      FluidTank tank = TankItem.getFluidTank(stack);
      if (tank.isEmpty()) {
        return model;
      }
      // always baked model as this override is only used in our model
      return ((Baked<?>)model).getCachedModel(tank.getFluid(), tank.getCapacity());
    }
  }

  /**
   * Wrapper that swaps the model for the GUI
   */
  private static class BakedGuiUniqueModel extends ForwardingBakedModel implements TransformTypeDependentItemBakedModel {
    private final BakedModel gui;
    public BakedGuiUniqueModel(BakedModel base, BakedModel gui) {
      wrapped = base;
      this.gui = gui;
    }

    /* Swap out GUI model if needed */

    @Override
    public BakedModel handlePerspective(TransformType cameraTransformType, PoseStack mat) {
      if(!(gui instanceof TransformTypeDependentItemBakedModel) || !(wrapped instanceof TransformTypeDependentItemBakedModel))
        return wrapped;
      if (cameraTransformType == TransformType.GUI) {
        return ((TransformTypeDependentItemBakedModel)gui).handlePerspective(cameraTransformType, mat);
      }
      return ((TransformTypeDependentItemBakedModel)wrapped).handlePerspective(cameraTransformType, mat);
    }
  }

  /**
   * Baked variant to load in the custom overrides
   * @param <T>  Parent model type, used to make this easier to extend
   */
  public static class Baked<T extends TankModel> extends BakedGuiUniqueModel {
    private final IModelConfiguration owner;
    private final ModelState originalTransforms;
    @SuppressWarnings("WeakerAccess")
    protected final T original;
    private final Cache<FluidStack,BakedModel> cache = CacheBuilder
      .newBuilder()
      .maximumSize(64)
      .build();

    @SuppressWarnings("WeakerAccess")
    protected Baked(IModelConfiguration owner, ModelState transforms, BakedModel baked, BakedModel gui, T original) {
      super(baked, gui);
      this.owner = owner;
      this.originalTransforms = transforms;
      this.original = original;
    }

    @Override
    public ItemOverrides getOverrides() {
      return FluidPartOverride.INSTANCE;
    }

    /**
     * Bakes the model with the given fluid element
     * @param owner        Owner for baking, should include the fluid texture
     * @param baseModel    Base model for original elements
     * @param fluid        Fluid element for baking
     * @param color        Color for the fluid part
     * @param luminosity   Luminosity for the fluid part
     * @return  Baked model
     */
    private BakedModel bakeWithFluid(IModelConfiguration owner, SimpleBlockModel baseModel, BlockElement fluid, int color, int luminosity) {
      // setup for baking, using dynamic location and sprite getter
      Function<Material,TextureAtlasSprite> spriteGetter = Material::sprite;
      TextureAtlasSprite particle = spriteGetter.apply(owner.resolveTexture("particle"));
      SimpleBakedModel.Builder builder = new SimpleBakedModel.Builder(owner.useSmoothLighting(), owner.isSideLit(), owner.isShadedInGui(), owner.getCameraTransforms(), ItemOverrides.EMPTY).particle(particle);
      // first, add all regular elements
      for (BlockElement element : baseModel.getElements()) {
        SimpleBlockModel.bakePart(builder, owner, element, originalTransforms, spriteGetter, BAKE_LOCATION);
      }
      // next, add in the fluid
      ColoredBlockModel.bakePart(builder, owner, fluid, color, luminosity, originalTransforms, spriteGetter, BAKE_LOCATION);
      return builder.build();
    }

    /**
     * Gets the model with the fluid part added
     * @param stack  Fluid stack to add
     * @return  Model with the fluid part
     */
    private BakedModel getModel(FluidStack stack) {
      // fetch fluid data
      FluidAttributes attributes = ((FluidExtensions)stack.getFluid()).getAttributes();
      int color = FluidVariantRendering.getColor(stack.getType());
      int luminosity = attributes.getLuminosity(stack);
      Map<String,Material> textures = ImmutableMap.of(
        "fluid", new Material(TextureAtlas.LOCATION_BLOCKS, FluidVariantRendering.getSprite(stack.getType()).getName()),
        "flowing_fluid", new Material(TextureAtlas.LOCATION_BLOCKS, FluidVariantRendering.getSprites(stack.getType())[1].getName()));
      IModelConfiguration textured = new ExtraTextureConfiguration(owner, textures);

      // add fluid part
      BlockElement fluid = original.fluid.getPart(stack.getAmount(), FluidVariantAttributes.isLighterThanAir(stack.getType()));
      // bake the model
      BakedModel baked = bakeWithFluid(textured, original.model, fluid, color, luminosity);

      // if we have GUI, bake a GUI variant
      if (original.gui != null) {
        baked = new BakedGuiUniqueModel(baked, bakeWithFluid(textured, original.gui, fluid, color, 0));
      }

      // return what we ended up with
      return baked;
    }

    /**
     * Gets a cached model with the fluid part added
     * @param fluid  Scaled contained fluid
     * @return  Cached model
     */
    private BakedModel getCachedModel(FluidStack fluid) {
      try {
        return cache.get(fluid, () -> getModel(fluid));
      }
      catch(ExecutionException e) {
        log.error(e);
        return this;
      }
    }

    /**
     * Gets a cached model with the fluid part added
     * @param fluid     Fluid contained
     * @param capacity  Tank capacity
     * @return  Cached model
     */
    private BakedModel getCachedModel(FluidStack fluid, long capacity) {
      int increments = original.fluid.getIncrements();
      return getCachedModel(new FluidStack(fluid, Mth.clamp(fluid.getAmount() * increments / capacity, 1, increments)));
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<Random> randomSupplier, RenderContext context) {
      if(blockView instanceof RenderAttachedBlockView renderAttachedBlockView && renderAttachedBlockView.getBlockEntityRenderAttachment(pos) instanceof IModelData data) {
        if ((original.forceModelFluid || Config.CLIENT.tankFluidModel.get()) && data.hasProperty(ModelProperties.FLUID_TANK)) {
          FluidTank tank = data.getData(ModelProperties.FLUID_TANK);
          if (tank != null && !tank.getFluid().isEmpty()) {
            ((FabricBakedModel)getCachedModel(tank.getFluid(), tank.getCapacity())).emitBlockQuads(blockView, state, pos, randomSupplier, context);
            return;
          }
        }
      }
      ((FabricBakedModel)wrapped).emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }

    /**
     * Gets the fluid location
     * @return  Fluid location data
     */
    public IncrementalFluidCuboid getFluid() {
      return original.fluid;
    }
  }

  /** Loader for this model */
  public static class Loader implements IModelLoader<TankModel> {
    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {}

    @Override
    public TankModel read(JsonDeserializationContext deserializationContext, JsonObject modelContents) {
      SimpleBlockModel model = SimpleBlockModel.deserialize(deserializationContext, modelContents);
      SimpleBlockModel gui = null;
      if (modelContents.has("gui")) {
        gui = SimpleBlockModel.deserialize(deserializationContext, GsonHelper.getAsJsonObject(modelContents, "gui"));
      }
      IncrementalFluidCuboid fluid = IncrementalFluidCuboid.fromJson(GsonHelper.getAsJsonObject(modelContents, "fluid"));
      boolean forceModelFluid = GsonHelper.getAsBoolean(modelContents, "render_fluid_in_model", false);
      return new TankModel(model, gui, fluid, forceModelFluid);
    }
  }
}
