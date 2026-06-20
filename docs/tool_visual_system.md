# Tool Visual System

Mobs Tool Forging tools are meant to look assembled from physical parts, not like one item recolored by tint indexes.

The implementation stores finished tool construction data on the item stack and composes resource-pack-friendly part sprites through the custom `mobstoolforging:parted_tool` and `mobstoolforging:parted_tool_part` model loaders.

## Stack Data

Finished tools store `mobstoolforging:tool_construction`:

```json
{
  "tool_type": "mobstoolforging:pickaxe",
  "head_material": "mobstoolforging:diamond",
  "handle_material": "mobstoolforging:oak",
  "binding_material": "mobstoolforging:copper",
  "quality": 100
}
```

Required fields are `tool_type`, `head_material`, and `handle_material`. Optional fields are `binding_material`, `wrap_material`, `focus_material`, and `treatment`.

## Material Visuals

Material visual definitions are generated under:

```text
assets/<namespace>/tooling/material_visuals/<material>.json
```

A material visual defines its family, palette, texture noise, fallback family, and emissive data. Resource packs can override these files to change how a material appears without changing gameplay tags or recipes.

## Tool Visuals

Tool visual definitions are generated under:

```text
assets/<namespace>/tooling/tool_visuals/<tool_type>.json
```

Each tool visual is an ordered layer list. Layers name their physical slot, such as `handle`, `pickaxe_head`, `guard`, `wrap`, `focus`, and `treatment_overlay`, and state which construction material powers that layer.

Layer order matters. Handles render first, wraps and heads render above them, binding or guards cover the joint, and treatment overlays stay subtle.

## Generated Sprites

Generated part sprites are written to:

```text
assets/mobstoolforging/textures/generated/tool_parts/<material>/<part>.png
```

These are part sprites, not full completed-tool permutations. The intended renderer should compose them dynamically from the stack's construction data.

## Adding A Material

1. Tag the source item as `mobstoolforging:materials/metals` or `mobstoolforging:materials/gems` if it can become a tool head.
2. For a bridge mod, call `MaterialCatalog.registerMaterial` if the material needs custom stats instead of fallback iron/diamond-like behavior.
3. Add or override `tooling/material_visuals/<material>.json`.
4. Provide explicit textures for iconic materials where generated sprites are not good enough.
5. If the material id is not one of MTF's built-ins, either call `MaterialCatalog.registerVisualMaterial` or list it in the layer's `materials` array inside the relevant tool visual JSON.

## Adding A Tool Visual

Add a `tooling/tool_visuals/<tool_type>.json` file in your mod namespace. Define the ordered layers and point them at your templates and generated output paths.

Registered external tool types can use the same model loader by setting `tool` to their registered `ToolTypeDefinition` id and `visual` to the visual JSON id:

```json
{
  "parent": "minecraft:item/handheld",
  "loader": "mobstoolforging:parted_tool",
  "tool": "mobs_more_weapons:greatsword",
  "visual": "mobs_more_weapons:greatsword"
}
```

Tool visual layers can include extra material ids:

```json
{
  "slot": "greatsword_blade",
  "material_from": "headMaterial",
  "materials": ["mobs_more_weapons:steel"],
  "z": 3
}
```

This is how bridge mods make non-MTF materials load textures instead of falling back to the model's particle sprite.
