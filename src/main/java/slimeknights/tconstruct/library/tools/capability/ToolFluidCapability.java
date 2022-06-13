package slimeknights.tconstruct.library.tools.capability;

import io.github.fabricators_of_create.porting_lib.transfer.fluid.FluidTank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import io.github.fabricators_of_create.porting_lib.util.FluidStack;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.capability.ToolCapabilityProvider.IToolCapabilityProvider;
import slimeknights.tconstruct.library.tools.nbt.IModDataView;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;
import slimeknights.tconstruct.library.tools.nbt.ModDataNBT;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

/**
 * Logic to make a tool a fluid handler
 */
@RequiredArgsConstructor
public class ToolFluidCapability implements SingleSlotStorage<FluidVariant> {
  /** Boolean key to set in volatile mod data to enable the fluid capability */
  public static final ResourceLocation TOTAL_TANKS = TConstruct.getResource("total_tanks");

  @Getter
  private final ItemStack container;
  private final Supplier<? extends IToolStackView> tool;

  /**
   * Runs a fluid handler function for a tank index
   * @param tank          Tank index
   * @param function      Function to run
   * @param defaultValue  Default value if none of the modifiers have the proper tank index
   * @param <T>  Return type
   * @return  Value from the modifiers
   */
  private <T> T runForTank(int tank, T defaultValue, ITankCallback<T> function) {
    IToolStackView tool = this.tool.get();
    for (ModifierEntry entry : tool.getModifierList()) {
      IFluidModifier fluidModifier = entry.getModifier().getModule(IFluidModifier.class);
      if (fluidModifier != null) {
        int currentTanks = fluidModifier.getTanks(tool.getVolatileData());
        if (tank < currentTanks) {
          return function.run(fluidModifier, tool, entry.getLevel(), tank);
        }
        // subtract tanks in the current modifier, tank is 0 indexed from the modifier
        tank -= currentTanks;
      }
    }
    return defaultValue;
  }

  @Override
  public long insert(FluidVariant insertedVariant, long maxAmount, TransactionContext transaction) {
    long totalFilled = 0;
    FluidStack resource = new FluidStack(insertedVariant, maxAmount);
    IToolStackView tool = this.tool.get();
    for (ModifierEntry entry : tool.getModifierList()) {
      IFluidModifier fluidModifier = entry.getModifier().getModule(IFluidModifier.class);
      if (fluidModifier != null) {
        // try filling each modifier
        long filled = fluidModifier.fill(tool, entry.getLevel(), resource, sim);
        if (filled > 0) {
          // if we filled the entire stack, we are done
          if (filled >= resource.getAmount()) {
            return totalFilled + filled;
          }
          // if this is our first successful fill, copy the resource to prevent changing the original stack
          if (totalFilled == 0) {
            resource = resource.copy();
          }
          // increase total and shrink the resource for next time
          totalFilled += filled;
          resource.shrink(filled);
        }
      }
    }
    return totalFilled;
  }

  @Nonnull
  @Override
  public FluidStack drain(FluidStack resource, boolean sim) {
    FluidStack drainedSoFar = FluidStack.EMPTY;
    IToolStackView tool = this.tool.get();
    for (ModifierEntry entry : tool.getModifierList()) {
      IFluidModifier fluidModifier = entry.getModifier().getModule(IFluidModifier.class);
      if (fluidModifier != null) {
        // try draining each modifier
        FluidStack drained = fluidModifier.drain(tool, entry.getLevel(), resource, sim);
        if (!drained.isEmpty()) {
          // if we managed to drain something, add it into our current drained stack, and decrease the amount we still want to drain
          if (drainedSoFar.isEmpty()) {
            // if the first time, make a copy of the resource before changing it
            // though we can skip copying if the first one is all we need
            if (drained.getAmount() >= resource.getAmount()) {
              return drained;
            } else {
              drainedSoFar = drained;
              resource = resource.copy();
            }
          } else {
            drainedSoFar.grow(drained.getAmount());
          }
          // if we drained everything desired, return
          resource.shrink(drained.getAmount());
          if (resource.isEmpty()) {
            return drainedSoFar;
          }
        }
      }
    }
    return drainedSoFar;
  }

  @Nonnull
  @Override
  public FluidStack drain(long maxDrain, boolean sim) {
    FluidStack drainedSoFar = FluidStack.EMPTY;
    FluidStack toDrain = FluidStack.EMPTY;
    IToolStackView tool = this.tool.get();
    for (ModifierEntry entry : tool.getModifierList()) {
      IFluidModifier fluidModifier = entry.getModifier().getModule(IFluidModifier.class);
      if (fluidModifier != null) {
        // try draining each modifier
        // if we have no drained anything yet, use the type insensitive hook
        if (toDrain.isEmpty()) {
          FluidStack drained = fluidModifier.drain(tool, entry.getLevel(), maxDrain, sim);
          if (!drained.isEmpty()) {
            // if we finished draining, we are done, otherwise try again later with the type senstive hooks
            maxDrain -= drained.getAmount();
            if (maxDrain > 0) {
              drainedSoFar = drained;
              toDrain = new FluidStack(drained, maxDrain);
            } else {
              return drained;
            }
          }
        } else {
          // if we already drained some fluid, type sensitive and increase our results
          FluidStack drained = fluidModifier.drain(tool, entry.getLevel(), toDrain, sim);
          if (!drained.isEmpty()) {
            drainedSoFar.grow(drained.getAmount());
            toDrain.shrink(drained.getAmount());
            if (toDrain.isEmpty()) {
              return drainedSoFar;
            }
          }
        }
      }
    }
    return drainedSoFar;
  }

  /** Adds the tanks from the fluid modifier to the tool */
  public static void addTanks(ModDataNBT volatileData, IFluidModifier modifier) {
    volatileData.putInt(TOTAL_TANKS, modifier.getTanks(volatileData) + volatileData.getInt(TOTAL_TANKS));
  }

  /** Interface for modifiers with fluid capabilities to return */
  @SuppressWarnings("unused")
  public interface IFluidModifier {
    /**
     * Determines how many fluid tanks are used by this modifier
     * @param volatileData  Volatile data instance
     * @return  Number of tanks used
     */
    default int getTanks(IModDataView volatileData) {
      return 0;
    }

    /**
     * Gets the fluid in the given tank
     * @param tool   Tool instance
     * @param level  Modifier level
     * @param tank   Tank index
     * @return  Fluid in the given tank
     */
    default FluidStack getFluidInTank(IToolStackView tool, int level, int tank) {
      return FluidStack.EMPTY;
    }

    /**
     * Gets the max capacity for the given tank
     * @param tool   Tool instance
     * @param level  Modifier level
     * @param tank   Tank index
     * @return  Fluid in the given tank
     */
    default long getTankCapacity(IToolStackView tool, int level, int tank) {
      return 0;
    }

    /**
     * Checks if the fluid is valid for the given tank
     * @param tool   Tool instance
     * @param level  Modifier level
     * @param tank   Tank index
     * @param fluid  Fluid to insert
     * @return  True if the fluid is valid
     */
    default boolean isFluidValid(IToolStackView tool, int level, int tank, FluidStack fluid) {
      return true;
    }

    /**
     * Fills fluid into tanks
     * @param tool     Tool instance
     * @param level    Modifier level
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be filled. If you want to store this stack, make a copy
     * @param sim   If SIMULATE, fill will only be simulated.
     * @return Amount of resource that was (or would have been, if simulated) filled.
     */
    long fill(IToolStackView tool, int level, FluidStack resource, boolean sim);

    /**
     * Drains fluid out of tanks, distribution is left entirely to the IFluidHandler.
     * @param tool     Tool instance
     * @param level    Modifier level
     * @param resource FluidStack representing the Fluid and maximum amount of fluid to be drained.
     * @param sim   If SIMULATE, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     * simulated) drained.
     */
    FluidStack drain(IToolStackView tool, int level, FluidStack resource, boolean sim);

    /**
     * Drains fluid out of internal tanks, distribution is left entirely to the IFluidHandler.
     * @param tool     Tool instance
     * @param level    Modifier level
     * @param maxDrain Maximum amount of fluid to drain.
     * @param sim   If SIMULATE, drain will only be simulated.
     * @return FluidStack representing the Fluid and amount that was (or would have been, if
     * simulated) drained.
     */
    FluidStack drain(IToolStackView tool, int level, long maxDrain, boolean sim);
  }

  /** Helper to run a function from {@link IFluidModifier} */
  @FunctionalInterface
  private interface ITankCallback<T> {
    T run(IFluidModifier module, IToolStackView tool, int level, int tank);
  }

  /** Provider instance for a fluid cap */
  public static class Provider implements IToolCapabilityProvider {
    private final LazyOptional<IFluidHandlerItem> fluidCap;
    public Provider(ItemStack stack, Supplier<? extends IToolStackView> toolStack) {
      this.fluidCap = LazyOptional.of(() -> new ToolFluidCapability(stack, toolStack));
    }

    @Override
    public <T> LazyOptional<T> getCapability(IToolStackView tool, Class<T> cap) {
      if (cap == IFluidHandlerItem.class && tool.getVolatileData().getInt(TOTAL_TANKS) > 0) {
        return fluidCap.cast();
      }
      return LazyOptional.empty();
    }
  }
}
