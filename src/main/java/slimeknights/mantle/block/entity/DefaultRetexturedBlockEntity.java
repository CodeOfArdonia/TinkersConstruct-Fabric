package slimeknights.mantle.block.entity;

import io.github.fabricators_of_create.porting_lib.common.util.Lazy;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.block.RetexturedBlock;
import slimeknights.mantle.client.model.data.IModelData;
import slimeknights.mantle.util.RetexturedHelper;

import javax.annotation.Nonnull;

import static slimeknights.mantle.util.RetexturedHelper.TAG_TEXTURE;

/**
 * Standard implementation for {@link IRetexturedBlockEntity}, use alongside {@link RetexturedBlock} and {@link slimeknights.mantle.item.RetexturedBlockItem}
 */
public class DefaultRetexturedBlockEntity extends MantleBlockEntity implements IRetexturedBlockEntity {

  private final Lazy<IModelData> data = Lazy.of(this::getRetexturedModelData);
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;

  public DefaultRetexturedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Nonnull
  @Override
  public IModelData getRenderData() {
    return this.data.get();
  }

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(this.texture);
  }

  @Override
  public void updateTexture(String name) {
    Block oldTexture = this.texture;
    this.texture = RetexturedHelper.getBlock(name);
    if (oldTexture != this.texture) {
      this.setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  protected void saveSynced(CompoundTag tags) {
    super.saveSynced(tags);
    if (this.texture != Blocks.AIR) {
      tags.putString(TAG_TEXTURE, this.getTextureName());
    }
  }

  @Override
  public void load(CompoundTag tags) {
    super.load(tags);
    if (tags.contains(TAG_TEXTURE, Tag.TAG_STRING)) {
      this.texture = RetexturedHelper.getBlock(tags.getString(TAG_TEXTURE));
      RetexturedHelper.onTextureUpdated(this);
    }
  }
}
