# Heating visual profiles

MTF selects heating visuals through item tags and always falls back to its generic
red-to-white metal profile when no profile matches.

Built-in selectors are:

- `#mobstoolforging:heating_visuals/iron`
- `#mobstoolforging:heating_visuals/copper`
- `#mobstoolforging:heating_visuals/gold`
- `#mobstoolforging:heating_visuals/netherite`

Add a modded ingot or its common ingot tag to one of those item tags to reuse that
effect. MTF resolves the source material of modular parts, so the finished workpiece
item itself does not need to be placed in the selector tag.

Custom effects are client resource JSON files at
`assets/<namespace>/heating_visual_profiles/<name>.json`. Each profile names the
item tag that selects it. Higher `priority` wins when more than one tag matches.

```json
{
  "tag": "example:heating_visuals/steel",
  "priority": 150,
  "colors": ["#210301", "#a31906", "#ff6b1a", "#ffd675", "#fffbea"],
  "visible_threshold": 0.1,
  "color_curve_exponent": 1.25,
  "surface_tint_strength": 0.45,
  "emission_start": 0.15,
  "emission_strength": 0.7,
  "halo_start": 0.34,
  "halo_alpha": 0.065,
  "halo_base_scale": 1.016,
  "halo_extra_scale": 0.012,
  "light_start": 0.22,
  "maximum_block_light": 13
}
```

The matching item tag belongs in a datapack at
`data/example/tags/item/heating_visuals/steel.json`. The profile belongs in a
resource pack because it controls client rendering; the tag remains datapack-driven.
