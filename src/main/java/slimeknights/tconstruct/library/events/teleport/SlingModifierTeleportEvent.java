package slimeknights.tconstruct.library.events.teleport;

import io.github.fabricators_of_create.porting_lib.entity.events.EntityEvents;
import lombok.Getter;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.world.entity.Entity;
import slimeknights.tconstruct.library.modifiers.ModifierEntry;
import slimeknights.tconstruct.library.tools.nbt.IToolStackView;

/** Event fired when an entity teleports using the ender sling modifier */
public class SlingModifierTeleportEvent extends EntityEvents.Teleport.EntityTeleportEvent {
  public static final Event<SlingEvent> SLING_MODIFIER_TELEPORT = EventFactory.createArrayBacked(SlingEvent.class, callbacks -> event -> {
    for (SlingEvent e : callbacks)
      e.onSlingModifierTeleport(event);
  });

  @Getter
  private final IToolStackView tool;
  @Getter
  private final ModifierEntry entry;
  public SlingModifierTeleportEvent(Entity entity, double targetX, double targetY, double targetZ, IToolStackView tool, ModifierEntry entry) {
    super(entity, targetX, targetY, targetZ);
    this.tool = tool;
    this.entry = entry;
  }

  @Override
  public void sendEvent() {
    SLING_MODIFIER_TELEPORT.invoker().onSlingModifierTeleport(this);
  }

  @FunctionalInterface
  public interface SlingEvent {
    void onSlingModifierTeleport(SlingModifierTeleportEvent event);
  }
}
