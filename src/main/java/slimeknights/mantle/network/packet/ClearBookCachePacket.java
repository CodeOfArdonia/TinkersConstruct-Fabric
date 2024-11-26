package slimeknights.mantle.network.packet;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.client.book.BookLoader;
import slimeknights.mantle.client.book.data.BookData;
import slimeknights.mantle.network.packet.OpenNamedBookPacket.ClientOnly;

import javax.annotation.Nullable;

/**
 * Packet sent by {@link slimeknights.mantle.command.ClearBookCacheCommand} to reset a book cache
 */
public record ClearBookCachePacket(@Nullable ResourceLocation book) implements IThreadsafePacket {

  public ClearBookCachePacket(FriendlyByteBuf buffer) {
    this(buffer.readBoolean() ? buffer.readResourceLocation() : null);
  }

  @Override
  public void encode(FriendlyByteBuf buf) {
    if (this.book != null) {
      buf.writeBoolean(true);
      buf.writeResourceLocation(this.book);
    } else {
      buf.writeBoolean(false);
    }
  }

  @Override
  public void handleThreadsafe(Context context) {
    if (this.book != null) {
      BookData bookData = BookLoader.getBook(this.book);
      if (bookData != null) {
        bookData.reset();
      } else {
        ClientOnly.errorStatus(this.book);
      }
    } else {
      BookLoader.resetAllBooks();
    }
  }
}
