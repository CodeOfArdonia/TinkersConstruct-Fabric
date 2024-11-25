package slimeknights.tconstruct.plugin.rei.entity;

import lombok.RequiredArgsConstructor;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.entry.renderer.EntryRenderer;
import me.shedaniel.rei.api.client.gui.widgets.Tooltip;
import me.shedaniel.rei.api.client.gui.widgets.TooltipContext;
import me.shedaniel.rei.api.common.entry.EntryStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.plugin.jei.entity.EntityMeltingRecipeCategory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Renderer for entity type ingredients
 */
@SuppressWarnings("rawtypes")
@RequiredArgsConstructor
public class EntityEntryRenderer implements EntryRenderer<EntityType> {

  public static final EntityEntryRenderer INSTANCE = new EntityEntryRenderer(16);

  /**
   * Entity types that will not render, as they either errored or are the wrong type
   */
  private static final Set<EntityType<?>> IGNORED_ENTITIES = new HashSet<>();

  /**
   * Square size of the renderer in pixels
   */
  private final int size;

  /**
   * Cache of entities for each entity type
   */
  private final Map<EntityType<?>, Entity> ENTITY_MAP = new HashMap<>();

  @Override
  public void render(EntryStack<EntityType> entry, GuiGraphics graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
    graphics.pose().pushPose();
    graphics.pose().translate(bounds.getCenterX() - this.size / 2, bounds.getCenterY() - this.size / 2, 0);
    Level world = Minecraft.getInstance().level;
    EntityType type = entry.getValue();
    if (world != null && !IGNORED_ENTITIES.contains(type)) {
      Entity entity;
      // players cannot be created using the type, but we can use the client player
      // side effect is it renders armor/items
      if (type == EntityType.PLAYER) {
        entity = Minecraft.getInstance().player;
      } else {
        // entity is created with the client world, but the entity map is thrown away when JEI restarts so they should be okay I think
        entity = this.ENTITY_MAP.computeIfAbsent(type, t -> t.create(world));
      }
      // only can draw living entities, plus non-living ones don't get recipes anyways
      if (entity instanceof LivingEntity livingEntity) {
        // scale down large mobs, but don't scale up small ones
        int scale = this.size / 2;
        float height = entity.getBbHeight();
        float width = entity.getBbWidth();
        if (height > 2 || width > 2) {
          scale = (int) (this.size / Math.max(height, width));
        }
        // catch exceptions drawing the entity to be safe, any caught exceptions blacklist the entity
        try {
          InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, this.size / 2, this.size, scale, 0, 10, livingEntity);
          graphics.pose().popPose();
          return;
        } catch (Exception e) {
          TConstruct.LOG.error("Error drawing entity " + BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
          IGNORED_ENTITIES.add(type);
          this.ENTITY_MAP.remove(type);
        }
      } else {
        // not living, so might as well skip next time
        IGNORED_ENTITIES.add(type);
        this.ENTITY_MAP.remove(type);
      }
    }

    // fallback, draw a pink and black "spawn egg"
    RenderUtils.setup(EntityMeltingRecipeCategory.BACKGROUND_LOC);
    int offset = (this.size - 16) / 2;
    graphics.blit(EntityMeltingRecipeCategory.BACKGROUND_LOC, offset, offset, 149f, 58f, 16, 16, 256, 256);
    graphics.pose().popPose();
  }

  @Override
  public Tooltip getTooltip(EntryStack<EntityType> entry, TooltipContext context) {
    List<Component> tooltip = new ArrayList<>();
    tooltip.add(entry.getValue().getDescription());
    if (context.getFlag().isAdvanced()) {
      tooltip.add((Component.literal(Objects.requireNonNull(BuiltInRegistries.ENTITY_TYPE.getKey(entry.getValue())).toString())).withStyle(ChatFormatting.DARK_GRAY));
    }
    return Tooltip.create(tooltip);
  }
}
