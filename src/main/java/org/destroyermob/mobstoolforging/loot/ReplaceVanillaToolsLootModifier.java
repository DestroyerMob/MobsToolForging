package org.destroyermob.mobstoolforging.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModLootModifiers;
import org.destroyermob.mobstoolforging.world.MaterialCatalog;
import org.destroyermob.mobstoolforging.world.VanillaToolConverter;

public class ReplaceVanillaToolsLootModifier extends LootModifier {
    public static final MapCodec<ReplaceVanillaToolsLootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
            codecStart(instance).apply(instance, ReplaceVanillaToolsLootModifier::new)
    );

    public ReplaceVanillaToolsLootModifier(LootItemCondition[] conditions) {
        super(conditions);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!MobsToolForgingConfig.CONVERT_VANILLA_LOOT_TO_MODULAR_TOOLS.get()) {
            return generatedLoot;
        }

        ResourceLocation handleMaterial = handleMaterial(context);
        for (int i = 0; i < generatedLoot.size(); i++) {
            ItemStack converted = VanillaToolConverter.convert(generatedLoot.get(i), handleMaterial);
            if (!converted.isEmpty()) {
                generatedLoot.set(i, converted);
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return ModLootModifiers.REPLACE_VANILLA_TOOLS.get();
    }

    private static ResourceLocation handleMaterial(LootContext context) {
        ResourceLocation lootTable = context.getQueriedLootTableId();
        if (lootTable.getPath().contains("trial_chamber")) {
            return MaterialCatalog.BREEZE;
        }
        if (context.getLevel().dimension() == Level.NETHER) {
            return MaterialCatalog.BLAZE;
        }
        return MaterialCatalog.OAK;
    }
}
