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

## Source Part Sprites

Hand-authored part sprites are loaded from:

```text
assets/mobstoolforging/textures/source/tool_parts/<material>/<material>_<part>_part.png
assets/mobstoolforging/textures/source/tool_parts/<material>/<material>_<part>_tool.png
```

The `_part` texture is used for standalone part item models. The `_tool` texture is used as a layer in assembled tool models. These are part sprites, not full completed-tool permutations; the renderer composes them dynamically from the stack's construction data.

## Adding A Material

1. Tag the source item as `mobstoolforging:materials/metals` or `mobstoolforging:materials/gems` if it can become a tool head.
2. For a bridge mod, call `MaterialCatalog.registerMaterial` if the material needs custom stats instead of fallback iron/diamond-like behavior.
3. Add or override `tooling/material_visuals/<material>.json`.
4. Provide hand-authored source textures for every material and part that should render.
5. If the material id is not one of MTF's built-ins, either call `MaterialCatalog.registerVisualMaterial` or list it in the layer's `materials` array inside the relevant tool visual JSON.

## Adding A Tool Visual

Add a `tooling/tool_visuals/<tool_type>.json` file in your mod namespace. Define the ordered layers and point them at your templates and source texture paths.

Registered external tool types can use the same model loader by setting `tool` to their registered `ToolTypeDefinition` id and `visual` to the visual JSON id:

```json
{
  "parent": "minecraft:item/handheld",
  "loader": "mobstoolforging:parted_tool",
  "tool": "mobs_more_weapons:greatsword",
  "visual": "mobs_more_weapons:greatsword"
}
```

External final-tool and part item models do not have to replace their base
`layer0` models just to support MTF-crafted stacks. MTF wraps ordinary inventory
models and switches to component-driven layered rendering when a stack has
`TOOL_CONSTRUCTION` or `TOOL_PART` data. Stacks without those components still
render through the original item model.

The automatic path expects part textures next to the external item textures:
`<part_item>_tool` for final tools and `<part_item>_part` for standalone parts.
Use `mobstoolforging:parted_tool` or `mobstoolforging:parted_tool_part` item
models when a compat pack needs explicit texture keys or a custom resource-pack
override.

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

Handle layers can choose how broad their fallback should be:

```json
{
  "slot": "handle",
  "material_from": "handleMaterial",
  "handle_strategy": "template_first",
  "handle_template": "mobs_more_weapons:tool_templates/greatsword/handle",
  "z": 1
}
```

`default_handle` and `exact_first` use exact handle textures first, then fall back to the layer's grayscale template if one exists. `template_first` and the older `template_handle` alias use the grayscale handle template first, then exact art. `template_only` ignores exact art. `explicit_only` and the older `explicit_handle` alias use exact handle art only. If no exact sprite and no usable grayscale template exists, MTF renders Minecraft's missing texture and logs a warning instead of crashing.

Part and tool layers can use separate fallback shapes:

```json
{
  "slot": "greatsword_blade",
  "material_from": "headMaterial",
  "tool_template": "mobs_more_weapons:tool_templates/greatsword/blade_tool",
  "part_template": "mobs_more_weapons:tool_templates/greatsword/blade_part",
  "texture_namespace": "mobs_more_weapons",
  "texture_pattern": "item/{material}_{slot}_{usage}",
  "z": 3
}
```

`tool_template` is used for finished tool layers. `part_template` is used for standalone part items. `handle_template` is used for handle layers. The older `template` key remains a shared fallback alias.

`texture_pattern` gives compat packs an escape hatch when item ids and texture ids do not follow MTF's defaults. `{usage}` becomes `tool` or `part`.

Grayscale fallback templates are tinted from `tooling/material_visuals/<material>.json`, so a material add-on can provide a palette once while a tool add-on provides a shape once.
