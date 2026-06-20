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

`tier` can also be a simple string such as `"iron"`, `"diamond"`, `"netherite"`, `"copper"`, or `"emerald"`. `items` and `tags` are accepted material sources. `visual_slots` controls which model layers should collect sprites for this material. `handle_items` maps specific handle items to this material id, but only use it when matching handle sprites exist.

Tool type JSON lets a datapack connect existing item classes to Mobs Tool Forging construction data:

```json
{
  "tool_item": "mobs_more_weapons:greatsword",
  "primary_part_type": "greatsword_blade",
  "primary_part_item": "mobs_more_weapons:greatsword_blade",
  "part_items": {
    "greatsword_guard": "mobs_more_weapons:greatsword_guard"
  },
  "required_assembly_parts": ["greatsword_guard"],
  "visual": "mobs_more_weapons:greatsword",
  "base_attack_damage_bonus": 5.5,
  "base_attack_speed_bonus": -2.9,
  "sword_like": true
}
```

Datapack tool types cannot create new item classes or custom combat callbacks. They can attach Mobs Tool Forging components, stats, recipes, and visuals to items that already exist.

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

Supported `slot` values are `head`, `handle`, `binding`, `guard`, `wrap`, `focus`, `treatment`, and `any`.

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

The texture key format is `layer_<slot>_<material>`, where non-MTF material ids include the namespace, for example `layer_greatsword_blade_mobs_more_weapons_steel`.
