package slimeknights.tconstruct.tables.block.entity.inventory;

import io.github.fabricators_of_create.porting_lib.util.INBTSerializable;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.nbt.CompoundTag;
import slimeknights.mantle.block.entity.MantleBlockEntity;

/** Interface for tinker chest TEs */
public interface IChestItemHandler extends Storage<ItemVariant>, INBTSerializable<CompoundTag>, IScalingContainer {
  /** Sets the parent of this block */
  void setParent(MantleBlockEntity parent);
}
