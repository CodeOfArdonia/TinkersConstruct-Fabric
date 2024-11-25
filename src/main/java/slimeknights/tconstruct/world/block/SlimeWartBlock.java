package slimeknights.tconstruct.world.block;

import net.minecraft.world.level.block.Block;
import slimeknights.tconstruct.shared.block.SlimeType;

/**
 * Simple block to hide ichor
 */
public class SlimeWartBlock extends Block {

  private final SlimeType foliageType;

  public SlimeWartBlock(Properties properties, SlimeType foliageType) {
    super(properties);
    this.foliageType = foliageType;
  }
}
