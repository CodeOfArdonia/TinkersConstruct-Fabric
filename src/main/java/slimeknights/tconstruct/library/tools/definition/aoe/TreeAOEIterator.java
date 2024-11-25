package slimeknights.tconstruct.library.tools.definition.aoe;

import com.google.common.collect.AbstractIterator;
import com.google.gson.JsonObject;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.data.GenericLoaderRegistry.IGenericLoader;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.utils.JsonUtils;
import slimeknights.tconstruct.tools.TinkerModifiers;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;

/**
 * Tree harvest logic that destroys a tree
 */
@RequiredArgsConstructor
public class TreeAOEIterator implements IAreaOfEffectIterator {

  public static final Loader LOADER = new Loader();

  /**
   * Max distance between the branch and the trunk
   */
  private static final int MAX_BRANCH_DISTANCE = 10;
  /**
   * Absolute distance to the left or right to mine, 0 or more
   */
  private final int width;
  /**
   * How far back to mine into the tree beyond the first block, 0 or more
   */
  private final int depth;

  @Override
  public IGenericLoader<? extends IAreaOfEffectIterator> getLoader() {
    return LOADER;
  }

  @Override
  public Iterable<BlockPos> getBlocks(IToolStackView tool, ItemStack stack, Player player, BlockState state, Level world, BlockPos origin, Direction sideHit, AOEMatchType matchType) {
    int expanded = tool.getModifierLevel(TinkerModifiers.expanded.getId());
    return calculate(tool, stack, player, state, world, origin, sideHit, this.width + (expanded + 1) / 2, this.depth + expanded / 2, matchType);
  }

  /**
   * Gets an iterator, either for a tree, or falling back to a cube
   *
   * @param tool       Tool used to mine the block
   * @param stack      Stack used to mine the block
   * @param player     Player instance
   * @param state      State being mined
   * @param world      World instance
   * @param origin     AOE origin
   * @param sideHit    Block side hit
   * @param extraWidth Mining width
   * @param extraDepth Mining depth
   * @param matchType  Match type to use when not a tree
   * @return Correct iterator for the targeted block
   */
  public static Iterable<BlockPos> calculate(IToolStackView tool, ItemStack stack, Player player, BlockState state, Level world, BlockPos origin, Direction sideHit, int extraWidth, int extraDepth, AOEMatchType matchType) {
    Direction depthDir;
    Direction widthDir;
    // if we have expanders, add them in
    if (extraDepth > 0 || extraWidth > 0) {
      // if hit the top or bottom, use facing direction
      if (sideHit.getAxis().isVertical()) {
        depthDir = player.getDirection();
      } else {
        depthDir = sideHit.getOpposite();
      }
      widthDir = depthDir.getClockWise();
    } else {
      depthDir = Direction.UP;
      widthDir = Direction.UP;
    }
    // TODO: would be nice to allow the stipped logs here as well as the logs
    return () -> new TreeIterator(world, state.getBlock(), origin, widthDir, extraWidth, depthDir, extraDepth);
  }

  /**
   * Iterator that continues up until the block does not match.
   * The way this works is it starts with the given dimensions to form the trunk. The trunk can then extend off into branches in the vertical or in horizontal directions.
   * <p>
   * Horizontal branches detect in a 3x2x2 area from the stored direction and upwards and don't care about whether the logs have a block below. They also can only split into up to 3 pieces
   * Vertical branches check a 3x3x1 cross shape above, requiring nothing to be below the block. They can split into up to 5 pieces
   * The trunk can start new branches within a 3x3x1 square area, again requiring nothing to be below the block
   */
  public static class TreeIterator extends AbstractIterator<BlockPos> {

    /**
     * Queue of upcoming positions to try
     */
    private final Queue<TreePos> upcomingPositions = new ArrayDeque<>();
    /**
     * Position for returns, saves some object allocation
     */
    private final BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
    /**
     * Branches that have been visited already
     */
    private final Set<BlockPos> branchVisited = new HashSet<>();

    private final Level world;
    private final Block filter;
    /**
     * Bounds for branch detection
     */
    private final int minX, maxX, minZ, maxZ;

    public TreeIterator(Level world, Block filter, BlockPos origin, Direction widthDir, int extraWidth, Direction depthDir, int extraDepth) {
      this.world = world;
      this.filter = filter;

      // first, enqueue the origin
      this.upcomingPositions.add(new TreePos(origin.above(), false));

      // next, start adding AOE
      int minX = origin.getX();
      int maxX = minX;
      int minZ = origin.getZ();
      int maxZ = minZ;
      if (extraDepth > 0 || extraWidth > 0) {
        // add extra blocks
        for (int d = 0; d <= extraDepth; d++) {
          for (int w = -extraWidth; w <= extraWidth; w++) {
            if (d != 0 || w != 0) {
              // if its valid, queue
              this.mutable.set(origin).move(depthDir, d).move(widthDir, w);
              if (this.isValidBlock(this.mutable)) {
                this.upcomingPositions.add(new TreePos(this.mutable, true));
                // update bounds
                if (this.mutable.getX() < minX) minX = this.mutable.getX();
                if (this.mutable.getX() > maxX) maxX = this.mutable.getX();
                if (this.mutable.getZ() < minZ) minZ = this.mutable.getZ();
                if (this.mutable.getZ() > maxZ) maxZ = this.mutable.getZ();
              }
            }
          }
        }
      }
      this.minX = minX;
      this.maxX = maxX;
      this.minZ = minZ;
      this.maxZ = maxZ;
    }

    /**
     * Checks if the position matches the filter block
     */
    private boolean isValidBlock(BlockPos pos) {
      return this.world.getBlockState(pos).getBlock() == this.filter;
    }

    /**
     * Checks if the block position outside the original tree
     */
    private boolean outsideTrunk(BlockPos pos) {
      return (pos.getX() < this.minX || pos.getX() > this.maxX || pos.getZ() < this.minZ || pos.getZ() > this.maxZ);
    }

    /**
     * Checks if the block position is a branch position, meaning outside the original tree
     */
    private boolean isBranch(BlockPos pos) {
      if (!this.outsideTrunk(pos)) {
        return false;
      }
      // find the distance to the nearest corner
      int deltaX = Math.min(Math.abs(pos.getX() - this.minX), Math.abs(pos.getX() - this.maxX));
      int deltaZ = Math.min(Math.abs(pos.getZ() - this.minZ), Math.abs(pos.getZ() - this.maxZ));
      if ((deltaX + deltaZ) > MAX_BRANCH_DISTANCE || this.branchVisited.contains(pos)) {
        return false;
      }
      this.branchVisited.add(pos.immutable());
      return this.isValidBlock(pos);
    }

    /**
     * Adds a branch to the queue at the current mutable position
     */
    private void addBranch(Direction direction) {
      this.upcomingPositions.add(new TreePos(this.mutable, direction));
    }

    /**
     * Tries to find a branch at the current mutable position
     */
    private void tryBranch(Direction direction) {
      // block must not have log both above and below it to count
      if (this.isBranch(this.mutable)) {
        // copies position, so safe to change after
        TreePos branchPos = new TreePos(this.mutable, direction);
        // must have a non-solid block below, and must be a corner or be 1-2 blocks tall (dark oak support/jungle sapling thick branches)
        if (!this.world.getBlockState(this.mutable.move(0, -1, 0)).canOcclude()) {
          this.upcomingPositions.add(branchPos);
        }
      }
    }

    @Override
    protected BlockPos computeNext() {
      while (!this.upcomingPositions.isEmpty()) {
        // if the position matches the block, add it. Some positions are already added
        TreePos treePos = this.upcomingPositions.remove();

        // trunk logic
        if (treePos.direction == Direction.UP) {
          boolean isTreeUp = treePos.isChecked || this.isValidBlock(treePos.pos);

          // find branches in all 4 directions if going up, assuming we are in the
          for (Direction direction : Plane.HORIZONTAL) {
            // if the position is a branch, meaning its a log with no log above it, queue it
            this.mutable.set(treePos.pos).move(direction);
            // if we did not find a log at the current position, treat the position as our new tree, for acacia
            this.tryBranch(!isTreeUp ? Direction.UP : direction);
          }

          if (isTreeUp) {
            // corner case, only care if we have a tree at current position
            boolean isMinX = treePos.pos.getX() == this.minX;
            boolean isMaxX = treePos.pos.getX() == this.maxX;
            boolean isMinZ = treePos.pos.getZ() == this.minZ;
            boolean isMaxZ = treePos.pos.getZ() == this.maxZ;
            // if either min or max on both axis, but not both (1x1), we are a corner, do corner case
            if (isMinX) {
              if (isMinZ) {
                this.mutable.set(treePos.pos).move(-1, 0, -1);
                this.tryBranch(Direction.WEST);
              }
              if (isMaxZ) {
                this.mutable.set(treePos.pos).move(-1, 0, 1);
                this.tryBranch(Direction.WEST);
              }
            }
            if (isMaxX) {
              if (isMinZ) {
                this.mutable.set(treePos.pos).move(1, 0, -1);
                this.tryBranch(Direction.EAST);
              }
              if (isMaxZ) {
                this.mutable.set(treePos.pos).move(1, 0, 1);
                this.tryBranch(Direction.EAST);
              }
            }

            // finally, return this position
            // insert the updated position into the queue and return the current position
            this.mutable.set(treePos.pos);
            this.upcomingPositions.add(treePos.move());
            // acacia can continue outside the original trunk, so start marking it visited to prevent redundancy
            if (this.outsideTrunk(treePos.pos)) {
              this.branchVisited.add(treePos.pos);
            }
            return this.mutable;
          }
        } else {
          // branch logic, should always be checked ahead of time (question is which further branches can we find)
          // continue in same direction
          this.mutable.set(treePos.pos).move(0, 1, 0);
          if (this.isBranch(this.mutable)) {
            this.addBranch(treePos.direction);
            // just direction, no up
          } else if (this.isBranch(this.mutable.move(treePos.direction).move(0, -1, 0))) {
            this.addBranch(treePos.direction);
            // direction and up
          } else if (this.isBranch(this.mutable.move(0, 1, 0))) {
            this.addBranch(treePos.direction);
          }
          // try each side, we check pos, above, then continuing the side
          Direction rotated = treePos.direction.getClockWise();
          this.mutable.set(treePos.pos).move(rotated);
          if (this.isBranch(this.mutable)) {
            this.addBranch(rotated);
          } else if (this.isBranch(this.mutable.move(0, 1, 0))) {
            this.addBranch(rotated);
          } else if (this.isBranch(this.mutable.move(treePos.direction).move(0, -1, 0))) {
            this.addBranch(rotated);
          } else if (this.isBranch(this.mutable.move(0, 1, 0))) {
            this.addBranch(rotated);
          }
          rotated = rotated.getOpposite();
          this.mutable.set(treePos.pos).move(rotated);
          if (this.isBranch(this.mutable)) {
            this.addBranch(rotated);
          } else if (this.isBranch(this.mutable.move(0, 1, 0))) {
            this.addBranch(rotated);
          } else if (this.isBranch(this.mutable.move(treePos.direction).move(0, -1, 0))) {
            this.addBranch(rotated);
          } else if (this.isBranch(this.mutable.move(0, 1, 0))) {
            this.addBranch(rotated);
          }
          return treePos.pos;
        }
      }
      return this.endOfData();
    }
  }

  /**
   * Helper class for queue contents
   */
  private static class TreePos {

    private final BlockPos.MutableBlockPos pos;
    private final Direction direction;
    /**
     * If true, this position has been validated already for a log
     */
    private boolean isChecked;

    TreePos(BlockPos pos, boolean isChecked) {
      // note this copies the mutable if already mutable
      this.pos = pos.mutable();
      this.direction = Direction.UP;
      this.isChecked = isChecked;
    }

    TreePos(BlockPos pos, Direction direction) {
      // note this copies the mutable if already mutable
      this.pos = pos.mutable();
      this.direction = direction;
      this.isChecked = true;
    }

    /**
     * Moves the tree position in the given direction
     */
    public TreePos move() {
      this.pos.move(this.direction);
      this.isChecked = false;
      return this;
    }
  }

  private static class Loader implements IGenericLoader<TreeAOEIterator> {

    @Override
    public TreeAOEIterator deserialize(JsonObject json) {
      int width = JsonUtils.getIntMin(json, "width_bonus", 0);
      int depth = JsonUtils.getIntMin(json, "depth_bonus", 0);
      return new TreeAOEIterator(width, depth);
    }

    @Override
    public TreeAOEIterator fromNetwork(FriendlyByteBuf buffer) {
      int width = buffer.readVarInt();
      int depth = buffer.readVarInt();
      return new TreeAOEIterator(width, depth);
    }

    @Override
    public void serialize(TreeAOEIterator object, JsonObject json) {
      json.addProperty("width_bonus", object.width);
      json.addProperty("depth_bonus", object.depth);
    }

    @Override
    public void toNetwork(TreeAOEIterator object, FriendlyByteBuf buffer) {
      buffer.writeVarInt(object.width);
      buffer.writeVarInt(object.depth);
    }
  }
}
