package org.destroyermob.mobstoolforging.world;

import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.EnchantmentTags;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.enchanting.GetEnchantmentLevelEvent;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Runtime effects for traits whose behavior cannot be expressed by the static tool profile. */
public final class ToolTraitEffects {
    private ToolTraitEffects() {
    }

    public static int adjustDurabilityDamage(ItemStack stack, int amount, RandomSource random) {
        if (amount <= 0) {
            return amount;
        }
        float wearMultiplier = 1.0F;
        wearMultiplier += ToolStatBuilder.traitPotency(level(stack, ToolTrait.SWIFT));
        wearMultiplier += ToolStatBuilder.traitPotency(level(stack, ToolTrait.JAGGED));
        wearMultiplier += 0.50F * ToolStatBuilder.traitPotency(level(stack, ToolTrait.FORCEFUL));
        int adjusted = Math.max(1, Math.round(amount * wearMultiplier));

        float reinforcedPotency = ToolStatBuilder.traitPotency(level(stack, ToolTrait.REINFORCED));
        float prevention = reinforcedPotency <= 0.0F ? 0.0F : 1.0F - 1.0F / (1.0F + 3.0F * reinforcedPotency);
        if (prevention <= 0.0F) {
            return adjusted;
        }
        int applied = 0;
        for (int point = 0; point < adjusted; point++) {
            if (random.nextFloat() >= prevention) {
                applied++;
            }
        }
        return applied;
    }

    public static float repairFraction(ItemStack stack) {
        float fraction = level(stack, ToolTrait.REINFORCED) > 0 ? 0.50F : 0.25F;
        if (level(stack, ToolTrait.STEADY) > 0) {
            fraction *= 1.50F;
        }
        return Math.min(1.0F, fraction);
    }

    public static void modifyBreakSpeed(PlayerEvent.BreakSpeed event) {
        Player player = event.getEntity();
        ItemStack tool = player.getMainHandItem();
        float bonus = workHardenedBonus(tool);
        if (bonus > 0.0F) {
            event.setNewSpeed(event.getNewSpeed() * (1.0F + bonus));
        }
        if (level(tool, ToolTrait.FOCUSED) <= 0) {
            return;
        }
        float speed = event.getNewSpeed();
        if (player.isEyeInFluid(FluidTags.WATER)) {
            double submergedSpeed = player.getAttributeValue(Attributes.SUBMERGED_MINING_SPEED);
            if (submergedSpeed > 0.0D && submergedSpeed < 1.0D) {
                speed /= (float) submergedSpeed;
            }
        }
        if (!player.onGround()) {
            speed *= 5.0F;
        }
        event.setNewSpeed(speed);
    }

    public static void modifyEnchantmentLevels(GetEnchantmentLevelEvent event) {
        ItemStack stack = event.getStack();
        int quickChargeBonus = discreteBonus(level(stack, ToolTrait.TENSIONED))
                + discreteBonus(level(stack, ToolTrait.FOCUSED));
        if (quickChargeBonus > 0) {
            event.getHolder(Enchantments.QUICK_CHARGE).filter(event::isTargetting)
                    .ifPresent(enchantment -> event.getEnchantments().set(
                            enchantment,
                            event.getEnchantments().getLevel(enchantment) + quickChargeBonus
                    ));
        }

        int fortunate = level(stack, ToolTrait.FORTUNATE);
        if (fortunate > 0) {
            int bonus = discreteBonus(fortunate);
            event.getHolder(Enchantments.FORTUNE).filter(event::isTargetting)
                    .ifPresent(enchantment -> event.getEnchantments().set(enchantment, event.getEnchantments().getLevel(enchantment) + bonus));
            event.getHolder(Enchantments.LOOTING).filter(event::isTargetting)
                    .ifPresent(enchantment -> event.getEnchantments().set(enchantment, event.getEnchantments().getLevel(enchantment) + bonus));
        }

        int resonant = level(stack, ToolTrait.RESONANT);
        if (resonant <= 0) {
            return;
        }
        highestInstalledEnchantment(stack).filter(event::isTargetting).ifPresent(enchantment ->
                event.getEnchantments().set(
                        enchantment,
                        event.getEnchantments().getLevel(enchantment) + discreteBonus(resonant)
                )
        );
    }

    public static void modifyIncomingDamage(LivingIncomingDamageEvent event) {
        ItemStack tool = attackingTool(event.getSource());
        if (tool.isEmpty()) {
            return;
        }

        float multiplier = currentOutputMultiplier(tool);
        if (event.getSource().getDirectEntity() instanceof Projectile) {
            float physicalDamage = ToolStatBuilder.profile(tool)
                    .map(ToolStatProfile::physicalDamageMultiplier)
                    .orElse(1.0F);
            float tensioned = 1.0F + 0.30F * ToolStatBuilder.traitPotency(level(tool, ToolTrait.TENSIONED));
            multiplier *= physicalDamage * tensioned;
        }
        if (multiplier != 1.0F) {
            event.setAmount(event.getAmount() * multiplier);
        }

        float armorIgnored = Math.min(0.75F, 0.30F * ToolStatBuilder.traitPotency(level(tool, ToolTrait.ADAMANT)));
        if (armorIgnored > 0.0F) {
            event.addReductionModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> reduction * (1.0F - armorIgnored));
        }
    }

    public static void applyPostDamageEffects(LivingDamageEvent.Post event) {
        if (event.getNewDamage() <= 0.0F || level(attackingTool(event.getSource()), ToolTrait.KINDLED) <= 0) {
            return;
        }
        event.getEntity().igniteForSeconds(5.0F);
    }

    public static void smeltBlockDrops(BlockDropsEvent event) {
        if (level(event.getTool(), ToolTrait.KINDLED) <= 0) {
            return;
        }
        List<ItemEntity> drops = event.getDrops();
        for (ItemEntity drop : drops) {
            ItemStack inputStack = drop.getItem();
            if (inputStack.isEmpty()) {
                continue;
            }
            SingleRecipeInput input = new SingleRecipeInput(inputStack.copyWithCount(1));
            RecipeHolder<SmeltingRecipe> recipe = event.getLevel().getRecipeManager()
                    .getRecipeFor(RecipeType.SMELTING, input, event.getLevel())
                    .orElse(null);
            if (recipe == null) {
                continue;
            }
            ItemStack result = recipe.value().assemble(input, event.getLevel().registryAccess());
            if (result.isEmpty()) {
                continue;
            }
            result.setCount(result.getCount() * inputStack.getCount());
            drop.setItem(result);
        }
    }

    private static java.util.Optional<Holder<Enchantment>> highestInstalledEnchantment(ItemStack stack) {
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        Holder<Enchantment> selected = null;
        int selectedLevel = 0;
        for (var entry : enchantments.entrySet()) {
            if (entry.getIntValue() > selectedLevel && !entry.getKey().is(EnchantmentTags.CURSE)) {
                selected = entry.getKey();
                selectedLevel = entry.getIntValue();
            }
        }
        return java.util.Optional.ofNullable(selected);
    }

    private static int discreteBonus(int traitLevel) {
        return traitLevel <= 0 ? 0 : Math.max(2, Math.round(2.0F * ToolStatBuilder.traitPotency(traitLevel)));
    }

    private static float workHardenedBonus(ItemStack stack) {
        int traitLevel = level(stack, ToolTrait.WORK_HARDENED);
        if (traitLevel <= 0 || !stack.isDamageableItem() || stack.getMaxDamage() <= 0) {
            return 0.0F;
        }
        float condition = 1.0F - stack.getDamageValue() / (float) stack.getMaxDamage();
        float baseBonus = condition < 0.25F ? 1.00F : condition < 0.50F ? 0.50F : condition < 0.75F ? 0.25F : 0.0F;
        return baseBonus * ToolStatBuilder.traitPotency(traitLevel);
    }

    public static float currentOutputMultiplier(ItemStack stack) {
        return 1.0F + workHardenedBonus(stack);
    }

    private static ItemStack attackingTool(DamageSource source) {
        ItemStack weapon = source.getWeaponItem();
        if (weapon != null && !weapon.isEmpty()) {
            return weapon;
        }
        Entity attacker = source.getEntity();
        return attacker instanceof LivingEntity living ? living.getMainHandItem() : ItemStack.EMPTY;
    }

    private static int level(ItemStack stack, ToolTrait trait) {
        return stack.isEmpty() ? 0 : ToolStatBuilder.traitLevel(stack, trait);
    }
}
