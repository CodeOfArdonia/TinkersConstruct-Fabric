package slimeknights.tconstruct.shared.network;

import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.shared.client.ClientGeneratePartTexturesCommand;

/**
 * Packet to tell the client to generate tool textures
 */
@RequiredArgsConstructor
public class GeneratePartTexturesPacket implements IThreadsafePacket {

  private final Operation operation;
  private final String modId;
  private final String materialPath;

  public GeneratePartTexturesPacket(FriendlyByteBuf buffer) {
    this.operation = buffer.readEnum(Operation.class);
    this.modId = buffer.readUtf(Short.MAX_VALUE);
    this.materialPath = buffer.readUtf(Short.MAX_VALUE);
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeEnum(this.operation);
    buffer.writeUtf(this.modId);
    buffer.writeUtf(this.materialPath);
  }

  @Override
  public void handleThreadsafe(Context context) {
    context.enqueueWork(() -> ClientGeneratePartTexturesCommand.generateTextures(this.operation, this.modId, this.materialPath));
  }

  public enum Operation {ALL, MISSING}
}
