package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.CampfireBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.mixin.CampfireBlockEntityAccessor;

public final class CampfireWorkpieceHeating {
    private static final float WORKABLE_EPSILON = 0.005F;

    private CampfireWorkpieceHeating() {
    }

    public static void placeWorkpiece(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getLevel();
        ItemStack stack = event.getItemStack();
        if (!canCampfireHeat(stack) || !isLitCampfire(level, event.getPos()) || !(level.getBlockEntity(event.getPos()) instanceof CampfireBlockEntity campfire)) {
            return;
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        if (level.isClientSide) {
            return;
        }

        if (insertWorkpiece(campfire, event.getEntity(), stack, level, event.getPos())) {
            event.getEntity().setItemInHand(event.getHand(), stack);
            event.getEntity().displayClientMessage(Component.translatable("message.mobstoolforging.campfire_workpiece_placed"), true);
        } else {
            event.getEntity().displayClientMessage(Component.translatable("message.mobstoolforging.campfire_workpiece_full"), true);
        }
    }

    public static boolean insertDroppedWorkpiece(ItemEntity itemEntity) {
        Level level = itemEntity.level();
        if (level.isClientSide) {
            return false;
        }

        ItemStack stack = itemEntity.getItem();
        if (!canCampfireHeat(stack)) {
            return false;
        }

        BlockPos campfirePos = heatingCampfirePos(itemEntity);
        if (campfirePos == null || !(level.getBlockEntity(campfirePos) instanceof CampfireBlockEntity campfire)) {
            return false;
        }

        if (!insertWorkpiece(campfire, null, stack, level, campfirePos)) {
            return false;
        }
        if (stack.isEmpty()) {
            itemEntity.discard();
        } else {
            itemEntity.setItem(stack);
            itemEntity.setPickUpDelay(10);
        }
        return true;
    }

    public static void warmCampfireSlots(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        if (level.isClientSide || !isLitCampfire(state) || !MobsToolForgingConfig.ENABLE_FORGE_HEATING.get() || !MobsToolForgingConfig.ENABLE_CAMPFIRE_LOW_HEAT.get()) {
            return;
        }

        boolean heated = false;
        boolean released = false;
        NonNullList<ItemStack> items = campfire.getItems();
        for (int slot = 0; slot < items.size(); slot++) {
            ItemStack stack = items.get(slot);
            Optional<HeatingRecipe> recipe = HeatingRecipeRegistry.find(HeatingSource.CAMPFIRE, stack);
            if (recipe.isEmpty() || recipe.get().targetTemperature() <= 0.0F) {
                continue;
            }
            float targetTemperature = recipe.get().targetTemperature();
            holdVanillaCampfireTimer(campfire, slot, recipe.get().ticks());
            float currentTemperature = WorkpieceHeat.temperature(stack, level);
            float nextTemperature = Math.min(targetTemperature, currentTemperature + temperatureStep(targetTemperature, recipe.get().ticks()));
            boolean reachedCampfireTarget = nextTemperature >= targetTemperature - WORKABLE_EPSILON;
            boolean workable = WorkpieceHeat.isWorkable(stack) || (reachedCampfireTarget && recipe.get().workable());
            WorkpieceHeat.setTemperature(stack, level, reachedCampfireTarget ? targetTemperature : nextTemperature, workable);
            heated = true;
            if (reachedCampfireTarget) {
                releaseHeatedWorkpiece(level, pos, campfire, items, slot, stack, recipe.get());
                released = true;
            }
        }
        if (heated) {
            campfire.setChanged();
            campfireHeatEffects(level, pos);
        }
        if (released) {
            markCampfireUpdated(level, pos, state, campfire);
        }
    }

    private static boolean insertWorkpiece(CampfireBlockEntity campfire, @Nullable LivingEntity entity, ItemStack stack, Level level, BlockPos pos) {
        Optional<HeatingRecipe> recipe = HeatingRecipeRegistry.find(HeatingSource.CAMPFIRE, stack);
        if (recipe.isPresent() && campfire.placeFood(entity, stack, recipe.get().ticks())) {
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ADD_ITEM, SoundSource.BLOCKS, 0.55F, 1.35F);
            return true;
        }
        return false;
    }

    private static boolean canCampfireHeat(ItemStack stack) {
        return MobsToolForgingConfig.ENABLE_FORGE_HEATING.get()
                && MobsToolForgingConfig.ENABLE_CAMPFIRE_LOW_HEAT.get()
                && HeatingRecipeRegistry.isHeatable(HeatingSource.CAMPFIRE, stack);
    }

    @Nullable
    private static BlockPos heatingCampfirePos(ItemEntity itemEntity) {
        Level level = itemEntity.level();
        BlockPos base = BlockPos.containing(itemEntity.getX(), itemEntity.getY() - 0.08D, itemEntity.getZ());
        if (isLitCampfire(level, base)) {
            return base;
        }
        if (isLitCampfire(level, base.below())) {
            return base.below();
        }

        BlockPos blockPos = itemEntity.blockPosition();
        if (isLitCampfire(level, blockPos)) {
            return blockPos;
        }
        BlockPos below = blockPos.below();
        return isLitCampfire(level, below) ? below : null;
    }

    private static boolean isLitCampfire(Level level, BlockPos pos) {
        return isLitCampfire(level.getBlockState(pos));
    }

    private static boolean isLitCampfire(BlockState state) {
        return state.getBlock() instanceof CampfireBlock
                && state.hasProperty(CampfireBlock.LIT)
                && state.getValue(CampfireBlock.LIT);
    }

    private static void holdVanillaCampfireTimer(CampfireBlockEntity campfire, int slot, int ticks) {
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor) campfire;
        int[] cookingProgress = accessor.mobstoolforging$cookingProgress();
        int[] cookingTime = accessor.mobstoolforging$cookingTime();
        if (slot < cookingProgress.length) {
            cookingProgress[slot] = 0;
        }
        if (slot < cookingTime.length) {
            cookingTime[slot] = ticks;
        }
    }

    private static void resetVanillaCampfireTimer(CampfireBlockEntity campfire, int slot) {
        CampfireBlockEntityAccessor accessor = (CampfireBlockEntityAccessor) campfire;
        int[] cookingProgress = accessor.mobstoolforging$cookingProgress();
        int[] cookingTime = accessor.mobstoolforging$cookingTime();
        if (slot < cookingProgress.length) {
            cookingProgress[slot] = 0;
        }
        if (slot < cookingTime.length) {
            cookingTime[slot] = 0;
        }
    }

    private static void releaseHeatedWorkpiece(Level level, BlockPos pos, CampfireBlockEntity campfire, NonNullList<ItemStack> items, int slot, ItemStack stack, HeatingRecipe recipe) {
        WorkpieceHeat.setTemperature(stack, level, recipe.targetTemperature(), recipe.workable());
        ItemStack dropStack = stack.copy();
        items.set(slot, ItemStack.EMPTY);
        resetVanillaCampfireTimer(campfire, slot);
        Containers.dropItemStack(level, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, dropStack);
    }

    private static void markCampfireUpdated(Level level, BlockPos pos, BlockState state, CampfireBlockEntity campfire) {
        campfire.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
        level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(state));
    }

    private static float temperatureStep(float targetTemperature, int ticks) {
        float coolingStep = 1.0F / Math.max(1, MobsToolForgingConfig.COOLING_TICKS.get());
        return targetTemperature / ticks + coolingStep;
    }

    private static void campfireHeatEffects(Level level, BlockPos pos) {
        if (level.getGameTime() % 40L != 0L) {
            return;
        }
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, pos.getX() + 0.5D, pos.getY() + 0.85D, pos.getZ() + 0.5D, 2, 0.18D, 0.08D, 0.18D, 0.01D);
        }
    }
}
