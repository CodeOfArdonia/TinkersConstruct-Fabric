package slimeknights.tconstruct.library.materials.definition;

import net.minecraft.resources.ResourceLocation;

/**
 * Internal record to represent a material ID with a variant. Use {@link MaterialVariantId} to create if needed
 */
record MaterialVariantIdImpl(MaterialId material, String variant) implements MaterialVariantId {

  @Override
  public MaterialId getId() {
    return this.material;
  }

  @Override
  public String getVariant() {
    return this.variant;
  }

  @Override
  public boolean hasVariant() {
    return true;
  }

  @Override
  public boolean matchesVariant(MaterialVariantId other) {
    return this.sameVariant(other);
  }

  @Override
  public ResourceLocation getLocation(char separator) {
    return new ResourceLocation(this.material.getNamespace(), this.material.getPath() + separator + this.variant);
  }

  @Override
  public String toString() {
    return this.material + "#" + this.variant;
  }
}
