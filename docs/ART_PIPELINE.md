# Art Pipeline

All tool part sprites are hand-authored source art. The mod should not generate replacement pixel art in code.

## Source Templates And Sprites

Current hand-authored source sprites live under:

```text
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/<material>/<material>_<part>_<usage>.png
```

Use lowercase names with single underscores only. Do not use double underscores or keep `_final` in committed asset names.

Tool part sprites use one of these usage suffixes:

- `<material>_<part>_part.png`: the standalone forged part item texture.
- `<material>_<part>_tool.png`: the layer assembled into the finished tool texture.

For example:

```text
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/diamond/diamond_axe_head_part.png
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/diamond/diamond_axe_head_tool.png
```

Handle sprites may provide a generic base and optional tool-specific bases. Handles are tool assembly layers, so they use the `_tool` suffix:

```text
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/<handle_material>/<handle_material>_handle_tool.png
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/<handle_material>/<handle_material>_<tool>_handle_tool.png
```

Datagen checks the tool-specific handle sprite first, then falls back to the generic handle sprite.

Handle visibility masks live under:

```text
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/handle_masks/<tool>_handle_mask.png
```

With `handle_strategy: "default_handle"`, the renderer draws the mask as the
tintable handle body and then draws the exact hand-authored handle texture above
it. This lets shapes such as shovels combine the shared handle mask with a small
material-specific nub. Tools whose handle shape cannot be expressed with the
shared handle plus a mask can provide a grayscale template and set
`handle_strategy: "template_handle"` on the handle layer.

If a grayscale fallback template is not present, the renderer uses the missing
texture and logs once. Missing art should be visible and diagnosable, never a
client crash.

For built-in handles, source material folders currently map like this:

- `mobstoolforging:oak` -> `stick`
- `mobstoolforging:blaze` -> `blaze_rod`
- `mobstoolforging:breeze` -> `breeze_rod`

## Compiled Finished Tool Templates

Run this helper when you need flattened grayscale references for augment or
overlay texture work:

```text
./gradlew compileBasicFinishedToolTextures
```

The utility composites the basic core layers for sword, shovel, pickaxe, axe,
and hoe in the same order used by the visual definitions. It writes normal and
`large_` grayscale PNGs, preserving the source template dimensions, to:

```text
src/main/resources/assets/mobstoolforging/textures/tool_templates/finished/
```

These files are helper templates assembled from hand-authored grayscale source
templates; they are not replacements for the runtime layered tool renderer.
Handle layers use the same broad handle masks as the runtime model, then place
the grayscale handle detail template over that mask.

The current Java renderer reads `large_canvas`, `large_in_hand`, and
`large_template` from visual JSON but does not render a separate large sprite
path yet. The compiled `large_` files are therefore reference outputs that track
the existing `large_*` source templates, not active in-game assets.

## Generated Outputs

Visual definitions are generated under:

```text
src/generated/resources/assets/mobstoolforging/tooling/material_visuals/<material>.json
src/generated/resources/assets/mobstoolforging/tooling/tool_visuals/<tool_type>.json
```

These generated JSON files are useful for development and for resource-pack-friendly defaults. They are not pixel art.

## Override Paths

Resource packs or later bridge mods can override source textures at the normal asset paths:

```text
assets/<namespace>/textures/source/tool_parts/<material>/<material>_<part>_<usage>.png
assets/<namespace>/tooling/material_visuals/<material>.json
assets/<namespace>/tooling/tool_visuals/<tool_type>.json
```

If a material or tool shape needs to render, add explicit source sprites or resource-pack overrides.

## Pattern Item Textures

Built-in physical pattern items use per-item textures when present. Add outline
art at:

```text
src/main/resources/assets/mobstoolforging/textures/item/<pattern_item_name>.png
```

For example:

```text
src/main/resources/assets/mobstoolforging/textures/item/pickaxe_head_pattern.png
src/main/resources/assets/mobstoolforging/textures/item/sword_guard_pattern.png
```

If a pattern texture is missing, datagen falls back to vanilla paper so unfinished
pattern art does not render as missing texture. After adding or replacing pattern
art, run `./gradlew runData` to regenerate the item models.

## Diagnostics And Fallbacks

The layered tool model logs missing sprites once per unique layer/material/key. Required missing layers render with Minecraft's missing-texture sprite so the broken layer is visible in-game instead of silently disappearing or replacing the whole tool with an unrelated fallback. Optional missing layers are skipped and logged once when that optional material is actually present on the stack.

If an explicit model texture override resolves to the missing texture, the client log includes the model texture key and requested sprite path. Treat that as a bad path, missing atlas source, or missing resource-pack override.

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
