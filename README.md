# Mobs Tool Forging

Mobs Tool Forging is a NeoForge 1.21.1 progression overhaul for making, assembling, maintaining, and visually customizing modular tools and armour through physical in-world workstations.

## Project Facts

- Mod id: `mobstoolforging`
- Current version: `0.1.0`
- Target: Minecraft 1.21.1, NeoForge 21.1.234, Java 21
- Common config: `config/mobstoolforging-common.toml`

## Current Features

- Tool Forge workstation for metal tool parts.
- Crude Anvil workstation for cheap early metal shaping with a lower quality cap.
- Lapidary Table workstation for coating heated metal tool parts with gem shells.
- Heating Forge and campfire workpiece warming backed by data-driven heating recipes, visible workpieces, temperature targets, and configurable cooling buffers.
- Toolmaker's Station variants for physical, no-GUI assembly and separating modular tools.
- Pattern Creation Station, cheap Pattern Boards, paper-compatible inputs, physical pattern items, and nearby Pattern Racks for selecting part templates.
- In-world station previews show the selected part shape before material is added.
- Smithing Hammer workflow with material placement, progress, timing quality cues, and block-entity visuals.
- Modular vanilla-style tool and weapon set: sword, shovel, pickaxe, axe, hoe, and mattock.
- Modular helmet, chestplate, leggings, and boots with leather, chainmail, plate, gem-shell, and external-component construction data.
- Physical Leather Station assembly, repair, and interaction flow, with wood variants built in-world from matching planks and logs.
- Drying Rack variants and data-driven drying recipes; the built-in route turns rotten flesh into leather.
- In-world flint knapping for modular starter flint parts, assembled at a Toolmaker's Station without Plant Fiber binding.
- Tag-driven starter materials:
  - Metals: iron, gold, copper
  - Gems: diamond, emerald
  - Handles: sticks, blaze rods, breeze rods, and common rod tags
- Finished tools store construction data and a stat profile built from head, handle, binding/guard, wrap, focus, and treatment.
- Forged parts and finished tools store workmanship quality: Crude, Worked, Well Forged, Fine, or Masterwork.
- Modular armour and tools can be repaired at the workstation appropriate to their construction; modular armour material repair is blocked from bypassing the loop in a vanilla anvil.
- Finished tools and armour can be renamed at a Toolmaker's Station with a named name tag.
- Layered item models render visible heads, handles, bindings/guards, wraps, foci, and treatments.
- Jade integration for workstation state, heat, patterns, drying, assembly, and progress.
- JEI categories for forge shaping, station work, pattern creation, heating, drying, and material-trait guidance.
- Better Enchanting datapack hooks for modular parts, enchantment targets, and tag display.
- Bridge data for MoreWeapons tool families when that mod is present.
- Hand-authored part sprites plus generated visual-definition data provide resource-pack-friendly defaults for the dynamic layered tool and armour model systems.

## Development

Requirements are Java 21 and the included Gradle wrapper.

Useful commands:

```bash
./gradlew --no-daemon runData
./gradlew --no-daemon build
```

The built jar is written to `build/libs/`.

## Configuration

The common config controls progression and unfinished systems:

- Vanilla material tool recipes are disabled by default so the forging loop owns tool progression.
- Starter flint progression, Crude Anvil availability, pattern racks/input requirements, copper harvest rules, start-of-job heat, timing quality, optional lapidary tools/abrasives, part-first enchanting, and loot conversion are configurable.
- `debugTemplateSelector=false` keeps the old template selector screen disabled during normal play.
- The blackstone foundry stores solid metals and ordered, material-tinted molten layers directly. It draws datapack-defined fuel fluids from glass-walled, four-bucket tanks whose capacity is independent of foundry size; only molten-metal capacity scales with the interior. Fluid temperatures and centralized material melting points use Celsius, with gameplay lava set to 2,000°C so it remains hot enough for every built-in material. A wall drain and faucet pour the bottom layer into casting receivers.

## Player Progression

The intended standalone path is flint knapping, Toolmaker's Station assembly, Pattern Boards, campfire low heat, and the Crude Anvil before upgrading to the Tool Forge and Heating Forge. Metal parts can then receive gem shells at the Lapidary Table, while leather and armour work use Leather Stations and Drying Racks.

See [docs/PROGRESSION.md](docs/PROGRESSION.md) for the exact early-game loop and default gates.

## Visual System

The mod uses part-aware rendered tools instead of flat tint recolors. See `docs/tool_visual_system.md` for the construction data, material visual JSON, tool visual JSON, generated part sprites, and bridge-mod conventions.

Part sprites are hand-authored and the model/definition outputs are generated around them; see [docs/ART_PIPELINE.md](docs/ART_PIPELINE.md).

## Data And Compatibility

Materials, forge templates, tool types, stat rules, station work, heating, drying, visuals, and bridge behavior are data-driven. The extension points and Java registration boundaries are documented in [docs/COMPATIBILITY_API.md](docs/COMPATIBILITY_API.md).

Minecraft Beyond removes direct vanilla armour and MoreWeapons recipes, injects forging supplies into mineshaft loot, and orders vanilla-equipment conversion after Apotheosis affix processing. MoreWeapons owns bridge data for its five weapon families; Better Enchanting supplies modular material and part-aware enchantment behavior.

## Current Limitations

- Pattern items define forged metal shapes. The Lapidary Table no longer uses patterns; it coats already-forged, heated metal tool parts with gem material.
- Armour base patterns create the finished wearable armour piece directly. Add-on patterns such as helmet combs, helmet visors, knees, and tassets install onto that armour afterward.
- Armour construction, rendering, repair, and part enchanting are active, but armour quality effects and the wider armour progression still need additional balance passes.
- The old template selector screen is a debug fallback only and is disabled during normal play unless the debug config is enabled.
- Bridge support is data-driven. MoreWeapons currently supplies bridge data for great swords, katanas, battle axes, knives, and machetes; other non-vanilla families still need bridge data from their owning mods.
- Some equipment and workstation art is still provisional despite the hand-authored part-sprite pipeline.
- Early progression uses placed flint knapping, Toolmaker's Station assembly, Pattern Boards, campfire low heat, and the Crude Anvil before the copper Tool Forge upgrade. Plant Fiber is not required for starter flint tools. See `docs/PROGRESSION.md`.
- The scalable blackstone foundry is the active high-heat melting and casting path, with direct solid input, wall-mounted fluid-fuel tanks, temperature-gated melting, ordered molten layers, alloys, reusable part casts, ingot casting, and storage-block casting.

## License

MIT. See [LICENSE](LICENSE).
