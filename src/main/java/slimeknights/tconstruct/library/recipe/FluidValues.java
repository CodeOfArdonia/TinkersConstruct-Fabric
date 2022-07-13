package slimeknights.tconstruct.library.recipe;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * These values define common amounts of fluids used in recipes.
 * <p>
 * Use these values when registering casting/melting interactions to ensure if the fluid API changes the values are still correct
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FluidValues {
  // metal
  /** Value of a single metal ingot, is divisible by 9 */
  public static final long INGOT = 90;
  /** Value of a single metal nugget */
  public static final long NUGGET = INGOT / 9;
  /** Value of a single metal block, is divisible by 81 */
  public static final long METAL_BLOCK = INGOT * 9;

  // gem
  /** Value of a gem such as diamond or emerald */
  public static final long GEM = 100;
  /** Value of a quarter gem */
  public static final long GEM_SHARD = GEM / 4;
  /** Value of a block of 9 gems, such as emerald or an ender pearl */
  public static final long LARGE_GEM_BLOCK = GEM * 9;
  /** Value of a block of 9 gems, such as emerald or an ender pearl */
  public static final long SMALL_GEM_BLOCK = GEM * 4;

  // clay/brick
  /** Value of a single brick, divisible by 2 */
  public static final long BRICK = 250;
  /** Value of a single metal brick block, is divisible by 36 */
  public static final long BRICK_BLOCK = BRICK * 4;

  // glass
  /** Value of a single glass block, also used for obsidian */
  public static final long GLASS_BLOCK = 1000;
  /** Value of a glass pane, slightly cheaper than vanilla */
  public static final long GLASS_PANE = GLASS_BLOCK / 4;

  // slime
  /** Value of a single slimeball, also used for clay, slime substitutes, and ender pearls */
  public static final long SLIMEBALL = 250;
  /** Value of a block worth 4 slime, see also congealed */
  public static final long SLIME_CONGEALED = SLIMEBALL * 4;
  /** Value of a block worth 9 slime */
  public static final long SLIME_BLOCK = SLIMEBALL * 9;

  // soup
  /** Value of a single bowl of soup */
  public static final long BOWL = 250;
  /** Value of a single bottle of a potion */
  public static final long BOTTLE = 250;

  // tank capacities
  /** Capacity of a seared or scorched lantern */
  public static final long LANTERN_CAPACITY = 50;
}
