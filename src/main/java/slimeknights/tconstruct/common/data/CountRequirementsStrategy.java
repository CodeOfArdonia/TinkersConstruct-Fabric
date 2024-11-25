package slimeknights.tconstruct.common.data;

import net.minecraft.advancements.RequirementsStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CountRequirementsStrategy implements RequirementsStrategy {

  private final int[] sizes;

  public CountRequirementsStrategy(int... sizes) {
    this.sizes = sizes;
  }

  @Override
  public String[][] createRequirements(Collection<String> strings) {
    String[][] requirements = new String[this.sizes.length][];
    List<String> list = new ArrayList<>(strings);
    int nextIndex = 0;
    for (int i = 0; i < this.sizes.length; i++) {
      requirements[i] = new String[this.sizes[i]];
      for (int j = 0; j < this.sizes[i]; j++) {
        requirements[i][j] = list.get(nextIndex);
        nextIndex++;
      }
    }
    return requirements;
  }
}
