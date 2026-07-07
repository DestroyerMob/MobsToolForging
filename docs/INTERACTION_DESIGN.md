# Interaction Design

Mobs Tool Forging should feel like physical tool work, not menu-first crafting.

## Primary Template Selection

The normal selection flow is:

1. Hold a physical pattern item, such as `pickaxe_head_pattern`.
2. Right-click the Crude Anvil or Tool Forge.
3. The station stores that template if it has no material or hammering progress.
4. The station renders a small physical preview of the selected part on its surface.

The old stonecutter-style template screen still exists, but it is a debug fallback only. It should be opened only when the debug template selector config is enabled.

Pattern items define the physical shape to make. They are intentionally not station-specific right now. The station defines the material process:

- Crude Anvil: metal materials with an early, lower quality cap.
- Tool Forge: metal materials with the normal quality cap.

The Lapidary Table does not use patterns. It starts from an already-forged, heated metal tool part and adds a gem shell to that existing shape.

Armour uses the same physical pattern idea, but base armour patterns produce the finished wearable armour piece directly. The Helmet Pattern makes a modular helmet rather than a loose skull part. Chestplate, leggings, and boots base patterns follow the same rule.

Add-on armour patterns are installed onto an existing compatible armour piece:

- Metal add-ons are forged on an anvil station.
- Gem add-ons are set on the Lapidary Table. This pass keeps armour compatibility but does not add new armour quality effects.

The add-on material can be replaced later by running the same compatible pattern and armour piece through the right station again.

## Forming A Part

After a pattern is placed:

1. Add matching material items to the station. Metal jobs check workshop heat when the job starts.
2. Hammer or work the materials.
3. The material display moves together as progress increases.
4. The completed output becomes the selected shape.

If the station has a selected template but no materials or progress, sneak-right-clicking it with an empty hand clears that template.

Sneak-right-clicking with a held item should not open the debug selector during normal play. It should give a small status hint instead of feeling like the action disappeared.

## Heat

Heat is a workshop condition by default rather than a repeated per-item chore. Lit campfires provide the first heat tier: right-click a lit campfire with any heat-requiring material or tool part to use the campfire's normal visible inventory slots, or drop one onto the fire to insert it if space is available. The campfire warms the workpiece to low heat and ejects it when the low-heat timer completes. Low heat targets 55% by default, while low-heat forging only requires the configurable cooling-margin threshold, 40% by default. Nearby lit campfires count as low workshop heat only for already-warmed materials; cold ingots must be warmed in the campfire first. The built-in early metals are copper, gold, and rough iron. A fueled Heating Forge provides hot workshop heat and keeps that heat buffered nearby for repeated shaping. High heat is reserved for future upgrades.

With `requireHeatAtJobStartOnly=true`, the station records the starting heat level in NBT. With `workpieceCoolsMidCraft=false`, hammering never stops halfway through because the workpiece cooled.

## Timing And Quality

When timing quality is enabled, the rendered workpiece pulses during a good hit window. A well-timed hit improves workmanship quality; a missed hit still advances progress and does not destroy material. If timing quality is disabled, station tier, starting heat, material difficulty, and optional helper inputs determine quality deterministically.

Quality is intentionally modest. It should reward care without making Masterwork mandatory for ordinary progression.

## Lapidary Table

The Lapidary Table no longer creates standalone gem parts from patterns. Place a heated metal tool part on the table, add the matching gem material cost for that part shape, then work it until the gem shell is set into the surface.

The output uses the gem as its visible material, keeps the metal part as a core material, and is named as a metal-gem part. Head pieces inherit the gem head stats when assembled into a tool. The coated part's quality averages the original metal part quality with the gem-work quality, and gem/coated parts can still be polished afterward.

The Lapidary Table does not require a Gem Cutter's Knife when `gemcuttersFileRequired=false`. Empty-hand/table action can complete the work. The knife remains useful as optional precision help for quality. Gem materials can require an abrasive tier before they can be applied. Built-in diamond requires diamond-tier abrasive, currently Diamond Powder or the `mobstoolforging:lapidary_abrasives/diamond` item tag. Emerald, ruby, and sapphire do not require abrasive yet. Any valid lapidary abrasive can still improve table quality.

## Toolmaker's Station

Toolmaker's Station variants remain physical and no-GUI. Place compatible parts on the station, then use an empty hand or a Smithing Hammer to assemble the finished tool. A Smithing Hammer still separates one placed finished tool into its stored parts when possible.

The screwdriver item remains registered, but it is not required for normal finished-tool assembly.
