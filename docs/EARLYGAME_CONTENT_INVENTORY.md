# EarlyGame Content Inventory

Source: `/Users/ethanhellyer/Downloads/earlygame-1.19-1.1.2.jar`

Mod: `earlygame`, version `1.19-1.1.2`

Author: JayCeeCreates

License: MIT. Keep the copyright and MIT license text when repurposing any asset or code.

This document inventories the jar so Mobs Tool Forging can decide what to repurpose and where. It is not an implementation plan by itself.

## Current MTF Use

- `flint_shard.png` -> `mobstoolforging:textures/item/flint_shard.png`
- `plant_fiber.png` -> `mobstoolforging:textures/item/plant_fiber.png`
- Old EarlyGame flint tool textures are no longer used. Starter flint tools now come from placed flint knapping and ground assembly, producing MTF modular tools from actual flint parts.

The jar does not contain a flint shovel texture.

## Items

### Early Materials

- `plant_fiber` - Plant Fiber
- `lashing` - Lashing
- `flint_shard` - Flint Shard
- `copper_nugget` - Copper Nugget

### Utility Items

- `fire_starter` - Fire Starter
- `slingshot` - Slingshot

### Rock Items

- `stone_rock`
- `andesite_rock`
- `diorite_rock`
- `granite_rock`
- `deepslate_rock`
- `calcite_rock`
- `tuff_rock`
- `sandstone_rock`
- `red_sandstone_rock`

### Flint Tools

- `flint_knife`
- `flint_pickaxe`
- `flint_axe`
- `flint_saw`

No flint shovel exists in this jar.

### Copper Gear

- `copper_sword`
- `copper_knife`
- `copper_shovel`
- `copper_pickaxe`
- `copper_axe`
- `copper_saw`
- `copper_hoe`
- `copper_helmet`
- `copper_chestplate`
- `copper_leggings`
- `copper_boots`

### Knife and Saw Extensions

- `iron_knife`
- `iron_saw`
- `golden_knife`
- `golden_saw`
- `diamond_knife`
- `diamond_saw`
- `netherite_knife`
- `netherite_saw`

## Blocks

### Loose Rock Blocks

- `stone_rock_block`
- `andesite_rock_block`
- `diorite_rock_block`
- `granite_rock_block`
- `deepslate_rock_block`
- `calcite_rock_block`
- `tuff_rock_block`
- `sandstone_rock_block`
- `red_sandstone_rock_block`

### Cobbled Stone Variants

- `cobbled_andesite`
- `cobbled_diorite`
- `cobbled_granite`
- `cobbled_calcite`
- `cobbled_tuff`

### World Pickup Block

- `stick_twig_block`

## Textures

### Item Textures

- `andesite_rock.png`
- `calcite_rock.png`
- `copper_axe.png`
- `copper_boots.png`
- `copper_chestplate.png`
- `copper_helmet.png`
- `copper_hoe.png`
- `copper_knife.png`
- `copper_leggings.png`
- `copper_nugget.png`
- `copper_pickaxe.png`
- `copper_saw.png`
- `copper_shovel.png`
- `copper_sword.png`
- `deepslate_rock.png`
- `diamond_knife.png`
- `diamond_saw.png`
- `diorite_rock.png`
- `fire_starter.png`
- `flint_axe.png`
- `flint_knife.png`
- `flint_pickaxe.png`
- `flint_saw.png`
- `flint_shard.png`
- `golden_knife.png`
- `golden_saw.png`
- `granite_rock.png`
- `iron_knife.png`
- `iron_saw.png`
- `lashing.png`
- `netherite_knife.png`
- `netherite_saw.png`
- `plant_fiber.png`
- `red_sandstone_rock.png`
- `sandstone_rock.png`
- `slingshot.png`
- `slingshot_pulling_0.png`
- `slingshot_pulling_1.png`
- `slingshot_pulling_2.png`
- `stone_rock.png`
- `tuff_rock.png`

### Block Textures

- `cobbled_andesite.png`
- `cobbled_calcite.png`
- `cobbled_diorite.png`
- `cobbled_granite.png`
- `cobbled_tuff.png`

### Armor Textures

These are in the `minecraft` namespace in the source jar:

- `assets/minecraft/textures/models/armor/copper_layer_1.png`
- `assets/minecraft/textures/models/armor/copper_layer_2.png`

If repurposed, these should probably move into the Mobs Tool Forging namespace instead of overriding `minecraft`.

## Item Models

Most item models are standard generated or handheld item models pointing at the matching texture.

Useful source model groups:

- Flint tools, not currently used by MTF: `flint_knife`, `flint_pickaxe`, `flint_axe`, `flint_saw`
- Copper tools: `copper_sword`, `copper_knife`, `copper_shovel`, `copper_pickaxe`, `copper_axe`, `copper_saw`, `copper_hoe`
- Knife/saw variants: iron, gold, diamond, netherite
- Slingshot pull states: `slingshot`, `slingshot_pulling_0`, `slingshot_pulling_1`, `slingshot_pulling_2`
- Rocks and early materials

## Recipes

### Flint and Survival Recipes

MTF does not use EarlyGame's flint-shard crafting recipes. The active starter loop uses placed flint knapping, sticks/handles, and Plant Fiber as a ground-assembly catalyst.

Source jar recipes:

- `lashing`: 2x2 `plant_fiber`
- `fire_starter`: lashing plus sticks
- `slingshot`: sticks plus plank plus lashing
- `flint_knife`: flint shard plus stick
- `flint_axe`: flint shard plus lashing plus stick
- `flint_pickaxe`: flint shards plus lashing plus stick
- `flint_saw`: flint shard plus lashing plus sticks

### Copper Recipes

- `copper_nugget_from_ingot`: copper ingot -> 9 copper nuggets
- `copper_ingot_from_nugget`: 9 copper nuggets -> copper ingot
- `copper_nugget_from_smelting`: smelt copper gear -> copper nugget
- `copper_nugget_from_blasting`: blast copper gear -> copper nugget
- `copper_sword`: vanilla-like sword pattern using copper ingots and sticks
- `copper_knife`: copper ingot plus stick
- `copper_shovel`: vanilla-like shovel pattern
- `copper_pickaxe`: vanilla-like pickaxe pattern
- `copper_axe`: vanilla-like axe pattern
- `copper_saw`: saw pattern using copper ingots and sticks
- `copper_hoe`: vanilla-like hoe pattern
- Copper armor uses vanilla armor patterns with copper ingots

### Knife and Saw Recipes

- `iron_knife`: iron ingot plus stick
- `iron_saw`: iron ingot plus sticks
- `golden_knife`: gold ingot plus stick
- `golden_saw`: gold ingot plus sticks
- `diamond_knife`: diamond plus stick
- `diamond_saw`: diamond plus sticks
- `netherite_knife_smithing`: diamond knife plus netherite ingot
- `netherite_saw_smithing`: diamond saw plus netherite ingot

The source jar is Minecraft 1.19, so netherite smithing has no template ingredient.

### Rock to Block Recipes

- 4 stone rocks -> cobblestone
- 4 deepslate rocks -> cobbled deepslate
- 4 sandstone rocks -> sandstone
- 4 red sandstone rocks -> red sandstone
- 4 andesite rocks -> cobbled andesite
- 4 diorite rocks -> cobbled diorite
- 4 granite rocks -> cobbled granite
- 4 calcite rocks -> cobbled calcite
- 4 tuff rocks -> cobbled tuff

### Cobbled Variant Smelting

- cobbled andesite -> andesite
- cobbled diorite -> diorite
- cobbled granite -> granite
- cobbled calcite -> calcite
- cobbled tuff -> tuff

## Loot and Progression Data

### Vanilla Loot Table Overrides

The jar overrides vanilla block loot tables to support early resource gathering.

- grass, fern, tall grass, and large fern can drop `plant_fiber`
- dirt and grass block can drop `flint`
- stone can drop `stone_rock`
- andesite can drop `andesite_rock`
- diorite can drop `diorite_rock`
- granite can drop `granite_rock`
- deepslate can drop `deepslate_rock`
- calcite can drop `calcite_rock`
- tuff can drop `tuff_rock`
- sandstone can drop `sandstone_rock`
- red sandstone can drop `red_sandstone_rock`

### EarlyGame Block Loot

- loose rock blocks drop their matching rock item
- cobbled variants drop themselves
- `stick_twig_block` drops a vanilla stick

## Tags

### Common Item Tags

- `c:sticks`: `minecraft:stick`
- `c:copper_ingots`: `minecraft:copper_ingot`
- `c:copper_nuggets`: `earlygame:copper_nugget`
- `c:pickaxes`: flint pickaxe, copper pickaxe
- `c:axes`: flint axe, flint saw, copper axe, copper saw, iron/gold/diamond/netherite saws
- `c:swords`: flint knife, copper sword, copper knife, iron/gold/diamond/netherite knives
- `c:shovels`: copper shovel
- `c:hoes`: copper hoe

### EarlyGame Item Tags

- `earlygame:rocks`: stone, granite, andesite, diorite, sandstone, red sandstone rocks
- `earlygame:knives`: flint, copper, iron, gold, diamond, netherite knives
- `earlygame:saws`: flint, copper, iron, gold, diamond, netherite saws
- `earlygame:axes`: flint axe, copper axe, vanilla iron/gold/diamond/netherite axes
- `earlygame:stone_tools`: vanilla stone tools
- `earlygame:wooden_tools`: vanilla wooden tools

### EarlyGame Block Tags

- `earlygame:cobblestone`: vanilla cobblestone, cobbled deepslate, and EarlyGame cobbled variants
- `earlygame:rocks`: loose rock blocks
- `earlygame:rock_placeable_on`: natural ground and stone-like surfaces
- `earlygame:slow_digging`: dirt-like blocks and concrete powders
- `earlygame:concrete_powders`: all concrete powder colors
- `earlygame:blacklisted_blocks`: empty in the jar

### Other Integration Tags

- `mambience:is_bow`: slingshot
- `mambience:is_sword`: copper sword and EarlyGame knives
- `mambience:is_tool`: flint/copper tools and saws

## Mechanics Visible From Bytecode

### Config Defaults

- `warningMessage`: false
- `generateStones`: true
- `generateSticks`: true
- `harderGroundBlocks`: true
- `enableWoodenTools`: false
- `enableStoneTools`: false
- `enablePlanksAndSticks`: false
- `damageProbability`: 2.5
- `flintConsumeProb`: 0.3
- `flintSuccessProb`: 0.7
- `woodChoppingProb`: 0.5
- `fireStartProb`: 0.3

### Flint Tool Material

- durability: 20
- mining speed: 2.0
- attack damage bonus: 0.0
- mining level: 0
- enchantability: 0
- repair ingredient: none

Recovered constructor modifiers:

- flint pickaxe: damage modifier 0, speed modifier -1.8
- flint axe: damage modifier 3.0, speed modifier -2.5
- flint knife: damage modifier 1, speed modifier -0.6
- flint saw: damage modifier 1.0, speed modifier -2.5

### Copper Tool Material

- durability: 131
- mining speed: 4.0
- attack damage bonus: 1.0
- mining level: 1
- enchantability: 5
- repair ingredient: copper ingot

Recovered constructor modifiers:

- copper pickaxe: damage modifier 1, speed modifier -2.8
- copper axe: damage modifier 7.0, speed modifier -3.2
- copper sword: damage modifier 3, speed modifier -2.4
- copper shovel: damage modifier 1.5, speed modifier -3.0
- copper hoe: damage modifier -1, speed modifier -2.0
- copper knife: damage modifier 1, speed modifier 0.0
- copper saw: damage modifier 2.0, speed modifier -3.2

### Knife Behavior

Knives damage themselves by 1 when used to mine blocks.

### Saw Behavior

Saws are axe-like tools. They have custom speed handling for wooden materials and are tagged as axe-like tools in common tags.

Mobs Tool Forging is not adopting the saw in the crude flint pass. It only makes sense if the mod later adds a dedicated wood-processing or plank-cutting progression.

### Fire Starter Behavior

The fire starter attempts to light campfires or place fire on valid blocks.

- success chance uses `fireStartProb`
- default success chance is 0.3
- it plays flint-and-steel-style sound
- it damages itself by 1 on use

### Slingshot Behavior

The slingshot uses EarlyGame rock items as ammunition.

- max use duration: 72000 ticks
- pull progress reaches full around 20 ticks
- range value: 15
- rock projectile base damage: 2.0
- consumes rock ammo unless creative or Infinity-like behavior applies
- bytecode references bow-style enchantment hooks for damage, punch, and flame-like behavior

### Mixins

The jar includes these mixins:

- `BlockMiningMixin`
- `CopperMiningMixin`
- `IngredientMixin`
- `MatchingStackAccessor`
- `RecipeFieldAccessor`

Likely roles based on names, config, and lang keys:

- enforce pickaxe requirement for stone-like blocks
- enforce axe requirement for wood-like blocks
- make copper behave as the early bridge tier
- disable or rewrite wooden/stone/plank/stick recipes when configured
- support recipe-removal internals

## Repurpose Candidate Buckets

### Strong Fit For Mobs Tool Forging

- flint knife, hatchet, and pick textures as reference only; MTF renders modular flint tools from part visuals
- flint shard texture for compatibility content
- plant fiber texture for starter ground assembly
- lashing texture for possible future primitive binding or handle-wrap progression
- copper tool textures as reference or temporary placeholders for copper modular tools
- copper nugget texture if copper recycling becomes useful
- copper armor textures only if armor becomes part of the mod later

### Possible Fit, But Needs Design Decision

- saw tool family
- knife tool family
- fire starter
- slingshot and thrown rock projectiles
- loose rock items and rock blocks
- twig stick pickup block
- grass/dirt/stone loot overrides

### Probably Avoid Directly Porting

- vanilla recipe-removal mixins
- broad vanilla loot-table overrides
- namespace-level armor texture overrides under `assets/minecraft`
- full EarlyGame worldgen system unless Mobs Tool Forging intentionally adds a survival overhaul mode

## Notes For Mobs Tool Forging

- The asset style is simple and readable, useful for crude or placeholder tiers.
- The flint tools are intentionally crude and short-lived, matching the current Mobs Tool Forging progression goal.
- Copper stats from EarlyGame are close to the bridge-tier direction: worse than iron, better than flint, mining level 1.
- The source mod is Fabric 1.19, so code cannot be copied directly into the NeoForge 1.21.1 project without porting.
- Because the license is MIT, reuse is allowed as long as the license notice is preserved.
