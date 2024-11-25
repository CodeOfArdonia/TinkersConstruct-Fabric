package slimeknights.tconstruct.smeltery.block.entity.module.alloying;

import io.github.fabricators_of_create.porting_lib.mixin.accessors.common.accessor.RecipeManagerAccessor;
import lombok.RequiredArgsConstructor;
import net.minecraft.world.level.Level;
import slimeknights.mantle.block.entity.MantleBlockEntity;
import slimeknights.tconstruct.library.recipe.TinkerRecipeTypes;
import slimeknights.tconstruct.library.recipe.alloying.AlloyRecipe;
import slimeknights.tconstruct.library.recipe.alloying.IMutableAlloyTank;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

/**
 * Alloying module that supports only a single output
 */
@RequiredArgsConstructor
public class SingleAlloyingModule implements IAlloyingModule {

  private final MantleBlockEntity parent;
  private final IMutableAlloyTank alloyTank;
  private AlloyRecipe lastRecipe;

  /**
   * Gets a nonnull world instance from the parent
   */
  private Level getLevel() {
    return Objects.requireNonNull(this.parent.getLevel(), "Parent tile entity has null world");
  }

  /**
   * Finds the recipe to perform
   */
  @Nullable
  private AlloyRecipe findRecipe() {
    Level world = this.getLevel();
    if (this.lastRecipe != null && this.lastRecipe.canPerform(this.alloyTank)) {
      return this.lastRecipe;
    }
    // fetch the first recipe that matches the inputs and fits in the tank
    // means if for some reason two recipes both are vaiud, the tank contents can be used to choose
    Optional<AlloyRecipe> recipe = ((RecipeManagerAccessor) world.getRecipeManager())
      .port_lib$byType(TinkerRecipeTypes.ALLOYING.get())
      .values().stream()
      .filter(r -> r instanceof AlloyRecipe)
      .map(r -> (AlloyRecipe) r)
      .filter(r -> this.alloyTank.canFit(r.getOutput(), 0) && r.canPerform(this.alloyTank))
      .findAny();
    // if found, cache and return
    if (recipe.isPresent()) {
      this.lastRecipe = recipe.get();
      return this.lastRecipe;
    } else {
      return null;
    }
  }

  @Override
  public boolean canAlloy() {
    return this.findRecipe() != null;
  }

  @Override
  public void doAlloy() {
    AlloyRecipe recipe = this.findRecipe();
    if (recipe != null) {
      recipe.performRecipe(this.alloyTank);
    }
  }
}
