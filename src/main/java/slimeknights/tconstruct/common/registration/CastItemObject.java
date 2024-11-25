package slimeknights.tconstruct.common.registration;

import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeknights.mantle.registration.object.ItemObject;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Deferred wrapper holding gold, sand, and red sand casts
 */
public class CastItemObject extends ItemObject<Item> {

  @Getter
  private final ResourceLocation name;
  private final Supplier<? extends Item> sand;
  private final Supplier<? extends Item> redSand;
  @Getter
  private final TagKey<Item> singleUseTag;
  @Getter
  private final TagKey<Item> multiUseTag;

  public CastItemObject(ResourceLocation name, Item gold, Item sand, Item redSand) {
    super(gold);
    this.name = name;
    this.sand = () -> sand;
    this.redSand = () -> redSand;
    this.singleUseTag = this.makeTag("single_use");
    this.multiUseTag = this.makeTag("multi_use");
  }

  public CastItemObject(ResourceLocation name, ItemObject<? extends Item> gold, Supplier<? extends Item> sand, Supplier<? extends Item> redSand) {
    super(gold);
    this.name = name;
    this.sand = sand;
    this.redSand = redSand;
    this.singleUseTag = this.makeTag("single_use");
    this.multiUseTag = this.makeTag("multi_use");
  }

  /**
   * Gets the single use tag for this object
   *
   * @return Single use tag
   */
  protected TagKey<Item> makeTag(String type) {
    return TagKey.create(Registries.ITEM, new ResourceLocation(this.name.getNamespace(), "casts/" + type + "/" + this.name.getPath()));
  }

  /**
   * Gets the yellow sand variant
   *
   * @return Yellow sand variant
   */
  public Item getSand() {
    return Objects.requireNonNull(this.sand.get(), "CastItemObject missing sand");
  }

  /**
   * Gets the red sand variant
   *
   * @return Red sand variant
   */
  public Item getRedSand() {
    return Objects.requireNonNull(this.redSand.get(), "CastItemObject missing red sand");
  }

  /**
   * Gets a list of all variants
   *
   * @return All variants
   */
  public List<Item> values() {
    return Arrays.asList(this.get(), this.getSand(), this.getRedSand());
  }
}
