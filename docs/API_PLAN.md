# API Plan

This is a TODO for the public tool API, not a supported bridge contract yet.

## Current Scope

The current `ToolKind` enum supports only the built-in Mobs Tool Forging tool shapes:

- `sword`
- `shovel`
- `pickaxe`
- `axe`
- `hoe`

Those values drive current recipes, part items, template selection, tool construction data, tool stats, and generated visuals. Anything outside that enum is not wired into the gameplay loop yet.

## Mobs More Weapons Status

Mobs More Weapons greatswords are not supported yet. The mod should not ship generated greatsword bridge stubs or documentation that implies greatsword construction already works.

Future Mobs More Weapons support needs a real `ToolTypeDefinition` registry/data system instead of one-off enum additions. That system should let Mobs Tool Forging and bridge mods define new tool or weapon families through data.

## Future ToolTypeDefinition Shape

A future tool type definition should cover at least:

- Tool type id and display name.
- Required forged part types.
- Optional part slots such as guard, binding, wrap, focus, and treatment.
- Valid workstations and templates.
- Material costs per part.
- Crafting or assembly rules.
- Visual layer slots and texture/template paths.
- Attribute, durability, mining, and combat behavior.
- Compatibility hooks for bridge mods.

Until that exists, bridge mods should treat non-vanilla tool shapes as planned work only.
