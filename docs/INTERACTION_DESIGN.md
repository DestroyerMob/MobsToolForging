# Interaction Design

Mobs Tool Forging should feel like physical tool work, not menu-first crafting.

## Primary Template Selection

The normal selection flow is:

1. Hold a physical pattern item, such as `pickaxe_head_pattern`.
2. Right-click the Tool Forge or Lapidary Table.
3. The station stores that template if it has no material or hammering progress.
4. The station renders a small physical preview of the selected part on its surface.

The old stonecutter-style template screen still exists, but it is a debug fallback only. It should be opened only when the debug template selector config is enabled.

Pattern items define the physical shape to make. They are intentionally not station-specific right now. The station defines the material process:

- Tool Forge: metal materials.
- Lapidary Table: gem materials.

That means a Sword Blade Pattern can be used on either station, but the material placed afterward determines whether the work is forged metal or lapidary-cut gem.

Armour uses the same physical pattern idea, but base armour patterns produce the finished wearable armour piece directly. The Helmet Pattern makes a modular helmet rather than a loose skull part. Chestplate, leggings, and boots base patterns follow the same rule.

Add-on armour patterns are installed onto an existing compatible armour piece:

- Metal add-ons are forged on the Tool Forge.
- Gem add-ons are set on the Lapidary Table with Diamond Powder and a Gem Cutter's Knife.

The add-on material can be replaced later by running the same compatible pattern and armour piece through the right station again.

## Forming A Part

After a pattern is placed:

1. Add matching material items to the station.
2. Hammer the materials.
3. The material display moves together as progress increases.
4. The completed output becomes the selected shape.

If the station has a selected template but no materials or progress, sneak-right-clicking it with an empty hand clears that template.

Sneak-right-clicking with a held item should not open the debug selector during normal play. It should give a small status hint instead of feeling like the action disappeared.
