package slimeknights.tconstruct.library.modifiers.impl;

import io.github.fabricators_of_create.porting_lib.fluids.FluidStack;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.transfer.v1.context.ContainerItemContext;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.Modifier;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.util.ModifierHookMap.Builder;
import slimeknights.tconstruct.library.recipe.tinkerstation.ValidatedResult;
import slimeknights.tconstruct.library.tools.capability.ToolFluidCapability;
import slimeknights.tconstruct.library.tools.capability.ToolFluidCapability.FluidModifierHook;
import slimeknights.tconstruct.library.tools.capability.ToolFluidCapability.IFluidModifier;
import slimeknights.tconstruct.library.tools.context.ToolRebuildContext;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolContext;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;
import slimeknights.tconstruct.library.tools.nbt.ToolStack;
import slimeknights.tconstruct.library.utils.TooltipKey;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Modifier containing the standard tank, extend if you want to share this tank
 */
@RequiredArgsConstructor
public class TankModifier extends Modifier {

  private static final String FILLED_KEY = TConstruct.makeTranslationKey("modifier", "tank.filled");
  private static final String CAPACITY_KEY = TConstruct.makeTranslationKey("modifier", "tank.capacity");

  /**
   * Volatile NBT string indicating which modifier is in charge of logic for the one tank
   */
  private static final ResourceLocation OWNER = TConstruct.getResource("tank_owner");
  /**
   * Volatile NBT integer indicating the tank's max capacity
   */
  private static final ResourceLocation CAPACITY = TConstruct.getResource("tank_capacity");
  /**
   * Persistent NBT compound containing the fluid in the tank
   */
  private static final ResourceLocation FLUID = TConstruct.getResource("tank_fluid");

  /**
   * Helper function to parse a fluid from NBT
   */
  public static final BiFunction<CompoundTag, String, FluidStack> PARSE_FLUID = (nbt, key) -> FluidStack.loadFluidStackFromNBT(nbt.getCompound(key));

  private ModifierTank tank;
  private final long capacity;

  @Override
  protected void registerHooks(Builder hookBuilder) {
    super.registerHooks(hookBuilder);
    this.tank = new ModifierTank();
    hookBuilder.addHook(this.tank, ToolFluidCapability.HOOK);
  }

  @Override
  public void addVolatileData(ToolRebuildContext context, int level, ModDataNBT volatileData) {
    // set owner first
    ResourceLocation ownerKey = this.getOwnerKey();
    if (ownerKey != null && !volatileData.contains(ownerKey, Tag.TAG_STRING)) {
      volatileData.putString(ownerKey, this.getId().toString());
    }
    ToolFluidCapability.addTanks(context, this, volatileData, this.tank);
    if (this.capacity > 0) {
      this.addCapacity(volatileData, this.capacity * level);
    }
  }

  @Override
  public void addInformation(IToolStackView tool, int level, @Nullable Player player, List<Component> tooltip, TooltipKey tooltipKey, TooltipFlag tooltipFlag) {
    if (this.isOwner(tool)) {
      FluidStack current = this.getFluid(tool);
      if (!current.isEmpty()) {
        tooltip.add(Component.translatable(FILLED_KEY, current.getAmount(), current.getDisplayName()));
      }
      tooltip.add(Component.translatable(CAPACITY_KEY, this.getCapacity(tool)));
    }
  }

  @Override
  public ValidatedResult validate(IToolStackView tool, int level) {
    // ensure we don't have too much fluid if the capacity changed, if level is 0 there will be a new owner
    if (level > 0 && this.isOwner(tool)) {
      FluidStack fluidStack = this.getFluid(tool);
      if (!fluidStack.isEmpty()) {
        int capacity = this.getCapacity(tool);
        if (fluidStack.getAmount() > capacity) {
          fluidStack.setAmount(capacity);
          this.setFluid(tool, fluidStack);
        }
      }
    }

    return ValidatedResult.PASS;
  }

  @Override
  public void onRemoved(IToolStackView tool) {
    ModDataNBT persistentData = tool.getPersistentData();
    // if no one claims the tank, it either belonged to us or another removed modifier, so clean up data
    if (!persistentData.contains(OWNER, Tag.TAG_STRING)) {
      persistentData.remove(this.getFluidKey());
    }
  }

  /* Resource location keys */

  /**
   * Overridable method to change the owner key
   */
  @Nullable
  public ResourceLocation getOwnerKey() {
    return OWNER;
  }

  /**
   * Overridable method to change the capacity key
   */
  public ResourceLocation getCapacityKey() {
    return CAPACITY;
  }

  /**
   * Overridable method to change the fluid key
   */
  public ResourceLocation getFluidKey() {
    return FLUID;
  }


  /* Helpers */

  /**
   * Checks if the given modifier is the owner of the tank
   */
  public boolean isOwner(IModDataView volatileData) {
    ResourceLocation key = this.getOwnerKey();
    if (key == null) {
      return true;
    }
    return this.getId().toString().equals(volatileData.getString(key));
  }

  /**
   * Checks if the given modifier is the owner of the tank
   */
  public boolean isOwner(IToolStackView tool) {
    return this.isOwner(tool.getVolatileData());
  }

  /**
   * Gets the capacity of the tank
   */
  public int getCapacity(IModDataView volatileData) {
    return volatileData.getInt(this.getCapacityKey());
  }

  /**
   * Gets the capacity of the tank
   */
  public int getCapacity(IToolStackView tool) {
    return tool.getVolatileData().getInt(this.getCapacityKey());
  }

  /**
   * Adds the given capacity into volatile NBT
   */
  public void addCapacity(ModDataNBT volatileNBT, long amount) {
    ResourceLocation key = this.getCapacityKey();
    if (volatileNBT.contains(key, Tag.TAG_ANY_NUMERIC)) {
      amount += volatileNBT.getInt(key);
    }
    volatileNBT.putLong(key, amount);
  }

  /**
   * Gets the fluid in the tank
   */
  public FluidStack getFluid(IToolStackView tool) {
    return tool.getPersistentData().get(this.getFluidKey(), PARSE_FLUID);
  }

  /**
   * Sets the fluid in the tank
   */
  public FluidStack setFluid(ContainerItemContext context, IToolStackView tool, FluidStack fluid, TransactionContext tx) {
    int capacity = this.getCapacity(tool);
    if (fluid.getAmount() > capacity) {
      fluid.setAmount(capacity);
    }
    ItemStack newStack = context.getItemVariant().toStack();

    ToolStack toolStackCopy = ToolStack.copyFrom(newStack);

    toolStackCopy.getPersistentData().put(this.getFluidKey(), fluid.writeToNBT(new CompoundTag()));

    newStack.setTag(toolStackCopy.getNbt());

    if (context.exchange(ItemVariant.of(newStack), 1, tx) != 1)
      TConstruct.LOG.error("Failed to set fluid for tank modifier!");
    return fluid;
  }

  /**
   * Sets the fluid in the tank
   */
  public FluidStack setFluid(IToolStackView tool, FluidStack fluid) {
    if (fluid.isEmpty()) {
      tool.getPersistentData().remove(this.getFluidKey());
      return fluid;
    }
    int capacity = this.getCapacity(tool);
    if (fluid.getAmount() > capacity) {
      fluid.setAmount(capacity);
    }
    tool.getPersistentData().put(this.getFluidKey(), fluid.writeToNBT(new CompoundTag()));
    return fluid;
  }

  /**
   * Fills the tool with the given resource
   *
   * @param tool     Tool stack
   * @param current  Current tank contents
   * @param resource Resource to insert
   * @param amount   Amount to insert, overrides resource amount
   * @return Fluid after filling, or empty if nothing changed
   */
  public FluidStack fill(ContainerItemContext context, IToolStackView tool, FluidStack current, FluidStack resource, long amount, TransactionContext tx) {
    int capacity = this.getCapacity(tool);
    if (current.isEmpty()) {
      // cap fluid at capacity, store in tool
      resource.setAmount(Math.min(amount, capacity));
      return this.setFluid(context, tool, resource, tx);
    } else if (current.isFluidEqual(resource)) {
      // boost fluid by amount and store
      current.setAmount(Math.min(current.getAmount() + amount, capacity));
      return this.setFluid(context, tool, current, tx);
    }
    return FluidStack.EMPTY;
  }

  /**
   * Fills the tool with the given resource
   *
   * @param tool     Tool stack
   * @param current  Current tank contents
   * @param resource Resource to insert
   * @param amount   Amount to insert, overrides resource amount
   * @return Fluid after filling, or empty if nothing changed
   */
  public FluidStack fill(IToolStackView tool, FluidStack current, FluidStack resource, long amount) {
    int capacity = this.getCapacity(tool);
    if (current.isEmpty()) {
      // cap fluid at capacity, store in tool
      resource.setAmount(Math.min(amount, capacity));
      return this.setFluid(tool, resource);
    } else if (current.isFluidEqual(resource)) {
      // boost fluid by amount and store
      current.setAmount(Math.min(current.getAmount() + amount, capacity));
      return this.setFluid(tool, current);
    }
    return FluidStack.EMPTY;
  }

  /**
   * Drains the given amount from the tool
   *
   * @param tool    Tool
   * @param current Existing fluid
   * @param amount  Amount to drain
   * @return New fluid
   */
  public FluidStack drain(ContainerItemContext context, IToolStackView tool, FluidStack current, long amount, TransactionContext tx) {
    if (current.getAmount() < amount) {
      return this.setFluid(context, tool, FluidStack.EMPTY, tx);
    } else {
      current.shrink(amount);
      return this.setFluid(context, tool, current, tx);
    }
  }

  /**
   * Drains the given amount from the tool
   *
   * @param tool    Tool
   * @param current Existing fluid
   * @param amount  Amount to drain
   * @return New fluid
   */
  public FluidStack drain(IToolStackView tool, FluidStack current, long amount) {
    if (current.getAmount() < amount) {
      return this.setFluid(tool, FluidStack.EMPTY);
    } else {
      current.shrink(amount);
      return this.setFluid(tool, current);
    }
  }

  /**
   * Shared tank implementation of the fluid modifier
   */
  public class ModifierTank implements IFluidModifier, FluidModifierHook {

    @Override
    public int getTanks(IModDataView volatileData) {
      return TankModifier.this.isOwner(volatileData) ? 1 : 0;
    }

    @Override
    public FluidStack getFluidInTank(IToolStackView tool, int level, int tank) {
      return TankModifier.this.isOwner(tool) ? TankModifier.this.getFluid(tool) : FluidStack.EMPTY;
    }

    @Override
    public long getTankCapacity(IToolStackView tool, int level, int tank) {
      return TankModifier.this.isOwner(tool) ? TankModifier.this.getCapacity(tool) : 0;
    }

    @Override
    public long fill(ContainerItemContext context, IToolStackView tool, int level, FluidVariant resource, long maxAmount, TransactionContext tx) {
      if (!resource.isBlank() && TankModifier.this.isOwner(tool)) {
        // must not be too full
        FluidStack current = TankModifier.this.getFluid(tool);
        long remaining = TankModifier.this.getCapacity(tool) - current.getAmount();
        if (remaining <= 0) {
          return 0;
        }
        // must match existing fluid
        if (!current.isEmpty() && !current.isFluidEqual(resource)) {
          return 0;
        }
        // actual filling logic
        long filled = Math.min(remaining, maxAmount);
        if (filled > 0) {
          TankModifier.this.fill(context, tool, current, new FluidStack(resource, maxAmount), filled, tx);
        }
        return filled;
      }
      return 0;
    }

    @Override
    public long drain(ContainerItemContext context, IToolStackView tool, int level, FluidVariant resource, long maxAmount, TransactionContext tx) {
      if (!resource.isBlank() && TankModifier.this.isOwner(tool)) {
        // fluid type mismatches
        FluidStack current = TankModifier.this.getFluid(tool);
        if (current.isEmpty() || !current.isFluidEqual(resource)) {
          return 0;
        }
        // actual draining
        long drainedAmount = Math.min(current.getAmount(), maxAmount);
        FluidStack drained = new FluidStack(current, drainedAmount);
        TankModifier.this.drain(context, tool, current, drainedAmount, tx);
        return drained.getAmount();
      }
      return 0;
    }


    /* New hooks fallback to deprecated */

    @Override
    public int getTanks(IToolContext tool, Modifier modifier) {
      return this.getTanks(tool.getVolatileData());
    }

    @Override
    public SingleSlotStorage<FluidVariant> getSlot(IToolStackView tool, ModifierEntry modifier, int slot) {
      throw new RuntimeException("TODO: Not supported yet");
    }

    @Override
    public FluidStack getFluidInTank(IToolStackView tool, ModifierEntry modifier, int tank) {
      return this.getFluidInTank(tool, modifier.getLevel(), tank);
    }

    @Override
    public long getTankCapacity(IToolStackView tool, ModifierEntry modifier, int tank) {
      return this.getTankCapacity(tool, modifier.getLevel(), tank);
    }

    @Override
    public boolean isFluidValid(IToolStackView tool, ModifierEntry modifier, int tank, FluidStack fluid) {
      return this.isFluidValid(tool, modifier.getLevel(), tank, fluid);
    }

    @Override
    public long fill(ContainerItemContext context, IToolStackView tool, ModifierEntry modifier, FluidVariant resource, long maxAmount, TransactionContext tx) {
      return this.fill(context, tool, modifier.getLevel(), resource, maxAmount, tx);
    }

    @Override
    public long drain(ContainerItemContext context, IToolStackView tool, ModifierEntry modifier, FluidVariant resource, long maxAmount, TransactionContext tx) {
      return this.drain(context, tool, modifier.getLevel(), resource, maxAmount, tx);
    }
  }
}
