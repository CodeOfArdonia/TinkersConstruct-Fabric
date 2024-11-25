package slimeknights.tconstruct.library.tools.definition;

import com.google.common.collect.ImmutableMap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import slimeknights.mantle.network.packet.IThreadsafePacket;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Packet to sync tool definitions to the client
 */
@RequiredArgsConstructor
public class UpdateToolDefinitionDataPacket implements IThreadsafePacket {

  @Getter(AccessLevel.PROTECTED)
  private final Map<ResourceLocation, ToolDefinitionData> dataMap;

  public UpdateToolDefinitionDataPacket(FriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    ImmutableMap.Builder<ResourceLocation, ToolDefinitionData> builder = ImmutableMap.builder();
    for (int i = 0; i < size; i++) {
      ResourceLocation name = buffer.readResourceLocation();
      ToolDefinitionData data = ToolDefinitionData.read(buffer);
      builder.put(name, data);
    }
    this.dataMap = builder.build();
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeVarInt(this.dataMap.size());
    for (Entry<ResourceLocation, ToolDefinitionData> entry : this.dataMap.entrySet()) {
      buffer.writeResourceLocation(entry.getKey());
      entry.getValue().write(buffer);
    }
  }

  @Override
  public void handleThreadsafe(Context context) {
    ToolDefinitionLoader.getInstance().updateDataFromServer(this.dataMap);
  }
}
