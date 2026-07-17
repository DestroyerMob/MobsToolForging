# Test Matrix

Use this as the visible manual matrix for modular tooltip and layer checks.

| Test | Expected Tooltip Fields | Expected Visible Layers |
| --- | --- | --- |
| Gold pickaxe head with diamond coating + blaze handle | Normal tooltip includes `Quality: ...` and all three traits: `Adamant`, `Gilded`, and `Kindled`; Shift tooltip identifies Diamond as the head, Gold as the core, and Blaze as the handle; advanced tooltip shows the expected trait IDs and stat profile | `blaze` pickaxe handle and `diamond` pickaxe head |
| Crafted iron pickaxe + oak handle | Normal tooltip includes both `Reinforced` and `Steady`; Shift tooltip describes 75% wear prevention, stronger repair, faster mining, and stronger repair from the handle | `oak` pickaxe handle and `iron` pickaxe head |
| Crafted diamond sword + breeze handle + diamond guard | Normal tooltip includes `Adamant II` and `Swift`; Shift tooltip shows both diamond components and the breeze handle; advanced tooltip reports the diminished level-II Adamant potency | `breeze` sword handle, `diamond` sword blade, and `diamond` sword guard |
| Netherite-dipped iron pickaxe head + oak handle | Normal tooltip includes `Nether-Forged` and `Steady`; the item is fireproof, has doubled base durability, and retains the netherite treatment through assembly/disassembly | `oak` pickaxe handle, `iron` pickaxe head, and `netherite` treatment overlay |
| Forged iron sword guard item by itself | Item name: Iron Sword Guard; tooltip includes quality; stack part data: `part_type=sword_guard`, `material_id=mobstoolforging:iron`; no finished-tool effect tooltip | Standalone `iron` sword guard part sprite only |

Unknown tagged rod handles should craft as handles but resolve to a known visual handle material, currently Oak, unless a real visual material is added for them.

## Trait Behavior Checks

| Test | Expected Result |
| --- | --- |
| Component identity | A material grants the same primary trait as a head, coated core, guard, legacy binding/wrap/focus, or handle whenever that material is valid in the slot. Support slots do not add unrelated secondary traits. |
| Repeated materials | Trait potency is 100% at level I, 150% at level II, and 175% at level III. Tooltip numerals match the number of contributing components. |
| Gold flexibility | Every gold component still adds one Better Enchanting capacity. Gilded grants no additional XP or raw stat bonus. |
| Adamant | A level-I diamond component multiplies suitable-block mining speed by 1.35. Against an armored target, 30% of the armor reduction is ignored; unarmored targets receive no artificial damage bonus. |
| Kindled | A blaze component makes the item fireproof, replaces smeltable block drops with their smelting output, and ignites a successfully damaged target for five seconds. |
| Reinforced and repair | Over many durability actions, an iron component prevents approximately 75% of wear. One matching repair material restores 50%; adding Steady raises that to 75%. |
| Work-Hardened | A copper component adds no output above 75% condition, then adds 25%, 50%, and 100% mining and attack/projectile output below the three documented thresholds. The current multiplier is listed separately from physical base stats. |
| Enchantment traits | Emerald supplies effective Fortune II and Looting II even without those stored enchantments. Amethyst adds two effective levels to the highest-level installed non-curse enchantment without changing the stored tooltip level or consuming capacity. |
| Focused | Airborne and underwater mining penalties are removed without multiplying speed again when another effect already removes the underwater penalty. On a crossbow, Focused supplies effective Quick Charge II; the stored enchantments remain unchanged. |
| Tensioned | A spider-silk crossbow string supplies effective Quick Charge II and multiplies projectile damage by 1.30. It supplies no fictional harvest-tool bonuses. Leather does not appear as a tool-trait source. |
| Removed treatments | Nether-Treated and Echoing do not appear in JEI's standard material trait list, generated examples, or normal construction. Netherite dipping remains Nether-Forged. |
| Physical trait ordering | Keen, Forceful, Jagged, and Nether-Forged multiply the stored physical attack/tool stats before Sharpness or another unconditional damage enchantment is evaluated. |
| Effective stat tooltip | A modular tool visibly reports item-only attack damage, attack speed, suitable-block tool speed, and durability; Sharpness and Efficiency change the reported totals. Hold Shift to see the scope note. |
| Crossbow effective stats | A modular crossbow visibly reports charge seconds/ticks, its combined physical/projectile trait multiplier, durability, and any current Work-Hardened output multiplier. |

## Progression Checks

| Test | Expected Result |
| --- | --- |
| Early flint to copper route | Player can knap flint, assemble a Flint Pick at a Toolmaker's Station with a stick/handle, craft Pattern Boards, create patterns, and reach first copper parts without Plant Fiber binding, paper, screwdriver assembly, or the copper Tool Forge. |
| Crude Anvil start | Crude Anvil recipe is cheap, accepts basic metal templates, and can start materials whose definitions allow low heat after they have been warmed. Built-in copper, gold, and iron allow campfire-low heat. Output quality caps at Worked by default. |
| Tool Forge upgrade | Tool Forge recipe uses one copper block plus four copper ingots. It supports the same physical flow with a Fine quality cap by default. |
| Workshop heat | Right-clicking a lit campfire with a heatable ingot or metal part inserts one workpiece into the campfire's normal visible slots; dropped heatable workpieces on a lit campfire insert if a slot is free. The campfire ejects a low-heat workpiece when the timer completes. Cold ingots cannot start low-heat forging just by being near a campfire. Low-heat workpieces can cool from the 55% target down to `lowHeatMinimumForgeTemperature`. Finished parts retain heat data and can be quenched. A fueled Heating Forge provides hot buffered nearby heat. Hammering does not stop mid-craft when `requireHeatAtJobStartOnly=true` and `workpieceCoolsMidCraft=false`. |
| Toolmaker's Station assembly | Placed compatible parts assemble with empty-hand interaction or Smithing Hammer. A placed finished tool separates with Smithing Hammer. Screwdriver remains registered but is not required for normal assembly. |

## Quality Checks

| Test | Expected Result |
| --- | --- |
| Save/load quality | Forged/cut part quality persists after save/load, inventory transfer, assembly, disassembly, and tooltip display. |
| Assembly weighting | Finished tool quality derives from primary part weight, required support part weight, and assembly baseline. Legacy tools without quality display as Well Forged. |
| Timing enabled | The workpiece pulses during good timing windows. Good hits can improve quality; missed hits still complete the craft and do not destroy material. |
| Timing disabled | With `enableTimingQuality=false`, forging still completes and quality is determined by setup, heat, station tier, material difficulty, and optional lapidary helper inputs. |
| Stat effects | `qualityAffectsStats=true` applies modest penalties/bonuses. Masterwork is helpful but not required for normal progression. |

## Lapidary Checks

| Test | Expected Result |
| --- | --- |
| Heated metal part placement | A heated forged metal tool part can be placed on the Lapidary Table without selecting a pattern. A cold part is rejected with the heat message. |
| Gem shell material | Diamond material requires diamond-tier abrasive in the table first. Emerald, ruby, and sapphire material can be added after the heated part without abrasive. Required material count follows the matching part shape cost. |
| Coated part output | Completed output is named as a metal-gem part, stores gem material as `material_id`, stores metal core as `coating_base_material`, renders as the gem part, and head pieces assemble into tools with the gem head stats. |
| Coated quality | Coated part quality averages the original metal part quality with lapidary gem-work quality. Gem and coated parts can still be polished afterward. |
| Lapidary abrasive tiers | Diamond Powder and the `mobstoolforging:lapidary_abrasives/diamond` tag satisfy diamond's abrasive requirement. The umbrella `mobstoolforging:lapidary_abrasives` tag remains valid for optional quality helper checks. |
| Knife-required config | With `gemcuttersFileRequired=true`, empty-hand and hammer lapidary work are blocked with the knife hint. |

## Compatibility Checks

| Test | Expected Result |
| --- | --- |
| MoreWeapons bridge | Great sword, katana, battle axe, knife, and machete bridge templates still load, create patterns, shape parts, assemble tools, and keep their bridge JSON fields unchanged. |
| Better Enchanting tags | Existing modular part tags and Better Enchanting target tags still apply to tool parts and finished tools. |
| Existing data components | `TOOL_PART`, `TOOL_CONSTRUCTION`, template ids, and existing integer `quality` fields remain readable. Missing quality data defaults to Well Forged. |
