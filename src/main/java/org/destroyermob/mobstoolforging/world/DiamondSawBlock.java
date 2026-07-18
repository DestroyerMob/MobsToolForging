package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.StonecutterMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModBlocks;

/**
 * A stonecutter variant whose vanilla menu remains valid while this block is under it.
 * Vanilla's {@link StonecutterMenu} otherwise closes immediately because it only accepts
 * {@code minecraft:stonecutter} at the menu position.
 */
public class DiamondSawBlock extends StonecutterBlock {
    private static final Component CONTAINER_TITLE = Component.translatable("container.stonecutter");

    public DiamondSawBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        ContainerLevelAccess access = ContainerLevelAccess.create(level, pos);
        return new SimpleMenuProvider(
                (containerId, inventory, player) -> new StonecutterMenu(containerId, inventory, access) {
                    @Override
                    public boolean stillValid(Player menuPlayer) {
                        return stillValid(access, menuPlayer, ModBlocks.DIAMOND_SAW.get());
                    }
                },
                CONTAINER_TITLE
        );
    }
}
