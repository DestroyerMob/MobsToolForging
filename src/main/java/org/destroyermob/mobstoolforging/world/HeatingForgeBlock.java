package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class HeatingForgeBlock extends BaseEntityBlock {
    public static final MapCodec<HeatingForgeBlock> CODEC = simpleCodec(HeatingForgeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 3.0, 16.0),
            Block.box(1.0, 3.0, 1.0, 15.0, 11.0, 15.0),
            Block.box(0.0, 11.0, 0.0, 16.0, 14.0, 16.0)
    ).optimize();

    public HeatingForgeBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new HeatingForgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.HEATING_FORGE.get(), HeatingForgeBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof HeatingForgeBlockEntity forge)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!MobsToolForgingConfig.ENABLE_FORGE_HEATING.get()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.heating_disabled"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            return ignite(stack, forge, level, pos, player);
        }
        if (HeatingForgeBlockEntity.fuelBurnTime(stack) > 0) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (forge.acceptFuel(stack)) {
                level.playSound(null, pos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 0.5F, 0.85F);
                player.displayClientMessage(Component.translatable("message.mobstoolforging.heating_fuel_added"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        if (HeatingForgeBlockEntity.isHeatableWorkpiece(stack)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (forge.acceptWorkpiece(stack)) {
                level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.5F, 1.1F);
                player.displayClientMessage(Component.translatable("message.mobstoolforging.heating_workpiece_added"), true);
            } else {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.heating_workpiece_busy"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        if (MaterialCatalog.isMaterial(stack)) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.heating_gems_rejected"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof HeatingForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (forge.hasWorkpiece()) {
            giveOrDrop(player, forge.removeWorkpiece());
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return InteractionResult.CONSUME;
        }
        if (player.isShiftKeyDown() && forge.hasFuel()) {
            giveOrDrop(player, forge.removeFuel());
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 0.85F);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable(forge.isLit() ? "message.mobstoolforging.heating_lit" : "message.mobstoolforging.heating_status"), true);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof HeatingForgeBlockEntity forge) {
            ItemStack workpiece = forge.workpieceDropStack();
            if (!workpiece.isEmpty()) {
                Block.popResource(level, pos, workpiece);
            }
            ItemStack fuel = forge.fuelDropStack();
            if (!fuel.isEmpty()) {
                Block.popResource(level, pos, fuel);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
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

    private static ItemInteractionResult ignite(ItemStack stack, HeatingForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!forge.hasFuel() && !forge.isLit()) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.heating_needs_fuel"), true);
            return ItemInteractionResult.CONSUME;
        }
        if (forge.ignite()) {
            if (!player.getAbilities().instabuild) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                } else {
                    stack.shrink(1);
                }
            }
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.8F, 1.0F);
            player.displayClientMessage(Component.translatable("message.mobstoolforging.heating_ignited"), true);
        }
        return ItemInteractionResult.CONSUME;
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (!stack.isEmpty() && !player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }
}
