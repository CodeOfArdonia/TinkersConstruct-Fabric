package slimeknights.tconstruct.common.network;

import io.github.fabricators_of_create.porting_lib.transfer.item.SlottedStackStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import slimeknights.mantle.network.packet.IThreadsafePacket;

import java.util.Optional;

public class InventorySlotSyncPacket implements IThreadsafePacket {

  public final ItemStack itemStack;
  public final int slot;
  public final BlockPos pos;

  public InventorySlotSyncPacket(ItemStack itemStack, int slot, BlockPos pos) {
    this.itemStack = itemStack;
    this.slot = slot;
    this.pos = pos;
  }

  public InventorySlotSyncPacket(FriendlyByteBuf buffer) {
    this.itemStack = buffer.readItem();
    this.slot = buffer.readShort();
    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(FriendlyByteBuf packetBuffer) {
    packetBuffer.writeItem(this.itemStack);
    packetBuffer.writeShort(this.slot);
    packetBuffer.writeBlockPos(this.pos);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  /**
   * Safely runs client side only code in a method only called on client
   */
  private static class HandleClient {

    private static void handle(InventorySlotSyncPacket packet) {
      Level world = Minecraft.getInstance().level;
      if (world != null) {
        Optional.ofNullable(ItemStorage.SIDED.find(world, packet.pos, null))
          .filter(cap -> cap instanceof SlottedStackStorage)
          .ifPresent(cap -> {
            ((SlottedStackStorage) cap).setStackInSlot(packet.slot, packet.itemStack);
            //noinspection ConstantConditions
            Minecraft.getInstance().levelRenderer.blockChanged(null, packet.pos, null, null, 0);
          });
      }
    }
  }
}
