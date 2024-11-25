package slimeknights.tconstruct.gadgets.entity;

import io.github.fabricators_of_create.porting_lib.entity.IEntityAdditionalSpawnData;
import io.github.fabricators_of_create.porting_lib.entity.PortingLibEntity;
import net.fabricmc.fabric.api.entity.EntityPickInteractionAware;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import slimeknights.tconstruct.common.Sounds;
import slimeknights.tconstruct.gadgets.TinkerGadgets;
import slimeknights.tconstruct.library.utils.Util;

public class FancyItemFrameEntity extends ItemFrame implements EntityPickInteractionAware, IEntityAdditionalSpawnData {

  private static final int DIAMOND_TIMER = 300;
  private static final EntityDataAccessor<Integer> VARIANT = SynchedEntityData.defineId(FancyItemFrameEntity.class, EntityDataSerializers.INT);
  private static final String TAG_VARIANT = "Variant";
  private static final String TAG_ROTATION_TIMER = "RotationTimer";

  private int rotationTimer = 0;

  public FancyItemFrameEntity(EntityType<? extends FancyItemFrameEntity> type, Level level) {
    super(type, level);
  }

  public FancyItemFrameEntity(Level levelIn, BlockPos blockPos, Direction face, FrameType variant) {
    super(TinkerGadgets.itemFrameEntity.get(), levelIn);
    this.pos = blockPos;
    this.setDirection(face);
    this.entityData.set(VARIANT, variant.getId());
  }

  /**
   * Quick helper as two types spin
   */
  private static boolean doesRotate(int type) {
    return type == FrameType.GOLD.getId() || type == FrameType.REVERSED_GOLD.getId() || type == FrameType.DIAMOND.getId();
  }

  /**
   * Resets the rotation timer to 0
   */
  public void updateRotationTimer(boolean overturn) {
    this.rotationTimer = overturn ? -DIAMOND_TIMER : 0;
  }

  @Override
  public InteractionResult interact(Player player, InteractionHand hand) {
    if (!player.isShiftKeyDown() && this.getFrameId() == FrameType.CLEAR.getId() && !this.getItem().isEmpty()) {
      BlockPos behind = this.blockPosition().relative(this.direction.getOpposite());
      BlockState state = this.level().getBlockState(behind);
      if (!state.isAir()) {
        InteractionResult result = state.use(this.level(), player, hand, Util.createTraceResult(behind, this.direction, false));
        if (result.consumesAction()) {
          return result;
        }
      }
    }
    return super.interact(player, hand);
  }

  @Override
  public void tick() {
    super.tick();
    // diamond spins on both sides
    int frameId = this.getFrameId();
    if (frameId == FrameType.DIAMOND.getId()) {
      this.rotationTimer++;
      // diamond winds down every 30 seconds, but does not go past 0, makes a full timer 3:30
      if (this.rotationTimer >= 300) {
        this.rotationTimer = 0;
        if (!this.level().isClientSide) {
          int curRotation = this.getRotation();
          if (curRotation > 0) {
            this.setRotation(curRotation - 1);
          }
        }
      }
      return;
    }
    // for gold and reversed gold, only increment timer serverside
    if (!this.level().isClientSide) {
      if (doesRotate(frameId)) {
        this.rotationTimer++;
        if (this.rotationTimer >= 20) {
          this.rotationTimer = 0;
          int curRotation = this.getRotation();
          if (frameId == FrameType.REVERSED_GOLD.getId()) {
            // modulo is not positive bounded, so we have to manually ensure positive
            curRotation -= 1;
            if (curRotation == -1) {
              curRotation = 7;
            }
            this.setRotation(curRotation);
          } else {
            this.setRotation(curRotation + 1);
          }
        }
      }
    }
  }

  @Override
  public void setItem(ItemStack stack, boolean updateComparator) {
    super.setItem(stack, updateComparator);
    // spinning frames reset to 0 on changing item
    if (updateComparator && !this.level().isClientSide && doesRotate(this.getFrameId())) {
      this.setRotation(0, false);
    }
  }

  /**
   * Internal logic to set the rotation
   */
  private void setRotationRaw(int rotationIn, boolean updateComparator) {
    this.getEntityData().set(DATA_ROTATION, rotationIn);
    if (updateComparator) {
      this.level().updateNeighbourForOutputSignal(this.pos, Blocks.AIR);
    }
  }

  @Override
  protected void setRotation(int rotationIn, boolean updateComparator) {
    this.rotationTimer = 0;
    // diamond goes 0-8 rotation, no modulo and needs to sync with client
    if (this.getFrameId() == FrameType.DIAMOND.getId()) {
      if (!this.level().isClientSide && updateComparator) {
        // play a sound as diamond is special
        this.playSound(Sounds.ITEM_FRAME_CLICK.getSound(), 1.0f, 1.0f);
      }
      // diamond allows rotation between 0 and 16
      this.setRotationRaw(Math.min(rotationIn, 16), updateComparator);
    } else {
      // non diamond rotates around after 7
      this.setRotationRaw(rotationIn % 8, updateComparator);
    }
  }

  @Override
  protected void defineSynchedData() {
    super.defineSynchedData();
    this.entityData.define(VARIANT, 0);
  }

  /**
   * Gets the frame type
   */
  public FrameType getFrameType() {
    return FrameType.byId(this.getFrameId());
  }

  /**
   * Gets the frame type
   */
  public Item getFrameItem() {
    return TinkerGadgets.itemFrame.get(this.getFrameType());
  }

  /**
   * Gets the index of the frame type
   */
  protected int getFrameId() {
    return this.entityData.get(VARIANT);
  }

  @Override
  protected ItemStack getFrameItemStack() {
    return new ItemStack(this.getFrameItem());
  }

  @Override
  public ItemStack getPickedStack(Player player, HitResult target) {
    ItemStack held = this.getItem();
    if (held.isEmpty()) {
      return new ItemStack(this.getFrameItem());
    } else {
      return held.copy();
    }
  }

  @Override
  public boolean fireImmune() {
    return super.fireImmune() || this.getFrameId() == FrameType.NETHERITE.getId();
  }

  @Override
  public boolean ignoreExplosion() {
    return super.ignoreExplosion() || this.getFrameId() == FrameType.NETHERITE.getId();
  }

  @Override
  public int getAnalogOutput() {
    if (this.getItem().isEmpty()) {
      return 0;
    }
    int rotation = this.getRotation();
    if (this.getFrameId() == FrameType.DIAMOND.getId()) {
      return Math.min(15, rotation + 1);
    }
    return rotation % 8 + 1;
  }


  @Override
  public void addAdditionalSaveData(CompoundTag compound) {
    super.addAdditionalSaveData(compound);
    int frameId = this.getFrameId();
    compound.putInt(TAG_VARIANT, frameId);
    if (doesRotate(frameId)) {
      compound.putInt(TAG_ROTATION_TIMER, this.rotationTimer);
    }
  }

  @Override
  public void readAdditionalSaveData(CompoundTag compound) {
    super.readAdditionalSaveData(compound);
    int frameId = compound.getInt(TAG_VARIANT);
    this.entityData.set(VARIANT, frameId);
    if (doesRotate(frameId)) {
      this.rotationTimer = compound.getInt(TAG_ROTATION_TIMER);
    }
  }

  @Override
  public Packet<ClientGamePacketListener> getAddEntityPacket() {
    return PortingLibEntity.getEntitySpawningPacket(this);
  }

  @Override
  public void writeSpawnData(FriendlyByteBuf buffer) {
    buffer.writeVarInt(this.getFrameId());
    buffer.writeBlockPos(this.pos);
    buffer.writeVarInt(this.direction.get3DDataValue());
  }

  @Override
  public void readSpawnData(FriendlyByteBuf buffer) {
    this.entityData.set(VARIANT, buffer.readVarInt());
    this.pos = buffer.readBlockPos();
    this.setDirection(Direction.from3DDataValue(buffer.readVarInt()));
  }


  @Override
  protected Component getTypeName() {
    return Component.translatable(this.getFrameItem().getDescriptionId());
  }
}
