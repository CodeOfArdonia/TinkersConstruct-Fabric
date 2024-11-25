package slimeknights.tconstruct.smeltery.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import slimeknights.mantle.client.screen.ElementScreen;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.smeltery.block.entity.controller.MelterBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;
import slimeknights.tconstruct.smeltery.client.screen.module.GuiFuelModule;
import slimeknights.tconstruct.smeltery.client.screen.module.GuiMeltingModule;
import slimeknights.tconstruct.smeltery.client.screen.module.GuiTankModule;
import slimeknights.tconstruct.smeltery.menu.MelterContainerMenu;

import javax.annotation.Nullable;

public class MelterScreen extends AbstractContainerScreen<MelterContainerMenu> implements IScreenWithFluidTank {

  private static final ResourceLocation BACKGROUND = TConstruct.getResource("textures/gui/melter.png");
  private static final ElementScreen SCALA = new ElementScreen(176, 0, 52, 52, 256, 256);
  private static final ElementScreen FUEL_SLOT = new ElementScreen(176, 52, 18, 36, 256, 256);
  private static final ElementScreen FUEL_TANK = new ElementScreen(194, 52, 14, 38, 256, 256);

  private final GuiMeltingModule melting;
  private final GuiFuelModule fuel;
  private final GuiTankModule tank;

  public MelterScreen(MelterContainerMenu container, Inventory inv, Component name) {
    super(container, inv, name);
    MelterBlockEntity te = container.getTile();
    if (te != null) {
      FuelModule fuelModule = te.getFuelModule();
      this.melting = new GuiMeltingModule(this, te.getMeltingInventory(), fuelModule::getTemperature, slot -> true);
      this.fuel = new GuiFuelModule(this, fuelModule, 153, 32, 12, 36, 152, 15, container.isHasFuelSlot());
      this.tank = new GuiTankModule(this, te.getTank(), 90, 16, 52, 52, MelterContainerMenu.TOOLTIP_FORMAT);
    } else {
      this.melting = null;
      this.fuel = null;
      this.tank = null;
    }
  }

  @Override
  public void render(GuiGraphics graphics, int x, int y, float partialTicks) {
    this.renderBackground(graphics);
    super.render(graphics, x, y, partialTicks);
    this.renderTooltip(graphics, x, y);
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
    GuiUtil.drawBackground(graphics, this, BACKGROUND);

    // fuel
    if (this.fuel != null) {
      // draw the correct background for the fuel type
      if (this.menu.isHasFuelSlot()) {
        FUEL_SLOT.draw(graphics, BACKGROUND, this.leftPos + 150, this.topPos + 31);
      } else {
        FUEL_TANK.draw(graphics, BACKGROUND, this.leftPos + 152, this.topPos + 31);
      }
      this.fuel.draw(graphics, BACKGROUND);
    }

    // fluids
    if (this.tank != null) this.tank.draw(graphics);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);
    int checkX = mouseX - this.leftPos;
    int checkY = mouseY - this.topPos;

    // highlight hovered tank
    if (this.tank != null) this.tank.highlightHoveredFluid(graphics, checkX, checkY);
    // highlight hovered fuel
    if (this.fuel != null) this.fuel.renderHighlight(graphics, checkX, checkY);

    // scala
    RenderUtils.setup(BACKGROUND);
    SCALA.draw(graphics, BACKGROUND, 90, 16);

    // heat bars
    if (this.melting != null) {
      this.melting.drawHeatBars(graphics, BACKGROUND);
    }
  }

  @Override
  protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderTooltip(graphics, mouseX, mouseY);

    // tank tooltip
    if (this.tank != null) this.tank.renderTooltip(graphics, mouseX, mouseY);

    // heat tooltips
    if (this.melting != null) this.melting.drawHeatTooltips(graphics, mouseX, mouseY);

    // fuel tooltip
    if (this.fuel != null) this.fuel.addTooltip(graphics, mouseX, mouseY, true);
  }

  @Nullable
  @Override
  public Object getIngredientUnderMouse(double mouseX, double mouseY) {
    Object ingredient = null;
    int checkX = (int) mouseX - this.leftPos;
    int checkY = (int) mouseY - this.topPos;

    // try fuel first, its faster
    if (this.fuel != null)
      ingredient = this.fuel.getIngredient(checkX, checkY);

    if (this.tank != null && ingredient == null)
      ingredient = this.tank.getIngreientUnderMouse(checkX, checkY);

    return ingredient;
  }
}
