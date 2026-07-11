package org.destroyermob.mobstoolforging.integration.everycomp;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.level.BlockDropsEvent;

/** Restores the block item when an Every Compat block resolves to an empty loot table. */
public final class EveryCompatDropFallback {
    private EveryCompatDropFallback() {
    }

    public static void addMissingSelfDrop(BlockDropsEvent event) {
        if (!event.getDrops().isEmpty()
                || !(event.getBreaker() instanceof Player player)
                || player.isCreative()) {
            return;
        }

        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(event.getState().getBlock());
        ResourceLocation dropId = originalEveryCompatId(blockId);
        if (dropId == null) {
            return;
        }

        Item item = BuiltInRegistries.ITEM.getOptional(dropId).orElse(Items.AIR);
        if (item == Items.AIR) {
            return;
        }

        BlockPos pos = event.getPos();
        event.getDrops().add(new ItemEntity(
                event.getLevel(),
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                new ItemStack(item)
        ));
    }

    private static ResourceLocation originalEveryCompatId(ResourceLocation blockId) {
        if ("everycomp".equals(blockId.getNamespace())) {
            return blockId;
        }
        if (!"visualworkbench".equals(blockId.getNamespace())) {
            return null;
        }

        String path = blockId.getPath();
        int separator = path.indexOf('/');
        if (separator <= 0 || !"everycomp".equals(path.substring(0, separator))) {
            return null;
        }
        return ResourceLocation.fromNamespaceAndPath(
                path.substring(0, separator),
                path.substring(separator + 1)
        );
    }
}
