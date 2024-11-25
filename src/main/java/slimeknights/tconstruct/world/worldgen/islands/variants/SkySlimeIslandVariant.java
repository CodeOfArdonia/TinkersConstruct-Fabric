package slimeknights.tconstruct.world.worldgen.islands.variants;

import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.fluids.TinkerFluids;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerStructures;
import slimeknights.tconstruct.world.TinkerWorld;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Island variant for sky slime islands
 */
public class SkySlimeIslandVariant extends AbstractSlimeIslandVariant {

  public SkySlimeIslandVariant(int index, SlimeType dirtType) {
    super(index, dirtType, SlimeType.SKY);
  }

  @Override
  public ResourceLocation getStructureName(String variantName) {
    return TConstruct.getResource("slime_islands/sky/" + this.dirtType.getSerializedName() + "_" + variantName);
  }

  @Override
  protected SlimeType getCongealedSlimeType(RandomSource random) {
    return random.nextBoolean() ? SlimeType.SKY : SlimeType.EARTH;
  }

  @Override
  public BlockState getLakeFluid() {
    return Objects.requireNonNull(TinkerFluids.skySlime.getBlock()).defaultBlockState();
  }

  @Nullable
  @Override
  public BlockState getVines() {
    return TinkerWorld.skySlimeVine.get().defaultBlockState();
  }

  @Nullable
  @Override
  public ConfiguredFeature<?, ?> getTreeFeature(RandomSource random, RegistryAccess registryAccess) {
    return registryAccess.registryOrThrow(Registries.CONFIGURED_FEATURE).get(TinkerStructures.skySlimeIslandTree);
  }
}
