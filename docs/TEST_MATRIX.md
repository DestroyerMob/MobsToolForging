# Test Matrix

Use this as the visible manual matrix for modular tooltip and layer checks.

| Test | Expected Tooltip Fields | Expected Visible Layers |
| --- | --- | --- |
| Crafted diamond pickaxe + blaze handle + copper binding | Normal tooltip: `Traits: Kindled • Resonant`; Shift tooltip also shows Head: Diamond, Handle: Blaze, Binding: Copper, plus short trait descriptions; advanced tooltip shows raw stat profile | `blaze` pickaxe handle, `diamond` pickaxe head, `copper` pickaxe binding |
| Crafted iron pickaxe + oak handle + leather wrap + nether treatment | Normal tooltip: `Traits: Sure Grip • Nether-Treated`; Shift tooltip also shows Head: Iron, Handle: Oak, Wrap: Leather, Treatment: Nether, plus short trait descriptions; advanced tooltip shows raw stat profile | `oak` pickaxe handle, `leather` pickaxe wrap, `iron` pickaxe head, `nether` pickaxe treatment overlay |
| Crafted diamond sword + breeze handle + amethyst focus + guard, using a diamond guard for this check | Normal tooltip: `Traits: Swift • Stabilized • Focused`; Shift tooltip also shows Head: Diamond, Handle: Breeze, Guard: Diamond, Focus: Amethyst, plus short trait descriptions; advanced tooltip shows raw stat profile | `breeze` sword handle, `diamond` sword blade, `diamond` sword guard, `amethyst` sword focus |
| Forged iron sword guard item by itself | Item name: Iron Sword Guard; stack part data: `part_type=sword_guard`, `material_id=mobstoolforging:iron`; no finished-tool effect tooltip | Standalone `iron` sword guard part sprite only |

Unknown tagged rod handles should craft as handles but resolve to a known visual handle material, currently Oak, unless a real visual material is added for them.
