# Early Progression

Mobs Tool Forging does not use wood or stone as tool-head materials. Wood remains useful as handles, but it is not a head tier in the modular system.

## Intended Standalone Loop

1. Gather sticks and flint.
2. Smash flint against a hard, pickaxe-mineable block to chip it into flint shards.
3. Craft crude flint tools from flint shards.
4. Use the Flint Pick to harvest stone, coal, and copper ore.
5. Smelt copper.
6. Craft the copper-based Tool Forge and Smithing Hammer.
7. Forge copper heads as the first real metal tool tier.
8. Use a copper pick to harvest iron ore.
9. Move into iron modular tools.

## Flint

Flint tools are crude survival tools, not modular tools. They do not use bindings, foci, treatments, quality, or traits.

Flint shards come from right-clicking a hard surface with vanilla flint. Any block tagged `minecraft:mineable/pickaxe` counts as a hard surface for this first pass. Knapping has a chance to fail so the player may need a few hits. A successful hit consumes one flint, gives one shard, and has a bonus roll for a second shard.

The Flint Pick is deliberately narrow:

- mines stone and cobblestone
- mines coal ore
- mines copper ore when `flintCanMineCopper` is enabled
- does not mine iron ore by default
- does not mine iron-tier or diamond-tier ores

## Copper

Copper is the first real metal tier. It is weaker than iron but can mine iron ore, which gives it a clear bridge role instead of being cosmetic.

## Config

- `enableCrudeFlintTools = true`
- `disableStoneTools = true`
- `disableWoodenTools = false`
- `flintCanMineCopper = true`
- `flintCanMineIron = false`
- `flintKnappingSuccessChance = 0.6`
- `flintKnappingBonusShardChance = 0.4`

Vanilla stone and wooden tool recipes are removed at server start according to these config values. Wooden tools remain enabled by default for standalone friendliness.
