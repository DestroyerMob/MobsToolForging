# Blackstone Foundry

Build an open-topped blackstone shell with a solid floor, place the Foundry Forge controller in a wall, and include at least one Foundry Fuel Tank. Foundry Glass may replace straight wall blocks without breaking the structure. The interior may be up to 9 by 9 by 8 blocks; each interior block provides 1,000 mB of molten-metal capacity. Fuel tanks always hold 4,000 mB of one registered fuel fluid and do not scale with the structure.

Right-click the controller with supported solid metal. Inputs melt directly in the foundry without a crucible item. Molten materials remain in insertion order: a Foundry Drain exposes the bottom layer, while the visible surface shows the top layer.

Replace a wall block with a Foundry Drain, attach a Foundry Faucet to its outside face, and place a casting receiver directly below the faucet. Toggle the faucet by hand or power it with redstone. A Casting Table consumes 90 mB and produces one ingot; a Casting Basin consumes 810 mB and produces one storage block. Casts cool for 60 ticks before their output can be collected.

Foundry Glass provides the no-GUI contents display. Quarter-level marks make volume readable; molten layers render as separate material-tinted vertical bands at their actual fill heights, with a tiled animated molten texture and a representative material marker. The bottom, next-to-drain layer pulses. The currently melting solid glows, shrinks, and sinks instead of disappearing all at once. This makes fill level, material order, incorrect mixtures, drain order, and active melting readable without Jade or a recipe viewer.

## Fuel temperature

Fuel definitions live under `data/<namespace>/mobstoolforging/foundry_fuels/*.json`. A fuel may select one exact `fluid` or a `fluid_tag`. `temperature_c` is the effective foundry temperature in degrees Celsius. `amount` is consumed to begin one burn lasting `burn_ticks`.

```json
{
  "fluid": "minecraft:lava",
  "temperature_c": 2000,
  "amount": 1000,
  "burn_ticks": 2400
}
```

The tank accepts standard NeoForge fluid containers and renders the actual fluid texture and tint. It cannot mix two fuel fluids. A foundry only draws a batch whose temperature meets the current material's melting point, so an insufficient fuel stays in its tank instead of being silently wasted. Vanilla lava is intentionally assigned an effective foundry temperature of 2,000°C for gameplay, allowing it to melt every built-in material.

## Datapack melting points

Each material has one temperature definition under `data/<namespace>/mobstoolforging/foundry_melting_points/*.json`:

```json
{
  "material": "mobstoolforging:iron",
  "melting_point_c": 1538
}
```

This single value applies to every melting recipe and recycled part that produces that material. Materials without a definition use a 1,000°C compatibility fallback. Built-in values are tin 232°C, carbon processing 800°C, bronze 950°C, gold 1,064°C, copper 1,085°C, steel 1,450°C, iron 1,538°C, and netherite 1,800°C. Netherite is fictional; its value is a gameplay choice.

## Datapack melting recipes

Files live under `data/<namespace>/mobstoolforging/foundry_melting/*.json` and reload with datapacks. An input may use either `item` or `tag`:

```json
{
  "input": { "tag": "c:ingots/iron" },
  "material": "mobstoolforging:iron",
  "amount": 90,
  "ticks": 400
}
```

`amount` is measured in millibuckets. Temperature does not belong in these item recipes; it comes from the output material's central melting-point definition. A pack can raise netherite above 2,000°C and add a hotter fuel without changing its ingot and scrap recipes.

## Datapack alloy recipes

Files live under `data/<namespace>/mobstoolforging/foundry_alloys/*.json`. Ratios are exact; complete batches react automatically while excess molten inputs remain as separate layers.

```json
{
  "result": "mobstoolforging:steel",
  "inputs": {
    "mobstoolforging:iron": 90,
    "mobstoolforging:carbon": 10
  },
  "output_amount": 90
}
```

MTF includes steel (one iron ingot plus carbon from coal or charcoal) and bronze (three copper ingots plus one `c:ingots/tin` ingot). `output_amount` may be smaller than the input total to represent slag or oxidation loss.

## Casting, reforging, and recycling

Parts poured into reusable gold casts always begin with Crude workmanship and casting porosity. Polishing changes finish, not workmanship. To reforge an uncoated metal part, heat it, select its matching pattern on a Smithing Anvil, place the hot part, and hammer it as normal. Reforging preserves the material and alloy composition, clears casting porosity, and replaces the old workmanship score.

Uncoated tool and armor parts may also be dropped or inserted into a formed foundry for recycling. Recovery is deliberately below 100 percent and improves with remaining durability and workmanship.

Quenching from 60–89 percent heat hardens a part; quenching at 90 percent or above makes it brittle. Reheat a hardened or brittle part to 30–65 percent and let it cool fully to temper it. Tempering improves effective workmanship, while brittleness reduces it sharply. Hold Shift over a part or assembled item to inspect origin, heat treatment, effective score, and alloy composition.
