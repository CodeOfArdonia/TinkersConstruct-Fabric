package slimeknights.tconstruct.tools.logic;

import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.PlayerComponent;
import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents;
import io.github.fabricators_of_create.porting_lib.entity.events.PlayerTickEvents;
import io.github.fabricators_of_create.porting_lib.util.LazyOptional;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.events.ToolEquipmentChangeEvent;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.modifiers.TinkerHooks;
import slimeknights.tconstruct.library.tools.context.EquipmentChangeContext;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Map;

/**
 * Capability to make it easy for modifiers to store common data on the player, primarily used for armor
 */
public class EquipmentChangeWatcher implements EntityComponentInitializer {

  /**
   * Capability ID
   */
  private static final ResourceLocation ID = TConstruct.getResource("equipment_watcher");
  /**
   * Capability type
   */
  public static final ComponentKey<PlayerLastEquipment> CAPABILITY = ComponentRegistry.getOrCreate(ID, PlayerLastEquipment.class);

  /**
   * Registers this capability
   */
  public static void register() {
//    FMLJavaModLoadingContext.get().getModEventBus().addListener(EventPriority.NORMAL, false, RegisterCapabilitiesEvent.class, event -> event.register(PlayerLastEquipment.class));

    // equipment change is used on both sides
    ServerEntityEvents.EQUIPMENT_CHANGE.register(EquipmentChangeWatcher::onEquipmentChange);

    // only need to use the cap and the player tick on the client
    if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
      PlayerTickEvents.END.register(EquipmentChangeWatcher::onPlayerTick);
//      MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, EquipmentChangeWatcher::attachCapability);
    }
  }


  /* Events */

  /**
   * Serverside modifier hooks
   */
  private static void onEquipmentChange(LivingEntity entity, EquipmentSlot slot, @Nonnull ItemStack from, @Nonnull ItemStack to) {
    runModifierHooks(entity, slot, from, to);
  }

  /**
   * Event listener to attach the capability
   */
  @Override
  public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
    registry.registerForPlayers(CAPABILITY, PlayerLastEquipment::new);
    EntityEvents.ON_REMOVE.register((entity, reason) -> {
      if (entity instanceof Player player)
        CAPABILITY.get(player).run();
    });
  }

  /**
   * Client side modifier hooks
   */
  private static void onPlayerTick(Player player) {
    // only run for client side players every 5 ticks
    CAPABILITY.maybeGet(player).ifPresent(PlayerLastEquipment::update);
  }


  /* Helpers */

  /**
   * Shared modifier hook logic
   */
  private static void runModifierHooks(LivingEntity entity, EquipmentSlot changedSlot, ItemStack original, ItemStack replacement) {
    EquipmentChangeContext context = new EquipmentChangeContext(entity, changedSlot, original, replacement);

    // first, fire event to notify an item was removed
    IToolStackView tool = context.getOriginalTool();
    if (tool != null) {
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(TinkerHooks.EQUIPMENT_CHANGE).onUnequip(tool, entry, context);
      }
      // only path that should bring you here that did not already call the modifier method is when your shield breaks. ideally we will switch to a forge onStoppedUsing method instead
      // TODO 1.19: consider simplier check, such as the tool having the active modifier tag set. Will need to do a bit of work for bows which don't set modifiers though
      if (!entity.isUsingItem() || entity.getItemBySlot(changedSlot) != entity.getUseItem()) {
        ModifierUtil.finishUsingItem(tool);
      }
    }

    // next, fire event to notify an item was added
    tool = context.getReplacementTool();
    if (tool != null) {
      for (ModifierEntry entry : tool.getModifierList()) {
        entry.getHook(TinkerHooks.EQUIPMENT_CHANGE).onEquip(tool, entry, context);
      }
    }

    // finally, fire events on all other slots to say something changed
    for (EquipmentSlot otherSlot : EquipmentSlot.values()) {
      if (otherSlot != changedSlot) {
        tool = context.getToolInSlot(otherSlot);
        if (tool != null) {
          for (ModifierEntry entry : tool.getModifierList()) {
            entry.getHook(TinkerHooks.EQUIPMENT_CHANGE).onEquipmentChange(tool, entry, context, otherSlot);
          }
        }
      }
    }
    // fire event for modifiers that want to watch equipment when not equipped
    new ToolEquipmentChangeEvent(context).sendEvent();
  }

  /* Required methods */

  /**
   * Data class that runs actual update logic
   */
  protected static class PlayerLastEquipment implements PlayerComponent<PlayerLastEquipment>, Runnable {

    @Nullable
    private final Player player;
    private final Map<EquipmentSlot, ItemStack> lastItems = new EnumMap<>(EquipmentSlot.class);
    private LazyOptional<PlayerLastEquipment> capability;

    private PlayerLastEquipment(@Nullable Player player) {
      this.player = player;
      for (EquipmentSlot slot : EquipmentSlot.values()) {
        this.lastItems.put(slot, ItemStack.EMPTY);
      }
      this.capability = LazyOptional.of(() -> this);
    }

    /**
     * Called on player tick to update the stacks and run the event
     */
    public void update() {
      // run twice a second, should be plenty fast enough
      if (this.player != null) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
          ItemStack newStack = this.player.getItemBySlot(slot);
          ItemStack oldStack = this.lastItems.get(slot);
          if (!ItemStack.matches(oldStack, newStack)) {
            this.lastItems.put(slot, newStack.copy());
            runModifierHooks(this.player, slot, oldStack, newStack);
          }
        }
      }
    }

    /**
     * Called on capability invalidate to invalidate
     */
    @Override
    public void run() {
      this.capability.invalidate();
      this.capability = LazyOptional.of(() -> this);
    }

//    @Nonnull
//    @Override
//    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
//      return CAPABILITY.orEmpty(cap, capability);
//    }

    @Override
    public void readFromNbt(CompoundTag tag) {

    }

    @Override
    public void writeToNbt(CompoundTag tag) {

    }
  }
}
