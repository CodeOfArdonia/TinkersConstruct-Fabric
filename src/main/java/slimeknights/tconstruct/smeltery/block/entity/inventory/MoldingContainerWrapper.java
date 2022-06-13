package slimeknights.tconstruct.smeltery.block.entity.inventory;

import com.google.common.collect.Iterators;
import io.github.fabricators_of_create.porting_lib.transfer.TransferUtil;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.world.item.ItemStack;
import slimeknights.tconstruct.library.recipe.molding.IMoldingContainer;

/** Wrapper around an item handler for the sake of use as a molding inventory */
@RequiredArgsConstructor
public class MoldingContainerWrapper implements IMoldingContainer {
  private final Storage<ItemVariant> handler;
  private final int slot;

  @Getter @Setter
  private ItemStack pattern = ItemStack.EMPTY;

  @Override
  public ItemStack getMaterial() {
    try (Transaction t = TransferUtil.getTransaction()) {
      StorageView<ItemVariant> view = Iterators.get(handler.iterator(t), slot);
      int stackSize = Math.min(view.getResource().getItem().getMaxStackSize(), (int) view.getAmount());
      return view.getResource().toStack(stackSize);
    }
  }
}
