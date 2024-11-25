package slimeknights.tconstruct.smeltery.block.entity.controller;

import io.github.fabricators_of_create.porting_lib.block.CustomRenderBoundingBoxBlockEntity;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import io.github.fabricators_of_create.porting_lib.transfer.item.ItemHandlerHelper;
import lombok.Getter;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.SlottedStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageUtil;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SidedStorageBlockEntity;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import slimeknights.mantle.block.entity.IRetexturedBlockEntity;
import slimeknights.mantle.block.entity.NameableBlockEntity;
import slimeknights.mantle.client.model.data.IModelData;
import slimeknights.mantle.client.model.data.ModelDataMap;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.mantle.util.RetexturedHelper;
import slimeknights.tconstruct.common.multiblock.IMasterLogic;
import slimeknights.tconstruct.common.multiblock.IServantLogic;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.smeltery.block.controller.ControllerBlock;
import slimeknights.tconstruct.smeltery.block.controller.SmelteryControllerBlock;
import slimeknights.tconstruct.smeltery.block.entity.module.EntityMeltingModule;
import slimeknights.tconstruct.smeltery.block.entity.module.FuelModule;
import slimeknights.tconstruct.smeltery.block.entity.module.MeltingModuleInventory;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.HeatingStructureMultiblock.StructureData;
import slimeknights.tconstruct.smeltery.block.entity.multiblock.MultiblockResult;
import slimeknights.tconstruct.smeltery.block.entity.tank.IDisplayFluidListener;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;
import slimeknights.tconstruct.smeltery.block.entity.tank.SmelteryTank;
import slimeknights.tconstruct.smeltery.menu.HeatingStructureContainerMenu;
import slimeknights.tconstruct.smeltery.network.StructureErrorPositionPacket;
import slimeknights.tconstruct.smeltery.network.StructureUpdatePacket;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import static slimeknights.mantle.util.RetexturedHelper.TAG_TEXTURE;

public abstract class HeatingStructureBlockEntity extends NameableBlockEntity implements IMasterLogic, ISmelteryTankHandler, IRetexturedBlockEntity, SidedStorageBlockEntity, CustomRenderBoundingBoxBlockEntity {

  private static final String TAG_STRUCTURE = "structure";
  private static final String TAG_TANK = "tank";
  private static final String TAG_INVENTORY = "inventory";
  private static final String TAG_ERROR_POS = "errorPos";

  /**
   * Ticker instance for the serverside
   */
  public static final BlockEntityTicker<HeatingStructureBlockEntity> SERVER_TICKER = (level, pos, state, self) -> self.serverTick(level, pos, state);
  /**
   * Ticker instance for the clientside
   */
  public static final BlockEntityTicker<HeatingStructureBlockEntity> CLIENT_TICKER = (level, pos, state, self) -> self.clientTick(level, pos, state);

  /**
   * Sub module to detect the multiblock for this structure
   */
  private final HeatingStructureMultiblock<?> multiblock = this.createMultiblock();

  /**
   * Position of the block causing the structure to not form
   */
  @Nullable
  @Getter
  private BlockPos errorPos;
  /**
   * Number of ticks the error will remain visible for
   */
  private int errorVisibleFor = 0;
  /**
   * Temporary hack until forge fixes {@link #onLoad()}, do a first tick listener here as drains don't tick
   */
  private boolean addedDrainListeners = false;

  /* Saved data, written to Tag */
  /**
   * Current structure contents
   */
  @Nullable
  @Getter
  protected StructureData structure;
  /**
   * Tank instance for this smeltery
   */
  @Getter
  protected final SmelteryTank<HeatingStructureBlockEntity> tank = new SmelteryTank<>(this);
  /**
   * Capability to pass to drains for fluid handling
   */
  @Nullable
  @Getter
  private SlottedStorage<FluidVariant> fluidCapability;

  /**
   * Inventory handling melting items
   */
  @Getter
  protected final MeltingModuleInventory meltingInventory = this.createMeltingInventory();

  /**
   * Fuel module
   */
  @Getter
  protected final FuelModule fuelModule = new FuelModule(this, () -> this.structure != null ? this.structure.getTanks() : Collections.emptyList());
  /**
   * Current fuel consumption rate
   */
  protected int fuelRate = 81;


  /**
   * Module handling entity interaction
   */
  protected final EntityMeltingModule entityModule = new EntityMeltingModule(this, this.tank, this::canMeltEntities, this::insertIntoInventory, () -> this.structure == null ? null : this.structure.getBounds());


  /* Instance data, this data is not written to Tag */
  /**
   * Timer to allow delaying actions based on number of ticks alive
   */
  protected int tick = 0;
  /**
   * Updates every second. Once it reaches 10, checks above the smeltery for a layer to see if we can expand up
   */
  private int expandCounter = 0;
  /**
   * If true, structure will check for an update next tick
   */
  private boolean structureUpdateQueued = false;
  /**
   * If true, fluids have changed since the last update and should be synced to the client, synced at most once every 4 ticks
   */
  private boolean fluidUpdateQueued = false;
  /**
   * Cache of the bounds for the case of no structure
   */
  private AABB defaultBounds;
  @Nonnull
  @Getter
  private Block texture = Blocks.AIR;

  /* Client display */
  @Getter
  private final IModelData modelData = new ModelDataMap.Builder().withProperty(RetexturedHelper.BLOCK_PROPERTY).withProperty(IDisplayFluidListener.PROPERTY).build();
  private final List<WeakReference<IDisplayFluidListener>> fluidDisplayListeners = new ArrayList<>();

  /* Misc helpers */
  /**
   * Function to drop an item
   */
  protected final Consumer<ItemStack> dropItem = this::dropItem;

  protected HeatingStructureBlockEntity(BlockEntityType<? extends HeatingStructureBlockEntity> type, BlockPos pos, BlockState state, Component name) {
    super(type, pos, state, name);
  }

  /* Abstract methods */

  /**
   * Creates the multiblock for this tile
   */
  protected abstract HeatingStructureMultiblock<?> createMultiblock();

  /**
   * Creates the melting inventory for this structure
   */
  protected abstract MeltingModuleInventory createMeltingInventory();

  /**
   * Called while active to heat the contained items
   */
  protected abstract void heat();


  /* Logic */

  /**
   * Updates the error position and syncs to the client if relevant
   */
  private void updateErrorPos() {
    BlockPos oldErrorPos = this.errorPos;
    this.errorPos = this.multiblock.getLastResult().getPos();
    if (!Objects.equals(oldErrorPos, this.errorPos)) {
      TinkerNetwork.getInstance().sendToClientsAround(new StructureErrorPositionPacket(this.worldPosition, this.errorPos), this.level, this.worldPosition);
    }
  }

  /**
   * Handles the client tick
   */
  protected void clientTick(Level level, BlockPos pos, BlockState state) {
    if (this.errorVisibleFor > 0) {
      this.errorVisibleFor--;
    }
    if (!this.addedDrainListeners) {
      this.addedDrainListeners = true;
      if (this.structure != null) {
        this.structure.forEachContained(sPos -> {
          if (level.getBlockEntity(sPos) instanceof IDisplayFluidListener listener) {
            this.fluidDisplayListeners.add(new WeakReference<>(listener));
          }
        });
        // if we have listeners and a fluid, send a first update
        if (!this.fluidDisplayListeners.isEmpty()) {
          FluidStack fluid = IDisplayFluidListener.normalizeFluid(this.tank.getFluidInTank(0));
          if (!fluid.isEmpty()) {
            this.updateListeners(fluid);
          }
        }
      }
    }
  }

  /**
   * Handles the server tick
   */
  protected void serverTick(Level level, BlockPos pos, BlockState state) {
    if (level.isClientSide) {
      if (this.errorVisibleFor > 0) {
        this.errorVisibleFor--;
      }
      return;
    }
    // invalid state, just a safety check in case its air somehow
    if (!state.hasProperty(ControllerBlock.IN_STRUCTURE)) {
      return;
    }

    // run structure update if requested
    if (this.structureUpdateQueued) {
      this.checkStructure();
      this.structureUpdateQueued = false;
    }

    // if we have a structure, run smeltery logic
    if (this.structure != null && state.getValue(SmelteryControllerBlock.IN_STRUCTURE)) {
      // every 15 seconds, check above the smeltery to try to expand
      if (this.tick == 0) {
        this.expandCounter++;
        if (this.expandCounter >= 10 && this.structure.getInnerY() < this.multiblock.getMaxHeight()) {
          this.expandCounter = 0;
          // instead of rechecking the whole structure, just recheck the layer above and queue an update if its usable
          if (this.multiblock.canExpand(this.structure, level)) {
            this.updateStructure();
          } else {
            this.updateErrorPos();
          }
        }
      } else if (this.tick % 4 == 0) {
        // check the next inside position to see if its a valid inner block every other tick
        if (!this.multiblock.isInnerBlock(level, this.structure.getNextInsideCheck())) {
          this.updateStructure();
        }
      }

      // main heating logic
      this.heat();

      // fluid update sync every four ticks, whether it has tanks or not
      if (this.tick % 4 == 3) {
        if (this.fluidUpdateQueued) {
          this.fluidUpdateQueued = false;
          this.tank.syncFluids();
        }
      }
    } else if (this.tick == 0) {
      this.updateStructure();
    }

    // update tick timer
    this.tick = (this.tick + 1) % 20;
  }

  /**
   * Drops an item into the level
   *
   * @param stack Item to drop
   */
  protected void dropItem(ItemStack stack) {
    assert this.level != null;
    if (!this.level.isClientSide && !stack.isEmpty()) {
      double x = (double) (this.level.random.nextFloat() * 0.5F) + 0.25D;
      double y = (double) (this.level.random.nextFloat() * 0.5F) + 0.25D;
      double z = (double) (this.level.random.nextFloat() * 0.5F) + 0.25D;
      BlockPos pos = this.worldPosition.relative(this.getBlockState().getValue(ControllerBlock.FACING));
      ItemEntity itementity = new ItemEntity(this.level, (double) pos.getX() + x, (double) pos.getY() + y, (double) pos.getZ() + z, stack);
      itementity.setDefaultPickUpDelay();
      this.level.addFreshEntity(itementity);
    }
  }


  /* Capability */

  @Nonnull
  @Override
  public Storage<ItemVariant> getItemStorage(@Nullable Direction direction) {
    return this.meltingInventory;
  }

  /* Structure */

  /**
   * Marks the smeltery for a structure check
   */
  public void updateStructure() {
    this.structureUpdateQueued = true;
  }

  /**
   * Sets the structure and updates results of the new size, good method to override
   *
   * @param structure New structure
   */
  protected void setStructure(@Nullable StructureData structure) {
    this.structure = structure;
  }

  /**
   * Attempts to locate a valid smeltery structure
   */
  protected void checkStructure() {
    if (this.level == null || this.level.isClientSide) {
      return;
    }
    boolean wasFormed = this.getBlockState().getValue(ControllerBlock.IN_STRUCTURE);
    StructureData oldStructure = this.structure;
    StructureData newStructure = this.multiblock.detectMultiblock(this.level, this.worldPosition, this.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING));

    // update block state
    boolean formed = newStructure != null;
    if (formed != wasFormed) {
      this.level.setBlockAndUpdate(this.worldPosition, this.getBlockState().setValue(ControllerBlock.IN_STRUCTURE, formed));
    }

    // structure info updates
    if (formed) {
      // sync size to the client
      TinkerNetwork.getInstance().sendToClientsAround(
        new StructureUpdatePacket(this.worldPosition, newStructure.getMinPos(), newStructure.getMaxPos(), newStructure.getTanks()), this.level, this.worldPosition);

      // update tank capability, do first for update listeners on the drain blocks
      if (this.fluidCapability == null) {
        this.fluidCapability = this.tank;
      }

      // set master positions
      newStructure.assignMaster(this, oldStructure);
      this.setStructure(newStructure);
    } else {
      // remove tank capability
      if (this.fluidCapability != null) {
        this.fluidCapability = null;
      }

      // clear positions
      if (oldStructure != null) {
        oldStructure.clearMaster(this);
      }
      this.setStructure(null);
    }

    // update the error position, we do on both success and failure for the sake of expanding positions
    this.updateErrorPos();

    // clear expand counter either way
    this.expandCounter = 0;
  }

  /**
   * Called when the controller is broken to invalidate the master in all servants
   */
  public void invalidateStructure() {
    if (this.structure != null) {
      this.structure.clearMaster(this);
      this.structure = null;
      this.errorPos = null;
    }
  }

  @Override
  public void notifyChange(IServantLogic servant, BlockPos pos, BlockState state) {
    // structure invalid? can ignore this, will automatically check later
    if (this.structure == null) {
      return;
    }

    assert this.level != null;
    if (this.multiblock.shouldUpdate(this.level, this.structure, pos, state)) {
      this.updateStructure();
    }
  }

  /**
   * Gets the last result from this multiblock
   */
  public MultiblockResult getStructureResult() {
    return this.multiblock.getLastResult();
  }

  /* Tank */

  @Override
  public void updateFluidsFromPacket(List<FluidStack> fluids) {
    this.tank.setFluids(fluids);
  }

  /**
   * Updates all fluid display listeners
   */
  private void updateListeners(FluidStack fluid) {
    Iterator<WeakReference<IDisplayFluidListener>> iterator = this.fluidDisplayListeners.iterator();
    while (iterator.hasNext()) {
      IDisplayFluidListener listener = iterator.next().get();
      if (listener == null) {
        iterator.remove();
      } else {
        listener.notifyDisplayFluidUpdated(fluid);
      }
    }
  }

  /**
   * Updates the fluid displayed in the block, only used client side
   *
   * @param fluid Fluid
   */
  private void updateDisplayFluid(FluidStack fluid) {
    if (this.level != null && this.level.isClientSide) {
      // update ourself
      fluid = IDisplayFluidListener.normalizeFluid(fluid);
      this.modelData.setData(IDisplayFluidListener.PROPERTY, fluid);
      BlockState state = this.getBlockState();
      this.level.sendBlockUpdated(this.worldPosition, state, state, 48);
      this.updateListeners(fluid);
    }
  }

  @Override
  public void addDisplayListener(IDisplayFluidListener listener) {
    boolean have = false;
    for (WeakReference<IDisplayFluidListener> existing : this.fluidDisplayListeners) {
      if (existing.get() == listener) {
        have = true;
        break;
      }
    }
    if (!have) {
      this.fluidDisplayListeners.add(new WeakReference<>(listener));
    }
    listener.notifyDisplayFluidUpdated(IDisplayFluidListener.normalizeFluid(this.tank.getFluidInTank(0)));
  }

  @Override
  public void notifyFluidsChanged(FluidChange type, FluidStack fluid) {
    if (type == FluidChange.ORDER_CHANGED) {
      this.updateDisplayFluid(fluid);
    } else {
      // mark that fluids need an update on the client
      this.fluidUpdateQueued = true;
      this.setChangedFast();
    }
  }

  @Override
  public AABB getRenderBoundingBox() {
    if (this.structure != null) {
      return this.structure.getBounds();
    } else if (this.defaultBounds == null) {
      this.defaultBounds = new AABB(this.worldPosition, this.worldPosition.offset(1, 1, 1));
    }
    return this.defaultBounds;
  }

  /* Heating helpers */

  /**
   * Checks if we can melt entities
   *
   * @return True if we can melt entities
   */
  private boolean canMeltEntities() {
    if (this.fuelModule.hasFuel()) {
      return true;
    }
    return this.fuelModule.findFuel(false) > 0;
  }

  /**
   * Inserts an item into the inventory
   *
   * @param stack Stack to insert
   */
  private ItemStack insertIntoInventory(ItemStack stack) {
    try (Transaction t = TransferUtil.getTransaction()) {
      long inserted = StorageUtil.tryInsertStacking(this.meltingInventory, ItemVariant.of(stack), stack.getCount(), t);
      t.commit();
      return ItemHandlerHelper.copyStackWithSize(stack, (int) (stack.getCount() - inserted));
    }
  }


  /* UI and sync */

  @Nullable
  @Override
  public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
    return new HeatingStructureContainerMenu(id, inv, this);
  }

  /**
   * Sets the structure info on the client side
   *
   * @param minPos Min structure position
   * @param maxPos Max structure position
   */
  public void setStructureSize(BlockPos minPos, BlockPos maxPos, List<BlockPos> tanks) {
    this.setStructure(this.multiblock.createClient(minPos, maxPos, tanks));
    this.fuelModule.clearCachedDisplayListeners();
    if (this.structure == null) {
      this.fluidDisplayListeners.clear();
    } else {
      this.fluidDisplayListeners.removeIf(reference -> {
        IDisplayFluidListener listener = reference.get();
        return listener == null || !this.structure.contains(listener.getListenerPos());
      });
    }
  }

  /**
   * Updates the error position from the server
   */
  public void setErrorPos(@Nullable BlockPos errorPos) {
    this.errorPos = errorPos;
    if (errorPos != null && this.level != null) {
      // 10 seconds after its set
      this.errorVisibleFor = 200;
    }
  }

  /**
   * If true, the error position should be visible
   */
  public boolean isHighlightError() {
    return this.errorVisibleFor > 0;
  }

  /**
   * If true, the given item triggers debug blocks
   */
  protected abstract boolean isDebugItem(ItemStack stack);

  /**
   * If true, debug blocks should show in the TESR to the given player
   */
  public boolean showDebugBlockBorder(Player player) {
    return this.isDebugItem(player.getMainHandItem())
      || this.isDebugItem(player.getOffhandItem())
      || this.isDebugItem(player.getItemBySlot(EquipmentSlot.HEAD));
  }


  /* Retexturing */

  @Override
  public String getTextureName() {
    return RetexturedHelper.getTextureName(this.texture);
  }

  @Override
  public void updateTexture(String name) {
    Block oldTexture = this.texture;
    this.texture = RetexturedHelper.getBlock(name);
    if (oldTexture != this.texture) {
      this.setChangedFast();
      RetexturedHelper.onTextureUpdated(this);
    }
  }



  /* Tag */

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void load(CompoundTag nbt) {
    super.load(nbt);
    if (nbt.contains(TAG_TANK, Tag.TAG_COMPOUND)) {
      this.tank.read(nbt.getCompound(TAG_TANK));
      FluidStack first = this.tank.getFluidInTank(0);
      if (!first.isEmpty()) {
        this.updateDisplayFluid(first);
      }
    }
    if (nbt.contains(TAG_INVENTORY, Tag.TAG_COMPOUND)) {
      this.meltingInventory.readFromTag(nbt.getCompound(TAG_INVENTORY));
    }
    if (nbt.contains(TAG_STRUCTURE, Tag.TAG_COMPOUND)) {
      this.setStructure(this.multiblock.readFromTag(nbt.getCompound(TAG_STRUCTURE)));
      if (this.structure != null) {
        this.fluidCapability = this.tank;
      }
    }
    // only exists to be sent server to client in update packets
    if (nbt.contains(TAG_ERROR_POS, Tag.TAG_COMPOUND)) {
      this.errorPos = NbtUtils.readBlockPos(nbt.getCompound(TAG_ERROR_POS));
    }
    this.fuelModule.readFromTag(nbt);
    if (nbt.contains(TAG_TEXTURE, Tag.TAG_STRING)) {
      this.texture = RetexturedHelper.getBlock(nbt.getString(TAG_TEXTURE));
      RetexturedHelper.onTextureUpdated(this);
    }
  }

  @Override
  public void saveAdditional(CompoundTag compound) {
    // Tag that just writes to disk
    super.saveAdditional(compound);
    if (this.structure != null) {
      compound.put(TAG_STRUCTURE, this.structure.writeToTag());
    }
    this.fuelModule.writeToTag(compound);
  }

  @Override
  public void saveSynced(CompoundTag compound) {
    // Tag that writes to disk and syncs to client
    super.saveSynced(compound);
    compound.put(TAG_TANK, this.tank.write(new CompoundTag()));
    compound.put(TAG_INVENTORY, this.meltingInventory.writeToTag());
    if (this.texture != Blocks.AIR) {
      compound.putString(TAG_TEXTURE, this.getTextureName());
    }
  }

  @Override
  public CompoundTag getUpdateTag() {
    // Tag that just syncs to client
    CompoundTag nbt = super.getUpdateTag();
    if (this.structure != null) {
      nbt.put(TAG_STRUCTURE, this.structure.writeClientTag());
    }
    // sync error position, not actually saved in Tag
    if (this.errorPos != null) {
      nbt.put(TAG_ERROR_POS, NbtUtils.writeBlockPos(this.errorPos));
    }
    return nbt;
  }


  /* Helpers */


  /**
   * Handles the unchecked cast for a block entity ticker
   */
  @Nullable
  public static <HAVE extends HeatingStructureBlockEntity, RET extends BlockEntity> BlockEntityTicker<RET> getTicker(Level level, BlockEntityType<RET> expected, BlockEntityType<HAVE> have) {
    return BlockEntityHelper.castTicker(expected, have, level.isClientSide ? CLIENT_TICKER : SERVER_TICKER);
  }

  @Override
  public IModelData getRenderData() {
    return this.modelData;
  }
}
