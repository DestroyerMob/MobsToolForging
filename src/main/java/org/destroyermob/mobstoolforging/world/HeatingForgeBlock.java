package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
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
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModItems;

public class HeatingForgeBlock extends BaseEntityBlock {
    public static final MapCodec<HeatingForgeBlock> CODEC = simpleCodec(HeatingForgeBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            Block.box(0.0, 2.0, 14.0, 14.0, 16.0, 16.0),
            Block.box(2.0, 2.0, 0.0, 16.0, 16.0, 2.0),
            Block.box(14.0, 2.0, 2.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 8.0, 0.0, 2.0, 16.0, 14.0),
            Block.box(0.0, 2.0, 0.0, 2.0, 8.0, 2.0),
            Block.box(0.0, 2.0, 2.0, 2.0, 4.0, 14.0),
            Block.box(2.0, 11.0, 2.0, 14.0, 12.0, 14.0)
    ).optimize();
    private static final VoxelShape EAST_SHAPE = rotateClockwise(NORTH_SHAPE);
    private static final VoxelShape SOUTH_SHAPE = rotateClockwise(EAST_SHAPE);
    private static final VoxelShape WEST_SHAPE = rotateClockwise(SOUTH_SHAPE);
    private static final double[] WORKPIECE_SLOT_X = {-0.21875D, -0.15625D, 0.15625D, 0.21875D};
    private static final double[] WORKPIECE_SLOT_Z = {-0.15625D, 0.21875D, -0.21875D, 0.15625D};
    private static final double ASH_TRAY_CLICK_MAX_Y = 0.5D;

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
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof HeatingForgeBlockEntity forge) || !forge.isLit()) {
            return;
        }
        if (random.nextFloat() < 0.75F) {
            level.addParticle(
                    ParticleTypes.WHITE_SMOKE,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.22D,
                    pos.getY() + 0.9D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.22D,
                    (random.nextDouble() - 0.5D) * 0.015D,
                    0.055D + random.nextDouble() * 0.025D,
                    (random.nextDouble() - 0.5D) * 0.015D
            );
        }
        if (random.nextFloat() < 0.25F) {
            level.addParticle(
                    ParticleTypes.SMALL_FLAME,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.16D,
                    pos.getY() + 0.78D,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.16D,
                    0.0D,
                    0.01D,
                    0.0D
            );
        }
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
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_disabled"));
            }
            return ItemInteractionResult.CONSUME;
        }
        if (EmptyMainHandInteractions.shouldFallbackToEmptyHand(player, hand)
                && !canHandleItem(stack, forge, player, state, pos, hitResult)) {
            return EmptyMainHandInteractions.itemResult(useWithoutItem(state, level, pos, player, hitResult), level);
        }
        if (forge instanceof LavaHeatingForgeBlockEntity fluidForge) {
            return useFluidForgeItem(stack, state, level, pos, player, hand, hitResult, fluidForge);
        }
        if (isFireStick(stack)) {
            return hasFireSticksInBothHands(player) ? ignite(stack, forge, level, pos, player) : ItemInteractionResult.CONSUME;
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
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_fuel_added"));
            } else {
                DebugFeedback.actionBar(player, heatingFuelRejectedMessage(forge));
            }
            return ItemInteractionResult.CONSUME;
        }
        if (HeatingForgeBlockEntity.isHeatableWorkpiece(stack)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            int slot = workpieceSlotFromHit(state, pos, hitResult);
            float temperature = WorkpieceHeat.temperature(stack, level);
            if (forge.acceptWorkpiece(stack, slot)) {
                level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.5F, 1.1F);
                HeatingInteractionEffects.movedHotMetal(level, pos, temperature);
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_workpiece_added"));
            } else {
                DebugFeedback.actionBar(player, heatingWorkpieceRejectedMessage(forge, slot));
            }
            return ItemInteractionResult.CONSUME;
        }
        if (MaterialCatalog.isMaterial(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof HeatingForgeBlockEntity forge)) {
            return InteractionResult.PASS;
        }
        if (!player.isShiftKeyDown() && EmptyMainHandInteractions.shouldDeferToOffhand(
                player,
                stack -> canHandleItem(stack, forge, player, state, pos, hitResult)
        )) {
            return InteractionResult.PASS;
        }
        if (forge instanceof LavaHeatingForgeBlockEntity fluidForge) {
            return useFluidForgeWithoutItem(state, level, pos, player, hitResult, fluidForge);
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            return clearSelectedResidue(forge, level, pos, player, cleanupTargetFromHit(pos, hitResult), true);
        }
        int slot = workpieceSlotFromHit(state, pos, hitResult);
        if (!forge.workpieceStack(slot).isEmpty()) {
            ItemStack removed = forge.removeWorkpiece(slot);
            float temperature = WorkpieceHeat.temperature(removed, level);
            giveOrDrop(player, removed);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            HeatingInteractionEffects.movedHotMetal(level, pos, temperature);
            return InteractionResult.CONSUME;
        }
        InteractionResult residueResult = clearSelectedResidue(forge, level, pos, player, cleanupTargetFromHit(pos, hitResult), false);
        if (residueResult.consumesAction()) {
            return residueResult;
        }
        DebugFeedback.actionBar(player, Component.translatable(forge.isLit() ? "message.mobstoolforging.heating_lit" : "message.mobstoolforging.heating_status"));
        return InteractionResult.CONSUME;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof HeatingForgeBlockEntity forge) {
            for (ItemStack workpiece : forge.workpieceDropStacks()) {
                Block.popResource(level, pos, workpiece);
            }
            ItemStack fuel = forge.fuelDropStack();
            if (!fuel.isEmpty()) {
                Block.popResource(level, pos, fuel);
            }
            ItemStack ash = forge.ashDropStack();
            if (!ash.isEmpty()) {
                Block.popResource(level, pos, ash);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        // The authored model's open/work side points west before blockstate rotation.
        return switch (state.getValue(FACING)) {
            case EAST -> SOUTH_SHAPE;
            case SOUTH -> WEST_SHAPE;
            case WEST -> NORTH_SHAPE;
            default -> EAST_SHAPE;
        };
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
        if (forge.hasSpentFuelBed()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_clear_embers"));
            return ItemInteractionResult.CONSUME;
        }
        if (forge.ashTrayFull()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_ash_full"));
            return ItemInteractionResult.CONSUME;
        }
        if (!forge.isFuelBedFull() && !forge.isLit()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_needs_full_fuel_bed"));
            return ItemInteractionResult.CONSUME;
        }
        if (forge.ignite()) {
            if (!player.getAbilities().instabuild) {
                if (stack.is(Items.FLINT_AND_STEEL)) {
                    stack.hurtAndBreak(1, player, EquipmentSlot.MAINHAND);
                } else if (isFireStick(stack)) {
                    consumeFireSticks(player);
                } else {
                    stack.shrink(1);
                }
            }
            level.playSound(null, pos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 0.8F, 1.0F);
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_ignited"));
        }
        return ItemInteractionResult.CONSUME;
    }

    private static Component heatingFuelRejectedMessage(HeatingForgeBlockEntity forge) {
        if (forge.hasSpentFuelBed()) {
            return Component.translatable("message.mobstoolforging.heating_clear_embers");
        }
        if (forge.ashTrayFull()) {
            return Component.translatable("message.mobstoolforging.heating_ash_full");
        }
        if (forge.isFuelBedFull()) {
            return Component.translatable("message.mobstoolforging.heating_fuel_full");
        }
        return Component.translatable("message.mobstoolforging.heating_fuel_rejected");
    }

    private static Component heatingWorkpieceRejectedMessage(HeatingForgeBlockEntity forge, int slot) {
        if (forge instanceof LavaHeatingForgeBlockEntity fluidForge) {
            if (!fluidForge.hasFuel()) {
                return Component.translatable("message.mobstoolforging.lava_heating_needs_fluid");
            }
            if (!fluidForge.workpieceStack(slot).isEmpty()) {
                return Component.translatable("message.mobstoolforging.heating_workpiece_slot_busy");
            }
            return Component.translatable("message.mobstoolforging.heating_workpiece_busy");
        }
        if (forge.hasSpentFuelBed()) {
            return Component.translatable("message.mobstoolforging.heating_clear_embers");
        }
        if (forge.ashTrayFull()) {
            return Component.translatable("message.mobstoolforging.heating_ash_full");
        }
        if (!forge.isFuelBedFull()) {
            return Component.translatable("message.mobstoolforging.heating_needs_full_fuel_bed");
        }
        if (!forge.workpieceStack(slot).isEmpty()) {
            return Component.translatable("message.mobstoolforging.heating_workpiece_slot_busy");
        }
        return Component.translatable("message.mobstoolforging.heating_workpiece_busy");
    }

    private static int workpieceSlotFromHit(BlockState state, BlockPos pos, BlockHitResult hitResult) {
        double hitX = hitResult.getLocation().x - pos.getX() - 0.5D;
        double hitZ = hitResult.getLocation().z - pos.getZ() - 0.5D;
        double radians = Math.toRadians(modelRotationDegrees(state.getValue(FACING)));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        int closestSlot = 0;
        double closestDistance = Double.MAX_VALUE;
        for (int slot = 0; slot < WORKPIECE_SLOT_X.length; slot++) {
            double slotX = rotatedX(WORKPIECE_SLOT_X[slot], WORKPIECE_SLOT_Z[slot], cos, sin);
            double slotZ = rotatedZ(WORKPIECE_SLOT_X[slot], WORKPIECE_SLOT_Z[slot], cos, sin);
            double dx = hitX - slotX;
            double dz = hitZ - slotZ;
            double distance = dx * dx + dz * dz;
            if (distance < closestDistance) {
                closestDistance = distance;
                closestSlot = slot;
            }
        }
        return closestSlot;
    }

    private static InteractionResult clearSelectedResidue(
            HeatingForgeBlockEntity forge,
            Level level,
            BlockPos pos,
            Player player,
            CleanupTarget target,
            boolean requireSelection
    ) {
        if (target == CleanupTarget.ASH) {
            if (forge.hasAsh()) {
                giveOrDrop(player, forge.collectAsh());
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 0.75F);
                return InteractionResult.CONSUME;
            }
            if (requireSelection || forge.hasSpentFuelBed() || forge.hasFuel()) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_no_ash"));
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        if (forge.hasSpentFuelBed()) {
            forge.clearSpentFuelBed();
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 0.45F, 0.65F);
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_embers_cleared"));
            return InteractionResult.CONSUME;
        }
        if (forge.hasFuel()) {
            if (forge.isLit()) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_fuel_burning"));
                return InteractionResult.CONSUME;
            }
            giveOrDrop(player, forge.removeFuel());
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 0.85F);
            return InteractionResult.CONSUME;
        }
        if (requireSelection || forge.hasAsh()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_no_cinder"));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private static CleanupTarget cleanupTargetFromHit(BlockPos pos, BlockHitResult hitResult) {
        double hitY = hitResult.getLocation().y - pos.getY();
        return hitY < ASH_TRAY_CLICK_MAX_Y ? CleanupTarget.ASH : CleanupTarget.FUEL_BED;
    }

    private static double rotatedX(double x, double z, double cos, double sin) {
        return x * cos + z * sin;
    }

    private static double rotatedZ(double x, double z, double cos, double sin) {
        return -x * sin + z * cos;
    }

    private static int modelRotationDegrees(Direction direction) {
        // Matches the authored forge model rotation used by the blockstate and renderer.
        return switch (direction) {
            case EAST -> 180;
            case SOUTH -> 270;
            case WEST -> 0;
            default -> 90;
        };
    }

    private static boolean isFireStick(ItemStack stack) {
        return stack.is(ModItems.FIRE_STICK.get());
    }

    private static boolean canHandleItem(ItemStack stack, HeatingForgeBlockEntity forge, Player player,
                                         BlockState state, BlockPos pos, BlockHitResult hitResult) {
        if (forge instanceof LavaHeatingForgeBlockEntity fluidForge) {
            return FluidUtil.getFluidHandler(stack).isPresent()
                    || HeatingForgeBlockEntity.isHeatableWorkpiece(stack)
                    && fluidForge.canAcceptWorkpiece(stack, workpieceSlotFromHit(state, pos, hitResult));
        }
        if (isFireStick(stack)) {
            return hasFireSticksInBothHands(player);
        }
        if (stack.is(Items.FLINT_AND_STEEL) || stack.is(Items.FIRE_CHARGE)) {
            return true;
        }
        if (HeatingForgeBlockEntity.fuelBurnTime(stack) > 0) {
            return forge.canAcceptFuel(stack);
        }
        return HeatingForgeBlockEntity.isHeatableWorkpiece(stack)
                && forge.canAcceptWorkpiece(stack, workpieceSlotFromHit(state, pos, hitResult));
    }

    private static ItemInteractionResult useFluidForgeItem(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult,
            LavaHeatingForgeBlockEntity forge
    ) {
        if (FluidUtil.getFluidHandler(stack).isPresent()) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            FluidStack before = forge.fluidStack();
            int beforeAmount = forge.fluidAmount();
            if (!FluidUtil.interactWithFluidHandler(player, hand, forge.fluidHandler(hitResult.getDirection()))) {
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lava_heating_fluid_rejected"));
                return ItemInteractionResult.CONSUME;
            }

            FluidStack after = forge.fluidStack();
            boolean added = forge.fluidAmount() > beforeAmount;
            FluidStack soundFluid = added ? after : before;
            SoundEvent sound = soundFluid.isEmpty() ? null : soundFluid.getFluidType().getSound(
                    soundFluid,
                    added ? SoundActions.BUCKET_EMPTY : SoundActions.BUCKET_FILL
            );
            level.playSound(
                    null,
                    pos,
                    sound == null ? (added ? SoundEvents.BUCKET_EMPTY : SoundEvents.BUCKET_FILL) : sound,
                    SoundSource.BLOCKS,
                    0.8F,
                    1.0F
            );
            FluidStack displayFluid = after.isEmpty() ? before : after;
            DebugFeedback.actionBar(player, Component.translatable(
                    added ? "message.mobstoolforging.lava_heating_fluid_added" : "message.mobstoolforging.lava_heating_fluid_removed",
                    displayFluid.getHoverName(),
                    forge.fluidAmount(),
                    forge.tankCapacity()
            ));
            return ItemInteractionResult.CONSUME;
        }

        if (HeatingForgeBlockEntity.isHeatableWorkpiece(stack)) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            int slot = workpieceSlotFromHit(state, pos, hitResult);
            float temperature = WorkpieceHeat.temperature(stack, level);
            if (forge.acceptWorkpiece(stack, slot)) {
                level.playSound(null, pos, SoundEvents.CHAIN_PLACE, SoundSource.BLOCKS, 0.5F, 1.1F);
                HeatingInteractionEffects.movedHotMetal(level, pos, temperature);
                DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.heating_workpiece_added"));
            } else {
                DebugFeedback.actionBar(player, heatingWorkpieceRejectedMessage(forge, slot));
            }
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static InteractionResult useFluidForgeWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult,
            LavaHeatingForgeBlockEntity forge
    ) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        int slot = workpieceSlotFromHit(state, pos, hitResult);
        if (!forge.workpieceStack(slot).isEmpty()) {
            ItemStack removed = forge.removeWorkpiece(slot);
            float temperature = WorkpieceHeat.temperature(removed, level);
            giveOrDrop(player, removed);
            level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5F, 1.0F);
            HeatingInteractionEffects.movedHotMetal(level, pos, temperature);
            return InteractionResult.CONSUME;
        }
        if (forge.fluidStack().isEmpty()) {
            DebugFeedback.actionBar(player, Component.translatable("message.mobstoolforging.lava_heating_status_empty"));
        } else {
            DebugFeedback.actionBar(player, Component.translatable(
                    forge.isLit() ? "message.mobstoolforging.lava_heating_status_active" : "message.mobstoolforging.lava_heating_status",
                    forge.fluidStack().getHoverName(),
                    forge.fluidAmount(),
                    forge.tankCapacity()
            ));
        }
        return InteractionResult.CONSUME;
    }

    private static boolean hasFireSticksInBothHands(Player player) {
        return player.getItemInHand(InteractionHand.MAIN_HAND).is(ModItems.FIRE_STICK.get())
                && player.getItemInHand(InteractionHand.OFF_HAND).is(ModItems.FIRE_STICK.get());
    }

    private static void consumeFireSticks(Player player) {
        player.getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
        player.getItemInHand(InteractionHand.OFF_HAND).shrink(1);
    }

    private static void giveOrDrop(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (WorkpieceHeat.hasHeat(stack)) {
            int freeSlot = player.getInventory().getFreeSlot();
            if (freeSlot >= 0) {
                player.getInventory().setItem(freeSlot, stack);
                return;
            }
            player.drop(stack, false);
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static VoxelShape rotateClockwise(VoxelShape shape) {
        VoxelShape[] rotated = {Shapes.empty()};
        shape.toAabbs().forEach(box -> rotated[0] = Shapes.or(
                rotated[0],
                Shapes.box(1.0D - box.maxZ, box.minY, box.minX, 1.0D - box.minZ, box.maxY, box.maxX)
        ));
        return rotated[0].optimize();
    }

    private enum CleanupTarget {
        ASH,
        FUEL_BED
    }
}
