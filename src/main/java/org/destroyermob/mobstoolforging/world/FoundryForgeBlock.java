package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.MobsToolForging;

public class FoundryForgeBlock extends StatefulFoundryBlock {
    public static final MapCodec<FoundryForgeBlock> CODEC = simpleCodec(FoundryForgeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public FoundryForgeBlock(BlockBehaviour.Properties properties) {
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
        return new FoundryForgeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, ModBlockEntities.FOUNDRY_FORGE.get(), FoundryForgeBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof FoundryForgeBlockEntity forge) || !forge.isLit()) {
            return;
        }
        if (random.nextFloat() < 0.55F) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.5D, pos.getY() + 0.42D, pos.getZ() + 0.5D, 0.0D, 0.02D, 0.0D);
        }
        if (random.nextFloat() < 0.35F) {
            level.addParticle(ParticleTypes.LAVA, pos.getX() + 0.5D, pos.getY() + 0.55D, pos.getZ() + 0.5D, 0.0D, 0.0D, 0.0D);
        }
        if (forge.isStoked() && random.nextFloat() < 0.65F) {
            level.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + 0.5D, pos.getY() + 0.58D,
                    pos.getZ() + 0.5D, 0.0D, 0.035D, 0.0D);
        }
        if (random.nextFloat() < (forge.isStoked() ? 0.08F : 0.03F)) {
            level.playLocalSound(pos, SoundEvents.BLASTFURNACE_FIRE_CRACKLE, SoundSource.BLOCKS,
                    forge.isStoked() ? 0.75F : 0.45F, forge.isStoked() ? 1.15F : 0.9F, false);
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FoundryForgeBlockEntity forge)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.is(Items.LAVA_BUCKET)) {
            if (!level.isClientSide) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.foundry_lava_use_tank"));
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (stack.is(Items.BLAZE_POWDER)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (!forge.refreshStructure()) {
                DebugFeedback.actionBar(player, FoundryFeedback.structureDiagnostic(forge));
                return ItemInteractionResult.CONSUME;
            }
            if (!forge.stoke()) {
                DebugFeedback.actionBar(player, Component.translatable(
                        "message.mobstoolforging.foundry_stoke_full",
                        FoundryForgeBlockEntity.MAX_STOKE_TICKS / 20
                ));
                return ItemInteractionResult.CONSUME;
            }
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 0.85F, 0.8F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.FLAME,
                        pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                        18, 0.3D, 0.15D, 0.3D, 0.025D);
                serverLevel.sendParticles(ParticleTypes.LAVA,
                        pos.getX() + 0.5D, pos.getY() + 0.65D, pos.getZ() + 0.5D,
                        6, 0.25D, 0.08D, 0.25D, 0.0D);
            }
            awardStokingAdvancement(player);
            DebugFeedback.actionBar(player, Component.translatable(
                    "message.mobstoolforging.foundry_stoked",
                    forge.stokeTicksRemaining() / 20
            ));
            return ItemInteractionResult.CONSUME;
        }
        if (!FoundryForgeBlockEntity.isMeltable(stack)) {
            if (EmptyMainHandInteractions.shouldFallbackToEmptyHand(player, hand)) {
                return EmptyMainHandInteractions.itemResult(useWithoutItem(state, level, pos, player, hitResult), level);
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!forge.refreshStructure()) {
            DebugFeedback.actionBar(player, FoundryFeedback.structureDiagnostic(forge));
            return ItemInteractionResult.CONSUME;
        }
        int accepted = forge.acceptSolid(stack);
        if (accepted <= 0) {
            DebugFeedback.actionBar(player, Component.translatable(
                    "message.mobstoolforging.foundry_input_full",
                    forge.moltenAmountMb() + forge.solidReservedMb(),
                    forge.fluidCapacityMb()
            ));
        } else {
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.55F, 0.75F);
            DebugFeedback.actionBar(player, Component.translatable(
                    "message.mobstoolforging.foundry_input_added",
                    accepted,
                    forge.solidItemCount()
            ));
        }
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FoundryForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!forge.refreshStructure()) {
            DebugFeedback.actionBar(player, FoundryFeedback.structureDiagnostic(forge));
            return InteractionResult.CONSUME;
        }
        FoundryForgeBlockEntity.ConnectedFuelStatus fuel = forge.connectedFuelStatus();
        DebugFeedback.actionBar(player, Component.translatable(
                "message.mobstoolforging.foundry_inspect",
                forge.structureWidth(),
                forge.structureDepth(),
                forge.structureHeight(),
                forge.solidItemCount(),
                forge.moltenAmountMb(),
                forge.fluidCapacityMb(),
                fuel.amountMb(),
                FoundryFeedback.operatingStatus(forge, fuel)
        ));
        return InteractionResult.CONSUME;
    }

    private static void awardStokingAdvancement(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || serverPlayer.getServer() == null) {
            return;
        }
        var advancement = serverPlayer.getServer().getAdvancements().get(ResourceLocation.fromNamespaceAndPath(
                MobsToolForging.MOD_ID, "foundry/stoked"));
        if (advancement != null) {
            serverPlayer.getAdvancements().award(advancement, "stoked");
        }
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
