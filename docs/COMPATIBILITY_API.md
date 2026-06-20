# Compatibility API

Mobs Tool Forging has two compatibility layers:

- Datapacks and resource packs can add tagged materials, recipes, and textures for registered systems.
- Bridge mods can register new tool families, templates, traits, material stats, and custom behavior.

Datapacks alone cannot create new Java item classes or new combat behavior. Full compatibility with a weapon mod such as Mobs More Weapons should be done by a small bridge mod.

## Datapack Capabilities

Datapacks can:

- Add source items to `mobstoolforging:materials`, `mobstoolforging:materials/metals`, or `mobstoolforging:materials/gems`.
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
    "layer_handle_oak": "mobstoolforging:generated/tool_parts/oak/sword_handle",
    "layer_greatsword_blade_mobs_more_weapons_steel": "mobs_more_weapons:item/tool_parts/steel_greatsword_blade",
    "particle": "mobs_more_weapons:item/tool_parts/steel_greatsword_blade"
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
