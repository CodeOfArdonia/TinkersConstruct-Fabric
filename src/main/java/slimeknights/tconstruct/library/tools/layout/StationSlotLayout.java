package slimeknights.tconstruct.library.tools.layout;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.recipe.partbuilder.Pattern;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNullElse;

/**
 * A full layout for the tinker station
 */
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class StationSlotLayout {

  private static final ResourceLocation EMPTY_NAME = TConstruct.getResource("empty");
  public static final StationSlotLayout EMPTY = new StationSlotLayout("", LayoutIcon.EMPTY, null, LayoutSlot.EMPTY, Collections.emptyList());

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private transient ResourceLocation name = EMPTY_NAME;
  private final String translation_key;
  private final LayoutIcon icon;
  @Nullable
  private final Integer sortIndex;
  private final LayoutSlot tool_slot;
  private final List<LayoutSlot> input_slots;

  /**
   * Creates a new builder instance
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * If true, this layout is the primary layout for a station
   */
  public boolean isMain() {
    return this.sortIndex == null;
  }

  /**
   * Gets the sort index for the given layout
   */
  public int getSortIndex() {
    return requireNonNullElse(this.sortIndex, 255);
  }

  /**
   * Gets the icon for this layout
   */
  public LayoutIcon getIcon() {
    return requireNonNullElse(this.icon, LayoutIcon.EMPTY);
  }

  /* Slots */

  /**
   * Gets the contents of the tool slot
   */
  public LayoutSlot getToolSlot() {
    return requireNonNullElse(this.tool_slot, LayoutSlot.EMPTY);
  }

  /**
   * Gets positions for all input slots
   */
  public List<LayoutSlot> getInputSlots() {
    return requireNonNullElse(this.input_slots, Collections.emptyList());
  }

  /**
   * Gets the number of input slots
   */
  public int getInputCount() {
    return this.getInputSlots().size();
  }

  /**
   * Gets the slot for the given index, includes the tool slot
   */
  public LayoutSlot getSlot(int index) {
    if (index == 0) {
      return this.getToolSlot();
    }
    List<LayoutSlot> inputs = this.getInputSlots();
    if (index < 0 || index > inputs.size()) {
      return LayoutSlot.EMPTY;
    }
    return inputs.get(index - 1);
  }


  /* Buffers */

  /**
   * Reads a slot from the packet buffer
   */
  public static StationSlotLayout read(FriendlyByteBuf buffer) {
    ResourceLocation name = buffer.readResourceLocation();
    String translationKey = buffer.readUtf(Short.MAX_VALUE);
    LayoutIcon icon = LayoutIcon.read(buffer);
    Integer sortIndex = null;
    if (buffer.readBoolean()) {
      sortIndex = buffer.readVarInt();
    }
    LayoutSlot toolSlot = LayoutSlot.read(buffer);
    int max = buffer.readVarInt();
    ImmutableList.Builder<LayoutSlot> inputs = ImmutableList.builder();
    for (int i = 0; i < max; i++) {
      inputs.add(LayoutSlot.read(buffer));
    }
    StationSlotLayout layout = new StationSlotLayout(translationKey, icon, sortIndex, toolSlot, inputs.build());
    layout.setName(name);
    return layout;
  }

  /**
   * Writes a slot to the packet buffer
   */
  public void write(FriendlyByteBuf buffer) {
    buffer.writeResourceLocation(this.name);
    buffer.writeUtf(this.getTranslationKey());
    this.icon.write(buffer);
    if (this.sortIndex != null) {
      buffer.writeBoolean(true);
      buffer.writeVarInt(this.sortIndex);
    } else {
      buffer.writeBoolean(false);
    }
    this.getToolSlot().write(buffer);
    List<LayoutSlot> inputs = this.getInputSlots();
    buffer.writeVarInt(inputs.size());
    for (LayoutSlot slot : inputs) {
      slot.write(buffer);
    }
  }


  /* Text */

  /**
   * Gets the translation key for this slot, suffixing description at the end forms the full description
   */
  public String getTranslationKey() {
    return requireNonNullElse(this.translation_key, "");
  }

  /**
   * Cache of display name
   */
  private transient Component displayName = null;
  /**
   * Cache of display name
   */
  private transient Component description = null;

  /**
   * Gets the display name from the unlocalized name of {@link #getTranslationKey()}
   */
  public Component getDisplayName() {
    if (this.displayName == null) {
      this.displayName = Component.translatable(this.getTranslationKey());
    }
    return this.displayName;
  }

  /**
   * Gets the description from the unlocalized name of {@link #getTranslationKey()}
   */
  public Component getDescription() {
    if (this.description == null) {
      this.description = Component.translatable(this.getTranslationKey() + ".description");
    }
    return this.description;
  }

  @Accessors(fluent = true)
  public static class Builder {

    private static final Pattern PICKAXE = new Pattern(TConstruct.MOD_ID, "pickaxe");

    @Setter
    private String translationKey = null;
    private LayoutIcon icon = LayoutIcon.EMPTY;
    private Integer sortIndex = null;
    private LayoutSlot toolSlot = null;
    private final ImmutableList.Builder<LayoutSlot> inputSlots = ImmutableList.builder();

    private Builder() {}

    /**
     * Sets the sort index of this layout, unused for non-main layouts
     */
    public Builder sortIndex(int index) {
      this.sortIndex = index;
      return this;
    }

    /* Icons */

    /**
     * Sets the given item as both the name and icon
     */
    public Builder item(ItemStack stack) {
      this.icon(stack);
      this.translationKey = stack.getDescriptionId();
      return this;
    }

    /**
     * Sets the icon of this layout to a stack
     */
    public Builder icon(ItemStack stack) {
      this.icon = LayoutIcon.ofItem(stack);
      return this;
    }

    /**
     * Sets the icon of this layout to a pattern
     */
    public Builder icon(Pattern pattern) {
      this.icon = LayoutIcon.ofPattern(pattern);
      return this;
    }


    /* Slots */

    /**
     * Sets the tool slot properties
     */
    public Builder toolSlot(Pattern icon, @Nullable String name, int x, int y, @Nullable Ingredient filter) {
      this.toolSlot = new LayoutSlot(icon, name, x, y, filter);
      return this;
    }

    /**
     * Sets the tool slot properties
     */
    public Builder toolSlot(int x, int y, @Nullable Ingredient filter) {
      return this.toolSlot(PICKAXE, null, x, y, filter);
    }

    /**
     * Sets the tool slot properties
     */
    public Builder toolSlot(int x, int y) {
      return this.toolSlot(x, y, null);
    }

    /**
     * Adds an input slot with the given properties
     */
    public Builder addInputSlot(@Nullable Pattern icon, @Nullable String name, int x, int y, @Nullable Ingredient filter) {
      this.inputSlots.add(new LayoutSlot(icon, name, x, y, filter));
      return this;
    }

    /**
     * Adds an input slot with the given properties
     */
    public Builder addInputSlot(@Nullable Pattern icon, @Nullable String name, int x, int y) {
      return this.addInputSlot(icon, name, x, y, null);
    }

    /**
     * Adds an input slot with the given properties
     */
    public Builder addInputSlot(@Nullable Pattern icon, int x, int y) {
      return this.addInputSlot(icon, null, x, y);
    }

    /**
     * Adds an input as the given item
     */
    public Builder addInputItem(Pattern icon, ItemLike item, int x, int y) {
      return this.addInputSlot(icon, item.asItem().getDescriptionId(), x, y, Ingredient.of(item));
    }

    /**
     * Adds an input as the given item
     */
    public Builder addInputItem(ItemLike item, int x, int y) {
      return this.addInputItem(new Pattern(Objects.requireNonNull(BuiltInRegistries.ITEM.getKey(item.asItem()))), item, x, y);
    }

    /**
     * Builds a station slot layout
     */
    public StationSlotLayout build() {
      return new StationSlotLayout(this.translationKey, this.icon, this.sortIndex, this.toolSlot, this.inputSlots.build());
    }
  }
}
