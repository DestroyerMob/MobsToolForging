package org.destroyermob.mobstoolforging.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.destroyermob.mobstoolforging.world.LeatherStationBlock;

public class LeatherStationBlockItem extends BlockItem {
    public LeatherStationBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        return getBlock() instanceof LeatherStationBlock ? super.place(context) : InteractionResult.FAIL;
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!(getBlock() instanceof LeatherStationBlock) || !super.placeBlock(context, state)) {
            return false;
        }
        Level level = context.getLevel();
        BlockPos otherPos = context.getClickedPos().relative(LeatherStationBlock.otherHalfDirection(state));
        level.setBlock(otherPos, state.setValue(LeatherStationBlock.PART, BedPart.HEAD), Block.UPDATE_ALL);
        return true;
    }
}
