# Mobs Tool Forging

A NeoForge 1.21.1 mod that adds physical, modular tool making without changing vanilla anvil behavior.

## Current Features

- Smithing Anvil workstation for metal tool heads.
- Crude Anvil workstation for cheap early metal shaping with a lower quality cap.
- Lapidary Table workstation for gem tool heads.
- Heating Forge and campfire workpiece warming for start-of-job metal heat.
- Toolmaker's Bench for physical, no-GUI assembly and separating modular tools.
- Pattern Creation Station, cheap Pattern Boards, paper-compatible inputs, and physical pattern items for selecting part templates.
- In-world station previews show the selected part shape before material is added.
- Smithing Hammer workflow with material placement, progress, timing quality cues, and block-entity visuals.
- Modular vanilla-style tool and weapon set: sword, shovel, pickaxe, axe, and hoe.
- Modular armour base pieces with material add-ons: helmet, chestplate, leggings, and boots.
- In-world flint knapping and Plant Fiber ground assembly for modular starter flint tools.
- Tag-driven starter materials:
  - Metals: iron, gold, copper
  - Gems: diamond, emerald
  - Handles: sticks, blaze rods, breeze rods, and common rod tags
- Finished tools store construction data and a stat profile built from head, handle, binding/guard, wrap, focus, and treatment.
- Forged parts and finished tools store workmanship quality: Crude, Worked, Well Forged, Fine, or Masterwork.
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
- Starter flint progression, Crude Anvil availability, pattern input requirements, copper harvest rules, start-of-job heat, timing quality, lapidary abrasive rules, and loot conversion are configurable.
- `debugTemplateSelector=false` keeps the old template selector screen disabled during normal play.
- Bloomery, crucible, and casting switches are present but reserved for future work and default to off.

## Visual System

The mod uses part-aware rendered tools instead of flat tint recolors. See `docs/tool_visual_system.md` for the construction data, material visual JSON, tool visual JSON, generated part sprites, and bridge-mod conventions.

Procedural sprites are placeholder defaults. Final-quality art should come from hand-authored source sprites and greyscale templates; see `docs/ART_PIPELINE.md`.

## Current Limitations

- Pattern items currently define shape only. The station defines the material process: metals are shaped on the Crude Anvil or Smithing Anvil, and gems are shaped on the Lapidary Table.
- Armour base patterns create the finished wearable armour piece directly. Add-on patterns such as helmet combs, helmet visors, knees, and tassets install onto that armour afterward.
- Armour add-ons follow the same material split as tool heads. This pass keeps armour quality fields compatible but does not add new armour quality effects or armour progression.
- The old template selector screen is a debug fallback only and is disabled during normal play unless the debug config is enabled.
- Bridge support is data-driven. MoreWeapons currently supplies bridge data for great swords, katanas, battle axes, knives, and machetes; other non-vanilla families still need bridge data from their owning mods.
- Generated part sprites are useful for wiring tests, but they are not final art.
- Early progression uses grass/fern Plant Fiber, placed flint knapping, ground assembly, Pattern Boards, campfire low heat, and the Crude Anvil before the copper Smithing Anvil upgrade. See `docs/PROGRESSION.md`.
- Bloomery, crucible, and casting progression are scaffolded in config/data but are not the active production path yet.
