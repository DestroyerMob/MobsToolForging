# Mobs Tool Forging

A NeoForge 1.21.1 mod that adds physical, modular tool making without changing vanilla anvil behavior.

## Current Features

- Tool Forge workstation for metal tool heads.
- Lapidary Table workstation for gem tool heads.
- Physical pattern items select part templates on workstations.
- In-world station previews show the selected part shape before material is added.
- Smithing Hammer workflow with material placement, progress, and block-entity visuals.
- Modular vanilla-style tool and weapon set: sword, shovel, pickaxe, axe, and hoe.
- Tag-driven starter materials:
  - Metals: iron, gold, copper, netherite
  - Gems: diamond, emerald
  - Handles: sticks, blaze rods, breeze rods, and common rod tags
- Finished tools store construction data and a stat profile built from head, handle, binding/guard, wrap, focus, treatment, and quality.
- Layered item models render visible heads, handles, bindings/guards, wraps, foci, and treatments.
- Generated visual-definition data and part sprites provide placeholder defaults for the dynamic layered tool model system.

## Development

Requirements:

- Java 21
- NeoForge 21.1.233
- Gradle wrapper included

Useful commands:

```bash
./gradlew --no-daemon runData
./gradlew --no-daemon build
```

The built jar is written to `build/libs/`.

## Visual System

The mod uses part-aware rendered tools instead of flat tint recolors. See `docs/tool_visual_system.md` for the construction data, material visual JSON, tool visual JSON, generated part sprites, and bridge-mod conventions.

Procedural sprites are placeholder defaults. Final-quality art should come from hand-authored source sprites and greyscale templates; see `docs/ART_PIPELINE.md`.

## Current Limitations

- Pattern items currently define shape only. The station defines the material process: metals are shaped on the Tool Forge, and gems are shaped on the Lapidary Table.
- The old template selector screen is a debug fallback only and is disabled during normal play unless the debug config is enabled.
- Bridge support for non-vanilla tool families, such as Mobs More Weapons greatswords, is planned but not implemented. See `docs/API_PLAN.md`.
- Generated part sprites are useful for wiring tests, but they are not final art.
