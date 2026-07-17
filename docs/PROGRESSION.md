# Early Progression

Mobs Tool Forging does not use wood or stone as tool-head materials. Wood remains useful as handles, but it is not a head tier in the modular system.

## Intended Standalone Loop

1. Gather sticks and vanilla flint.
2. Sneak-right-click flint onto the top of a sturdy block to place a knapping workpiece.
3. Look at the placed flint with an item tagged `mobstoolforging:knapping_tools`; vanilla flint is included by default.
4. Sneak-scroll to choose the target part, or use the **Previous Knapping Target** and **Next Knapping Target** controls without sneaking. Both controls are controller-bindable and exposed by controller-support mods such as Controlify; their default keyboard fallbacks are F6 and F7.
5. Right-click the placed flint four times with the knapping tool to turn one flint into one flint part.
6. Take the knapped flint part to a Toolmaker's Station.
7. Add the required stick handle, and for swords add a flint sword guard.
8. Assemble the modular flint tool with an empty hand or a Smithing Hammer. Plant Fiber is not required.
9. Use the Flint Pick to harvest stone, coal, and copper ore.
10. Craft Pattern Boards from planks and use them in the Pattern Creation Station to make physical head patterns without paper.
11. Craft a Crude Anvil from cobblestone, warm heat-requiring materials or tool parts on a lit campfire, and make basic copper, gold, or rough iron parts.
12. Assemble finished tools on a Toolmaker's Station with an empty hand or a Smithing Hammer; a screwdriver is not required for normal assembly.
13. Upgrade to the copper-based Tool Forge when you can afford one copper block plus four copper ingots.
14. Use the Tool Forge and Heating Forge for higher quality caps, easier timing, and repeated hot-work support.
15. For gem-coated tools, forge a metal part, heat it, then use the Lapidary Table to set the gem shell onto that part.

## Flint

Flint tools are starter modular tools assembled from actual Mobs Tool Forging parts. Knapping has no failure chance: one placed flint becomes one selected part after four valid strikes.

The old `flint_shard` item remains registered for compatibility, but flint shards are no longer part of the active starter loop and the flint-shard crafting recipes are no longer generated.

The starter part cycle is fixed:

- Flint Sword Blade
- Flint Sword Guard
- Flint Shovel Head
- Flint Pickaxe Head
- Flint Axe Head
- Flint Hoe Head

Starter flint tools use the same physical Toolmaker's Station assembly as the rest of the modular tool loop. Place the knapped primary part, add one stick or handle, add a flint sword guard for swords, then assemble with an empty hand or a Smithing Hammer. Plant Fiber is not consumed and is not stored as a final binding or wrap component.

The Flint Pick is deliberately narrow:

- uses vanilla wooden-tier mining rules
- mines basic stone progression blocks that wooden pickaxes can mine
- mines copper ore when `copperRequiresWoodenTool` is enabled
- does not mine iron ore by default
- does not mine stone-tier or diamond-tier ores

## Copper

Copper is the first real metal tier. It uses vanilla stone-tier mining rules, so it can mine iron ore but cannot mine diamond ore. Its durability, speed, damage, and enchantability are still copper-specific rather than copied wholesale from stone.

## Crude Anvil And Heat

The Crude Anvil is the early bridge between flint and the proper Tool Forge. It uses the same physical station flow as the Tool Forge, but it has a lower quality cap and is intended for basic copper, gold, and rough iron work.

Metal forging now checks heat when the job starts by default. A lit campfire supplies low heat for early work: right-click the campfire with any heat-requiring material or tool part to place it in one of the campfire's normal visible slots, or drop one onto the fire to insert it if a slot is free. The campfire warms the workpiece toward the configured campfire heat level and ejects it when that low-heat timer finishes. Low heat targets 55% by default, and low-heat materials remain usable until they cool below `lowHeatMinimumForgeTemperature`, 40% by default. A nearby lit campfire counts as low workshop heat only for an already-warmed workpiece, so it does not make a cold ingot forge-ready on its own. Copper, gold, and iron allow low heat by default; datapack metals can opt in with `minimum_forge_heat: "low"` in their material definition. A fueled Heating Forge supplies hot workshop heat for a longer buffer, so players can keep shaping nearby without reheating every individual ingot. With `requireHeatAtJobStartOnly=true` and `workpieceCoolsMidCraft=false`, hammering does not stop because a workpiece cooled mid-craft.

The Tool Forge is the upgrade path, not the first gate. Its default recipe is one copper block plus four copper ingots, and it earns that cost through a higher quality cap, better timing setup, proper Heating Forge support, and future high-heat hooks.

## Patterns And Assembly

Pattern Boards are the default low-friction input for the Pattern Creation Station when `basicPatternsRequirePaper=false`. Paper is still accepted for compatibility and future blueprint-style use. Existing `pattern_station_paper_cost` JSON remains valid; it is treated as a generic pattern input cost.

The Lapidary Table does not use patterns. It coats an already-forged heated metal tool part with gem material, producing a metal-gem part that looks like the gem part while retaining the metal as a core material. Head pieces use the gem head stats when assembled into a finished tool.

Toolmaker's Station variants remain physical and no-GUI. Place the shaped parts on the station, then use an empty hand or a Smithing Hammer to assemble them. A Smithing Hammer still separates one placed finished tool. The screwdriver item remains registered for compatibility, but it is no longer part of the normal tool assembly loop.

## Quality

Forged and coated parts store a workmanship quality score that maps to Crude, Worked, Well Forged, Fine, or Masterwork. Coated gem parts average the original metal part quality with the gem-work quality and can still be polished. Finished tool quality is derived from the primary part, required support parts, and a small assembly baseline. Existing tools or parts without quality data read as Well Forged.

Timing quality is optional and non-destructive. A good hit during the visible workpiece pulse can improve the score, while misses still progress the craft and do not destroy material. If timing quality is disabled, quality is determined by station tier, starting heat, material difficulty, and optional lapidary help. Gem shell materials may also declare an abrasive tier requirement; built-in diamond requires diamond-tier abrasive before it can be applied, while emerald, ruby, and sapphire do not require abrasive yet.

Quality affects finished tool stats modestly when `qualityAffectsStats=true`: Crude and Worked are small penalties, Well Forged is baseline, Fine is a small bonus, and Masterwork is a moderate durability bonus with small stat help. Masterwork is not required for normal play.

## Config

- `enableCrudeFlintTools = true`
- `enableCrudeAnvil = true`
- `basicPatternsRequirePaper = false`
- `disableStoneTools = true`
- `disableWoodenTools = false`
- `copperRequiresWoodenTool = true`
- `enableCampfireLowHeat = true`
- `requireHeatAtJobStartOnly = true`
- `workpieceCoolsMidCraft = false`
- `enableQuality = true`
- `enableTimingQuality = true`

`enableCrudeFlintTools` gates placed flint knapping, starter flint parts, and creative visibility for prebuilt flint tools. `enablePlantFiberDrops` controls optional Plant Fiber drops; Plant Fiber is not required for starter flint assembly. `enableCrudeAnvil` gates the Crude Anvil recipe. Vanilla stone and wooden tool recipes are removed at server start according to the tool-recipe config values. Wooden tools remain enabled by default for standalone friendliness.
