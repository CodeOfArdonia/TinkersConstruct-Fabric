package slimeknights.tconstruct.smeltery.block.entity.component;

import io.github.fabricators_of_create.porting_lib.block.CustomDataPacketHandlingBlockEntity;
import io.github.fabricators_of_create.porting_lib.block.CustomUpdateTagHandlingBlockEntity;
import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.util.BlockEntityHelper;
import slimeknights.tconstruct.smeltery.TinkerSmeltery;
import slimeknights.tconstruct.smeltery.block.entity.component.SmelteryInputOutputBlockEntity.SmelteryFluidIO;
import slimeknights.tconstruct.smeltery.block.entity.tank.IDisplayFluidListener;
import slimeknights.tconstruct.smeltery.block.entity.tank.ISmelteryTankHandler;

import java.util.Objects;

/**
 * Fluid IO extension to display controller fluid
 */
public class DrainBlockEntity extends SmelteryFluidIO implements IDisplayFluidListener, CustomUpdateTagHandlingBlockEntity, CustomDataPacketHandlingBlockEntity {

  @Getter
  private FluidStack displayFluid = FluidStack.EMPTY;

  public DrainBlockEntity(BlockPos pos, BlockState state) {
    this(TinkerSmeltery.drain.get(), pos, state);
  }

  protected DrainBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
    super(type, pos, state);
  }

  @Override
  public void notifyDisplayFluidUpdated(FluidStack fluid) {
    if (!fluid.isFluidEqual(this.displayFluid)) {
      // no need to copy as the fluid was copied by the caller
      this.displayFluid = fluid;
      this.getModelData().setData(IDisplayFluidListener.PROPERTY, this.displayFluid);
      assert this.level != null;
      BlockState state = this.getBlockState();
      this.level.sendBlockUpdated(this.worldPosition, state, state, 48);
    }
  }

  @Override
  public BlockPos getListenerPos() {
    return this.getBlockPos();
  }


  /* Updating */

  /**
   * Attaches this TE to the master as a display fluid listener
   */
  private void attachFluidListener() {
    BlockPos masterPos = this.getMasterPos();
    if (masterPos != null && this.level != null && this.level.isClientSide) {
      BlockEntityHelper.get(ISmelteryTankHandler.class, this.level, masterPos).ifPresent(te -> te.addDisplayListener(this));
    }
  }

  // override instead of writeSynced to avoid writing master to the main tag twice
  @Override
  public CompoundTag getUpdateTag() {
    CompoundTag nbt = super.getUpdateTag();
    this.writeMaster(nbt);
    return nbt;
  }

  @Override
  public void handleUpdateTag(CompoundTag tag) {
    BlockPos oldMaster = this.getMasterPos();
    CustomUpdateTagHandlingBlockEntity.super.handleUpdateTag(tag);
    if (!Objects.equals(oldMaster, this.getMasterPos())) {
      this.attachFluidListener();
    }
  }

  @Override
  protected boolean shouldSyncOnUpdate() {
    return true;
  }

  @Override
  public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
    CompoundTag tag = pkt.getTag();
    if (tag != null) {
      BlockPos oldMaster = this.getMasterPos();
      this.load(tag);
      if (!Objects.equals(oldMaster, this.getMasterPos())) {
        this.attachFluidListener();
      }

    }
  }
}
