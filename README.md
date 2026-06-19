# Mobs Tool Forging

A NeoForge 1.21.1 mod that adds physical, modular tool making without changing vanilla anvil behavior.

## Current Features

- Tool Forge workstation for metal tool heads.
- Lapidary Table workstation for gem tool heads.
- Smithing Hammer workflow with material placement, progress, and block-entity visuals.
- Modular vanilla-style tool and weapon set: sword, shovel, pickaxe, axe, and hoe.
- Tag-driven starter materials:
  - Metals: iron, gold, copper, netherite
  - Gems: diamond, emerald
  - Handles: sticks, blaze rods, breeze rods, and common rod tags
- Finished tools store construction data for future part-aware rendering.
- Generated visual-definition data and part sprites for a dynamic layered tool model system.

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

The mod is moving toward part-aware rendered tools instead of flat tint recolors. See `docs/tool_visual_system.md` for the construction data, material visual JSON, tool visual JSON, generated part sprites, and bridge-mod conventions.
