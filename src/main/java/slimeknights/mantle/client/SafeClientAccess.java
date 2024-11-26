package slimeknights.mantle.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

/**
 * Class to add one level of static indirection to client only lookups
 */
public class SafeClientAccess {

  /**
   * Gets the currently pressed key for tooltips, returns UNKNOWN on a server
   */
  public static TooltipKey getTooltipKey() {
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      return ClientOnly.getPressedKey();
    }
    return TooltipKey.UNKNOWN;
  }

  /**
   * Gets the client player entity, or null on a server
   */
  @Nullable
  public static Player getPlayer() {
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      return ClientOnly.getClientPlayer();
    }
    return null;
  }

  /**
   * Gets the client player entity, or null on a server
   */
  @Nullable
  public static Level getLevel() {
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      return ClientOnly.getClientLevel();
    }
    return null;
  }

  /**
   * This class is only loaded on the client, so is safe to reference client only methods
   */
  private static class ClientOnly {

    /**
     * Gets the currently pressed key modifier for tooltips
     */
    public static TooltipKey getPressedKey() {
      if (Screen.hasShiftDown()) {
        return TooltipKey.SHIFT;
      }
      if (Screen.hasControlDown()) {
        return TooltipKey.CONTROL;
      }
      if (Screen.hasAltDown()) {
        return TooltipKey.ALT;
      }
      return TooltipKey.NORMAL;
    }

    /**
     * Gets the client player instance
     */
    @Nullable
    public static Player getClientPlayer() {
      return Minecraft.getInstance().player;
    }

    /**
     * Gets the client level instance
     */
    @Nullable
    public static Level getClientLevel() {
      return Minecraft.getInstance().level;
    }
  }
}
