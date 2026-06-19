# Interaction Design

Mobs Tool Forging should feel like physical tool work, not menu-first crafting.

## Primary Template Selection

The normal selection flow is:

1. Hold a physical pattern item, such as `pickaxe_head_pattern`.
2. Right-click the Tool Forge or Lapidary Table.
3. The station stores that template if it has no material or hammering progress.
4. The station renders a small physical preview of the selected part on its surface.

The old stonecutter-style template screen still exists, but it is a debug fallback only. It should be opened only when the debug template selector config is enabled.

## Forming A Part

After a pattern is placed:

1. Add matching material items to the station.
2. Hammer the materials.
3. The material display moves together as progress increases.
4. The completed output becomes the selected shape.

If the station has a selected template but no materials or progress, sneak-right-clicking it with an empty hand clears that template.
