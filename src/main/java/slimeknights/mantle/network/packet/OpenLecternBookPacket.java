package slimeknights.mantle.network.packet;

import lombok.AllArgsConstructor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import slimeknights.mantle.item.ILecternBookItem;

/**
 * Packet to open a book on a lectern
 */
@AllArgsConstructor
public class OpenLecternBookPacket implements IThreadsafePacket {

  private final BlockPos pos;
  private final ItemStack book;

  public OpenLecternBookPacket(FriendlyByteBuf buffer) {
    this.pos = buffer.readBlockPos();
    this.book = buffer.readItem();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeBlockPos(this.pos);
    buffer.writeItem(this.book);
  }

  @Override
  public void handleThreadsafe(Context context) {
    if (this.book.getItem() instanceof ILecternBookItem) {
      ((ILecternBookItem) this.book.getItem()).openLecternScreenClient(this.pos, this.book);
    }
  }
}
