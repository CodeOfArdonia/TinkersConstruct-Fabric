package slimeknights.tconstruct.world.worldgen.islands;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import slimeknights.tconstruct.world.worldgen.islands.variants.IIslandVariant;

import java.util.Optional;

/**
 * Base logic for all island variants
 */
public abstract class AbstractIslandStructure extends Structure {

  protected static final String[] SIZES = new String[]{"0x1x0", "2x2x4", "4x1x6", "8x1x11", "11x1x11"};
  protected final IIslandSettings iIslandSettings;

  public AbstractIslandStructure(Structure.StructureSettings structureSettings, IIslandSettings settings) {
    super(structureSettings);
    this.iIslandSettings = settings;
  }

  @Override
  public GenerationStep.Decoration step() {
    return GenerationStep.Decoration.SURFACE_STRUCTURES;
  }

  /**
   * Base logic to generate the islands
   */
  protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
    // get height
    ChunkPos chunkPos = context.chunkPos();
    RandomSource random = RandomSource.create(chunkPos.x + chunkPos.z * 0x9E7F71L);
    ChunkGenerator generator = context.chunkGenerator();
    Rotation rotation = Rotation.getRandom(random);
    int height = this.iIslandSettings.getHeight(context.chunkPos(), generator, context.heightAccessor(), rotation, random, context.randomState());

    // biome check
    BlockPos targetPos = context.chunkPos().getMiddleBlockPosition(height);

    // find variant
    return Optional.of(new GenerationStub(targetPos, builder -> {
      RandomSource rand = context.random();
      IIslandVariant variant = this.iIslandSettings.getVariant(rand);
      Mirror mirror = Util.getRandom(Mirror.values(), rand);
      builder.addPiece(new SlimeIslandPiece(context.structureTemplateManager(), variant, Util.getRandom(SIZES, rand), targetPos, variant.getTreeFeature(rand, context.registryAccess()), rotation, mirror));
    }));
  }

  /**
   * Interface allowing configuring the abstract island
   */
  protected interface IIslandSettings {

    /**
     * Gets the variant of this island
     */
    IIslandVariant getVariant(RandomSource random);

    /**
     * Gets the height to generate this island
     */
    default int getHeight(ChunkPos chunkPos, ChunkGenerator generator, LevelHeightAccessor pLevel, Rotation rotation, RandomSource random, RandomState randomState) {
      int xOffset;
      int yOffset;
      switch (rotation) {
        case CLOCKWISE_90 -> {
          xOffset = -5;
          yOffset = 5;
        }
        case CLOCKWISE_180 -> {
          xOffset = -5;
          yOffset = -5;
        }
        case COUNTERCLOCKWISE_90 -> {
          xOffset = 5;
          yOffset = -5;
        }
        default -> {
          xOffset = 5;
          yOffset = 5;
        }
      }

      // determine height
      int x = chunkPos.getBlockX(7);
      int z = chunkPos.getBlockZ(7);
      int minXMinZ = generator.getFirstOccupiedHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, pLevel, randomState);
      int minXMaxZ = generator.getFirstOccupiedHeight(x, z + yOffset, Heightmap.Types.WORLD_SURFACE_WG, pLevel, randomState);
      int maxXMinZ = generator.getFirstOccupiedHeight(x + xOffset, z, Heightmap.Types.WORLD_SURFACE_WG, pLevel, randomState);
      int maxXMaxZ = generator.getFirstOccupiedHeight(x + xOffset, z + yOffset, Heightmap.Types.WORLD_SURFACE_WG, pLevel, randomState);
      // from the smallest of the 4 positions, add 60 plus another random 50, limit to 20 blocks below world height (tallest island is 13 blocks, 7 blocks for trees)
      return Math.min(Math.min(Math.min(minXMinZ, minXMaxZ), Math.min(maxXMinZ, maxXMaxZ)) + 60 + random.nextInt(50), generator.getGenDepth() - 20);
    }
  }
}
