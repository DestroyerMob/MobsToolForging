package org.destroyermob.mobstoolforging.item;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.world.ToolKind;

public class ModularSwordItem extends SwordItem implements ModularToolItem {
    public ModularSwordItem(Properties properties) {
        super(Tiers.IRON, properties.attributes(SwordItem.createAttributes(Tiers.IRON, 3, -2.4F)));
    }

    @Override
    public ToolKind toolKind() {
        return ToolKind.SWORD;
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
        return isBrokenTool(context.getItemInHand()) ? InteractionResult.FAIL : super.useOn(context);
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
}
