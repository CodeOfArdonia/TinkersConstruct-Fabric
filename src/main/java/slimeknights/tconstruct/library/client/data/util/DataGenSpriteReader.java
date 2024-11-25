package slimeknights.tconstruct.library.client.data.util;

import com.mojang.blaze3d.platform.NativeImage;
import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;

/**
 * Logic to read sprites from existing images and return native images which can later be modified
 */
@Log4j2
@RequiredArgsConstructor
public class DataGenSpriteReader extends AbstractSpriteReader {

  private final ExistingFileHelper existingFileHelper;
  private final String folder;

  @Override
  public boolean exists(ResourceLocation path) {
    return this.existingFileHelper.exists(path, PackType.CLIENT_RESOURCES, ".png", this.folder);
  }

  @Override
  public NativeImage read(ResourceLocation path) throws IOException {
    try {
      Resource resource = this.existingFileHelper.getResource(path, PackType.CLIENT_RESOURCES, ".png", this.folder);
      NativeImage image = NativeImage.read(resource.open());
      this.openedImages.add(image);
      return image;
    } catch (IOException e) {
      log.error("Failed to read image at {}", path);
      throw e;
    }
  }
}
