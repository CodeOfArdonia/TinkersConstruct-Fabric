package slimeknights.mantle.registration.object;

import lombok.Getter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.state.properties.WoodType;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import static slimeknights.mantle.registration.RegistrationHelper.castDelegate;

/**
 * Extension of the fence object with all other wood blocks
 */
public class WoodBlockObject extends FenceBuildingBlockObject {

  @Getter
  private final WoodType woodType;
  // basic
  private final Supplier<? extends Block> log;
  private final Supplier<? extends Block> strippedLog;
  private final Supplier<? extends Block> wood;
  private final Supplier<? extends Block> strippedWood;
  // doors
  private final Supplier<? extends FenceGateBlock> fenceGate;
  private final Supplier<? extends DoorBlock> door;
  private final Supplier<? extends TrapDoorBlock> trapdoor;
  // redstone
  private final Supplier<? extends PressurePlateBlock> pressurePlate;
  private final Supplier<? extends ButtonBlock> button;
  // signs
  private final Supplier<? extends StandingSignBlock> sign;
  private final Supplier<? extends WallSignBlock> wallSign;
  // tags
  @Getter
  private final TagKey<Block> logBlockTag;
  @Getter
  private final TagKey<Item> logItemTag;

  public WoodBlockObject(ResourceLocation name, WoodType woodType, BuildingBlockObject planks,
                         Supplier<? extends Block> log, Supplier<? extends Block> strippedLog, Supplier<? extends Block> wood, Supplier<? extends Block> strippedWood,
                         Supplier<? extends FenceBlock> fence, Supplier<? extends FenceGateBlock> fenceGate, Supplier<? extends DoorBlock> door, Supplier<? extends TrapDoorBlock> trapdoor,
                         Supplier<? extends PressurePlateBlock> pressurePlate, Supplier<? extends ButtonBlock> button,
                         Supplier<? extends StandingSignBlock> sign, Supplier<? extends WallSignBlock> wallSign) {
    super(planks, fence);
    this.woodType = woodType;
    this.log = log;
    this.strippedLog = strippedLog;
    this.wood = wood;
    this.strippedWood = strippedWood;
    this.fenceGate = fenceGate;
    this.door = door;
    this.trapdoor = trapdoor;
    this.pressurePlate = pressurePlate;
    this.button = button;
    this.sign = sign;
    this.wallSign = wallSign;
    ResourceLocation tagName = new ResourceLocation(name.getNamespace(), name.getPath() + "_logs");
    this.logBlockTag = TagKey.create(Registries.BLOCK, tagName);
    this.logItemTag = TagKey.create(Registries.ITEM, tagName);
  }

  public WoodBlockObject(ResourceLocation name, WoodType woodType, BuildingBlockObject planks,
                         Block log, Block strippedLog, Block wood, Block strippedWood,
                         Block fence, Block fenceGate, Block door, Block trapdoor,
                         Block pressurePlate, Block button, Block sign, Block wallSign) {
    super(planks, () -> (FenceBlock) fence);
    this.woodType = woodType;
    this.log = castDelegate(log);
    this.strippedLog = castDelegate(strippedLog);
    this.wood = castDelegate(wood);
    this.strippedWood = castDelegate(strippedWood);
    this.fenceGate = castDelegate((FenceGateBlock) fenceGate);
    this.door = castDelegate((DoorBlock) door);
    this.trapdoor = castDelegate((TrapDoorBlock) trapdoor);
    this.pressurePlate = castDelegate((PressurePlateBlock) pressurePlate);
    this.button = castDelegate((ButtonBlock) button);
    this.sign = castDelegate((StandingSignBlock) sign);
    this.wallSign = castDelegate((WallSignBlock) wallSign);
    ResourceLocation tagName = new ResourceLocation(name.getNamespace(), name.getPath() + "_logs");
    this.logBlockTag = TagKey.create(Registries.BLOCK, tagName);
    this.logItemTag = TagKey.create(Registries.ITEM, tagName);
  }

  /**
   * Gets the log for this wood type
   */
  public Block getLog() {
    return this.log.get();
  }

  /**
   * Gets the stripped log for this wood type
   */
  public Block getStrippedLog() {
    return this.strippedLog.get();
  }

  /**
   * Gets the wood for this wood type
   */
  public Block getWood() {
    return this.wood.get();
  }

  /**
   * Gets the stripped wood for this wood type
   */
  public Block getStrippedWood() {
    return this.strippedWood.get();
  }

  /* Doors */

  /**
   * Gets the fence gate for this wood type
   */
  public FenceGateBlock getFenceGate() {
    return this.fenceGate.get();
  }

  /**
   * Gets the door for this wood type
   */
  public DoorBlock getDoor() {
    return this.door.get();
  }

  /**
   * Gets the trapdoor for this wood type
   */
  public TrapDoorBlock getTrapdoor() {
    return this.trapdoor.get();
  }

  /* Redstone */

  /**
   * Gets the pressure plate for this wood type
   */
  public PressurePlateBlock getPressurePlate() {
    return this.pressurePlate.get();
  }

  /**
   * Gets the button for this wood type
   */
  public ButtonBlock getButton() {
    return this.button.get();
  }

  /* Signs */

  /* Gets the sign for this wood type, can also be used to get the item */
  public StandingSignBlock getSign() {
    return this.sign.get();
  }

  /* Gets the wall sign for this wood type */
  public WallSignBlock getWallSign() {
    return this.wallSign.get();
  }

  @Override
  public List<Block> values() {
    return Arrays.asList(
      this.get(), this.getSlab(), this.getStairs(), this.getFence(),
      this.getLog(), this.getStrippedLog(), this.getWood(), this.getStrippedWood(),
      this.getFenceGate(), this.getDoor(), this.getTrapdoor(),
      this.getPressurePlate(), this.getButton(), this.getSign(), this.getWallSign());
  }

  /**
   * Variants of wood for the register function
   */
  public enum WoodVariant {LOG, WOOD, PLANKS}
}
