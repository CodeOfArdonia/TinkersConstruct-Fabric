package slimeknights.tconstruct.library;

import com.google.common.base.CharMatcher;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import gnu.trove.map.hash.THashMap;
import gnu.trove.set.hash.THashSet;
import gnu.trove.set.hash.TLinkedHashSet;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.oredict.OreDictionary;

import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import slimeknights.mantle.client.CreativeTab;
import slimeknights.mantle.util.RecipeMatch;
import slimeknights.tconstruct.library.events.MaterialEvent;
import slimeknights.tconstruct.library.materials.IMaterialStats;
import slimeknights.tconstruct.library.materials.Material;
import slimeknights.tconstruct.library.modifiers.IModifier;
import slimeknights.tconstruct.library.smeltery.MeltingRecipe;
import slimeknights.tconstruct.library.tinkering.PartMaterialType;
import slimeknights.tconstruct.library.tools.IToolPart;
import slimeknights.tconstruct.library.tools.Pattern;
import slimeknights.tconstruct.library.tools.Shard;
import slimeknights.tconstruct.library.tools.ToolCore;
import slimeknights.tconstruct.library.traits.ITrait;

public final class TinkerRegistry {

  // the logger for the library
  public static final Logger log = Util.getLogger("API");

  private TinkerRegistry() {
  }

  /*---------------------------------------------------------------------------
  | CREATIVE TABS                                                             |
  ---------------------------------------------------------------------------*/
  public static CreativeTab tabTools = new CreativeTab("TinkerTools", new ItemStack(Items.iron_pickaxe));
  public static CreativeTab
      tabSmeltery =
      new CreativeTab("TinkerSmeltery", new ItemStack(Item.getItemFromBlock(Blocks.stonebrick)));
  public static CreativeTab
      tabWorld =
      new CreativeTab("TinkerWorld", new ItemStack(Item.getItemFromBlock(Blocks.slime_block)));

  /*---------------------------------------------------------------------------
  | MATERIALS                                                                 |
  ---------------------------------------------------------------------------*/

  // Identifier to Material mapping. Hashmap so we can look it up directly without iterating
  private static final Map<String, Material> materials = Maps.newLinkedHashMap();
  private static final Map<String, ITrait> traits = new THashMap<String, ITrait>();
  // traceability information who registered what. Used to find errors.
  private static final Map<String, String> materialRegisteredByMod = new THashMap<String, String>();
  private static final Map<String, Map<String, String>> statRegisteredByMod = new THashMap<String, Map<String, String>>();
  private static final Map<String, Map<String, String>> traitRegisteredByMod = new THashMap<String, Map<String, String>>();

  // contains all cancelled materials, allows us to eat calls regarding the material silently
  private static final Set<String> cancelledMaterials = new THashSet<String>();


  public static void addMaterial(Material material, IMaterialStats stats, ITrait trait) {
    addMaterial(material, stats);
    addMaterialTrait(material.identifier, trait);
  }

  public static void addMaterial(Material material, ITrait trait) {
    addMaterial(material);
    addMaterialTrait(material.identifier, trait);
  }

  public static void addMaterial(Material material, IMaterialStats stats) {
    addMaterial(material);
    addMaterialStats(material.identifier, stats);
  }

  /**
   * Registers a material. The materials identifier has to be lowercase and not contain any spaces.
   * Identifiers have to be globally unique!
   */
  public static void addMaterial(Material material) {
    // ensure material identifiers are safe
    if(CharMatcher.WHITESPACE.matchesAnyOf(material.getIdentifier())) {
      error("Could not register Material \"%s\": Material identifier must not contain any spaces.", material.identifier);
      return;
    }
    if(CharMatcher.JAVA_UPPER_CASE.matchesAnyOf(material.getIdentifier())) {
      error("Could not register Material \"%s\": Material identifier must be completely lowercase.", material.identifier);
      return;
    }

    // duplicate material
    if(materials.containsKey(material.identifier)) {
      String registeredBy = materialRegisteredByMod.get(material.identifier);
      error(String.format(
          "Could not register Material \"%s\": It was already registered by %s",
          material.identifier,
          registeredBy));
      return;
    }

    MaterialEvent.MaterialRegisterEvent event = new MaterialEvent.MaterialRegisterEvent(material);

    if(MinecraftForge.EVENT_BUS.post(event)) {
      // event cancelled
      log.trace("Addition of material {} cancelled by event", material.getIdentifier());
      cancelledMaterials.add(material.getIdentifier());
      return;
    }

    // register material
    materials.put(material.identifier, material);
    putMaterialTrace(material.identifier);
  }

  public static Material getMaterial(String identifier) {
    return materials.containsKey(identifier) ? materials.get(identifier) : Material.UNKNOWN;
  }

  public static Collection<Material> getAllMaterials() {
    return materials.values();
  }


  /*---------------------------------------------------------------------------
  | TRAITS & STATS                                                            |
  ---------------------------------------------------------------------------*/

  public static void addTrait(ITrait trait) {
    // Trait might already have been registered since modifiers and materials share traits
    if(traits.containsKey(trait.getIdentifier())) {
      return;
    }

    traits.put(trait.getIdentifier(), trait);

    String activeMod = Loader.instance().activeModContainer().getModId();
    putTraitTrace(trait.getIdentifier(), trait, activeMod);
  }

  public static void addMaterialStats(String materialIdentifier, IMaterialStats stats) {
    if(cancelledMaterials.contains(materialIdentifier)) {
      return;
    }
    if(!materials.containsKey(materialIdentifier)) {
      error(String.format("Could not add Stats \"%s\" to \"%s\": Unknown Material", stats.getIdentifier(),
                          materialIdentifier));
      return;
    }

    Material material = materials.get(materialIdentifier);
    addMaterialStats(material, stats);
  }

  public static void addMaterialStats(Material material, IMaterialStats stats) {
    if(material == null) {
      error(String.format("Could not add Stats \"%s\": Material is null", stats.getIdentifier()));
      return;
    }
    if(cancelledMaterials.contains(material.identifier)) {
      return;
    }

    String identifier = material.identifier;
    // duplicate stats
    if(material.getStats(stats.getIdentifier()) != null) {
      String registeredBy = "Unknown";
      Map<String, String> matReg = statRegisteredByMod.get(identifier);
      if(matReg != null) {
        registeredBy = matReg.get(stats.getIdentifier());
      }

      error(String.format(
          "Could not add Stats to \"%s\": Stats of type \"%s\" were already registered by %s. Use the events to modify stats.",
          identifier, stats.getIdentifier(), registeredBy));
      return;
    }

    // ensure there are default stats present
    if(Material.UNKNOWN.getStats(stats.getIdentifier()) == null) {
      error("Could not add Stat of type \"%s\": Default Material does not have default stats for said type. Please add default-values to the default material \"unknown\" first.", stats
          .getIdentifier());
      return;
    }

    MaterialEvent.StatRegisterEvent<?> event = new MaterialEvent.StatRegisterEvent<IMaterialStats>(material, stats);
    MinecraftForge.EVENT_BUS.post(event);

    // overridden stats from event
    if(event.getResult() == Event.Result.ALLOW) {
      stats = event.newStats;
    }


    material.addStats(stats);

    String activeMod = Loader.instance().activeModContainer().getModId();
    putStatTrace(identifier, stats, activeMod);
  }

  public static void addMaterialTrait(String materialIdentifier, ITrait trait) {
    if(cancelledMaterials.contains(materialIdentifier)) {
      return;
    }
    if(!materials.containsKey(materialIdentifier)) {
      error(String.format("Could not add Trait \"%s\" to \"%s\": Unknown Material",
                          trait.getIdentifier(), materialIdentifier));
      return;
    }

    Material material = materials.get(materialIdentifier);
    addMaterialTrait(material, trait);
  }

  public static void addMaterialTrait(Material material, ITrait trait) {
    if(material == null) {
      error(String.format("Could not add Trait \"%s\": Material is null", trait.getIdentifier()));
      return;
    }
    if(cancelledMaterials.contains(material.identifier)) {
      return;
    }

    String identifier = material.identifier;
    // duplicate traits
    if(material.hasTrait(trait.getIdentifier())) {
      String registeredBy = "Unknown";
      Map<String, String> matReg = traitRegisteredByMod.get(identifier);
      if(matReg != null) {
        registeredBy = matReg.get(trait.getIdentifier());
      }

      error(String.format(
          "Could not add Trait to \"%s\": Trait \"%s\" was already registered by %s",
          identifier, trait.getIdentifier(), registeredBy));
      return;
    }

    MaterialEvent.TraitRegisterEvent<?> event = new MaterialEvent.TraitRegisterEvent<ITrait>(material, trait);
    if(MinecraftForge.EVENT_BUS.post(event)) {
      // cancelled
      log.trace("Trait {} on {} cancelled by event", trait.getIdentifier(), material.getIdentifier());
      return;
    }

    addTrait(trait);
    material.addTrait(trait);
  }

  public static ITrait getTrait(String identifier) {
    return traits.get(identifier);
  }

  /*---------------------------------------------------------------------------
  | TOOLS & WEAPONS & Crafting                                                |
  ---------------------------------------------------------------------------*/

  /** This set contains all known tools */
  private static final Set<ToolCore> tools = new TLinkedHashSet<ToolCore>();
  private static final Set<IToolPart> toolParts = new TLinkedHashSet<IToolPart>();
  private static final Set<ToolCore> toolStationCrafting = Sets.newLinkedHashSet();
  private static final Set<ToolCore> toolForgeCrafting = Sets.newLinkedHashSet();
  private static final List<ItemStack> stencilTableCrafting = Lists.newLinkedList();
  private static Shard shardItem;

  public static void registerTool(ToolCore tool) {
    tools.add(tool);

    for(PartMaterialType pmt : tool.requiredComponents) {
      toolParts.addAll(pmt.getPossibleParts());
    }
  }

  public static Set<ToolCore> getTools() {
    return ImmutableSet.copyOf(tools);
  }
  public static Set<IToolPart> getToolParts() {
    return ImmutableSet.copyOf(toolParts);
  }

  /** Adds a tool to the Crafting UI of both the Tool Station as well as the Tool Forge */
  public static void registerToolCrafting(ToolCore tool) {
    registerToolStationCrafting(tool);
    registerToolForgeCrafting(tool);
  }

  /** Adds a tool to the Crafting UI of the Tool Station */
  public static void registerToolStationCrafting(ToolCore tool) {
    toolStationCrafting.add(tool);
  }

  public static Set<ToolCore> getToolStationCrafting() {
    return ImmutableSet.copyOf(toolStationCrafting);
  }

  /** Adds a tool to the Crafting UI of the Tool Forge */
  public static void registerToolForgeCrafting(ToolCore tool) {
    toolForgeCrafting.add(tool);
  }

  public static Set<ToolCore> getToolForgeCrafting() {
    return ImmutableSet.copyOf(toolForgeCrafting);
  }

  /** Adds a new pattern to craft to the stenciltable. NBT sensitive. Has to be a Pattern. */
  public static void registerStencilTableCrafting(ItemStack stencil) {
    if(!(stencil.getItem() instanceof Pattern)) {
      error(String.format(
          "Stencil Table Crafting has to be a pattern (%s)", stencil.toString()));
      return;
    }
    stencilTableCrafting.add(stencil);
  }

  public static List<ItemStack> getStencilTableCrafting() {
    return ImmutableList.copyOf(stencilTableCrafting);
  }

  public static void setShardItem(Shard shard) {
    if(shard == null) {
      return;
    }
    shardItem = shard;
  }

  public static ItemStack getShard(Material material) {
    ItemStack out = material.getShard();
    if(out == null) {
      out = shardItem.getItemstackWithMaterial(material);
    }
    return out;
  }

  /*---------------------------------------------------------------------------
  | Modifiers                                                                 |
  ---------------------------------------------------------------------------*/
  private static final Map<String, IModifier> modifiers = new THashMap<String, IModifier>();

  public static void registerModifier(IModifier modifier) {
    modifiers.put(modifier.getIdentifier(), modifier);
  }

  public static IModifier getModifier(String identifier) {
    return modifiers.get(identifier);
  }

  public static Collection<IModifier> getAllModifiers() {
    return modifiers.values();
  }

  /*---------------------------------------------------------------------------
  | Smeltery                                                                  |
  ---------------------------------------------------------------------------*/
  private static List<MeltingRecipe> meltingRegistry = Lists.newLinkedList();
  private static Map<FluidStack, Integer> smelteryFuels = Maps.newHashMap();

  /** Registers this item with all its metadatas to melt into amount of the given fluid. */
  public static void registerMelting(Item item, Fluid fluid, int amount) {
    ItemStack stack = new ItemStack(item, 1, OreDictionary.WILDCARD_VALUE);
    meltingRegistry.add(new MeltingRecipe(new RecipeMatch.Item(stack, 1, amount), fluid));
  }

  /** Registers this block with all its metadatas to melt into amount of the given fluid. */
  public static void registerMelting(Block block, Fluid fluid, int amount) {
    ItemStack stack = new ItemStack(block, 1, OreDictionary.WILDCARD_VALUE);
    meltingRegistry.add(new MeltingRecipe(new RecipeMatch.Item(stack, 1, amount), fluid));
  }

  /** Registers this itemstack NBT-SENSITIVE to melt into amount of the given fluid. */
  public static void registerMelting(ItemStack stack, Fluid fluid, int amount) {
    meltingRegistry.add(new MeltingRecipe(new RecipeMatch.ItemCombination(amount, stack), fluid));
  }

  public static void registerMelting(String oredict, Fluid fluid, int amount) {
    meltingRegistry.add(new MeltingRecipe(new RecipeMatch.Oredict(oredict, 1, amount), fluid));
  }

  public static void registerMelting(MeltingRecipe recipe) {
    meltingRegistry.add(recipe);
  }

  public static MeltingRecipe getMelting(ItemStack stack) {
    for(MeltingRecipe recipe : meltingRegistry) {
      if(recipe.matches(stack)) {
        return recipe;
      }
    }

    return null;
  }

  /**
   * Registers a liquid to be used as smeltery fuel.
   * Temperature is derived from fluid temperature.
   * @param fluidStack   The fluid. Amount is the minimal increment that is consumed at once.
   * @param fuelDuration How many ticks the consumtpion of the fluidStack lasts.
   */
  public static void registerSmelteryFuel(FluidStack fluidStack, int fuelDuration) {
    smelteryFuels.put(fluidStack, fuelDuration);
  }

  /** Checks if the given fluidstack can be used as smeltery fuel */
  public static boolean isSmelteryFuel(FluidStack in) {
    for(Map.Entry<FluidStack, Integer> entry : smelteryFuels.entrySet()) {
      if(entry.getKey().isFluidEqual(in)) {
        return true;
      }
    }

    return false;
  }

  /** Reduces the fluidstack by one increment of the fuel and returns how much fuel duration it gives. */
  public static int consumeSmelteryFuel(FluidStack in) {
    for(Map.Entry<FluidStack, Integer> entry : smelteryFuels.entrySet()) {
      if(entry.getKey().isFluidEqual(in)) {
        FluidStack fuel = entry.getKey();
        int out = entry.getValue();
        if(in.amount < fuel.amount) {
          float coeff = (float)in.amount/(float)fuel.amount;
          out = Math.round(coeff * in.amount);
          in.amount = 0;
        }
        else {
          in.amount -= fuel.amount;
        }

        return out;
      }
    }

    return 0;
  }

  /*---------------------------------------------------------------------------
  | Traceability & Internal stuff                                             |
  ---------------------------------------------------------------------------*/

  static void putMaterialTrace(String materialIdentifier) {
    String activeMod = Loader.instance().activeModContainer().getName();
    materialRegisteredByMod.put(materialIdentifier, activeMod);
  }

  static void putStatTrace(String materialIdentifier, IMaterialStats stats, String trace) {
    if(!statRegisteredByMod.containsKey(materialIdentifier)) {
      statRegisteredByMod.put(materialIdentifier, new HashMap<String, String>());
    }
    statRegisteredByMod.get(materialIdentifier).put(stats.getIdentifier(), trace);
  }

  static void putTraitTrace(String materialIdentifier, ITrait trait, String trace) {
    if(!traitRegisteredByMod.containsKey(materialIdentifier)) {
      traitRegisteredByMod.put(materialIdentifier, new HashMap<String, String>());
    }
    traitRegisteredByMod.get(materialIdentifier).put(trait.getIdentifier(), trace);
  }

  public static String getTrace(Material material) {
    return materialRegisteredByMod.get(material.identifier);
  }

  private static void error(String message, Object... params) {
    throw new TinkerAPIException(String.format(message, params));
  }
}
