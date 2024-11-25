package slimeknights.tconstruct.world.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BuddingAmethystBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import slimeknights.tconstruct.common.registration.GeodeItemObject;
import slimeknights.tconstruct.common.registration.GeodeItemObject.BudSize;

public class BuddingCrystalBlock extends CrystalBlock {

  private static final Direction[] DIRECTIONS = Direction.values();

  private final GeodeItemObject geode;

  public BuddingCrystalBlock(GeodeItemObject geode, SoundEvent chimeSound, Properties props) {
    super(chimeSound, props);
    this.geode = geode;
  }

  @Override
  public void randomTick(BlockState pState, ServerLevel pLevel, BlockPos pPos, RandomSource pRandom) {
    if (pRandom.nextInt(5) == 0) {
      Direction direction = DIRECTIONS[pRandom.nextInt(DIRECTIONS.length)];
      BlockPos blockpos = pPos.relative(direction);
      BlockState blockstate = pLevel.getBlockState(blockpos);
      Block block = null;
      if (BuddingAmethystBlock.canClusterGrowAtState(blockstate)) {
        block = this.geode.getBud(BudSize.SMALL);
      } else {
        for (BudSize size : BudSize.SIZES) {
          if (blockstate.is(this.geode.getBud(size))) {
            block = this.geode.getBud(size.getNext());
            break;
          }
        }
      }
      if (block != null) {
        BlockState state = block.defaultBlockState().setValue(AmethystClusterBlock.FACING, direction).setValue(AmethystClusterBlock.WATERLOGGED, blockstate.getFluidState().getType() == Fluids.WATER);
        pLevel.setBlockAndUpdate(blockpos, state);
      }
    }
  }
}
