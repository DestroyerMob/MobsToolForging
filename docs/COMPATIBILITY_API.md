# Compatibility API

Mobs Tool Forging has two compatibility layers:

- Datapacks and resource packs can add tagged materials, recipes, and textures for registered systems.
- Bridge mods can register new tool families, templates, traits, material stats, and custom behavior.

Datapacks alone cannot create new Java item classes or new combat behavior. Full compatibility with a weapon mod such as Mobs More Weapons should be done by a small bridge mod.

## Datapack Capabilities

Datapacks can:

- Add source items to `mobstoolforging:materials`, `mobstoolforging:materials/metals`, or `mobstoolforging:materials/gems`.
- Add material definitions in `data/<namespace>/mobstoolforging/materials/<material>.json`.
- Add tool type definitions in `data/<namespace>/mobstoolforging/tool_types/<tool_type>.json`.
- Add trait definitions in `data/<namespace>/mobstoolforging/traits/<trait>.json`.
- Add additive stat rules in `data/<namespace>/mobstoolforging/stat_rules/<rule>.json`.
- Add forge template definitions in `data/<namespace>/mobstoolforging/forge_templates/<template>.json`.
- Add handles to `mobstoolforging:tool_handles`.
- Add bindings, wraps, foci, and treatments through their existing tags.
- Add a generic modular recipe for a registered external tool type:

```json
{
  "type": "mobstoolforging:crafting_special_modular_tool",
  "category": "equipment",
  "tool_type": "mobs_more_weapons:greatsword"
}
```

Material definition JSON lets a compat datapack say what an item or tag means to this mod:

```json
{
  "category": "metal",
  "display_item": "mobs_more_weapons:steel_ingot",
  "minimum_forge_heat": "hot",
  "items": ["mobs_more_weapons:steel_ingot"],
  "tags": ["c:ingots/steel"],
  "translation_key": "material.mobs_more_weapons.steel",
  "tier": {
    "base": "iron",
    "max_damage": 420,
    "mining_speed": 6.5,
    "attack_damage_bonus": 2.0,
    "enchantment_value": 16,
    "repair_item": "mobs_more_weapons:steel_ingot"
  },
  "visual_slots": ["headMaterial", "bindingMaterial"],
  "handle_items": []
}
```

`minimum_forge_heat` controls which workshop heat tier can start metal work for this material. Valid values are `"none"`, `"low"`, `"hot"`, and `"high"`; `"minimum_heat_level"` is accepted as an alias. Built-in copper, gold, and iron use `"low"` so campfire-tier forging can start early work. Other datapack metals default to `"hot"` unless they opt into `"low"` explicitly. Gem materials default to `"none"` because they are used as Lapidary Table shell material rather than forged directly.

Gem materials can set `required_lapidary_abrasive_tier` to require an abrasive in the Lapidary Table before that material can be applied as a shell. `"lapidary_abrasive_tier"` is accepted as an alias, missing/blank/`"none"` means no requirement, and bare values like `"diamond"` resolve to `mobstoolforging:diamond`. A tier is satisfied by an item tag at `data/<namespace>/tags/item/lapidary_abrasives/<path>.json`; built-in diamond uses `mobstoolforging:diamond`, currently supplied by Diamond Powder through `mobstoolforging:lapidary_abrasives/diamond`. Emerald, ruby, and sapphire do not require abrasive by default.

```json
{
  "category": "gem",
  "display_item": "minecraft:diamond",
  "required_lapidary_abrasive_tier": "diamond",
  "tags": ["c:gems/diamond"],
  "tier": "diamond"
}
```

`tier` can also be a simple string such as `"iron"`, `"diamond"`, `"netherite"`, `"copper"`, or `"emerald"`. `items` and `tags` are accepted material sources. `visual_slots` controls which model layers should collect sprites for this material. `handle_items` maps specific handle items to this material id, but only use it when matching handle sprites exist.

Tool type JSON lets a datapack connect existing item classes to Mobs Tool Forging construction data:

```json
{
  "primary_part_type": "greatsword_blade",
  "tool_items": {
    "mobstoolforging:iron": "mobs_more_weapons:iron_great_sword",
    "mobstoolforging:gold": "mobs_more_weapons:golden_great_sword",
    "mobstoolforging:diamond": "mobs_more_weapons:diamond_great_sword"
  },
  "part_items": {
    "greatsword_blade": {
      "mobstoolforging:iron": "mobs_more_weapons:iron_great_sword_blade",
      "mobstoolforging:gold": "mobs_more_weapons:golden_great_sword_blade",
      "mobstoolforging:diamond": "mobs_more_weapons:diamond_great_sword_blade"
    },
    "wide_guard": {
      "mobstoolforging:iron": "mobs_more_weapons:iron_wide_guard",
      "mobstoolforging:gold": "mobs_more_weapons:golden_wide_guard",
      "mobstoolforging:diamond": "mobs_more_weapons:diamond_wide_guard"
    }
  },
  "required_assembly_parts": ["wide_guard"],
  "visual": "mobs_more_weapons:greatsword",
  "base_attack_damage_bonus": 5.5,
  "base_attack_speed_bonus": -2.9,
  "entity_interaction_range_bonus": 1.5,
  "block_interaction_range_bonus": 0.0,
  "sword_like": true
}
```

`tool_item`, `primary_part_item`, and string-valued `part_items` still work for one-item modular families. Use `tool_items`, `primary_part_items`, or object-valued `part_items` when the target mod has material-specific item ids. The keys are MTF material ids, and the values are already-registered item ids.

`entity_interaction_range_bonus` and `block_interaction_range_bonus` are optional main-hand attribute modifiers for bridge tools whose original items change reach. Leave them out for vanilla-range tools.

Datapack tool types cannot create new item classes or custom combat callbacks. They can attach Mobs Tool Forging components, stats, recipes, and visuals to items that already exist. A modular recipe only matches when the resolved part items and the resolved output item are valid for the construction material.

External physical patterns can use the generic `mobstoolforging:template_pattern` item with a component:

```json
{
  "id": "mobstoolforging:template_pattern",
  "components": {
    "mobstoolforging:forge_template": "mobs_more_weapons:greatsword_blade"
  }
}
```

Use that result in a normal recipe to create a pattern item for any loaded forge template.

Forge template JSON keeps the existing `pattern_station_paper_cost` field for compatibility. The field now means the generic Pattern Creation Station input cost: Pattern Boards by default, or paper when `basicPatternsRequirePaper=true`. Existing bridge JSON does not need to be renamed.

Station-work JSON can target `"tool_forge"`, `"smithing_anvil"`, `"crude_anvil"`, `"stone_anvil"`, `"lapidary_table"`, `"sawmill"`, `"leather_station"`, or `"toolmakers_bench"` as the `workstation` value. Crude Anvil recipes should be treated as lower-tier physical anvil work, not a separate tool-family schema.

Station-work quality behavior is selected per recipe with `quality_mode`; it is not inferred from the workstation. `"none"` leaves the declared output unchanged, `"static"` applies the recipe's declared starting quality without timing prompts, and `"timed"` enables the hit-timing QTE and writes the completed score to modular tool parts, armour parts, or armour outputs. Missing `quality_mode` defaults to `"none"` so existing datapacks retain their previous output data.

```json
{
  "workstation": "tool_forge",
  "input": { "item": "minecraft:iron_ingot", "count": 2 },
  "output": {
    "tool_part": "crossbow_limbs",
    "material": "mobstoolforging:iron",
    "quality": 100
  },
  "pattern": "mobstoolforging:crossbow_limbs",
  "required_hits": 5,
  "quality_mode": "timed",
  "minimum_hammer_level": "stone"
}
```

Crossbow strings are also data-driven. An item must be included in `mobstoolforging:crossbow_strings`; a material definition can map that item to the material id stored on the assembled crossbow and select `guardMaterial` visuals. Vanilla String, Plant Fiber, and Blaze Thread are the built-in examples.

## Quality Compatibility

Tool parts and finished tools still store quality as an integer field on the existing `TOOL_PART` and `TOOL_CONSTRUCTION` components. The score maps to public display levels: Crude, Worked, Well Forged, Fine, and Masterwork. Missing quality data defaults to Well Forged, so existing saves and bridge-created stacks remain valid.

Lapidary-coated tool parts store the visible material as the part `material_id` and the original metal core as optional `coating_base_material` on `TOOL_PART`. Finished tools carry a coated primary head's core as optional `head_base_material` on `TOOL_CONSTRUCTION`. Stat rules can target the primary head core with the `head_base`, `headBase`, `core`, or `coreMaterial` slot names.

Finished tool quality is derived during physical Toolmaker's Station assembly from the primary part, required support parts, and a small assembly baseline. Datapacks and bridge mods can keep emitting the same components and let MTF compute quality naturally.

Quality stat effects are intentionally modest and config-gated by `qualityAffectsStats`. Armour part quality fields remain compatible, but this pass does not add new armour quality effects or armour progression.

The MoreWeapons bridge schema, Better Enchanting part tags, enchantment target tags, and existing data component names remain valid. Add optional fields where useful; do not rename existing bridge fields or part tags just to adopt the quality pass.

Trait JSON controls display identity:

```json
{
  "translation_key": "tooltip.mobs_more_weapons.trait.heavy",
  "description_translation_key": "tooltip.mobs_more_weapons.trait.heavy.desc",
  "color": "dark_gray",
  "category": "handling",
  "suppresses": []
}
```

Stat rule JSON adds small construction-based modifiers after the built-in rules:

```json
{
  "tool_type": "mobs_more_weapons:greatsword",
  "slot": "guard",
  "material": "mobs_more_weapons:steel",
  "durability_multiplier": 1.12,
  "attack_speed_bonus": -0.05,
  "traits": ["mobs_more_weapons:heavy"],
  "affinities": []
}
```

Supported `slot` values are `head`, `head_base`, `headBase`, `core`, `coreMaterial`, `handle`, `binding`, `guard`, `wrap`, `focus`, `treatment`, and `any`.

Resource packs can:

- Override normal item models and textures.
- Provide `assets/<namespace>/tooling/tool_visuals/<tool_type>.json`.
- Provide layer textures used by `mobstoolforging:parted_tool`.

## Bridge Mod Capabilities

Bridge mods can register a new tool family:

```java
ResourceLocation greatsword = ResourceLocation.fromNamespaceAndPath("mobs_more_weapons", "greatsword");

ToolTypeRegistry.registerToolType(ToolTypeDefinition.builder(greatsword, "greatsword_blade")
        .visual(greatsword)
        .toolItem(MobsMoreWeaponsItems.GREATSWORD::get)
        .partItem("greatsword_blade", BridgeItems.GREATSWORD_BLADE::get)
        .requiredAssemblyPart("greatsword_guard", BridgeItems.GREATSWORD_GUARD::get)
        .baseStats(5.5F, -2.9F)
        .swordLike(true)
        .build());
```

They can register physical station templates:

```java
ToolTypeRegistry.registerTemplate(new ForgeTemplateDefinition(
        ResourceLocation.fromNamespaceAndPath("mobs_more_weapons", "greatsword_blade"),
        greatsword,
        "greatsword_blade",
        4,
        5,
        "forge_template.mobs_more_weapons.greatsword_blade"
));
```

A bridge pattern item can point directly at that template id:

```java
new ToolTemplateItem(ResourceLocation.fromNamespaceAndPath("mobs_more_weapons", "greatsword_blade"), new Item.Properties());
```

They can register custom traits and stat behavior:

```java
ResourceLocation heavy = ResourceLocation.fromNamespaceAndPath("mobs_more_weapons", "heavy");

ToolTraitRegistry.registerTrait(new ToolTraitDefinition(
        heavy,
        "tooltip.mobs_more_weapons.trait.heavy",
        "tooltip.mobs_more_weapons.trait.heavy.desc",
        ChatFormatting.DARK_GRAY,
        "handling"
));

ToolTypeRegistry.registerStatModifier((definition, construction, stats) -> {
    if (definition.id().equals(greatsword)) {
        stats.addAttackDamage(1.0F);
        stats.addAttackSpeed(-0.2F);
        stats.addTrait(heavy);
    }
});
```

They can register material and handle visuals:

```java
MaterialCatalog.registerMaterial(
        ResourceLocation.fromNamespaceAndPath("mobs_more_weapons", "steel"),
        MaterialCategory.METAL,
        MobsMoreWeaponsItems.STEEL_INGOT.get(),
        MobsMoreWeaponsTiers.STEEL
);

MaterialCatalog.registerHandleMaterial(
        MobsMoreWeaponsItems.REINFORCED_HANDLE.get(),
        ResourceLocation.fromNamespaceAndPath("mobs_more_weapons", "reinforced_handle")
);
```

## Visual Model Hook

External finished tools can use the dynamic model loader:

```json
{
  "parent": "minecraft:item/handheld",
  "loader": "mobstoolforging:parted_tool",
  "tool": "mobs_more_weapons:greatsword",
  "visual": "mobs_more_weapons:greatsword",
  "textures": {
    "layer_handle_oak": "mobstoolforging:source/tool_parts/stick/stick_handle_tool",
    "layer_greatsword_blade_mobs_more_weapons_steel": "mobs_more_weapons:item/tool_parts/steel_greatsword_blade_tool",
    "particle": "mobs_more_weapons:item/tool_parts/steel_greatsword_blade_tool"
  }
}
```

MTF also wraps ordinary inventory item models at bake time. If an external stack
has MTF's `TOOL_CONSTRUCTION` or `TOOL_PART` component, MTF will attempt to
render it from the registered tool type and visual definitions even if the
external item's own model is still a plain `layer0` model. Plain external stacks
without MTF components keep their original model.

The component-driven renderer uses these default texture conventions:

- final tool layer for part item `namespace:iron_great_sword_blade`:
  `namespace:item/iron_great_sword_blade_tool`
- standalone part layer for part item `namespace:iron_great_sword_blade`:
  `namespace:item/iron_great_sword_blade_part`
- handles use MTF's built-in handle sprites.

Use explicit `mobstoolforging:parted_tool` or `parted_tool_part` item models
only when a compat pack needs to override those conventions or fully control
model resources.

External visual definitions can list extra material ids per layer so the model loader knows which sprites to collect:

```json
{
  "canvas": 16,
  "large_canvas": 32,
  "large_in_hand": true,
  "layers": [
    {
      "slot": "greatsword_blade",
      "material_from": "headMaterial",
      "materials": ["mobs_more_weapons:steel"],
      "z": 3
    }
  ]
}
```

Layer JSON can also define explicit fallback templates and texture conventions:

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

`usage` resolves to `tool` for assembled layers and `part` for standalone part items. Available placeholders are `{namespace}`, `{material_namespace}`, `{material}`, `{material_path}`, `{slot}`, and `{usage}`.

Handle layers can choose rendering priority:

- `default_handle` or `exact_first`: exact texture first, then template fallback.
- `template_first` or `template_handle`: grayscale template first, then exact texture.
- `template_only`: grayscale template only.
- `explicit_only` or `explicit_handle`: exact texture only.
- `masked_handle`: reshape the material's ordinary handle source texture through
  `assets/<visual_namespace>/textures/source/tool_parts/handle_masks/<visual>_handle_mask.png`
  at runtime. Addon materials use
  `assets/<material_namespace>/textures/source/tool_parts/<material>/<material>_handle_tool.png`.

This lets a weapon add-on decide whether a custom handle silhouette should override MTF's built-in stick/blaze/breeze handle art.

The texture key format is `layer_<slot>_<material>`, where non-MTF material ids include the namespace, for example `layer_greatsword_blade_mobs_more_weapons_steel`.
