package slimeknights.tconstruct.tools.data.sprite;

import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.client.data.material.AbstractPartSpriteProvider;
import slimeknights.tconstruct.library.materials.stats.MaterialStatsId;
import slimeknights.tconstruct.tools.stats.RepairKitStats;

/**
 * This class handles all tool part sprites generated by Tinkers' Construct. You can freely use this in your addon to generate TiC part textures for a new material
 * Do not use both this and {@link TinkerMaterialSpriteProvider} in a single generator for an addon, if you need to use both make two instances of {@link slimeknights.tconstruct.library.client.data.material.MaterialPartTextureGenerator}
 */
public class TinkerPartSpriteProvider extends AbstractPartSpriteProvider {

  public static final MaterialStatsId PLATE = new MaterialStatsId(TConstruct.MOD_ID, "plate");
  public static final MaterialStatsId SLIMESUIT = new MaterialStatsId(TConstruct.MOD_ID, "slimesuit");

  public TinkerPartSpriteProvider() {
    super(TConstruct.MOD_ID);
  }

  @Override
  public String getName() {
    return "Tinkers' Construct Parts";
  }

  @Override
  protected void addAllSpites() {
    // heads
    this.addHead("round_plate");
    this.addHead("large_plate");
    this.addHead("small_blade");
    // handles
    this.addHandle("tool_handle");
    this.addHandle("tough_handle");
    // bow
    this.addBowstring("bowstring");
    // misc
    this.addBinding("tool_binding");
    this.addPart("repair_kit", RepairKitStats.ID);

    // plate textures
    this.addSprite("armor/plate/helmet_modifiers/tconstruct_embellishment", PLATE);
    this.addSprite("armor/plate/helmet_modifiers/tconstruct_embellishment_broken", PLATE);
    this.addSprite("armor/plate/chestplate_modifiers/tconstruct_embellishment", PLATE);
    this.addSprite("armor/plate/chestplate_modifiers/tconstruct_embellishment_broken", PLATE);
    this.addSprite("armor/plate/leggings_modifiers/tconstruct_embellishment", PLATE);
    this.addSprite("armor/plate/leggings_modifiers/tconstruct_embellishment_broken", PLATE);
    this.addSprite("armor/plate/boot_modifiers/tconstruct_embellishment", PLATE);
    this.addSprite("armor/plate/boot_modifiers/tconstruct_embellishment_broken", PLATE);
    this.addTexture("models/armor/plate/layer_1", PLATE);
    this.addTexture("models/armor/plate/layer_2", PLATE);

    // shield textures
    this.addSprite("armor/travelers/shield_modifiers/tconstruct_embellishment", PLATE);
    this.addSprite("armor/travelers/shield_modifiers/tconstruct_embellishment_broken", PLATE);
    this.addSprite("armor/plate/shield_modifiers/tconstruct_embellishment", PLATE);
    this.addSprite("armor/plate/shield_modifiers/tconstruct_embellishment_broken", PLATE);
    this.addSprite("armor/plate/shield_large_modifiers/tconstruct_embellishment", PLATE);
    this.addSprite("armor/plate/shield_large_modifiers/tconstruct_embellishment_broken", PLATE);

    // staff
    this.addSprite("staff/modifiers/tconstruct_embellishment", PLATE);
    this.addSprite("staff/large_modifiers/tconstruct_embellishment", PLATE);

    // slimesuit textures
    this.addSprite("armor/slime/skull_modifiers/tconstruct_embellishment", SLIMESUIT);
    this.addSprite("armor/slime/skull_modifiers/tconstruct_embellishment_broken", SLIMESUIT);
    this.addSprite("armor/slime/wings_modifiers/tconstruct_embellishment", SLIMESUIT);
    this.addSprite("armor/slime/wings_modifiers/tconstruct_embellishment_broken", SLIMESUIT);
    this.addSprite("armor/slime/shell_modifiers/tconstruct_embellishment", SLIMESUIT);
    this.addSprite("armor/slime/shell_modifiers/tconstruct_embellishment_broken", SLIMESUIT);
    this.addSprite("armor/slime/boot_modifiers/tconstruct_embellishment", SLIMESUIT);
    this.addSprite("armor/slime/boot_modifiers/tconstruct_embellishment_broken", SLIMESUIT);
    this.addTexture("models/armor/slime/layer_1", SLIMESUIT);
    this.addTexture("models/armor/slime/layer_2", SLIMESUIT);
    this.addTexture("models/armor/slime/wings", SLIMESUIT);

    // tools
    // pickaxe
    this.buildTool("pickaxe").addBreakableHead("head").addHandle("handle").addBinding("binding");
    this.buildTool("sledge_hammer").withLarge().addBreakableHead("head").addBreakableHead("back").addBreakableHead("front").addHandle("handle");
    this.buildTool("vein_hammer").withLarge().addBreakableHead("head").addHead("back").addBreakableHead("front").addHandle("handle");
    // shovel
    this.buildTool("mattock").addBreakableHead("axe").addBreakableHead("pick"); // handle provided by pickaxe
    this.buildTool("pickadze").addBreakableHead("axe"); // handle and "pick" head provided by other tools
    this.buildTool("excavator").withLarge().addBreakableHead("head").addHead("binding").addHandle("handle").addHandle("grip");
    // axe
    this.buildTool("hand_axe").addBreakableHead("head").addBinding("binding"); // handle provided by pickaxe
    this.buildTool("broad_axe").withLarge().addBreakableHead("blade").addBreakableHead("back").addHandle("handle").addBinding("binding");
    // scythe
    this.buildTool("kama").addBreakableHead("head").addBinding("binding"); // handle provided by pickaxe
    this.buildTool("scythe").withLarge().addBreakableHead("head").addHandle("handle").addHandle("accessory").addBinding("binding");
    // sword
    this.buildTool("dagger").addBreakableHead("blade").addHandle("crossguard");
    this.buildTool("sword").addBreakableHead("blade").addHandle("guard").addHandle("handle");
    this.buildTool("cleaver").withLarge().addBreakableHead("head").addBreakableHead("shield").addHandle("handle").addHandle("guard");
    // bow
    this.buildTool("crossbow")
      .addLimb("limb").addGrip("body")
      .addBowstring("bowstring").addBowstring("bowstring_1").addBowstring("bowstring_2").addBowstring("bowstring_3");
    this.buildTool("longbow").withLarge()
      .addLimb("limb_bottom").addLimb("limb_bottom_1").addLimb("limb_bottom_2").addLimb("limb_bottom_3")
      .addLimb("limb_top").addLimb("limb_top_1").addLimb("limb_top_2").addLimb("limb_top_3")
      .addGrip("grip")
      .addBreakableBowstring("bowstring").addBowstring("bowstring_1").addBowstring("bowstring_2").addBowstring("bowstring_3");
  }
}
