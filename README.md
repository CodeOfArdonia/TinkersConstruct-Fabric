# Supporoform

This is a fork of `Tinker's Construct`/`Hephaestus`, contains bug fixes and some slight modifications designed for my
modpack.

Also, this is an LTS version only for 1.20.1 fabric. So don't ask me for porting to other version, please use
[Hephaestus](https://github.com/Alpha-s-Stuff/TinkersConstruct) instead.

## Fixes

- Casting Table&Basin will not drop casts and items correctly.
- Crash client when you open the gui provide by a broken smeltery.
- Some recipes cannot be used due to wrong tags.
- When throwing items directly in to smeltery it will wrongly stack.
- Crash server when wearing piglin/zombie_piglin head.
- Piggy backpack will make your chestplate disappear.
- When trying to fill a container without enough space, it will cause fluid copy. (Now it will deny fluid transfer)
- When putting a single fuel in heater it will disappear without adding furnace burn time.

## Modifications

- Remove skeletons smelt to milk recipes.

## Capability

Since most code don't have massive modification, mods depend on `Hephaestus` can be used on `Supporoform`, but not 100%.

The following mods have tested and can still be used on `Supporoform`

- EMI Addon: Extra Mod Integrations

## FAQ

### Can I just simply replace `Hephaestus` and this mod with each other?

Yes, you can safely replace it. All registry names are not modified.

### Why not just make an addon to fix `Hephaestus`?

This mod is firstly planned to use some mixins to fix `Hephaestus` bugs. But after some research I found that there are
some code I cannot modify directly, which means some bugs cannot be fixed with addons.

## Any Questions?

Join our Discord: https://discord.gg/NDzz2upqAk
