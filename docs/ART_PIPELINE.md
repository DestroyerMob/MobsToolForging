# Art Pipeline

The generated procedural sprites are placeholders. They exist so modular tools stay visible during gameplay development and so missing wiring is easy to notice.

Final quality requires hand-authored greyscale templates and hand-authored source sprites. Code should not replace actual pixel art; the generator is a safety net for early testing, not the art direction.

## Source Templates And Sprites

Current hand-authored source sprites live under:

```text
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/<material>/<sprite>.png
```

Handle sprites may provide a generic base and optional tool-specific bases:

```text
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/<handle_material>/handle.png
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/<handle_material>/<tool>_handle.png
```

Datagen checks the tool-specific handle sprite first, then falls back to `handle.png`, then falls back to procedural drawing. The handle mask is still applied after the base sprite is selected.

Handle visibility masks live under:

```text
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/handle_masks/<tool>.png
```

For built-in handles, source material folders currently map like this:

- `mobstoolforging:oak` -> `stick`
- `mobstoolforging:blaze` -> `blaze_rod`
- `mobstoolforging:breeze` -> `breeze_rod`

## Generated Outputs

Datagen writes generated part sprites under:

```text
src/generated/resources/assets/mobstoolforging/textures/generated/tool_parts/<material>/<part>.png
```

Visual definitions are generated under:

```text
src/generated/resources/assets/mobstoolforging/tooling/material_visuals/<material>.json
src/generated/resources/assets/mobstoolforging/tooling/tool_visuals/<tool_type>.json
```

These generated files are useful for development and for resource-pack-friendly defaults. They should not be treated as final pixel art.

## Override Paths

Resource packs or later bridge mods can override generated textures at the normal asset paths:

```text
assets/<namespace>/textures/generated/tool_parts/<material>/<part>.png
assets/<namespace>/tooling/material_visuals/<material>.json
assets/<namespace>/tooling/tool_visuals/<tool_type>.json
```

If a material or tool shape needs to look polished, add explicit source sprites or resource-pack overrides instead of adding more procedural drawing code.

## Bridge Mod Visuals

Bridge mods can use the same override paths in their own namespace. For non-MTF material ids, make the material visible to the model loader in one of two ways:

- Register it from Java with `MaterialCatalog.registerVisualMaterial`.
- Add the material id to a visual layer's `materials` array.

Example layer:

```json
{
  "slot": "greatsword_blade",
  "material_from": "headMaterial",
  "materials": ["mobs_more_weapons:steel"],
  "z": 3
}
```

The texture key for that layer would be:

```text
layer_greatsword_blade_mobs_more_weapons_steel
```
