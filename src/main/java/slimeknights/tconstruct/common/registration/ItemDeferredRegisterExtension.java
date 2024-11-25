package slimeknights.tconstruct.common.registration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.deferred.ItemDeferredRegister;
import slimeknights.mantle.registration.object.ItemObject;

public class ItemDeferredRegisterExtension extends ItemDeferredRegister {

  public ItemDeferredRegisterExtension(String modID) {
    super(modID);
  }

  /**
   * Registers a set of three cast items at once
   *
   * @param name  Base name of cast
   * @param props Item properties
   * @return Object containing casts
   */
  public CastItemObject registerCast(String name, Item.Properties props) {
    ItemObject<Item> cast = this.register(name + "_cast", props);
    ItemObject<Item> sandCast = this.register(name + "_sand_cast", props);
    ItemObject<Item> redSandCast = this.register(name + "_red_sand_cast", props);
    return new CastItemObject(new ResourceLocation(this.resourceName(name)), cast, sandCast, redSandCast);
  }
}
