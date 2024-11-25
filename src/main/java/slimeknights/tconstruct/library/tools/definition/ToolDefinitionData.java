package slimeknights.tconstruct.library.tools.definition;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.fabricators_of_create.porting_lib.tool.ToolAction;
import lombok.AccessLevel;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.minecraft.network.FriendlyByteBuf;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.ModifierHook;
import slimeknights.tconstruct.library.modifiers.util.ModifierHookMap;
import slimeknights.tconstruct.library.tools.SlotType;
import slimeknights.tconstruct.library.tools.definition.aoe.IAreaOfEffectIterator;
import slimeknights.tconstruct.library.tools.definition.harvest.IHarvestLogic;
import slimeknights.tconstruct.library.tools.definition.module.IToolModule;
import slimeknights.tconstruct.library.tools.definition.weapon.IWeaponAttack;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.MultiplierNBT;
import slimeknights.tconstruct.library.tools.nbt.StatsNBT;
import slimeknights.tconstruct.library.tools.stat.INumericToolStat;
import slimeknights.tconstruct.library.tools.stat.IToolStat;
import slimeknights.tconstruct.library.tools.stat.ModifierStatsBuilder;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static java.util.Objects.requireNonNullElse;

/**
 * This class contains all data pack configurable data for a tool, before materials are factored in.
 * Contains info about how to craft a tool and how it behaves.
 */
@SuppressWarnings("ClassCanBeRecord")
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ToolDefinitionData {

  @VisibleForTesting
  protected static final Stats EMPTY_STATS = new Stats(StatsNBT.EMPTY, MultiplierNBT.EMPTY);
  /**
   * Empty tool data definition instance
   */
  public static final ToolDefinitionData EMPTY = new ToolDefinitionData(Collections.emptyList(), EMPTY_STATS, DefinitionModifierSlots.EMPTY, Collections.emptyList(), Collections.emptySet(), null, null, ModifierHookMap.EMPTY);

  @Nullable
  private final List<PartRequirement> parts;
  @Nullable
  private final Stats stats;
  @Nullable
  private final DefinitionModifierSlots slots;
  @Nullable
  private final List<ModifierEntry> traits;
  @Nullable
  @VisibleForTesting
  protected final Set<ToolAction> actions;
  @Nullable
  private final Harvest harvest;
  @Nullable
  private final IWeaponAttack attack;
  @Nullable
  private final ModifierHookMap modules;


  /* Getters */

  /**
   * Gets a list of all parts in the tool
   */
  public List<PartRequirement> getParts() {
    return requireNonNullElse(this.parts, Collections.emptyList());
  }

  /**
   * Gets the stat sub object on the tool
   */
  protected Stats getStats() {
    return requireNonNullElse(this.stats, EMPTY_STATS);
  }

  /**
   * Gets the starting slots on the tool
   */
  protected DefinitionModifierSlots getSlots() {
    return requireNonNullElse(this.slots, DefinitionModifierSlots.EMPTY);
  }

  /**
   * Gets a list of all traits of the tool
   */
  public List<ModifierEntry> getTraits() {
    return requireNonNullElse(this.traits, Collections.emptyList());
  }

  /**
   * Checks if the tool can natively perform the given tool action
   */
  public boolean canPerformAction(ToolAction action) {
    return this.actions != null && this.actions.contains(action);
  }

  /**
   * Gets the default number of slots for the given type
   *
   * @param type Type
   * @return Number of starting slots on new tools
   */
  public int getStartingSlots(SlotType type) {
    return this.getSlots().getSlots(type);
  }

  /**
   * Gets the map of internal module hooks
   */
  public ModifierHookMap getModules() {
    return requireNonNullElse(this.modules, ModifierHookMap.EMPTY);
  }

  /**
   * Gets the given module from the tool
   */
  public <T> T getModule(ModifierHook<T> hook) {
    return this.getModules().getOrDefault(hook);
  }


  /* Stats */

  /**
   * Gets a set of bonuses applied to this tool, for stat building
   */
  public Set<IToolStat<?>> getAllBaseStats() {
    return this.getStats().getBase().getContainedStats();
  }

  /**
   * Determines if the given stat is defined in this definition, for stat building
   */
  public boolean hasBaseStat(IToolStat<?> stat) {
    return this.getStats().getBase().hasStat(stat);
  }

  /**
   * Gets the value of a stat in this tool, or the default value if missing
   */
  public <T> T getBaseStat(IToolStat<T> toolStat) {
    return this.getStats().getBase().get(toolStat);
  }

  /**
   * Gets the multiplier for this stat to use for modifiers
   * <p>
   * In most cases, its better to use {@link IToolStackView#getMultiplier(INumericToolStat)} as that takes the modifier multiplier into account
   */
  public float getMultiplier(INumericToolStat<?> toolStat) {
    return this.getStats().getMultipliers().get(toolStat);
  }


  /* Tool building */

  /**
   * Applies the extra tool stats to the tool like a modifier
   *
   * @param builder Tool stats builder
   */
  public void buildStatMultipliers(ModifierStatsBuilder builder) {
    if (this.stats != null) {
      MultiplierNBT multipliers = this.stats.getMultipliers();
      for (INumericToolStat<?> stat : multipliers.getContainedStats()) {
        stat.multiplyAll(builder, multipliers.get(stat));
      }
    }
  }

  /**
   * Adds the starting slots to the given mod data
   *
   * @param persistentModData Mod data
   */
  public void buildSlots(ModDataNBT persistentModData) {
    if (this.slots != null) {
      for (SlotType type : this.slots.containedTypes()) {
        persistentModData.setSlots(type, this.slots.getSlots(type));
      }
    }
  }


  /* Harvest */

  // TODO: migrate harvest into modules

  /**
   * Gets the tools's harvest logic
   */
  public IHarvestLogic getHarvestLogic() {
    if (this.harvest != null && this.harvest.logic != null) {
      return this.harvest.logic;
    }
    return IHarvestLogic.DEFAULT;
  }

  /**
   * Gets the AOE logic for this tool
   */
  public IAreaOfEffectIterator getAOE() {
    if (this.harvest != null && this.harvest.aoe != null) {
      return this.harvest.aoe;
    }
    return IAreaOfEffectIterator.DEFAULT;
  }


  /* Attack */

  // TODO: migrate attack into modules

  /**
   * Gets the tool's attack logic
   */
  public IWeaponAttack getAttack() {
    return requireNonNullElse(this.attack, IWeaponAttack.DEFAULT);
  }


  /* Packet buffers */

  /**
   * Writes a tool definition stat object to a packet buffer
   */
  public void write(FriendlyByteBuf buffer) {
    List<PartRequirement> parts = this.getParts();
    buffer.writeVarInt(parts.size());
    for (PartRequirement part : parts) {
      part.write(buffer);
    }
    Stats stats = this.getStats();
    stats.getBase().toNetwork(buffer);
    stats.getMultipliers().toNetwork(buffer);
    this.getSlots().write(buffer);
    List<ModifierEntry> traits = this.getTraits();
    buffer.writeVarInt(traits.size());
    for (ModifierEntry entry : traits) {
      entry.write(buffer);
    }
    if (this.actions == null) {
      buffer.writeVarInt(0);
    } else {
      buffer.writeVarInt(this.actions.size());
      for (ToolAction action : this.actions) {
        buffer.writeUtf(action.name());
      }
    }
    IHarvestLogic.LOADER.toNetwork(this.getHarvestLogic(), buffer);
    IAreaOfEffectIterator.LOADER.toNetwork(this.getAOE(), buffer);
    IWeaponAttack.LOADER.toNetwork(this.getAttack(), buffer);
    IToolModule.write(this.getModules(), buffer);
  }

  /**
   * Reads a tool definition stat object from a packet buffer
   */
  public static ToolDefinitionData read(FriendlyByteBuf buffer) {
    int size = buffer.readVarInt();
    ImmutableList.Builder<PartRequirement> parts = ImmutableList.builder();
    for (int i = 0; i < size; i++) {
      parts.add(PartRequirement.read(buffer));
    }
    StatsNBT bonuses = StatsNBT.fromNetwork(buffer);
    MultiplierNBT multipliers = MultiplierNBT.fromNetwork(buffer);
    DefinitionModifierSlots slots = DefinitionModifierSlots.read(buffer);
    size = buffer.readVarInt();
    ImmutableList.Builder<ModifierEntry> traits = ImmutableList.builder();
    for (int i = 0; i < size; i++) {
      traits.add(ModifierEntry.read(buffer));
    }
    size = buffer.readVarInt();
    ImmutableSet.Builder<ToolAction> actions = ImmutableSet.builder();
    for (int i = 0; i < size; i++) {
      actions.add(ToolAction.get(buffer.readUtf()));
    }
    IHarvestLogic harvestLogic = IHarvestLogic.LOADER.fromNetwork(buffer);
    IAreaOfEffectIterator aoe = IAreaOfEffectIterator.LOADER.fromNetwork(buffer);
    IWeaponAttack attack = IWeaponAttack.LOADER.fromNetwork(buffer);
    ModifierHookMap modules = IToolModule.read(buffer);
    return new ToolDefinitionData(parts.build(), new Stats(bonuses, multipliers), slots, traits.build(), actions.build(), new Harvest(harvestLogic, aoe), attack, modules);
  }

  /**
   * Internal stats object
   */
  @RequiredArgsConstructor
  public static class Stats {

    @Nullable
    private final StatsNBT base;
    @Nullable
    private final MultiplierNBT multipliers;

    /**
     * Bonus to add to each stat on top of the base value
     */
    public StatsNBT getBase() {
      return requireNonNullElse(this.base, StatsNBT.EMPTY);
    }

    /**
     * Multipliers to apply after modifiers
     */
    public MultiplierNBT getMultipliers() {
      return requireNonNullElse(this.multipliers, MultiplierNBT.EMPTY);
    }
  }

  /**
   * Defines harvest properties
   */
  @Data
  protected static class Harvest {

    @Nullable
    private final IHarvestLogic logic;
    @Nullable
    private final IAreaOfEffectIterator aoe;
  }
}
