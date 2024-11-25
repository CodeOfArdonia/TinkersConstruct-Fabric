package slimeknights.tconstruct.common.data.tags;

import io.github.fabricators_of_create.porting_lib.data.ExistingFileHelper;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.common.TinkerTags;
import slimeknights.tconstruct.library.data.tinkering.AbstractModifierTagProvider;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;

public class ModifierTagProvider extends AbstractModifierTagProvider {

  public ModifierTagProvider(FabricDataOutput output, ExistingFileHelper existingFileHelper) {
    super(output, TConstruct.MOD_ID, existingFileHelper);
  }

  @Override
  protected void addTags() {
    this.tag(TinkerTags.Modifiers.GEMS).addOptional(ModifierIds.diamond, ModifierIds.emerald);
    this.tag(TinkerTags.Modifiers.INVISIBLE_INK_BLACKLIST)
      .add(TinkerModifiers.embellishment.getId(), TinkerModifiers.dyed.getId(), TinkerModifiers.creativeSlot.getId(), TinkerModifiers.statOverride.getId())
      .addOptional(ModifierIds.shiny, TinkerModifiers.golden.getId());
    this.tag(TinkerTags.Modifiers.EXTRACT_MODIFIER_BLACKLIST)
      .add(TinkerModifiers.embellishment.getId(), TinkerModifiers.dyed.getId(), TinkerModifiers.creativeSlot.getId(), TinkerModifiers.statOverride.getId());
    // blacklist modifiers that are not really slotless, they just have a slotless recipe
    this.tag(TinkerTags.Modifiers.EXTRACT_SLOTLESS_BLACKLIST).add(ModifierIds.luck, ModifierIds.toolBelt);

    // modifiers in this tag support both left click and right click interaction
    this.tag(TinkerTags.Modifiers.DUAL_INTERACTION)
      .add(TinkerModifiers.bucketing.getId(), TinkerModifiers.spilling.getId(),
        TinkerModifiers.glowing.getId(), TinkerModifiers.firestarter.getId(),
        TinkerModifiers.stripping.getId(), TinkerModifiers.tilling.getId(), TinkerModifiers.pathing.getId(),
        TinkerModifiers.shears.getId(), TinkerModifiers.harvest.getId())
      .addOptional(ModifierIds.pockets);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Modifier Tag Provider";
  }
}
