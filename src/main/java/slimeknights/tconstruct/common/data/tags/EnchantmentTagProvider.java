package slimeknights.tconstruct.common.data.tags;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.enchantment.Enchantment;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.modifiers.ModifierId;
import slimeknights.tconstruct.tools.TinkerModifiers;
import slimeknights.tconstruct.tools.data.ModifierIds;

import java.util.concurrent.CompletableFuture;

public class EnchantmentTagProvider extends FabricTagProvider.EnchantmentTagProvider {

  public EnchantmentTagProvider(FabricDataOutput output, CompletableFuture<HolderLookup.Provider> completableFuture) {
    super(output, completableFuture);
  }

  @Override
  protected void addTags(HolderLookup.Provider provider) {
    // upgrade
    this.modifierTag(TinkerModifiers.experienced.getId(), "cyclic:experience_boost", "ensorcellation:exp_boost");
    this.modifierTag(ModifierIds.killager, "ensorcellation:damage_illager");
    this.modifierTag(TinkerModifiers.magnetic.getId(), "cyclic:magnet");
    this.modifierTag(TinkerModifiers.necrotic.getId(), "cyclic:life_leech", "ensorcellation:leech");
    this.modifierTag(TinkerModifiers.severing.getId(), "cyclic:beheading", "ensorcellation:vorpal");
    this.modifierTag(ModifierIds.stepUp, "cyclic:step");
    this.modifierTag(TinkerModifiers.soulbound.getId(), "ensorcellation:soulbound");
    this.modifierTag(ModifierIds.trueshot, "ensorcellation:trueshot");

    // defense
    this.modifierTag(ModifierIds.knockbackResistance, "cyclic:steady");
    this.modifierTag(TinkerModifiers.magicProtection.getId(), "ensorcellation:magic_protection");
    this.modifierTag(ModifierIds.revitalizing, "ensorcellation:vitality");

    // ability
    this.modifierTag(TinkerModifiers.autosmelt.getId(), "cyclic:auto_smelt", "ensorcellation:smelting");
    this.modifierTag(TinkerModifiers.doubleJump.getId(), "cyclic:launch", "walljump:doublejump");
    this.modifierTag(TinkerModifiers.expanded.getId(), "cyclic:excavate", "ensorcellation:excavating", "ensorcellation:furrowing");
    this.modifierTag(ModifierIds.luck, "ensorcellation:hunter");
    this.modifierTag(TinkerModifiers.multishot.getId(), "cyclic:multishot", "ensorcellation:volley");
    this.modifierTag(ModifierIds.reach, "cyclic:reach", "ensorcellation:reach");
    this.modifierTag(TinkerModifiers.tilling.getId(), "ensorcellation:tilling");
    this.modifierTag(TinkerModifiers.reflecting.getId(), "parry:rebound");
  }

  /**
   * Creates a builder for a tag for the given modifier
   */
  private void modifierTag(ModifierId modifier, String... ids) {
    TagsProvider.TagAppender<Enchantment> appender = this.tag(TagKey.create(Registries.ENCHANTMENT, TConstruct.getResource("modifier_like/" + modifier.getPath())));
    for (String id : ids) {
      appender.addOptional(new ResourceLocation(id));
    }
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Block Enchantment Tags";
  }
}
