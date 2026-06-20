# API Plan

This document describes the current compatibility bridge and the parts that are still planned work.

## Current Built-In Scope

The `ToolKind` enum still represents only the built-in Mobs Tool Forging shapes:

- `sword`
- `shovel`
- `pickaxe`
- `axe`
- `hoe`

Those enum values are now treated as built-in convenience definitions, not the public ceiling of the system. New tool families should use `ToolTypeDefinition` through `ToolTypeRegistry`.

## Current Bridge API

Bridge mods can now register:

- Tool families with `ToolTypeRegistry.registerToolType`.
- Material-aware tool and part item mappings with `ToolTypeDefinition.Builder.toolItem(material, item)` and `partItem(partType, material, item)`.
- Forge or lapidary templates with `ToolTypeRegistry.registerTemplate`.
- Extra stat behavior with `ToolTypeRegistry.registerStatModifier`.
- Custom visible traits with `ToolTraitRegistry.registerTrait`.
- Custom material tiers with `MaterialCatalog.registerMaterial`.
- Extra visual material ids with `MaterialCatalog.registerVisualMaterial`.
- Custom handle item to visual-material mappings with `MaterialCatalog.registerHandleMaterial`.

Datapacks can also define tool families in `data/<namespace>/mobstoolforging/tool_types/<id>.json`. Both one-item modular tools and material-specific external items are supported through `tool_item`/`part_items` or `tool_items`/material maps.

The generic recipe serializer `mobstoolforging:crafting_special_modular_tool` can craft registered tool types by `tool_type` id. Recipe matching now checks that the selected material can resolve a valid output item and valid part items before claiming a match.

## Mobs More Weapons Status

Mobs More Weapons greatswords are not built in yet. They are now feasible through a small bridge mod, but the base mod still does not ship Mobs More Weapons items, generated greatsword parts, or greatsword recipes.

A Mobs More Weapons bridge should register a `ToolTypeDefinition` such as `mobs_more_weapons:greatsword`, register blade and guard templates, provide physical pattern items, and provide visual JSON/sprites for the greatsword layers.

For material-specific Mobs More Weapons items, use material-aware item maps so `mobstoolforging:iron` resolves to `iron_great_sword`, `mobstoolforging:gold` resolves to `golden_great_sword`, and so on. Reusable guard patterns can declare `compatible_tool_types` in template JSON, but full independent `PartDefinition` registries are still future work.

## Remaining Planned Work

The bridge is no longer Java-only for basic external tool families, but a few deeper API pieces are still planned.

Future work should cover:

- A first-class `PartDefinition` registry independent from a single tool type.
- Datapack-defined material stat profiles.
- More explicit workstation compatibility per template.
- A cleaner public event timing story for bridge registration.
- Optional API helpers for external item classes that want MTF fire resistance and tooltip behavior.
- A proactive `/mtf validate_visuals` style diagnostic command for missing exact textures, missing templates, unknown materials, and ambiguous recipes.
