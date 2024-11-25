package slimeknights.tconstruct.library.materials.traits;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.mantle.network.packet.IThreadsafePacket;
import slimeknights.tconstruct.library.materials.MaterialRegistry;
import slimeknights.tconstruct.library.materials.definition.MaterialId;

import java.util.HashMap;
import java.util.Map;

@Getter
@AllArgsConstructor
public class UpdateMaterialTraitsPacket implements IThreadsafePacket {

  protected final Map<MaterialId, MaterialTraits> materialToTraits;

  public UpdateMaterialTraitsPacket(FriendlyByteBuf buffer) {
    int materialCount = buffer.readInt();
    this.materialToTraits = new HashMap<>(materialCount);
    for (int i = 0; i < materialCount; i++) {
      MaterialId id = new MaterialId(buffer.readResourceLocation());
      MaterialTraits traits = MaterialTraits.read(buffer);
      this.materialToTraits.put(id, traits);
    }
  }

  @Override
  public void encode(FriendlyByteBuf buffer) {
    buffer.writeInt(this.materialToTraits.size());
    this.materialToTraits.forEach((materialId, traits) -> {
      buffer.writeResourceLocation(materialId);
      traits.write(buffer);
    });
  }

  @Override
  public void handleThreadsafe(Context context) {
    MaterialRegistry.updateMaterialTraitsFromServer(this);
  }
}
