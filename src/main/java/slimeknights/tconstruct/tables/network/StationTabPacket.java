package slimeknights.tconstruct.tables.network;

import io.github.fabricators_of_create.porting_lib.util.NetworkHooks;
import lombok.RequiredArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.common.network.TinkerNetwork;
import slimeknights.tconstruct.tables.block.ITabbedBlock;

@RequiredArgsConstructor
public class StationTabPacket implements IThreadsafePacket {

  private final BlockPos pos;

  public StationTabPacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(this.pos);
  }

  @Override
  public void handleThreadsafe(Context context) {
    ServerPlayer sender = context.getSender();
    if (sender != null) {
      ItemStack heldStack = sender.containerMenu.getCarried();
      if (!heldStack.isEmpty()) {
        // set it to empty, so it's doesn't get dropped
        sender.containerMenu.setCarried(ItemStack.EMPTY);
      }

      Level world = sender.getCommandSenderWorld();
      if (!world.hasChunkAt(this.pos)) {
        return;
      }
      BlockState state = world.getBlockState(this.pos);
      if (state.getBlock() instanceof ITabbedBlock) {
        ((ITabbedBlock) state.getBlock()).openGui(sender, sender.getCommandSenderWorld(), this.pos);
      } else {
        MenuProvider provider = state.getMenuProvider(sender.getCommandSenderWorld(), this.pos);
        if (provider != null) {
          NetworkHooks.openScreen(sender, provider, this.pos);
        }
      }

      if (!heldStack.isEmpty()) {
        sender.containerMenu.setCarried(heldStack);
        TinkerNetwork.getInstance().sendVanillaPacket(sender, new ClientboundContainerSetSlotPacket(-1, -1, -1, heldStack));
      }
    }
  }
}
