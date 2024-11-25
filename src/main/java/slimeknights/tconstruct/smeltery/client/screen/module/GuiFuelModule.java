package slimeknights.tconstruct.smeltery.client.screen.module;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.client.screen.ScalableElementScreen;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule.FuelInfo;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * GUI component handling the fuel module
 */
@RequiredArgsConstructor
public class GuiFuelModule {

  private static final ScalableElementScreen FIRE = new ScalableElementScreen(176, 136, 14, 14, 256, 256);

  // tooltips
  private static final String TOOLTIP_TEMPERATURE = TConstruct.makeTranslationKey("gui", "melting.fuel.temperature");
  private static final List<Component> TOOLTIP_NO_TANK = Collections.singletonList(Component.translatable(TConstruct.makeTranslationKey("gui", "melting.fuel.no_tank")));
  private static final List<Component> TOOLTIP_NO_FUEL = Collections.singletonList(Component.translatable(TConstruct.makeTranslationKey("gui", "melting.fuel.empty")));
  private static final Component TOOLTIP_INVALID_FUEL = Component.translatable(TConstruct.makeTranslationKey("gui", "melting.fuel.invalid")).withStyle(ChatFormatting.RED);
  private static final Component TOOLTIP_SOLID_FUEL = Component.translatable(TConstruct.makeTranslationKey("gui", "melting.fuel.solid"));

  private final AbstractContainerScreen<?> screen;
  private final FuelModule fuelModule;
  /**
   * location to draw the tank
   */
  private final int x, y, width, height;
  /**
   * location to draw the fire
   */
  private final int fireX, fireY;
  /**
   * If true, UI has a fuel slot
   */
  private final boolean hasFuelSlot;

  private FuelInfo fuelInfo = FuelInfo.EMPTY;

  /**
   * Checks if the fuel tank is hovered
   *
   * @param checkX X position to check
   * @param checkY Y position to check
   * @return True if hovered
   */
  private boolean isHovered(int checkX, int checkY) {
    return GuiUtil.isHovered(checkX, checkY, this.x - 1, this.y - 1, this.width + 2, this.height + 2);
  }

  /**
   * Draws the fuel at the correct location
   *
   * @param graphics Gui graphics instance
   * @param texture  The texture to render
   */
  public void draw(GuiGraphics graphics, ResourceLocation texture) {
    // draw fire
    int fuel = this.fuelModule.getFuel();
    int fuelQuality = this.fuelModule.getFuelQuality();
    if (fuel > 0 && fuelQuality > 0) {
      FIRE.drawScaledYUp(graphics, texture, this.fireX + this.screen.leftPos, this.fireY + this.screen.topPos, 14 * fuel / fuelQuality);
    }

    // draw tank second, it changes the image
    // store fuel info into a field for other methods, this one updates most often
    if (!this.hasFuelSlot) {
      this.fuelInfo = this.fuelModule.getFuelInfo();
      if (!this.fuelInfo.isEmpty()) {
        GuiUtil.renderFluidTank(graphics.pose(), this.screen, this.fuelInfo.getFluid(), this.fuelInfo.getTotalAmount(), this.fuelInfo.getCapacity(), this.x, this.y, this.width, this.height, 100);
      }
    }
  }

  /**
   * Highlights the hovered fuel
   *
   * @param graphics Gui graphics instance
   * @param checkX   Top corner relative mouse X
   * @param checkY   Top corner relative mouse Y
   */
  public void renderHighlight(GuiGraphics graphics, int checkX, int checkY) {
    if (this.isHovered(checkX, checkY)) {
      // if there is a fuel slot, render highlight lower
      if (this.hasFuelSlot) {
        if (checkY > this.y + 18) {
          GuiUtil.renderHighlight(graphics, this.x, this.y + 18, this.width, this.height - 18);
        }
      } else {
        // full fluid highlight
        GuiUtil.renderHighlight(graphics, this.x, this.y, this.width, this.height);
      }
    }
  }

  /**
   * Adds the tooltip for the fuel
   *
   * @param graphics Gui graphics instance
   * @param mouseX   Mouse X position
   * @param mouseY   Mouse Y position
   */
  public void addTooltip(GuiGraphics graphics, int mouseX, int mouseY, boolean hasTank) {
    int checkX = mouseX - this.screen.leftPos;
    int checkY = mouseY - this.screen.topPos;

    if (this.isHovered(checkX, checkY)) {
      List<Component> tooltip;
      // if an item or we have a fuel slot, do item tooltip
      if (this.hasFuelSlot || this.fuelInfo.isItem()) {
        // if there is a fuel slot, start below the fuel slot
        if (!this.hasFuelSlot || checkY > this.y + 18) {
          if (hasTank) {
            // no invalid fuel, we assume the slot is validated (hasFuelSlot is only true for the heater which validates)
            int temperature = this.fuelModule.getTemperature();
            if (temperature > 0) {
              tooltip = Arrays.asList(TOOLTIP_SOLID_FUEL, Component.translatable(TOOLTIP_TEMPERATURE, temperature).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            } else {
              tooltip = TOOLTIP_NO_FUEL;
            }
          } else {
            tooltip = TOOLTIP_NO_TANK;
          }
        } else {
          tooltip = Collections.emptyList();
        }
      } else if (!this.fuelInfo.isEmpty()) {
        FluidStack fluid = this.fuelInfo.getFluid();
        tooltip = FluidTooltipHandler.getFluidTooltip(fluid, this.fuelInfo.getTotalAmount());
        int temperature = this.fuelInfo.getTemperature();
        if (temperature > 0) {
          tooltip.add(1, Component.translatable(TOOLTIP_TEMPERATURE, temperature).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
          tooltip.add(1, TOOLTIP_INVALID_FUEL);
        }
      } else {
        tooltip = hasTank ? TOOLTIP_NO_FUEL : TOOLTIP_NO_TANK;
      }

      graphics.renderComponentTooltip(Screens.getTextRenderer(this.screen), tooltip, mouseX, mouseY);
    }
  }

  /**
   * Gets the fluid stack under the mouse
   *
   * @param checkX Mouse X position
   * @param checkY Mouse Y position
   * @return Fluid stack under mouse
   */
  @Nullable
  public FluidStack getIngredient(int checkX, int checkY) {
    if (!this.hasFuelSlot && this.isHovered(checkX, checkY) && !this.fuelInfo.isEmpty()) {
      return this.fuelInfo.getFluid();
    }
    return null;
  }
}
