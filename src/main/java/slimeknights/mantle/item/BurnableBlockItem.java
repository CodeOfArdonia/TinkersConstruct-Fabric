package slimeknights.mantle.item;

import net.fabricmc.fabric.api.registry.FuelRegistry;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

public class BurnableBlockItem extends BlockItem {

  public BurnableBlockItem(Block blockIn, Properties builder, int burnTime) {
    super(blockIn, builder);
    FuelRegistry.INSTANCE.add(this, burnTime);
  }
}
