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
- Forge or lapidary templates with `ToolTypeRegistry.registerTemplate`.
- Extra stat behavior with `ToolTypeRegistry.registerStatModifier`.
- Custom visible traits with `ToolTraitRegistry.registerTrait`.
- Custom material tiers with `MaterialCatalog.registerMaterial`.
- Extra visual material ids with `MaterialCatalog.registerVisualMaterial`.
- Custom handle item to visual-material mappings with `MaterialCatalog.registerHandleMaterial`.

The generic recipe serializer `mobstoolforging:crafting_special_modular_tool` can craft registered tool types by `tool_type` id.

## Mobs More Weapons Status

Mobs More Weapons greatswords are not built in yet. They are now feasible through a small bridge mod, but the base mod still does not ship Mobs More Weapons items, generated greatsword parts, or greatsword recipes.

A Mobs More Weapons bridge should register a `ToolTypeDefinition` such as `mobs_more_weapons:greatsword`, register blade and guard templates, provide physical pattern items, and provide visual JSON/sprites for the greatsword layers.

## Remaining Planned Work

The current bridge is Java-first. Fully data-driven tool type registration is still planned.

Future work should cover:

- Datapack-defined `ToolTypeDefinition` records.
- Datapack-defined template definitions.
- Datapack-defined material stat profiles.
- More explicit workstation compatibility per template.
- A cleaner public event timing story for bridge registration.
- Optional API helpers for external item classes that want MTF fire resistance and tooltip behavior.
