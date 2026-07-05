package org.destroyermob.mobstoolforging.world;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class ToolAssemblyEnchantments {
    private ToolAssemblyEnchantments() {
    }

    public static boolean mergeOnto(ItemStack output, Iterable<ItemStack> sources, HolderLookup.Provider registries) {
        if (output.isEmpty()) {
            return false;
        }

        RegistryAccess registryAccess = registries instanceof RegistryAccess access ? access : null;
        ItemEnchantments.Mutable merged = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(output));
        boolean changed = false;
        for (ItemStack source : sources) {
            if (source.isEmpty()) {
                continue;
            }
            ItemEnchantments enchantments = EnchantmentHelper.getEnchantmentsForCrafting(source);
            for (Object2IntMap.Entry<Holder<Enchantment>> entry : enchantments.entrySet()) {
                if (!merge(registryAccess, merged, entry.getKey(), entry.getIntValue())) {
                    return false;
                }
                changed = true;
            }
        }

        if (!changed) {
            return true;
        }

        EnchantmentHelper.setEnchantments(output, merged.toImmutable());
        if (registryAccess != null) {
            BetterEnchantingBridge.applyFusion(registryAccess, output);
        }
        BetterEnchantingBridge.clampEnchantments(output);
        return true;
    }

    public static List<ItemStack> copyToolEnchantmentsToViableParts(ItemStack tool, List<ItemStack> parts) {
        if (tool.isEmpty() || parts.isEmpty()) {
            return List.copyOf(parts);
        }

        ItemEnchantments toolEnchantments = EnchantmentHelper.getEnchantmentsForCrafting(tool);
        if (toolEnchantments.isEmpty()) {
            return List.copyOf(parts);
        }

        List<ItemStack> result = new ArrayList<>(parts.size());
        for (ItemStack part : parts) {
            result.add(part.copy());
        }

        boolean changed = false;
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : toolEnchantments.entrySet()) {
            changed |= copyToBestPart(result, entry.getKey(), entry.getIntValue());
        }
        return changed ? List.copyOf(result) : List.copyOf(parts);
    }

    public static boolean hasEnchantments(Iterable<ItemStack> parts) {
        for (ItemStack part : parts) {
            if (!part.isEmpty() && !EnchantmentHelper.getEnchantmentsForCrafting(part).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    public static void syncRoutedToolEnchantments(ItemStack tool, HolderLookup.Provider registries) {
        if (tool.isEmpty() || !(registries instanceof RegistryAccess registryAccess)) {
            return;
        }
        BetterEnchantingBridge.syncRoutedToolEnchantments(registryAccess, tool);
    }

    private static boolean copyToBestPart(List<ItemStack> parts, Holder<Enchantment> enchantment, int level) {
        if (level <= 0) {
            return false;
        }

        ItemStack target = ItemStack.EMPTY;
        int existingLevel = 0;
        for (ItemStack part : parts) {
            int partLevel = EnchantmentHelper.getEnchantmentsForCrafting(part).getLevel(enchantment);
            if (partLevel > 0) {
                target = part;
                existingLevel = partLevel;
                break;
            }
        }

        if (target.isEmpty()) {
            for (ItemStack part : parts) {
                if (canCarryEnchantment(part, enchantment)) {
                    target = part;
                    break;
                }
            }
        }

        if (target.isEmpty()) {
            return false;
        }

        int newLevel = Math.max(existingLevel, BetterEnchantingBridge.clampLevel(enchantment, level));
        if (existingLevel >= newLevel) {
            return false;
        }

        ItemEnchantments.Mutable enchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(target));
        enchantments.set(enchantment, newLevel);
        EnchantmentHelper.setEnchantments(target, enchantments.toImmutable());
        BetterEnchantingBridge.clampEnchantments(target);
        return true;
    }

    private static boolean canCarryEnchantment(ItemStack stack, Holder<Enchantment> enchantment) {
        return !stack.isEmpty()
                && (BetterEnchantingBridge.matchesTarget(stack, enchantment) || stack.supportsEnchantment(enchantment));
    }

    private static boolean merge(RegistryAccess registryAccess, ItemEnchantments.Mutable merged, Holder<Enchantment> enchantment, int level) {
        if (level <= 0) {
            return true;
        }

        int existingLevel = merged.getLevel(enchantment);
        if (existingLevel <= 0 && conflictsWithAny(registryAccess, merged.toImmutable(), enchantment)) {
            return false;
        }

        int mergedLevel = existingLevel <= 0
                ? level
                : BetterEnchantingBridge.usesAdditiveMerge()
                ? existingLevel + level
                : vanillaMergedLevel(existingLevel, level, enchantment.value().getMaxLevel());
        merged.set(enchantment, BetterEnchantingBridge.clampLevel(enchantment, mergedLevel));
        return true;
    }

    private static int vanillaMergedLevel(int left, int right, int maxLevel) {
        int merged = left == right ? left + 1 : Math.max(left, right);
        return Math.min(merged, maxLevel);
    }

    private static boolean conflictsWithAny(RegistryAccess registryAccess, ItemEnchantments existing, Holder<Enchantment> candidate) {
        for (Object2IntMap.Entry<Holder<Enchantment>> entry : existing.entrySet()) {
            Holder<Enchantment> current = entry.getKey();
            if (!current.equals(candidate)
                    && !Enchantment.areCompatible(current, candidate)
                    && !BetterEnchantingBridge.areFusionIngredients(registryAccess, current, candidate)) {
                return true;
            }
        }
        return false;
    }

    public static List<ItemStack> copySources(ItemStack primary, Iterable<ItemStack> requiredParts) {
        List<ItemStack> sources = new ArrayList<>();
        if (!primary.isEmpty()) {
            sources.add(primary);
        }
        for (ItemStack requiredPart : requiredParts) {
            if (!requiredPart.isEmpty()) {
                sources.add(requiredPart);
            }
        }
        return sources;
    }

    private static final class BetterEnchantingBridge {
        private static final String EFFECTIVE_BALANCE = "com.betterenchanting.config.EffectiveBalance";
        private static final String LEVEL_RULES = "com.betterenchanting.data.EnchantmentLevelRules";
        private static final String FUSION_RECIPES = "com.betterenchanting.data.EnchantmentFusionRecipes";
        private static final String LIMIT_RULES = "com.betterenchanting.data.EnchantmentLimitRules";
        private static final String TARGET_TAGS = "com.betterenchanting.world.EnchantmentTargetTags";
        private static final String BETTER_ENCHANTING_NAMESPACE = "betterenchanting";
        private static final String TARGET_TAG_PREFIX = "targets/";

        private static Boolean additiveMerge;
        private static Method clampLevel;
        private static Method clampEnchantments;
        private static Method applyFusion;
        private static Method areRecipeIngredients;
        private static Method currentEnchantmentCount;
        private static Method maxEnchantments;
        private static Method resolveTargetTags;
        private static Method syncRoutedToolEnchantments;

        private BetterEnchantingBridge() {
        }

        private static boolean usesAdditiveMerge() {
            if (additiveMerge != null) {
                return additiveMerge;
            }
            try {
                Method method = Class.forName(EFFECTIVE_BALANCE).getMethod("usesAdditiveAnvilLevelMerging");
                additiveMerge = (Boolean) method.invoke(null);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                additiveMerge = false;
            }
            return additiveMerge;
        }

        private static int clampLevel(Holder<Enchantment> enchantment, int level) {
            if (level <= 0) {
                return 0;
            }
            Method method = clampLevelMethod();
            if (method == null) {
                return Math.min(level, enchantment.value().getMaxLevel());
            }
            try {
                return (Integer) method.invoke(null, enchantment, level);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return Math.min(level, enchantment.value().getMaxLevel());
            }
        }

        private static void clampEnchantments(ItemStack stack) {
            Method method = clampEnchantmentsMethod();
            if (method == null) {
                return;
            }
            try {
                method.invoke(null, stack);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            }
        }

        private static void applyFusion(RegistryAccess registryAccess, ItemStack stack) {
            Method method = applyFusionMethod();
            if (method == null) {
                return;
            }
            try {
                method.invoke(null, registryAccess, stack);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            }
        }

        private static boolean areFusionIngredients(RegistryAccess registryAccess, Holder<Enchantment> first, Holder<Enchantment> second) {
            if (registryAccess == null) {
                return false;
            }
            Method method = areRecipeIngredientsMethod();
            if (method == null) {
                return false;
            }
            try {
                return (Boolean) method.invoke(null, registryAccess, first, second);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return false;
            }
        }

        private static boolean matchesTarget(ItemStack stack, Holder<Enchantment> enchantment) {
            List<ResourceLocation> enchantmentTargets = betterEnchantingTargetTags(enchantment);
            if (enchantmentTargets.isEmpty()) {
                return false;
            }

            List<ResourceLocation> itemTargets = resolveTargetTags(stack);
            if (itemTargets.isEmpty()) {
                return false;
            }

            for (ResourceLocation target : enchantmentTargets) {
                if (itemTargets.contains(target)) {
                    return true;
                }
            }
            return false;
        }

        private static boolean fitsCapacity(ItemStack stack) {
            Method countMethod = currentEnchantmentCountMethod();
            Method maxMethod = maxEnchantmentsMethod();
            if (countMethod == null || maxMethod == null) {
                return true;
            }
            try {
                int count = (Integer) countMethod.invoke(null, stack);
                int max = (Integer) maxMethod.invoke(null, stack);
                return count <= max;
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return true;
            }
        }

        private static List<ResourceLocation> betterEnchantingTargetTags(Holder<Enchantment> enchantment) {
            return enchantment.tags()
                    .map(TagKey::location)
                    .filter(BetterEnchantingBridge::isBetterEnchantingTargetTag)
                    .toList();
        }

        private static boolean isBetterEnchantingTargetTag(ResourceLocation tagId) {
            return BETTER_ENCHANTING_NAMESPACE.equals(tagId.getNamespace())
                    && tagId.getPath().startsWith(TARGET_TAG_PREFIX);
        }

        private static List<ResourceLocation> resolveTargetTags(ItemStack stack) {
            Method method = resolveTargetTagsMethod();
            if (method == null) {
                return List.of();
            }
            try {
                Object value = method.invoke(null, stack);
                if (value instanceof List<?> list) {
                    List<ResourceLocation> tags = new ArrayList<>(list.size());
                    for (Object entry : list) {
                        if (entry instanceof ResourceLocation tag) {
                            tags.add(tag);
                        }
                    }
                    return List.copyOf(tags);
                }
            } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            }
            return List.of();
        }

        private static Method clampLevelMethod() {
            if (clampLevel == null) {
                clampLevel = method(LEVEL_RULES, "clampLevel", Holder.class, int.class);
            }
            return clampLevel;
        }

        private static Method clampEnchantmentsMethod() {
            if (clampEnchantments == null) {
                clampEnchantments = method(LEVEL_RULES, "clampEnchantments", ItemStack.class);
            }
            return clampEnchantments;
        }

        private static Method applyFusionMethod() {
            if (applyFusion == null) {
                applyFusion = method(FUSION_RECIPES, "apply", RegistryAccess.class, ItemStack.class);
            }
            return applyFusion;
        }

        private static Method areRecipeIngredientsMethod() {
            if (areRecipeIngredients == null) {
                areRecipeIngredients = method(FUSION_RECIPES, "areRecipeIngredients", RegistryAccess.class, Holder.class, Holder.class);
            }
            return areRecipeIngredients;
        }

        private static Method currentEnchantmentCountMethod() {
            if (currentEnchantmentCount == null) {
                currentEnchantmentCount = method(LIMIT_RULES, "currentEnchantmentCount", ItemStack.class);
            }
            return currentEnchantmentCount;
        }

        private static Method maxEnchantmentsMethod() {
            if (maxEnchantments == null) {
                maxEnchantments = method(LIMIT_RULES, "maxEnchantments", ItemStack.class);
            }
            return maxEnchantments;
        }

        private static Method resolveTargetTagsMethod() {
            if (resolveTargetTags == null) {
                resolveTargetTags = method(TARGET_TAGS, "resolve", ItemStack.class);
            }
            return resolveTargetTags;
        }

        private static void syncRoutedToolEnchantments(RegistryAccess registryAccess, ItemStack stack) {
            Method method = syncRoutedToolEnchantmentsMethod();
            if (method == null) {
                return;
            }
            try {
                method.invoke(null, registryAccess, stack);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
            }
        }

        private static Method syncRoutedToolEnchantmentsMethod() {
            if (syncRoutedToolEnchantments == null) {
                syncRoutedToolEnchantments = method("com.betterenchanting.compat.MobsToolForgingCompat", "syncRoutedToolEnchantments", RegistryAccess.class, ItemStack.class);
            }
            return syncRoutedToolEnchantments;
        }

        private static Method method(String className, String name, Class<?>... parameters) {
            try {
                return Class.forName(className).getMethod(name, parameters);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException exception) {
                return null;
            }
        }
    }
}
