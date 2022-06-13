package slimeknights.tconstruct.tools.item;

import io.github.fabricators_of_create.porting_lib.item.WalkOnSnowItem;
import io.github.fabricators_of_create.porting_lib.util.ArmorTextureItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.TConstruct;
import slimeknights.tconstruct.library.tools.definition.ModifiableArmorMaterial;
import slimeknights.tconstruct.library.tools.helper.ModifierUtil;
import slimeknights.tconstruct.library.tools.item.ModifiableArmorItem;
import slimeknights.tconstruct.tools.TinkerModifiers;

import javax.annotation.Nullable;

public class TravelersGearItem extends ModifiableArmorItem implements ArmorTextureItem, WalkOnSnowItem {
  /** Golden texture for armor */
  private static final String GOLDEN_ARMOR = TConstruct.resourceString("textures/models/armor/travelers_golden_1.png");
  /** Golden texture for leggings */
  private static final String GOLDEN_LEGS = TConstruct.resourceString("textures/models/armor/travelers_golden_2.png");

  public TravelersGearItem(ModifiableArmorMaterial material, ArmorSlotType slotType, Properties properties) {
    super(material, slotType, properties);
  }

  @Override
  public boolean canWalkOnPowderedSnow(ItemStack stack, LivingEntity wearer) {
    return slot == EquipmentSlot.FEET;
  }

  @Nullable
  @Override
  public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, String type) {
    if (ModifierUtil.getModifierLevel(stack, TinkerModifiers.golden.getId()) > 0) {
      return slot == EquipmentSlot.LEGS ? GOLDEN_LEGS : GOLDEN_ARMOR;
    }
    return null;
  }
//
//  @Override
//  public void initializeClient(Consumer<IItemRenderProperties> consumer) {
//    consumer.accept(new IItemRenderProperties() {
//      @Nonnull
//      @Override
//      public Model getBaseArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot armorSlot, HumanoidModel<?> _default) {
//        return TravelersGearModel.getModel(itemStack, armorSlot, _default);
//      }
//    });
//  }
}
