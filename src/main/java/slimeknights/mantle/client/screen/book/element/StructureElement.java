package slimeknights.mantle.client.screen.book.element;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import slimeknights.mantle.client.book.structure.StructureInfo;
import slimeknights.mantle.client.book.structure.level.TemplateLevel;
import slimeknights.mantle.client.render.MantleRenderTypes;
import slimeknights.mantle.client.screen.book.BookScreen;

import java.util.List;
import java.util.stream.IntStream;

public class StructureElement extends SizedBookElement {

  public boolean canTick = false;

  public float scale = 50f;
  public float transX = 0;
  public float transY = 0;
  public Transformation additionalTransform;
  public final StructureInfo renderInfo;
  public final TemplateLevel structureWorld;

  public long lastStep = -1;
  public long lastPrintedErrorTimeMs = -1;

  public StructureElement(int x, int y, int width, int height, StructureTemplate template, List<StructureTemplate.StructureBlockInfo> structure) {
    super(x, y, width, height);

    int[] size = {template.getSize().getX(), template.getSize().getY(), template.getSize().getZ()};

    this.scale = 100f / (float) IntStream.of(size).max().getAsInt();

    float sx = (float) width / (float) BookScreen.PAGE_WIDTH;
    float sy = (float) height / (float) BookScreen.PAGE_HEIGHT;

    this.scale *= Math.min(sx, sy);

    this.renderInfo = new StructureInfo(structure);

    this.structureWorld = new TemplateLevel(structure, this.renderInfo);

    this.transX = x + width / 2F;
    this.transY = y + height / 2F;

    this.additionalTransform = new Transformation(null, new Quaternionf().rotationXYZ(25 * (float) (Math.PI / 180.0), 0, 0), null, new Quaternionf().rotationXYZ(0, -45 * (float) (Math.PI / 180.0), 0));
  }

  @Override
  public void draw(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks, Font fontRenderer) {
    MultiBufferSource.BufferSource buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
    PoseStack transform = guiGraphics.pose();
    PoseStack.Pose lastEntryBeforeTry = transform.last();

    try {
      long currentTime = System.currentTimeMillis();

      if (this.lastStep < 0)
        this.lastStep = currentTime;
      else if (this.canTick && currentTime - this.lastStep > 200) {
        this.renderInfo.step();
        this.lastStep = currentTime;
      }

      if (!this.canTick) {
        this.renderInfo.reset();
      }

      int structureLength = this.renderInfo.structureLength;
      int structureWidth = this.renderInfo.structureWidth;
      int structureHeight = this.renderInfo.structureHeight;

      transform.pushPose();

      final BlockRenderDispatcher blockRender = Minecraft.getInstance().getBlockRenderer();

      transform.translate(this.transX, this.transY, Math.max(structureHeight, Math.max(structureWidth, structureLength)));
      transform.scale(this.scale, -this.scale, 1);
      transform.pushTransformation(this.additionalTransform);
      transform.mulPose(new Quaternionf());

      transform.translate(structureLength / -2f, structureHeight / -2f, structureWidth / -2f);

      for (int h = 0; h < structureHeight; h++) {
        for (int l = 0; l < structureLength; l++) {
          for (int w = 0; w < structureWidth; w++) {
            BlockPos pos = new BlockPos(l, h, w);
            BlockState state = this.structureWorld.getBlockState(pos);

            if (!state.isAir()) {
              transform.pushPose();
              transform.translate(l, h, w);

              int overlay;

              if (pos.equals(new BlockPos(1, 1, 1)))
                overlay = OverlayTexture.pack(0, true);
              else
                overlay = OverlayTexture.NO_OVERLAY;

              blockRender.getModelRenderer().tesselateBlock(
                this.structureWorld, blockRender.getBlockModel(state), state, pos, transform,
                buffer.getBuffer(MantleRenderTypes.TRANSLUCENT_FULLBRIGHT), false, this.structureWorld.random, state.getSeed(pos),
                overlay
              );

              transform.popPose();
            }
          }
        }
      }

      transform.popPose();
      transform.popPose();

    } catch (Exception e) {
      final long now = System.currentTimeMillis();

      if (now > this.lastPrintedErrorTimeMs + 1000) {
        e.printStackTrace();
        this.lastPrintedErrorTimeMs = now;
      }

      while (lastEntryBeforeTry != transform.last())
        transform.popPose();
    }

    buffer.endBatch();
  }

  @Override
  public void mouseClicked(double mouseX, double mouseY, int mouseButton) {
    super.mouseClicked(mouseX, mouseY, mouseButton);
  }

  @Override
  public void mouseDragged(double clickX, double clickY, double mouseX, double mouseY, double lastX, double lastY, int button) {
    double dx = mouseX - lastX;
    double dy = mouseY - lastY;
    this.additionalTransform = this.forRotation(dx * 80D / 104, dy * 0.8).compose(this.additionalTransform);
  }

  @Override
  public void mouseReleased(double mouseX, double mouseY, int clickedMouseButton) {
    super.mouseReleased(mouseX, mouseY, clickedMouseButton);
  }

  private Transformation forRotation(double rX, double rY) {
    Vector3f axis = new Vector3f((float) rY, (float) rX, 0);
    float angle = (float) Math.sqrt(axis.dot(axis));

    axis.normalize();

    return new Transformation(null, new Quaternionf().setAngleAxis(angle, axis.x(), axis.y(), axis.z()), null, null);
  }
}