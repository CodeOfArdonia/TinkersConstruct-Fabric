package slimeknights.tconstruct.smeltery.client.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import slimeknights.mantle.client.screen.ElementScreen;
import slimeknights.mantle.client.screen.MultiModuleScreen;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.GuiUtil;
import slimeknights.tconstruct.library.client.RenderUtils;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.entity.controller.HeatingStructureBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;
import slimeknights.tconstruct.smeltery.client.screen.module.GuiFuelModule;
import slimeknights.tconstruct.smeltery.client.screen.module.GuiMeltingModule;
import slimeknights.tconstruct.smeltery.client.screen.module.GuiSmelteryTank;
import slimeknights.tconstruct.smeltery.client.screen.module.HeatingStructureSideInventoryScreen;
import slimeknights.tconstruct.smeltery.menu.HeatingStructureContainerMenu;

import javax.annotation.Nullable;
import java.util.Objects;

public class HeatingStructureScreen extends MultiModuleScreen<HeatingStructureContainerMenu> implements IScreenWithFluidTank {

  public static final ResourceLocation BACKGROUND = TConstruct.getResource("textures/gui/smeltery.png");
  private static final ElementScreen SCALA = new ElementScreen(176, 76, 52, 52, 256, 256);

  private final HeatingStructureSideInventoryScreen sideInventory;
  private final HeatingStructureBlockEntity te;
  private final GuiSmelteryTank tank;
  public final GuiMeltingModule melting;
  private final GuiFuelModule fuel;

  public HeatingStructureScreen(HeatingStructureContainerMenu container, Inventory playerInventory, Component title) {
    super(container, playerInventory, title);

    HeatingStructureBlockEntity te = container.getTile();
    if (te != null) {
      this.te = te;
      this.tank = new GuiSmelteryTank(this, te.getTank(), 8, 16, SCALA.w, SCALA.h, Objects.requireNonNull(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(te.getType())));
      int slots = te.getMeltingInventory().getSlotCount();
      this.sideInventory = new HeatingStructureSideInventoryScreen(this, container.getSideInventory(), playerInventory, slots, HeatingStructureContainerMenu.calcColumns(slots));
      this.addModule(this.sideInventory);
      FuelModule fuelModule = te.getFuelModule();
      this.melting = new GuiMeltingModule(this, te.getMeltingInventory(), fuelModule::getTemperature, this.sideInventory::shouldDrawSlot);
      this.fuel = new GuiFuelModule(this, fuelModule, 71, 32, 12, 36, 70, 15, false);
    } else {
      this.te = null;
      this.tank = null;
      this.melting = null;
      this.fuel = null;
      this.sideInventory = null;
    }
  }


  @Override
  protected void containerTick() {
    super.containerTick();
    // if the smeltery becomes invalid or the slot size changes, kill the UI
    if (this.te == null || !this.te.getBlockState().getValue(ControllerBlock.IN_STRUCTURE)
      || this.te.getMeltingInventory().getSlotCount() != this.sideInventory.getSlotCount()) {
      this.onClose();
    }
  }

  @Override
  protected void renderBg(GuiGraphics graphics, float partialTicks, int mouseX, int mouseY) {
    // draw stuff with background
    GuiUtil.drawBackground(graphics, this, BACKGROUND);
    // fuel
    if (this.fuel != null) {
      this.fuel.draw(graphics, BACKGROUND);
    }

    // draw other components
    super.renderBg(graphics, partialTicks, mouseX, mouseY);

    // render fluids
    if (this.tank != null) this.tank.renderFluids(graphics);
  }

  @Override
  protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderLabels(graphics, mouseX, mouseY);

    assert this.minecraft != null;
    RenderUtils.setup(BACKGROUND);
    SCALA.draw(graphics, BACKGROUND, 8, 16);

    // highlight hovered fluids
    if (this.tank != null) this.tank.renderHighlight(graphics, mouseX, mouseY);
    if (this.fuel != null) this.fuel.renderHighlight(graphics, mouseX - this.leftPos, mouseY - this.topPos);

    // while this might make sense to draw in the side inventory logic, slots are rendered by the parent screen it seems
    // so we get the most accurate offset rendering it here, as we offset the foreground of submodules but they don't draw their own slots
    // I hate the whole multimodule system right now
    if (this.melting != null) this.melting.drawHeatBars(graphics, BACKGROUND);
  }

  @Override
  protected void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    super.renderTooltip(graphics, mouseX, mouseY);

    // fluid tooltips
    if (this.tank != null) this.tank.drawTooltip(graphics, mouseX, mouseY);
    if (this.fuel != null) {
      boolean hasTank = false;
      if (this.te.getStructure() != null) {
        hasTank = this.te.getStructure().hasTanks();
      }
      this.fuel.addTooltip(graphics, mouseX, mouseY, hasTank);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
    if (mouseButton == 0 && this.tank != null) {
      this.tank.handleClick((int) mouseX - this.cornerX, (int) mouseY - this.cornerY);
    }
    return super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Nullable
  @Override
  public Object getIngredientUnderMouse(double mouseX, double mouseY) {
    Object ingredient = null;

    int checkX = (int) mouseX - this.cornerX;
    int checkY = (int) mouseY - this.cornerY;

    // try fuel first, its faster
    if (this.fuel != null) ingredient = this.fuel.getIngredient(checkX, checkY);
    // then try tank
    if (this.tank != null && ingredient == null) ingredient = this.tank.getIngredient(checkX, checkY);

    return ingredient;
  }
}
