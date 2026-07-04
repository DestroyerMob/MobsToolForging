# Test Matrix

Use this as the visible manual matrix for modular tooltip and layer checks.

| Test | Expected Tooltip Fields | Expected Visible Layers |
| --- | --- | --- |
| Crafted diamond pickaxe + blaze handle + copper binding | Normal tooltip includes `Quality: ...` and `Traits: Kindled • Resonant`; Shift tooltip also shows Head: Diamond, Handle: Blaze, Binding: Copper, plus short trait descriptions; advanced tooltip shows raw stat profile and construction quality score | `blaze` pickaxe handle, `diamond` pickaxe head, `copper` pickaxe binding |
| Crafted iron pickaxe + oak handle + leather wrap + nether treatment | Normal tooltip includes `Quality: ...` and `Traits: Sure Grip • Nether-Treated`; Shift tooltip also shows Head: Iron, Handle: Oak, Wrap: Leather, Treatment: Nether, plus short trait descriptions; advanced tooltip shows raw stat profile | `oak` pickaxe handle, `leather` pickaxe wrap, `iron` pickaxe head, `nether` pickaxe treatment overlay |
| Crafted diamond sword + breeze handle + amethyst focus + guard, using a diamond guard for this check | Normal tooltip includes `Quality: ...` and `Traits: Swift • Stabilized • Focused`; Shift tooltip also shows Head: Diamond, Handle: Breeze, Guard: Diamond, Focus: Amethyst, plus short trait descriptions; advanced tooltip shows raw stat profile | `breeze` sword handle, `diamond` sword blade, `diamond` sword guard, `amethyst` sword focus |
| Forged iron sword guard item by itself | Item name: Iron Sword Guard; tooltip includes quality; stack part data: `part_type=sword_guard`, `material_id=mobstoolforging:iron`; no finished-tool effect tooltip | Standalone `iron` sword guard part sprite only |

Unknown tagged rod handles should craft as handles but resolve to a known visual handle material, currently Oak, unless a real visual material is added for them.

## Progression Checks

| Test | Expected Result |
| --- | --- |
| Early flint to copper route | Player can gather Plant Fiber, knap flint, make a Flint Pick, craft Pattern Boards, create patterns, and reach first copper parts without paper, screwdriver assembly, or the copper Smithing Anvil. |
| Crude Anvil start | Crude Anvil recipe is cheap, accepts basic metal templates, and can start materials whose definitions allow low heat after they have been warmed. Built-in copper, gold, and iron allow campfire-low heat. Output quality caps at Worked by default. |
| Smithing Anvil upgrade | Smithing Anvil recipe uses one copper block plus four copper ingots. It supports the same physical flow with a Fine quality cap by default. |
| Workshop heat | Right-clicking a lit campfire with a heatable ingot or metal part inserts one workpiece into the campfire's normal visible slots; dropped heatable workpieces on a lit campfire insert if a slot is free. The campfire ejects a low-heat workpiece when the timer completes. Cold ingots cannot start low-heat forging just by being near a campfire. Low-heat workpieces can cool from the 55% target down to `lowHeatMinimumForgeTemperature`. Finished parts retain heat data and can be quenched. A fueled Heating Forge provides hot buffered nearby heat. Hammering does not stop mid-craft when `requireHeatAtJobStartOnly=true` and `workpieceCoolsMidCraft=false`. |
| Toolmaker's Bench assembly | Placed compatible parts assemble with empty-hand interaction or Smithing Hammer. A placed finished tool separates with Smithing Hammer. Screwdriver remains registered but is not required for normal assembly. |

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
| Emerald, ruby, and sapphire parts | Gem parts complete on the Lapidary Table without a Gem Cutter's Knife and without Diamond Powder by default. Knife use provides optional quality help. |
| Diamond parts | Diamond parts require `mobstoolforging:diamond_powder` when `diamondRequiresAbrasive=true`; the `mobstoolforging:lapidary_abrasives` tag remains valid. |
| Knife-required config | With `gemcuttersFileRequired=true`, empty-hand and hammer lapidary work are blocked with the knife hint. |

## Compatibility Checks

| Test | Expected Result |
| --- | --- |
| MoreWeapons bridge | Great sword, katana, battle axe, knife, and machete bridge templates still load, create patterns, shape parts, assemble tools, and keep their bridge JSON fields unchanged. |
| Better Enchanting tags | Existing modular part tags and Better Enchanting target tags still apply to tool parts and finished tools. |
| Existing data components | `TOOL_PART`, `TOOL_CONSTRUCTION`, template ids, and existing integer `quality` fields remain readable. Missing quality data defaults to Well Forged. |
