package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.item.ToolTemplateItem;
import org.destroyermob.mobstoolforging.network.ModNetworking;
import org.destroyermob.mobstoolforging.registry.ModItems;

public abstract class ToolWorkstationBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    private final WorkstationKind kind;

    protected ToolWorkstationBlock(BlockBehaviour.Properties properties, WorkstationKind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected abstract MapCodec<? extends BaseEntityBlock> codec();

    public WorkstationKind kind() {
        return kind;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ToolForgeBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getClockWise());
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.getItem() instanceof ToolTemplateItem templateItem) {
            return applyTemplateItem(templateItem, forge, level, pos, player);
        }
        if (player.isShiftKeyDown()) {
            if (debugTemplateSelectorEnabled()) {
                openTemplateSelector(level, pos, player);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.sneak_hint"), true);
            }
            return ItemInteractionResult.CONSUME;
        }
        if (tryCollectOutput(forge, player)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        if (MaterialCatalog.isMaterial(stack)) {
            return placeMaterial(stack, forge, level, pos, player);
        }
        if (stack.is(ModItems.SMITHING_HAMMER.get())) {
            return work(stack, forge, level, pos, player);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            if (level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge && forge.template() != null) {
                if (forge.canChangeTemplate()) {
                    if (!level.isClientSide && forge.clearTemplate()) {
                        level.playSound(null, pos, SoundEvents.ITEM_FRAME_REMOVE_ITEM, SoundSource.BLOCKS, 0.6F, 1.15F);
                        player.displayClientMessage(Component.translatable("message.mobstoolforging.template_cleared"), true);
                    }
                    return InteractionResult.sidedSuccess(level.isClientSide);
                }
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.forge_busy"), true);
                }
                return InteractionResult.CONSUME;
            }
            if (debugTemplateSelectorEnabled()) {
                openTemplateSelector(level, pos, player);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.CONSUME;
        }
        if (level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge) {
            if (tryCollectOutput(forge, player)) {
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            if (!level.isClientSide) {
                if (forge.template() == null) {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.select_template"), true);
                } else {
                    player.displayClientMessage(Component.translatable("message.mobstoolforging.station_status", forge.materialCount(), forge.template().requiredMaterials(), forge.hitCount(), forge.template().requiredHits()), true);
                }
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private ItemInteractionResult applyTemplateItem(ToolTemplateItem templateItem, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (!templateItem.canUseOn(kind)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!forge.setTemplateFromItem(templateItem.template())) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.forge_busy"), true);
            return ItemInteractionResult.CONSUME;
        }
        level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.75F, 1.1F + level.random.nextFloat() * 0.1F);
        player.displayClientMessage(Component.translatable("message.mobstoolforging.template_selected", templateItem.template().displayName()), true);
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult placeMaterial(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (forge.template() == null) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.select_template"), true);
            return ItemInteractionResult.CONSUME;
        }
        ToolMaterialDefinition material = MaterialCatalog.resolve(stack).orElse(null);
        if (material == null) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (material.category() != kind.materialCategory()) {
            player.displayClientMessage(Component.translatable(kind.wrongStationMessage()), true);
            return ItemInteractionResult.CONSUME;
        }
        if (forge.materialId() != null && !forge.materialId().equals(material.id())) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.mixed_materials"), true);
            return ItemInteractionResult.CONSUME;
        }
        int taken = forge.acceptMaterials(stack, material);
        if (taken > 0) {
            level.playSound(null, pos, kind.placeSound(), SoundSource.BLOCKS, 0.8F, 0.9F + level.random.nextFloat() * 0.15F);
            player.awardStat(Stats.ITEM_USED.get(material.displayItem()));
            return ItemInteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable("message.mobstoolforging.materials_full"), true);
        return ItemInteractionResult.CONSUME;
    }

    private ItemInteractionResult work(ItemStack stack, ToolForgeBlockEntity forge, Level level, BlockPos pos, Player player) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!forge.canHammer()) {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.need_materials"), true);
            return ItemInteractionResult.CONSUME;
        }
        if (forge.hammer()) {
            if (!player.getAbilities().instabuild) {
                stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
            }
            level.playSound(null, pos, kind.workSound(), SoundSource.BLOCKS, 0.45F, 1.1F + level.random.nextFloat() * 0.2F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(kind.workParticle(), pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 8, 0.18, 0.05, 0.18, 0.02);
            }
            if (forge.isComplete()) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.complete"), true);
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    private static boolean tryCollectOutput(ToolForgeBlockEntity forge, Player player) {
        if (!forge.isComplete()) {
            return false;
        }
        if (player.level().isClientSide) {
            return true;
        }
        ItemStack output = forge.outputStack();
        if (player.getInventory().add(output)) {
            forge.removeOutput();
            player.level().playSound(null, forge.getBlockPos(), net.minecraft.sounds.SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
        } else {
            player.displayClientMessage(Component.translatable("message.mobstoolforging.inventory_full"), true);
        }
        return true;
    }

    private static void openTemplateSelector(Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            ModNetworking.openTemplateSelector(serverPlayer, pos);
        }
    }

    private static boolean debugTemplateSelectorEnabled() {
        return MobsToolForgingConfig.DEBUG_TEMPLATE_SELECTOR.get();
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ToolForgeBlockEntity forge) {
            if (forge.isComplete()) {
                Block.popResource(level, pos, forge.outputStack());
            } else {
                ItemStack materialDrop = forge.materialDropStack();
                if (!materialDrop.isEmpty()) {
                    Block.popResource(level, pos, materialDrop);
                }
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeForState(state);
    }

    protected abstract VoxelShape shapeForState(BlockState state);

    @Override
    protected BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
}
