package slimeknights.tconstruct.smeltery.client.util;

import com.mojang.blaze3d.vertex.VertexConsumer;
import lombok.RequiredArgsConstructor;

/**
 * Vertex builder wrapper that tints all quads passed in
 */
@RequiredArgsConstructor
public class TintedVertexBuilder implements VertexConsumer {

  /**
   * Base vertex builder
   */
  private final VertexConsumer inner;
  /**
   * Tint color from 0-255
   */
  private final int tintRed, tintGreen, tintBlue, tintAlpha;

  @Override
  public VertexConsumer vertex(double x, double y, double z) {
    return this.inner.vertex(x, y, z);
  }

  @Override
  public VertexConsumer color(int red, int green, int blue, int alpha) {
    return this.inner.color((red * this.tintRed) / 0xFF, (green * this.tintGreen) / 0xFF, (blue * this.tintBlue) / 0xFF, (alpha * this.tintAlpha) / 0xFF);
  }

  @Override
  public void defaultColor(int red, int green, int blue, int alpha) {
    // TODO: is setting the default color here correct?
    this.inner.defaultColor((red * this.tintRed) / 0xFF, (green * this.tintGreen) / 0xFF, (blue * this.tintBlue) / 0xFF, (alpha * this.tintAlpha) / 0xFF);
  }

  @Override
  public void unsetDefaultColor() {
    this.inner.unsetDefaultColor();
  }

  @Override
  public VertexConsumer uv(float u, float v) {
    return this.inner.uv(u, v);
  }

  @Override
  public VertexConsumer overlayCoords(int u, int v) {
    return this.inner.overlayCoords(u, v);
  }

  @Override
  public VertexConsumer uv2(int u, int v) {
    return this.inner.uv2(u, v);
  }

  @Override
  public VertexConsumer normal(float x, float y, float z) {
    return this.inner.normal(x, y, z);
  }

  @Override
  public void endVertex() {
    this.inner.endVertex();
  }
}
