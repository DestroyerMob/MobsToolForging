package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public class FoundryDrainBlock extends Block {
    public static final MapCodec<FoundryDrainBlock> CODEC = simpleCodec(FoundryDrainBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public FoundryDrainBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        FoundryAccess.findController(level, pos).ifPresentOrElse(forge -> {
            Component material = forge.bottomMoltenMaterial()
                    .map(MaterialCatalog::displayName)
                    .orElse(Component.translatable("message.mobstoolforging.foundry_drain_empty"));
            DebugFeedback.actionBar(player, Component.translatable(
                    "message.mobstoolforging.foundry_drain_status",
                    forge.bottomMoltenLayer().map(FoundryForgeBlockEntity.MoltenLayer::amountMb).orElse(0),
                    material
            ));
        }, () -> DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.foundry_drain_unconnected")));
        return InteractionResult.CONSUME;
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
