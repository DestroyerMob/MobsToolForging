package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class CrucibleBlock extends BaseEntityBlock {
    public static final MapCodec<CrucibleBlock> CODEC = simpleCodec(CrucibleBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            Block.box(2.0D, 0.0D, 2.0D, 14.0D, 2.0D, 14.0D),
            Block.box(2.0D, 2.0D, 2.0D, 14.0D, 13.0D, 4.0D),
            Block.box(2.0D, 2.0D, 12.0D, 14.0D, 13.0D, 14.0D),
            Block.box(2.0D, 2.0D, 4.0D, 4.0D, 13.0D, 12.0D),
            Block.box(12.0D, 2.0D, 4.0D, 14.0D, 13.0D, 12.0D),
            Block.box(1.0D, 13.0D, 1.0D, 15.0D, 15.0D, 15.0D)
    ).optimize();

    public CrucibleBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrucibleBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return null;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        CrucibleContents contents = stack.get(ModDataComponents.CRUCIBLE_CONTENTS.get());
        if (contents != null && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible) {
            crucible.setContents(contents);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        CrucibleContents contents = crucible.contents();
        if (EmptyMainHandInteractions.shouldFallbackToEmptyHand(player, hand) && !canUseItem(stack, crucible, contents)) {
            return EmptyMainHandInteractions.itemResult(useWithoutItem(state, level, pos, player, hitResult), level);
        }

        if (contents.moltenMaterial().filter(MaterialCatalog.NETHERITE::equals).isPresent() && canTip(stack)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            ItemStack tipped = tippedCopy(stack);
            if (tipped.isEmpty()) {
                return ItemInteractionResult.CONSUME;
            }
            boolean creative = player.getAbilities().instabuild;
            if (creative || stack.getCount() <= 1) {
                player.setItemInHand(hand, tipped);
            } else {
                ItemStack remainder = stack.copy();
                remainder.setCount(stack.getCount() - 1);
                player.setItemInHand(hand, tipped);
                if (!player.getInventory().add(remainder)) {
                    player.drop(remainder, false);
                }
            }
            if (!creative) {
                crucible.setContents(contents.consumeMoltenUnit());
            }
            level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1.4F);
            player.displayClientMessage(Component.translatable("message.mobstoolforging.crucible_part_tipped"), true);
            return ItemInteractionResult.CONSUME;
        }

        if (!stack.isEmpty() && crucible.isEmpty()) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (crucible.acceptItem(stack)) {
                level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.5F, 1.15F);
                player.displayClientMessage(Component.translatable("message.mobstoolforging.crucible_item_inserted"), true);
            }
            return ItemInteractionResult.CONSUME;
        }

        if (!stack.isEmpty() && contents.hasMoltenMaterial()) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable("message.mobstoolforging.crucible_molten_not_usable"), true);
            }
            return ItemInteractionResult.CONSUME;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        ItemStack removed = crucible.removeItem();
        if (!removed.isEmpty()) {
            if (!player.getInventory().add(removed)) {
                player.drop(removed, false);
            }
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            return InteractionResult.CONSUME;
        }
        player.displayClientMessage(Component.translatable(statusKey(crucible.contents())), true);
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CrucibleBlockEntity crucible && !level.isClientSide) {
            Block.popResource(level, pos, crucible.asItemStack());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static boolean canUseItem(ItemStack stack, CrucibleBlockEntity crucible, CrucibleContents contents) {
        if (stack.isEmpty()) {
            return false;
        }
        if (contents.moltenMaterial().filter(MaterialCatalog.NETHERITE::equals).isPresent() && canTip(stack)) {
            return !tippedCopy(stack).isEmpty();
        }
        return crucible.isEmpty() && !stack.is(ModItems.CRUCIBLE.get());
    }

    private static boolean canTip(ItemStack stack) {
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        return data != null
                && data.treatment().isEmpty()
                && java.util.Arrays.stream(ToolKind.values()).anyMatch(toolKind -> toolKind.partType().equals(data.partType()));
    }

    private static ItemStack tippedCopy(ItemStack stack) {
        ToolPartData data = stack.get(ModDataComponents.TOOL_PART.get());
        if (data == null || data.treatment().isPresent()) {
            return ItemStack.EMPTY;
        }
        ItemStack tipped = stack.copyWithCount(1);
        tipped.set(ModDataComponents.TOOL_PART.get(), data.withTreatment(MaterialCatalog.NETHERITE));
        return tipped;
    }

    private static String statusKey(CrucibleContents contents) {
        if (contents.hasMoltenMaterial()) {
            return contents.isWhiteHot()
                    ? "message.mobstoolforging.crucible_status_molten_hot"
                    : "message.mobstoolforging.crucible_status_molten";
        }
        if (contents.hasItem()) {
            return "message.mobstoolforging.crucible_status_item";
        }
        return "message.mobstoolforging.crucible_status_empty";
    }
}
