# Mobs Tool Forging

A NeoForge 1.21.1 mod that adds physical, modular tool making without changing vanilla anvil behavior.

## Current Features

- Smithing Anvil workstation for metal tool heads.
- Lapidary Table workstation for gem tool heads.
- Heating Forge for heating metal workpieces before forging.
- Toolmaker's Bench for assembling and separating modular tools.
- Pattern Creation Station and physical pattern items for selecting part templates.
- In-world station previews show the selected part shape before material is added.
- Smithing Hammer workflow with material placement, progress, and block-entity visuals.
- Modular vanilla-style tool and weapon set: sword, shovel, pickaxe, axe, and hoe.
- In-world flint knapping and Plant Fiber ground assembly for modular starter flint tools.
- Tag-driven starter materials:
  - Metals: iron, gold, copper, netherite
  - Gems: diamond, emerald
  - Handles: sticks, blaze rods, breeze rods, and common rod tags
- Finished tools store construction data and a stat profile built from head, handle, binding/guard, wrap, focus, and treatment.
- Layered item models render visible heads, handles, bindings/guards, wraps, foci, and treatments.
- Jade and JEI integration for workstation state and shaping recipes.
- Better Enchanting datapack hooks for modular parts, enchantment targets, and tag display.
- Bridge data for MoreWeapons tool families when that mod is present.
- Generated visual-definition data and part sprites provide placeholder defaults for the dynamic layered tool model system.

## Development

Requirements:

- Java 21
- NeoForge 21.1.234
- Gradle wrapper included

Useful commands:

```bash
./gradlew --no-daemon runData
./gradlew --no-daemon build
```

The built jar is written to `build/libs/`.

## Configuration

The common config controls progression and unfinished systems:

- Vanilla material tool recipes are disabled by default so the forging loop owns tool progression.
- Starter flint progression, copper harvest rules, heated metal requirements, and loot conversion are configurable.
- `debugTemplateSelector=false` keeps the old template selector screen disabled during normal play.
- Bloomery, crucible, and casting switches are present but reserved for future work and default to off.

## Visual System

The mod uses part-aware rendered tools instead of flat tint recolors. See `docs/tool_visual_system.md` for the construction data, material visual JSON, tool visual JSON, generated part sprites, and bridge-mod conventions.

Procedural sprites are placeholder defaults. Final-quality art should come from hand-authored source sprites and greyscale templates; see `docs/ART_PIPELINE.md`.

## Current Limitations

- Pattern items currently define shape only. The station defines the material process: metals are shaped on the Smithing Anvil, and gems are shaped on the Lapidary Table.
- The old template selector screen is a debug fallback only and is disabled during normal play unless the debug config is enabled.
- Bridge support is data-driven. MoreWeapons currently supplies bridge data for great swords, katanas, battle axes, knives, and machetes; other non-vanilla families still need bridge data from their owning mods.
- Generated part sprites are useful for wiring tests, but they are not final art.
- Early progression uses grass/fern Plant Fiber, placed flint knapping, and ground assembly to make modular flint tools before copper and iron. See `docs/PROGRESSION.md`.
- Bloomery, crucible, and casting progression are scaffolded in config/data but are not the active production path yet.
