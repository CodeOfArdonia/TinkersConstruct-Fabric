package slimeknights.tconstruct.common.data.loot;

import com.google.common.collect.Sets;
import net.minecraft.core.Registry;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.shared.TinkerCommons;
import slimeknights.tconstruct.shared.block.SlimeType;
import slimeknights.tconstruct.world.TinkerWorld;

import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class EntityLootTableProvider extends EntityLoot {

//  @Override
  protected Iterable<EntityType<?>> getKnownEntities() {
    return Registry.ENTITY_TYPE.stream()
                                   // remove earth slime entity, we redirect to the vanilla loot table
                                   .filter((entity) -> TConstruct.MOD_ID.equals(Objects.requireNonNull(entity.getRegistryName()).getNamespace())
                                                       && entity != TinkerWorld.earthSlimeEntity.get())
                                   .collect(Collectors.toList());
  }

//  @Override
  protected void addTables() {
    this.add(TinkerWorld.skySlimeEntity.get(), dropSlimeballs(SlimeType.SKY));
    this.add(TinkerWorld.enderSlimeEntity.get(), dropSlimeballs(SlimeType.ENDER));
    this.add(TinkerWorld.terracubeEntity.get(),
                           LootTable.lootTable().withPool(LootPool.lootPool()
                                                                   .setRolls(ConstantValue.exactly(1))
                                                                   .add(LootItem.lootTableItem(Items.CLAY_BALL)
                                                                                          .apply(SetItemCountFunction.setCount(UniformGenerator.between(-2.0F, 1.0F)))
                                                                                          .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))
                                                                                          .apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE))))));
  }

  @Override
  public void accept(BiConsumer<ResourceLocation, LootTable.Builder> p_124377_) {
    this.addTables();
    Set<ResourceLocation> set = Sets.newHashSet();

    for(EntityType<?> entitytype : getKnownEntities()) {
      ResourceLocation resourcelocation = entitytype.getDefaultLootTable();
      if (isNonLiving(entitytype)) {
        if (resourcelocation != BuiltInLootTables.EMPTY && this.map.remove(resourcelocation) != null) {
          throw new IllegalStateException(String.format("Weird loottable '%s' for '%s', not a LivingEntity so should not have loot", resourcelocation, Registry.ENTITY_TYPE.getKey(entitytype)));
        }
      } else if (resourcelocation != BuiltInLootTables.EMPTY && set.add(resourcelocation)) {
        LootTable.Builder loottable$builder = this.map.remove(resourcelocation);
        if (loottable$builder == null) {
          throw new IllegalStateException(String.format("Missing loottable '%s' for '%s'", resourcelocation, Registry.ENTITY_TYPE.getKey(entitytype)));
        }

        p_124377_.accept(resourcelocation, loottable$builder);
      }
    }

    this.map.forEach(p_124377_);
  }

  protected boolean isNonLiving(EntityType<?> entitytype) {
    return !SPECIAL_LOOT_TABLE_TYPES.contains(entitytype) && entitytype.getCategory() == MobCategory.MISC;
  }

  private static LootTable.Builder dropSlimeballs(SlimeType type) {
    return LootTable.lootTable()
                    .withPool(LootPool.lootPool()
                                         .setRolls(ConstantValue.exactly(1))
                                         .add(LootItem.lootTableItem(TinkerCommons.slimeball.get(type))
                                                                .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                                                                .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))));
  }
}
