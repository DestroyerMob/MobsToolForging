package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModularMattockItem extends AxeItem implements ModularToolItem {
    public ModularMattockItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(AxeItem.createAttributes(Tiers.IRON, 6.0F, -3.1F)));
    }

    @Override
    public ToolKind toolKind() {
        return ToolKind.MATTOCK;
    }

    @Override
    public Component getName(ItemStack stack) {
        return getModularName(stack, super.getName(stack));
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return allowsFinishedToolEnchanting(stack, super.isEnchantable(stack));
    }

    @Override
    public int getEnchantmentValue(ItemStack stack) {
        return finishedToolEnchantmentValue(stack, super.getEnchantmentValue(stack));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        appendModularTooltip(stack, tooltip, flag);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return isBrokenTool(stack) ? 0.0F : super.getDestroySpeed(stack, state);
    }

    @Override
    public boolean canAttackBlock(BlockState state, Level level, BlockPos pos, Player player) {
        return !isBrokenTool(player.getMainHandItem()) && super.canAttackBlock(state, level, pos, player);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        return !isBrokenTool(stack) && super.mineBlock(stack, level, state, pos, miningEntity);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return !isBrokenTool(stack) && super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!isBrokenTool(stack)) {
            super.postHurtEnemy(stack, target, attacker);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (isBrokenTool(context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        InteractionResult axeResult = super.useOn(context);
        if (axeResult != InteractionResult.PASS) {
            return axeResult;
        }
        InteractionResult shovelResult = tryShovelUse(context);
        return shovelResult == InteractionResult.PASS ? tryHoeUse(context) : shovelResult;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        modularInventoryTick(stack, level);
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return isValidModularRepairItem(stack, repairCandidate, super.isValidRepairItem(stack, repairCandidate));
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(itemAbility)
                || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(itemAbility);
    }

    private static InteractionResult tryShovelUse(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (context.getClickedFace() == Direction.DOWN) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        BlockState state = level.getBlockState(pos);
        BlockState resultState = state.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
        if (resultState != null && level.getBlockState(pos.above()).isAir()) {
            level.playSound(player, pos, SoundEvents.SHOVEL_FLATTEN, SoundSource.BLOCKS, 1.0F, 1.0F);
        } else {
            resultState = state.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false);
            if (resultState != null && !level.isClientSide()) {
                level.levelEvent(null, 1009, pos, 0);
            }
        }

        if (resultState == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, resultState, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, resultState));
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static InteractionResult tryHoeUse(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState resultState = level.getBlockState(pos).getToolModifiedState(context, ItemAbilities.HOE_TILL, false);
        if (resultState == null) {
            return InteractionResult.PASS;
        }

        Player player = context.getPlayer();
        level.playSound(player, pos, SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        if (!level.isClientSide()) {
            level.setBlock(pos, resultState, 11);
            level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, resultState));
            if (player != null) {
                context.getItemInHand().hurtAndBreak(1, player, LivingEntity.getSlotForHand(context.getHand()));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}
