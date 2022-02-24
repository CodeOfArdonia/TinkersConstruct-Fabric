package slimeknights.tconstruct.common.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.world.entity.EntityType;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.world.TinkerWorld;

public class EntityTypeTagProvider extends FabricTagProvider.EntityTypeTagProvider {

  public EntityTypeTagProvider(FabricDataGenerator generatorIn) {
    super(generatorIn);
  }

  @Override
  public void generateTags() {
    this.tag(TinkerTags.EntityTypes.SLIMES)
        .add(EntityType.SLIME, TinkerWorld.earthSlimeEntity.get(), TinkerWorld.skySlimeEntity.get(), TinkerWorld.enderSlimeEntity.get(), TinkerWorld.terracubeEntity.get());
    this.tag(TinkerTags.EntityTypes.BACON_PRODUCER).add(EntityType.PIG, EntityType.PIGLIN, EntityType.HOGLIN);

    this.tag(TinkerTags.EntityTypes.MELTING_SHOW).add(EntityType.IRON_GOLEM, EntityType.SNOW_GOLEM, EntityType.VILLAGER, EntityType.PLAYER);
    this.tag(TinkerTags.EntityTypes.MELTING_HIDE).add(EntityType.GIANT);
    this.tag(TinkerTags.EntityTypes.PIGGYBACKPACK_BLACKLIST);

    this.tag(TinkerTags.EntityTypes.CREEPERS).add(EntityType.CREEPER);
    this.tag(TinkerTags.EntityTypes.RARE_MOBS).add(EntityType.WITHER, EntityType.ENDER_DRAGON, EntityType.ELDER_GUARDIAN, EntityType.EVOKER, EntityType.PLAYER);
  }

  @Override
  public String getName() {
    return "Tinkers Construct Entity Type TinkerTags";
  }

}
