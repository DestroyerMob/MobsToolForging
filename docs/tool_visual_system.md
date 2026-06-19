# Tool Visual System

Mobs Tool Forging tools are meant to look assembled from physical parts, not like one item recolored by tint indexes.

The current implementation stores finished tool construction data on the item stack and generates resource-pack-friendly visual inputs. A later client model pass should consume these JSON files and sprites through a custom `mobstoolforging:parted_tool` model loader and cached baked models.

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
2. Add or override `tooling/material_visuals/<material>.json`.
3. Provide explicit textures for iconic materials where generated sprites are not good enough.

## Adding A Tool Visual

Add a `tooling/tool_visuals/<tool_type>.json` file in your mod namespace. Define the ordered layers and point them at your templates and generated output paths.

Bridge mods can add visuals without changing Mobs Tool Forging code. For example, a Mobs More Weapons bridge can define:

```text
assets/mobs_more_weapons/tooling/tool_visuals/greatsword.json
```

The generated data includes a greatsword bridge stub showing the expected blade, handle, guard, wrap, focus, and treatment layers.
