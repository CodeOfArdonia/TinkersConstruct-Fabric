package slimeknights.tconstruct.world.worldgen.islands.variants;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerStructures;

import javax.annotation.Nullable;
import java.util.Objects;

public class EarthSlimeIslandVariant extends AbstractSlimeIslandVariant {

  public EarthSlimeIslandVariant(int index, SlimeType dirtType) {
    super(index, dirtType, SlimeType.EARTH);
  }

  @Override
  public ResourceLocation getStructureName(String variantName) {
    return TConstruct.getResource("slime_islands/earth/" + this.dirtType.getSerializedName() + "_" + variantName);
  }

  @Override
  protected SlimeType getCongealedSlimeType(RandomSource random) {
    return SlimeType.EARTH;
  }

  @Override
  public BlockState getLakeFluid() {
    return Objects.requireNonNull(TinkerFluids.earthSlime.getBlock()).defaultBlockState();
  }

  @Nullable
  @Override
  public ConfiguredFeature<?, ?> getTreeFeature(RandomSource random, RegistryAccess registryAccess) {
    return registryAccess.registryOrThrow(Registries.CONFIGURED_FEATURE).get(TinkerStructures.earthSlimeIslandTree);
  }

  private static boolean isWater(WorldGenLevel world, BlockPos pos) {
    return world.isEmptyBlock(pos) || world.getBlockState(pos).getBlock() == Blocks.WATER;
  }

  @Override
  public boolean isPositionValid(WorldGenLevel world, BlockPos pos, ChunkGenerator generator) {
    BlockPos up = pos.above();
    if (isWater(world, up)) {
      for (Direction direction : Plane.HORIZONTAL) {
        if (!isWater(world, up.relative(direction))) {
          return false;
        }
      }
      return true;
    }
    return false;
  }
}
