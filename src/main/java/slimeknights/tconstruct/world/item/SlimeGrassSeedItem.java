package slimeknights.tconstruct.world.item;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import slimeknights.mantle.item.TooltipItem;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerWorld;
import slimeknights.tconstruct.world.block.SlimeVineBlock;
import slimeknights.tconstruct.world.block.SlimeVineBlock.VineStage;

import javax.annotation.Nullable;

public class SlimeGrassSeedItem extends TooltipItem {

  private final SlimeType foliage;

  public SlimeGrassSeedItem(Properties properties, SlimeType foliage) {
    super(properties);
    this.foliage = foliage;
  }

  /**
   * Gets the slime type for the given block
   */
  @Nullable
  private static SlimeType getSlimeType(Block block) {
    for (SlimeType type : SlimeType.values()) {
      if (TinkerWorld.allDirt.get(type) == block) {
        return type;
      }
    }
    return null;
  }

  /**
   * Gets the vines associated with these seeds
   */
  @Nullable
  private Block getVines() {
    switch (this.foliage) {
      case SKY:
        return TinkerWorld.skySlimeVine.get();
      case ENDER:
        return TinkerWorld.enderSlimeVine.get();
    }
    return null;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    BlockPos pos = context.getClickedPos();
    Level world = context.getLevel();
    BlockState state = world.getBlockState(pos);
    BlockState newState = null;

    // try vines first
    if (state.getBlock() == Blocks.VINE) {
      Block slimyVines = this.getVines();
      if (slimyVines != null) {
        // copy over the directions
        newState = slimyVines.defaultBlockState().setValue(SlimeVineBlock.STAGE, VineStage.START);
        for (BooleanProperty prop : VineBlock.PROPERTY_BY_DIRECTION.values()) {
          if (state.getValue(prop)) {
            newState = newState.setValue(prop, true);
          }
        }
      }
    }

    // if vines did not succeed, try grass
    if (newState == null) {
      SlimeType type = getSlimeType(state.getBlock());
      if (type != null) {
        newState = TinkerWorld.slimeGrass.get(type).get(this.foliage).defaultBlockState();
      } else {
        return InteractionResult.PASS;
      }
    }

    // will have a state at this point
    if (!world.isClientSide) {
      world.setBlockAndUpdate(pos, newState);
      world.playSound(null, pos, newState.getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0f, 1.0f);
      Player player = context.getPlayer();
      if (player == null || !player.isCreative()) {
        context.getItemInHand().shrink(1);
      }
    }
    return InteractionResult.SUCCESS;
  }
}
