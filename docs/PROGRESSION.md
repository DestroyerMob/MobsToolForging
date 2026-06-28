# Early Progression

Mobs Tool Forging does not use wood or stone as tool-head materials. Wood remains useful as handles, but it is not a head tier in the modular system.

## Intended Standalone Loop

1. Break grass, ferns, tall grass, or large ferns for Plant Fiber.
2. Gather sticks and vanilla flint.
3. Sneak-right-click flint onto the top of a sturdy block to place a knapping workpiece.
4. Look at the placed flint with an item tagged `mobstoolforging:knapping_tools`; vanilla flint is included by default.
5. Sneak-scroll to choose the target part: sword blade, sword guard, shovel head, pickaxe head, axe head, or hoe head.
6. Right-click the placed flint four times with the knapping tool to turn one flint into one flint part.
7. Add the required stick handle, and for swords add a flint sword guard, to the ground assembly.
8. Right-click the valid assembly with Plant Fiber to consume one fiber and produce the modular flint tool.
9. Use the Flint Pick to harvest stone, coal, and copper ore.
10. Smelt copper, craft the copper-based Smithing Anvil and Smithing Hammer, and move into copper heads.
11. Use a copper pick to harvest iron ore, then move into iron modular tools.

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

Assembly is deliberately small and no-GUI. A knapped primary part creates a ground assembly automatically. Players can also sneak-place a starter flint primary part onto the ground. The assembly accepts one stick/handle for every tool, and swords additionally require a flint sword guard. Plant Fiber is only the assembly catalyst; it is consumed when the tool is produced and is not stored as a final binding or wrap component.

The Flint Pick is deliberately narrow:

- uses vanilla wooden-tier mining rules
- mines basic stone progression blocks that wooden pickaxes can mine
- mines copper ore when `copperRequiresWoodenTool` is enabled
- does not mine iron ore by default
- does not mine stone-tier or diamond-tier ores

## Copper

Copper is the first real metal tier. It uses vanilla stone-tier mining rules, so it can mine iron ore but cannot mine diamond ore. Its durability, speed, damage, and enchantability are still copper-specific rather than copied wholesale from stone.

## Config

- `enableCrudeFlintTools = true`
- `disableStoneTools = true`
- `disableWoodenTools = false`
- `copperRequiresWoodenTool = true`

`enableCrudeFlintTools` gates Plant Fiber drops, placed flint knapping, starter flint ground assembly, and creative visibility for prebuilt flint tools. Vanilla stone and wooden tool recipes are removed at server start according to the tool-recipe config values. Wooden tools remain enabled by default for standalone friendliness.
