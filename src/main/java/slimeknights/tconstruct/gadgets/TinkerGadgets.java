package slimeknights.tconstruct.gadgets;

import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.registry.CompostingChanceRegistry;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.apache.logging.log4j.Logger;
import slimeknights.mantle.item.BlockTooltipItem;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.TinkerModule;
import slimeknights.tconstruct.gadgets.block.FoodCakeBlock;
import slimeknights.tconstruct.gadgets.block.PunjiBlock;
import slimeknights.tconstruct.gadgets.data.GadgetRecipeProvider;
import slimeknights.tconstruct.gadgets.entity.EflnBallEntity;
import slimeknights.tconstruct.gadgets.entity.FancyItemFrameEntity;
import slimeknights.tconstruct.gadgets.entity.FrameType;
import slimeknights.tconstruct.gadgets.entity.GlowballEntity;
import slimeknights.tconstruct.gadgets.entity.shuriken.FlintShurikenEntity;
import slimeknights.tconstruct.gadgets.entity.shuriken.QuartzShurikenEntity;
import slimeknights.tconstruct.gadgets.item.EflnBallItem;
import slimeknights.tconstruct.gadgets.item.FancyItemFrameItem;
import slimeknights.tconstruct.gadgets.item.GlowBallItem;
import slimeknights.tconstruct.gadgets.item.PiggyBackPackItem;
import slimeknights.tconstruct.gadgets.item.PiggyBackPackItem.CarryPotionEffect;
import slimeknights.tconstruct.gadgets.item.ShurikenItem;
import slimeknights.tconstruct.gadgets.item.slimesling.BaseSlimeSlingItem;
import slimeknights.tconstruct.gadgets.item.slimesling.EarthSlimeSlingItem;
import slimeknights.tconstruct.gadgets.item.slimesling.EnderSlimeSlingItem;
import slimeknights.tconstruct.gadgets.item.slimesling.IchorSlimeSlingItem;
import slimeknights.tconstruct.gadgets.item.slimesling.SkySlimeSlingItem;
import slimeknights.tconstruct.library.utils.Util;
import slimeknights.tconstruct.shared.TinkerFood;
import slimeknights.tconstruct.shared.block.SlimeType;

import java.util.function.Function;

/**
 * Contains any special tools unrelated to the base tools
 */
@SuppressWarnings("unused")
public final class TinkerGadgets extends TinkerModule {

  public TinkerGadgets() {
//    slimeSling.values(); // Force enums to register
  }

  static final Logger log = Util.getLogger("tinker_gadgets");

  /*
   * Block base properties
   */
  private static final Item.Properties GADGET_PROPS = new Item.Properties();
  private static final Item.Properties UNSTACKABLE_PROPS = new Item.Properties().stacksTo(1);
  private static final Function<Block, ? extends BlockItem> DEFAULT_BLOCK_ITEM = (b) -> new BlockItem(b, GADGET_PROPS);
  private static final Function<Block, ? extends BlockItem> TOOLTIP_BLOCK_ITEM = (b) -> new BlockTooltipItem(b, GADGET_PROPS);
  private static final Function<Block, ? extends BlockItem> UNSTACKABLE_BLOCK_ITEM = (b) -> new BlockTooltipItem(b, UNSTACKABLE_PROPS);

  /*
   * Blocks
   */
  public static final ItemObject<PunjiBlock> punji = BLOCKS_DEFFERED.register("punji", () -> new PunjiBlock(builder(MapColor.PLANT, SoundType.GRASS).pushReaction(PushReaction.DESTROY).strength(3.0F).speedFactor(0.4F).noOcclusion()), TOOLTIP_BLOCK_ITEM);

  /*
   * Items
   */
  public static final ItemObject<PiggyBackPackItem> piggyBackpack = ITEMS_DEFFERED.register("piggy_backpack", () -> new PiggyBackPackItem(new Properties().stacksTo(16)));
  public static final EnumObject<FrameType, FancyItemFrameItem> itemFrame = ITEMS_DEFFERED.registerEnum(FrameType.values(), "item_frame", (type) -> new FancyItemFrameItem(GADGET_PROPS, (world, pos, dir) -> new FancyItemFrameEntity(world, pos, dir, type)));
  // slime tools
  private static final Item.Properties SLING_PROPS = new Item.Properties().stacksTo(1).durability(250);
  public static final EnumObject<SlimeType, BaseSlimeSlingItem> slimeSling = new EnumObject.Builder<SlimeType, BaseSlimeSlingItem>(SlimeType.class)
    .put(SlimeType.EARTH, ITEMS_DEFFERED.register("earth_slime_sling", () -> new EarthSlimeSlingItem(SLING_PROPS)))
    .put(SlimeType.SKY, ITEMS_DEFFERED.register("sky_slime_sling", () -> new SkySlimeSlingItem(SLING_PROPS)))
    .put(SlimeType.ICHOR, ITEMS_DEFFERED.register("ichor_slime_sling", () -> new IchorSlimeSlingItem(SLING_PROPS)))
    .put(SlimeType.ENDER, ITEMS_DEFFERED.register("ender_slime_sling", () -> new EnderSlimeSlingItem(SLING_PROPS)))
    .build();
  // throwballs
  public static final ItemObject<GlowBallItem> glowBall = ITEMS_DEFFERED.register("glow_ball", GlowBallItem::new);
  public static final ItemObject<EflnBallItem> efln = ITEMS_DEFFERED.register("efln_ball", EflnBallItem::new);

  // foods
  private static final BlockBehaviour.Properties CAKE = builder(SoundType.WOOL).pushReaction(PushReaction.DESTROY).strength(0.5F);
  public static final EnumObject<SlimeType, FoodCakeBlock> cake = BLOCKS_DEFFERED.registerEnum(SlimeType.LIQUID, "cake", type -> new FoodCakeBlock(CAKE, TinkerFood.getCake(type)), UNSTACKABLE_BLOCK_ITEM);
  public static final ItemObject<FoodCakeBlock> magmaCake = BLOCKS_DEFFERED.register("magma_cake", () -> new FoodCakeBlock(CAKE, TinkerFood.MAGMA_CAKE), UNSTACKABLE_BLOCK_ITEM);

  // Shurikens
  private static final Item.Properties THROWABLE_PROPS = new Item.Properties().stacksTo(16)/*.tab(TAB_GADGETS)*/;
  public static final ItemObject<ShurikenItem> quartzShuriken = ITEMS_DEFFERED.register("quartz_shuriken", () -> new ShurikenItem(THROWABLE_PROPS, QuartzShurikenEntity::new));
  public static final ItemObject<ShurikenItem> flintShuriken = ITEMS_DEFFERED.register("flint_shuriken", () -> new ShurikenItem(THROWABLE_PROPS, FlintShurikenEntity::new));

  /*
   * Entities
   */
  public static final RegistryObject<EntityType<FancyItemFrameEntity>> itemFrameEntity = ENTITIES.register("fancy_item_frame", () ->
    FabricEntityTypeBuilder.<FancyItemFrameEntity>create(
        MobCategory.MISC, FancyItemFrameEntity::new)
      .dimensions(EntityDimensions.fixed(0.5F, 0.5F))
      .trackRangeChunks(10)
      .trackedUpdateRate(Integer.MAX_VALUE)
      .entityFactory((spawnEntity, world) -> new FancyItemFrameEntity(TinkerGadgets.itemFrameEntity.get(), world))
      .forceTrackedVelocityUpdates(false)
  );
  public static final RegistryObject<EntityType<GlowballEntity>> glowBallEntity = ENTITIES.register("glow_ball", () ->
    FabricEntityTypeBuilder.<GlowballEntity>create(MobCategory.MISC, GlowballEntity::new)
      .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
      .trackRangeChunks(4)
      .trackedUpdateRate(10)
      .entityFactory((spawnEntity, world) -> new GlowballEntity(TinkerGadgets.glowBallEntity.get(), world))
      .forceTrackedVelocityUpdates(true)
  );
  public static final RegistryObject<EntityType<EflnBallEntity>> eflnEntity = ENTITIES.register("efln_ball", () ->
    FabricEntityTypeBuilder.<EflnBallEntity>create(MobCategory.MISC, EflnBallEntity::new)
      .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
      .trackRangeChunks(4)
      .trackedUpdateRate(10)
      .entityFactory((spawnEntity, world) -> new EflnBallEntity(TinkerGadgets.eflnEntity.get(), world))
      .forceTrackedVelocityUpdates(true)
  );
  public static final RegistryObject<EntityType<QuartzShurikenEntity>> quartzShurikenEntity = ENTITIES.register("quartz_shuriken", () ->
    FabricEntityTypeBuilder.<QuartzShurikenEntity>create(MobCategory.MISC, QuartzShurikenEntity::new)
      .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
      .trackRangeChunks(4)
      .trackedUpdateRate(10)
      .entityFactory((spawnEntity, world) -> new QuartzShurikenEntity(TinkerGadgets.quartzShurikenEntity.get(), world))
      .forceTrackedVelocityUpdates(true)
  );
  public static final RegistryObject<EntityType<FlintShurikenEntity>> flintShurikenEntity = ENTITIES.register("flint_shuriken", () ->
    FabricEntityTypeBuilder.<FlintShurikenEntity>create(MobCategory.MISC, FlintShurikenEntity::new)
      .dimensions(EntityDimensions.fixed(0.25F, 0.25F))
      .trackRangeChunks(4)
      .trackedUpdateRate(10)
      .entityFactory((spawnEntity, world) -> new FlintShurikenEntity(TinkerGadgets.flintShurikenEntity.get(), world))
      .forceTrackedVelocityUpdates(true)
  );

  /*
   * Potions
   */
  public static final RegistryObject<CarryPotionEffect> carryEffect = MOB_EFFECTS.register("carry", CarryPotionEffect::new);

  /*
   * Events
   */
  public static void commonSetup() {
//    PiggybackCapability.register();
//    event.enqueueWork(() -> {
    cake.forEach(block -> CompostingChanceRegistry.INSTANCE.add(block, 1.0f));
    CompostingChanceRegistry.INSTANCE.add(magmaCake.get(), 1.0f);
//    });
  }

  public static void gatherData(final FabricDataGenerator.Pack pack) {
    pack.addProvider(GadgetRecipeProvider::new);
  }


}
