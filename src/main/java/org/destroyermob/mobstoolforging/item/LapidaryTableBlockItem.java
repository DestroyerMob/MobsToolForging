package org.destroyermob.mobstoolforging.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import org.destroyermob.mobstoolforging.world.LapidaryTableBlock;

public class LapidaryTableBlockItem extends BlockItem {
    public LapidaryTableBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected boolean placeBlock(BlockPlaceContext context, BlockState state) {
        if (!(getBlock() instanceof LapidaryTableBlock) || !super.placeBlock(context, state)) {
            return false;
        }
        Level level = context.getLevel();
        BlockPos otherPos = context.getClickedPos().relative(LapidaryTableBlock.otherHalfDirection(state));
        level.setBlock(otherPos, state.setValue(LapidaryTableBlock.PART, BedPart.HEAD), Block.UPDATE_ALL);
        return true;
    }
}
