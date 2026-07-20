package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

public class FoundryFuelTankBlock extends BaseEntityBlock {
    public static final MapCodec<FoundryFuelTankBlock> CODEC = simpleCodec(FoundryFuelTankBlock::new);

    public FoundryFuelTankBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FoundryFuelTankBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (FluidUtil.getFluidHandler(stack).isEmpty() || !(level.getBlockEntity(pos) instanceof FoundryFuelTankBlockEntity tank)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }
        if (!FluidUtil.interactWithFluidHandler(player, hand, tank.fluidHandler())) {
            DebugFeedback.actionBar(player, Component.translatable(
                    "message.mobstoolforging.foundry_tank_rejected",
                    tank.fluidAmountMb(),
                    tank.capacityMb()
            ));
            return ItemInteractionResult.CONSUME;
        }
        DebugFeedback.actionBar(player, Component.translatable(
                "message.mobstoolforging.foundry_tank_filled",
                fluidName(tank.fluidStack()),
                tank.fluidAmountMb(),
                tank.capacityMb(),
                formatTemperature(tank.fuelTemperatureC())
        ));
        return ItemInteractionResult.CONSUME;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!(level.getBlockEntity(pos) instanceof FoundryFuelTankBlockEntity tank)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        DebugFeedback.actionBar(player, Component.translatable(
                "message.mobstoolforging.foundry_tank_status",
                fluidName(tank.fluidStack()),
                tank.fluidAmountMb(),
                tank.capacityMb(),
                formatTemperature(tank.fuelTemperatureC())
        ));
        return InteractionResult.CONSUME;
    }

    private static Component fluidName(FluidStack stack) {
        return stack.isEmpty() ? Component.translatable("message.mobstoolforging.foundry_tank_empty") : stack.getHoverName();
    }

    private static String formatTemperature(float temperature) {
        return String.format(java.util.Locale.ROOT, "%.0f°C", temperature);
    }
}
