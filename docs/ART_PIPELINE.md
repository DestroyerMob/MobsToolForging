# Art Pipeline

The generated procedural sprites are placeholders. They exist so modular tools stay visible during gameplay development and so missing wiring is easy to notice.

Final quality requires hand-authored greyscale templates and hand-authored source sprites. Code should not replace actual pixel art; the generator is a safety net for early testing, not the art direction.

## Source Templates And Sprites

Current hand-authored source sprites live under:

```text
src/main/resources/assets/mobstoolforging/textures/source/tool_parts/<material>/<sprite>.png
```

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
