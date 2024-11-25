package slimeknights.tconstruct.tables.block.entity.table;

import io.github.fabricators_of_create.porting_lib.block.CustomRenderBoundingBoxBlockEntity;
import io.github.fabricators_of_create.porting_lib.common.util.Lazy;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.client.model.data.IModelData;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.shared.block.entity.TableBlockEntity;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class RetexturedTableBlockEntity extends TableBlockEntity implements IRetexturedBlockEntity, CustomRenderBoundingBoxBlockEntity {

  private static final String TAG_TEXTURE = "texture";

  private final Lazy<IModelData> data = Lazy.of(this::getRetexturedModelData);
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;

  public RetexturedTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Component name, int size) {
    super(type, pos, state, name, size);
  }

  @Override
  public AABB getRenderBoundingBox() {
    return new AABB(this.worldPosition, this.worldPosition.offset(1, 2, 1));
  }


  /* Textures */

  @Nonnull
  @Override
  public IModelData getRenderData() {
    return this.data.get();
  }

  @Override
  public String getTextureName() {
    if (this.texture == Blocks.AIR) {
      return "";
    }
    return Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(this.texture)).toString();
  }

  private void textureUpdated() {
    // update the texture in BE data
    if (this.level != null && this.level.isClientSide) {
      Block normalizedTexture = this.texture == Blocks.AIR ? null : this.texture;
      IModelData data = this.getRenderData();
      if (data.getData(RetexturedHelper.BLOCK_PROPERTY) != normalizedTexture) {
        data.setData(RetexturedHelper.BLOCK_PROPERTY, normalizedTexture);
//        requestModelDataUpdate();
        BlockState state = this.getBlockState();
        this.level.sendBlockUpdated(this.worldPosition, state, state, 0);
      }
    }
  }

  @Override
  public void updateTexture(String name) {
    Block oldTexture = this.texture;
    this.texture = RetexturedHelper.getBlock(name);
    if (oldTexture != this.texture) {
      this.setChangedFast();
      this.textureUpdated();
    }
  }

  @Override
  public void saveSynced(CompoundTag tags) {
    super.saveSynced(tags);
    if (this.texture != Blocks.AIR) {
      tags.putString(TAG_TEXTURE, Objects.requireNonNull(BuiltInRegistries.BLOCK.getKey(this.texture)).toString());
    }
  }

  @Override
  public void load(CompoundTag tags) {
    super.load(tags);
    if (tags.contains(TAG_TEXTURE, Tag.TAG_STRING)) {
      this.texture = RetexturedHelper.getBlock(tags.getString(TAG_TEXTURE));
      this.textureUpdated();
      // legacy fallback for anyone who ported from 1.16 (though I doubt that would work). Remove sometime later in 1.18
    } else if (tags.contains("ForgeData", Tag.TAG_COMPOUND)) {
      CompoundTag forgeData = tags.getCompound("ForgeData");
      if (forgeData.contains(TAG_TEXTURE, Tag.TAG_STRING)) {
        this.texture = RetexturedHelper.getBlock(forgeData.getString(TAG_TEXTURE));
        this.textureUpdated();
        forgeData.remove(TAG_TEXTURE);
      }
    }
  }

  @Override
  public CompoundTag getTileData() {
    return this.getCustomData();
  }
}
