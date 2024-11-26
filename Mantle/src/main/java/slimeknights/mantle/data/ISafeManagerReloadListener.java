package slimeknights.mantle.data;

import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import slimeknights.mantle.Mantle;
import slimeknights.mantle.data.fabric.IdentifiableISafeManagerReloadListener;

import java.util.function.Consumer;

/**
 * Same as {@link ResourceManagerReloadListener}, but only runs if the mod loader state is valid, used as client resource listeners can cause a misleading crash report if something else throws.
 * TODO 1.19: move to {@code slimeknights.data.listener}
 */
public interface ISafeManagerReloadListener extends ResourceManagerReloadListener, IdentifiableResourceReloadListener {
  @Override
  default void onResourceManagerReload(ResourceManager resourceManager) {
//    if (ModLoader.isLoadingStateValid()) {
      onReloadSafe(resourceManager);
//    }
  }

  /**
   * Safely handle a resource manager reload. Only runs if the mod loading state is valid
   * @param resourceManager  Resource manager
   */
  void onReloadSafe(ResourceManager resourceManager);

  static ISafeManagerReloadListener create(ResourceLocation id, Consumer<ResourceManager> managerReloadListener) {
    return new IdentifiableISafeManagerReloadListener(id) {
      @Override
      public void onReloadSafe(ResourceManager resourceManager) {
        managerReloadListener.accept(resourceManager);
      }
    };
  }
}
