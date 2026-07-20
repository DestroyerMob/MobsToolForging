package org.destroyermob.mobstoolforging.world;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class FoundryAccess {
    private static final int HORIZONTAL_SCAN = 10;
    private static final int VERTICAL_SCAN = 8;

    private FoundryAccess() {
    }

    public static Optional<FoundryForgeBlockEntity> findController(Level level, BlockPos shellPos) {
        for (int y = shellPos.getY(); y >= shellPos.getY() - VERTICAL_SCAN; y--) {
            for (int x = shellPos.getX() - HORIZONTAL_SCAN; x <= shellPos.getX() + HORIZONTAL_SCAN; x++) {
                for (int z = shellPos.getZ() - HORIZONTAL_SCAN; z <= shellPos.getZ() + HORIZONTAL_SCAN; z++) {
                    if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof FoundryForgeBlockEntity forge
                            && forge.refreshStructure()
                            && forge.containsShellPosition(shellPos)) {
                        return Optional.of(forge);
                    }
                }
            }
        }
        return Optional.empty();
    }
}
