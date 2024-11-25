package slimeknights.tconstruct.smeltery.network;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeknights.tconstruct.smeltery.block.entity.FaucetBlockEntity;

/**
 * Sent to clients to activate the faucet animation clientside
 **/
public class FaucetActivationPacket extends FluidUpdatePacket {

  private final boolean isPouring;

  public FaucetActivationPacket(BlockPos pos, FluidStack fluid, boolean isPouring) {
    super(pos, fluid);
    this.isPouring = isPouring;
  }

  public FaucetActivationPacket(FriendlyByteBuf buffer) {
    super(buffer);
    this.isPouring = buffer.readBoolean();
  }

  @Override
  public void encode(FriendlyByteBuf packetBuffer) {
    super.encode(packetBuffer);
    packetBuffer.writeBoolean(this.isPouring);
  }

  @Override
  public void handleThreadsafe(Context context) {
    HandleClient.handle(this);
  }

  /**
   * Safely runs client side only code in a method only called on client
   */
  private static class HandleClient {

    private static void handle(FaucetActivationPacket packet) {
      assert Minecraft.getInstance().level != null;
      BlockEntity te = Minecraft.getInstance().level.getBlockEntity(packet.pos);
      if (te instanceof FaucetBlockEntity) {
        ((FaucetBlockEntity) te).onActivationPacket(packet.fluid, packet.isPouring);
      }
    }
  }
}
