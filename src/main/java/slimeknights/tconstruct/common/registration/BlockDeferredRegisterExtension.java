package slimeknights.tconstruct.common.registration;

import io.github.fabricators_of_create.porting_lib.util.RegistryObject;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import slimeknights.mantle.registration.deferred.BlockDeferredRegister;
import slimeknights.mantle.registration.object.EnumObject;
import slimeknights.mantle.registration.object.EnumObject.Builder;
import slimeknights.mantle.registration.object.ItemObject;
import slimeknights.tconstruct.common.registration.GeodeItemObject.BudSize;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Additional methods in deferred register extension
 */
@SuppressWarnings("UnusedReturnValue")
public class BlockDeferredRegisterExtension extends BlockDeferredRegister {

  private static final BlockBehaviour.Properties POTTED_PROPS = BlockBehaviour.Properties.of().forceSolidOff().instabreak().noOcclusion().pushReaction(PushReaction.DESTROY);

  public BlockDeferredRegisterExtension(String modID) {
    super(modID);
  }

  /**
   * Registers a geode block
   *
   * @param name       Geode name
   * @param color      Color of the geode
   * @param blockSound Sound of the block and budding block
   * @param props      Item props
   * @return The geode block
   */
  public GeodeItemObject registerGeode(String name, MapColor color, SoundType blockSound, SoundEvent chimeSound, Map<BudSize, SoundType> clusterSounds, int baseLight, Item.Properties props) {
    RegistryObject<Item> shard = this.itemRegister.register(name, () -> new Item(props));
    return new GeodeItemObject(shard, this, color, blockSound, chimeSound, clusterSounds, baseLight, props);
  }

  /**
   * Registers a potted form of the given block using the vanilla pot
   *
   * @param name  Name of the flower
   * @param block Block to put in the block
   * @return Potted block instance
   */
  public RegistryObject<FlowerPotBlock> registerPotted(String name, Supplier<? extends Block> block) {
//    Supplier<FlowerPotBlock> flowerPot = RegistrationHelper.castDelegate(Blocks.FLOWER_POT.delegate); TODO: PORT
    RegistryObject<FlowerPotBlock> potted = this.registerNoItem("potted_" + name, () -> new FlowerPotBlock(/*flowerPot, */block.get(), POTTED_PROPS));
//    flowerPot.get().addPlant(resource(name), potted);
    return potted;
  }

  /**
   * Registers a potted form of the given block using the vanilla pot
   */
  public RegistryObject<FlowerPotBlock> registerPotted(RegistryObject<? extends Block> block) {
    return this.registerPotted(block.getId().getPath(), block);
  }

  /**
   * Registers a potted form of the given block using the vanilla pot
   */
  public RegistryObject<FlowerPotBlock> registerPotted(ItemObject<? extends Block> block) {
    return this.registerPotted(block.getRegistryName().getPath(), block);
  }

  /**
   * Registers a potted form of the given block using the vanilla pot
   */
  public <T extends Enum<T> & StringRepresentable, B extends Block> EnumObject<T, FlowerPotBlock> registerPottedEnum(T[] values, String name, EnumObject<T, B> block) {
    EnumObject.Builder<T, FlowerPotBlock> builder = new Builder<>(values[0].getDeclaringClass());
    for (T value : values) {
      Supplier<? extends B> supplier = block.getSupplier(value);
      if (supplier != null) {
        builder.put(value, this.registerPotted(value.getSerializedName() + "_" + name, supplier));
      }
    }
    return builder.build();
  }
}
