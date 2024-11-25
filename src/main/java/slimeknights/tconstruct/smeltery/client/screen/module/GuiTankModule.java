package slimeknights.tconstruct.smeltery.client.screen.module;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.Getter;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.fluid.tooltip.FluidTooltipHandler;
import slimeknights.tconstruct.library.client.GuiUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Module handling the melter tank UI display
 */
public class GuiTankModule {

  /**
   * Tooltip for when the capacity is 0, it breaks some stuff
   */
  private static final Component NO_CAPACITY = Component.translatable(Mantle.makeDescriptionId("gui", "fluid.millibucket"), 0).withStyle(ChatFormatting.GRAY);

  private static final int TANK_INDEX = 0;
  private final AbstractContainerScreen<?> screen;
  private final SlottedStorage<FluidVariant> tank;
  @Getter
  private final int x, y, width, height;
  private final BiConsumer<Long, List<Component>> formatter;

  public GuiTankModule(AbstractContainerScreen<?> screen, SlottedStorage<FluidVariant> tank, int x, int y, int width, int height, ResourceLocation tooltipId) {
    this.screen = screen;
    this.tank = tank;
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    this.formatter = (amount, tooltip) -> FluidTooltipHandler.appendNamedList(tooltipId, amount, tooltip);
  }

  /**
   * Checks if the tank is hovered over
   *
   * @param checkX Screen relative mouse X
   * @param checkY Screen relative mouse Y
   * @return True if hovered
   */
  private boolean isHovered(int checkX, int checkY) {
    return GuiUtil.isHovered(checkX, checkY, this.x - 1, this.y - 1, this.width + 2, this.height + 2);
  }

  /**
   * Gets the height of the fluid in pixels
   *
   * @return Fluid height
   */
  private long getFluidHeight() {
    long capacity = this.tank.getSlot(TANK_INDEX).getCapacity();
    if (capacity == 0) {
      return this.height;
    }
    return this.height * this.tank.getSlot(TANK_INDEX).getAmount() / capacity;
  }

  /**
   * Draws the tank
   *
   * @param graphics Gui graphics instance
   */
  public void draw(GuiGraphics graphics) {
    GuiUtil.renderFluidTank(graphics.pose(), this.screen, new FluidStack(this.tank.getSlot(TANK_INDEX)), this.tank.getSlot(TANK_INDEX).getCapacity(), this.x, this.y, this.width, this.height, 100);
  }

  /**
   * Highlights the hovered fluid
   *
   * @param graphics Gui graphics instance
   * @param checkX   Mouse X position, screen relative
   * @param checkY   Mouse Y position, screen relative
   */
  public void highlightHoveredFluid(GuiGraphics graphics, int checkX, int checkY) {
    // highlight hovered fluid
    if (this.isHovered(checkX, checkY)) {
      long fluidHeight = this.getFluidHeight();
      long middle = this.y + this.height - fluidHeight;

      // highlight just fluid
      if (checkY > middle) {
        GuiUtil.renderHighlight(graphics, this.x, (int) middle, this.width, (int) fluidHeight);
      } else {
        // or highlight empty
        GuiUtil.renderHighlight(graphics, this.x, this.y, this.width, (int) (this.height - fluidHeight));
      }
    }
  }

  /**
   * Renders the tooltip for hovering over the tank
   *
   * @param graphics Gui graphics instance
   * @param mouseX   Global mouse X position
   * @param mouseY   Global mouse Y position
   */
  public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    int checkX = mouseX - this.screen.leftPos;
    int checkY = mouseY - this.screen.topPos;

    if (this.isHovered(checkX, checkY)) {
      FluidStack fluid = new FluidStack(this.tank.getSlot(TANK_INDEX));
      long amount = fluid.getAmount();
      long capacity = this.tank.getSlot(TANK_INDEX).getCapacity();

      // if hovering over the fluid, display with name
      final List<Component> tooltip;
      if (capacity > 0 && checkY > (this.y + this.height) - this.getFluidHeight()) {
        tooltip = FluidTooltipHandler.getFluidTooltip(fluid);
      } else {
        // function to call for amounts
        BiConsumer<Long, List<Component>> formatter = Screen.hasShiftDown()
          ? FluidTooltipHandler.BUCKET_FORMATTER
          : this.formatter;

        // add tooltips
        tooltip = new ArrayList<>();
        tooltip.add(GuiSmelteryTank.TOOLTIP_CAPACITY);
        if (capacity == 0) {
          tooltip.add(NO_CAPACITY);
        } else {
          formatter.accept(capacity, tooltip);
          if (capacity != amount) {
            tooltip.add(GuiSmelteryTank.TOOLTIP_AVAILABLE);
            formatter.accept(capacity - amount, tooltip);
          }
          // add shift message
          FluidTooltipHandler.appendShift(tooltip);
        }
      }

      // TODO: renderComponentTooltip->renderTooltip
      graphics.renderComponentTooltip(Screens.getTextRenderer(this.screen), tooltip, mouseX, mouseY);
    }
  }

  /**
   * Gets the fluid stack under the mouse
   *
   * @param checkX X position to check
   * @param checkY Y position to check
   * @return Fluid stack under mouse
   */
  @Nullable
  public FluidStack getIngreientUnderMouse(int checkX, int checkY) {
    if (this.isHovered(checkX, checkY) && checkY > (this.y + this.height) - this.getFluidHeight()) {
      return new FluidStack(this.tank.getSlot(TANK_INDEX));
    }
    return null;
  }

}
