package slimeknights.tconstruct.tools.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.data.ISafeManagerReloadListener;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.materials.definition.MaterialVariantId;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.material.MaterialIds;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class PlateArmorModel extends Model {

  /**
   * Singleton model instance, all data is passed in via setters
   */
  private static final Map<ResourceLocation, PlateArmorModel> MODELS = new HashMap<>();
  /**
   * Cached constructor to not need to create each tick
   */
  private static final Function<ResourceLocation, PlateArmorModel> CONSTRUCTOR = PlateArmorModel::new;
  /**
   * default name
   */
  private static final ResourceLocation PLATE = TConstruct.getResource("plate");

  /**
   * Listener to clear caches
   */
  public static final ISafeManagerReloadListener RELOAD_LISTENER = ISafeManagerReloadListener.create(TConstruct.getResource("plate_armor_cache"), manager -> MODELS.clear());

  /**
   * Gets the model for a given entity
   */
  public static Model getModel(ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel, ResourceLocation name) {
    PlateArmorModel model = MODELS.computeIfAbsent(name, CONSTRUCTOR);
    model.setup(baseModel, stack, slot);
    return model;
  }

  /**
   * Gets the model for a given entity
   */
  public static Model getModel(ItemStack stack, EquipmentSlot slot, HumanoidModel<?> baseModel) {
    return getModel(stack, slot, baseModel, PLATE);
  }

  /**
   * Cache of armor render types
   */
  private final Map<String, RenderType> ARMOR_RENDER_CACHE = new HashMap<>();
  /**
   * Cache of leg render types
   */
  private final Map<String, RenderType> LEG_RENDER_CACHE = new HashMap<>();
  /**
   * Function to get armor render type
   */
  private final Function<String, RenderType> ARMOR_GETTER = mat -> RenderType.entityCutoutNoCullZOffset(this.getArmorTexture(mat, 1));
  /**
   * Function to get armor render type
   */
  private final Function<String, RenderType> LEG_GETTER = mat -> RenderType.entityCutoutNoCullZOffset(this.getArmorTexture(mat, 2));

  private final ResourceLocation name;
  @Nullable
  private HumanoidModel<?> base;
  private String material = "";
  private boolean isLegs = false;
  /**
   * If true, applies the enchantment glint to extra layers
   */
  private boolean hasGlint = false;

  public PlateArmorModel(ResourceLocation name) {
    super(RenderType::entityCutoutNoCull);
    this.name = name;
  }

  @Override
  public void renderToBuffer(PoseStack matrices, VertexConsumer bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha) {
    if (this.base != null) {
      this.base.renderToBuffer(matrices, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
      if (!this.material.isEmpty() && ArmorModelHelper.buffer != null) {
        VertexConsumer overlayBuffer = ItemRenderer.getArmorFoilBuffer(ArmorModelHelper.buffer, this.isLegs ? this.LEG_RENDER_CACHE.computeIfAbsent(this.material, this.LEG_GETTER) : this.ARMOR_RENDER_CACHE.computeIfAbsent(this.material, this.ARMOR_GETTER), false, this.hasGlint);
        this.base.renderToBuffer(matrices, overlayBuffer, packedLightIn, packedOverlayIn, red, green, blue, alpha);
      }
    }
  }

  /**
   * Gets the armor texture for a material
   */
  private ResourceLocation getArmorTexture(String material, int variant) {
    MaterialVariantId variantId = MaterialVariantId.tryParse(material);
    if (variantId == null) {
      variantId = MaterialIds.cobalt;
    }
    ResourceLocation location = variantId.getLocation('_');
    return new ResourceLocation(this.name.getNamespace(), String.format("textures/models/armor/%s/layer_%d_%s_%s.png", this.name.getPath(), variant, location.getNamespace(), location.getPath()));
  }

  private void setup(HumanoidModel<?> base, ItemStack stack, EquipmentSlot slot) {
    this.base = base;
    if (ModifierUtil.getModifierLevel(stack, TinkerModifiers.golden.getId()) > 0) {
      this.material = MaterialIds.gold.toString();
    } else {
      this.material = ModifierUtil.getPersistentString(stack, TinkerModifiers.embellishment.getId());
    }
    this.isLegs = slot == EquipmentSlot.LEGS;
    this.hasGlint = stack.hasFoil();
  }
}
