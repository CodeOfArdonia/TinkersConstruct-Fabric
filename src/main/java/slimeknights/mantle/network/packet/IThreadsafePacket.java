package slimeknights.mantle.network.packet;

import java.util.function.Supplier;

/**
 * Packet instance that automatically wraps the logic in {@link Context#enqueueWork(Runnable)} for thread safety
 */
public interface IThreadsafePacket extends ISimplePacket {

  @Override
  default void handle(Supplier<Context> supplier) {
    Context context = supplier.get();
    context.enqueueWork(() -> this.handleThreadsafe(context));
    context.setPacketHandled(true);
  }

  /**
   * Handles receiving the packet on the correct thread
   * Packet is automatically set to handled as well by the base logic
   *
   * @param context Packet context
   */
  void handleThreadsafe(Context context);
}
