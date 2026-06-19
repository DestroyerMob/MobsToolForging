package org.destroyermob.mobstoolforging.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;

public class CrudeFlintPickItem extends PickaxeItem {
    public CrudeFlintPickItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        if (!MobsToolForgingConfig.ENABLE_CRUDE_FLINT_TOOLS.get() || !state.is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            return false;
        }
        if (isCopperTarget(state)) {
            return MobsToolForgingConfig.FLINT_CAN_MINE_COPPER.get();
        }
        if (isIronTarget(state)) {
            return MobsToolForgingConfig.FLINT_CAN_MINE_IRON.get();
        }
        if (state.is(BlockTags.NEEDS_IRON_TOOL)
                || state.is(BlockTags.NEEDS_DIAMOND_TOOL)
                || state.is(BlockTags.LAPIS_ORES)
                || state.is(Blocks.LAPIS_BLOCK)) {
            return false;
        }
        return super.isCorrectToolForDrops(stack, state);
    }

    private static boolean isCopperTarget(BlockState state) {
        return state.is(BlockTags.COPPER_ORES)
                || state.is(Blocks.COPPER_BLOCK)
                || state.is(Blocks.RAW_COPPER_BLOCK);
    }

    private static boolean isIronTarget(BlockState state) {
        return state.is(BlockTags.IRON_ORES)
                || state.is(Blocks.IRON_BLOCK)
                || state.is(Blocks.RAW_IRON_BLOCK);
    }
}
