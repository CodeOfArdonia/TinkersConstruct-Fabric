package slimeknights.tconstruct.smeltery.block.entity.controller;

import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.block.entity.NameableBlockEntity;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.fluid.FluidTankAnimated;
import slimeknights.tconstruct.library.utils.NBTTags;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.component.SearedTankBlock.TankType;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.controller.MelterBlock;
import slimeknights.tconstruct.smeltery.block.entity.ITankBlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;
import slimeknights.tconstruct.smeltery.block.entity.module.alloying.MixerAlloyTank;
import slimeknights.tconstruct.smeltery.block.entity.module.alloying.SingleAlloyingModule;
import slimeknights.tconstruct.smeltery.menu.AlloyerContainerMenu;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;

/**
 * Dedicated alloying block
 */
public class AlloyerBlockEntity extends NameableBlockEntity implements ITankBlockEntity, SidedStorageBlockEntity {

  /**
   * Max capacity for the tank
   */
  private static final long TANK_CAPACITY = TankType.INGOT_TANK.getCapacity();
  /**
   * Name of the container
   */
  private static final Component NAME = TConstruct.makeTranslation("gui", "alloyer");

  public static final BlockEntityTicker<AlloyerBlockEntity> SERVER_TICKER = (level, pos, state, self) -> self.tick(level, pos, state);

  /**
   * Tank for this mixer
   */
  @Getter
  protected final FluidTankAnimated tank = new FluidTankAnimated(TANK_CAPACITY, this);

  // modules
  /**
   * Logic for a mixer alloying
   */
  @Getter
  private final MixerAlloyTank alloyTank = new MixerAlloyTank(this, this.tank);
  /**
   * Base alloy logic
   */
  private final SingleAlloyingModule alloyingModule = new SingleAlloyingModule(this, this.alloyTank);
  /**
   * Fuel handling logic
   */
  @Getter
  private final FuelModule fuelModule = new FuelModule(this, () -> Collections.singletonList(this.worldPosition.below()));

  /**
   * Last comparator strength to reduce block updates
   */
  @Getter
  @Setter
  private int lastStrength = -1;

  /**
   * Internal tick counter
   */
  private int tick;

  public AlloyerBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.alloyer.get(), pos, state);
  }

  protected AlloyerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state, NAME);
  }

  /*
   * Capability
   */

  @Nonnull
  @Override
  public Storage<FluidVariant> getFluidStorage(@org.jetbrains.annotations.Nullable Direction direction) {
    return this.tank;
  }


  /*
   * Alloying
   */

  /**
   * Checks if the tile entity is active
   */
  private boolean isFormed() {
    BlockState state = this.getBlockState();
    return state.hasProperty(MelterBlock.IN_STRUCTURE) && state.getValue(MelterBlock.IN_STRUCTURE);
  }

  /**
   * Handles server tick
   */
  private void tick(Level level, BlockPos pos, BlockState state) {
    if (!this.isFormed()) {
      return;
    }

    switch (this.tick) {
      // tick 0: find fuel
      case 0 -> {
        this.alloyTank.setTemperature(this.fuelModule.findFuel(false));
        if (!this.fuelModule.hasFuel() && this.alloyingModule.canAlloy()) {
          this.fuelModule.findFuel(true);
        }
      }
      // tick 2: alloy alloys and consume fuel
      case 2 -> {
        boolean hasFuel = this.fuelModule.hasFuel();

        // update state for new fuel state
        if (state.getValue(ControllerBlock.ACTIVE) != hasFuel) {
          level.setBlockAndUpdate(pos, state.setValue(ControllerBlock.ACTIVE, hasFuel));
          // update the heater below
          BlockPos down = pos.below();
          BlockState downState = level.getBlockState(down);
          if (downState.is(TinkerTags.Blocks.FUEL_TANKS) && downState.hasProperty(ControllerBlock.ACTIVE) && downState.getValue(ControllerBlock.ACTIVE) != hasFuel) {
            level.setBlockAndUpdate(down, downState.setValue(ControllerBlock.ACTIVE, hasFuel));
          }
        }

        // actual alloying
        if (hasFuel) {
          this.alloyTank.setTemperature(this.fuelModule.getTemperature());
          this.alloyingModule.doAlloy();
          this.fuelModule.decreaseFuel(1);
        }
      }
    }
    this.tick = (this.tick + 1) % 4;
  }

  /**
   * Called when a neighbor of this block is changed to update the tank cache
   *
   * @param side Side changed
   */
  public void neighborChanged(Direction side) {
    this.alloyTank.refresh(side, true);
  }

  /*
   * Display
   */

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player playerEntity) {
    return new AlloyerContainerMenu(id, inv, this);
  }


  /*
   * NBT
   */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void saveSynced(CompoundTag tag) {
    super.saveSynced(tag);
    tag.put(NBTTags.TANK, this.tank.writeToNBT(new CompoundTag()));
  }

  @Override
  public void saveAdditional(CompoundTag tag) {
    super.saveAdditional(tag);
    this.fuelModule.writeToTag(tag);
  }

  @Override
  public void load(CompoundTag nbt) {
    super.load(nbt);
    this.tank.readFromNBT(nbt.getCompound(NBTTags.TANK));
    this.fuelModule.readFromTag(nbt);
  }
}
