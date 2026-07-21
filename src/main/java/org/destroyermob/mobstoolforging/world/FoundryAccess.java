package org.destroyermob.mobstoolforging.world;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.WeakHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public final class FoundryAccess {
    private static final int HORIZONTAL_SCAN = 10;
    private static final int VERTICAL_SCAN = 8;
    private static final Map<Level, LevelIndex> INDEXES = new WeakHashMap<>();

    private FoundryAccess() {
    }

    public static Optional<FoundryForgeBlockEntity> findController(Level level, BlockPos shellPos) {
        Optional<FoundryForgeBlockEntity> cached = cachedController(level, shellPos);
        if (cached.isPresent()) {
            return cached;
        }

        BlockPos.MutableBlockPos candidate = new BlockPos.MutableBlockPos();
        for (int y = shellPos.getY(); y >= shellPos.getY() - VERTICAL_SCAN; y--) {
            for (int x = shellPos.getX() - HORIZONTAL_SCAN; x <= shellPos.getX() + HORIZONTAL_SCAN; x++) {
                for (int z = shellPos.getZ() - HORIZONTAL_SCAN; z <= shellPos.getZ() + HORIZONTAL_SCAN; z++) {
                    candidate.set(x, y, z);
                    if (level.isLoaded(candidate)
                            && level.getBlockEntity(candidate) instanceof FoundryForgeBlockEntity forge
                            && forge.refreshStructure()
                            && forge.containsShellPosition(shellPos)) {
                        return Optional.of(forge);
                    }
                }
            }
        }
        return Optional.empty();
    }

    static synchronized void updateController(FoundryForgeBlockEntity forge) {
        Level level = forge.getLevel();
        if (level == null) {
            return;
        }
        LevelIndex index = INDEXES.computeIfAbsent(level, ignored -> new LevelIndex());
        long controller = forge.getBlockPos().asLong();
        index.removeController(controller);
        if (!forge.isFormed() || forge.isRemoved()) {
            return;
        }
        Set<Long> shellPositions = new HashSet<>();
        forge.forEachShellPosition(pos -> {
            long shell = pos.asLong();
            index.controllersByShell.put(shell, controller);
            shellPositions.add(shell);
        });
        index.shellsByController.put(controller, shellPositions);
    }

    static synchronized void removeController(FoundryForgeBlockEntity forge) {
        Level level = forge.getLevel();
        if (level == null) {
            return;
        }
        LevelIndex index = INDEXES.get(level);
        if (index != null) {
            index.removeController(forge.getBlockPos().asLong());
        }
    }

    private static synchronized Optional<FoundryForgeBlockEntity> cachedController(Level level, BlockPos shellPos) {
        LevelIndex index = INDEXES.get(level);
        if (index == null) {
            return Optional.empty();
        }
        Long controller = index.controllersByShell.get(shellPos.asLong());
        if (controller == null) {
            return Optional.empty();
        }
        BlockPos controllerPos = BlockPos.of(controller);
        if (level.isLoaded(controllerPos)
                && level.getBlockEntity(controllerPos) instanceof FoundryForgeBlockEntity forge
                && !forge.isRemoved()
                && forge.isFormed()
                && forge.containsShellPosition(shellPos)) {
            return Optional.of(forge);
        }
        index.removeController(controller);
        return Optional.empty();
    }

    private static final class LevelIndex {
        private final Map<Long, Long> controllersByShell = new HashMap<>();
        private final Map<Long, Set<Long>> shellsByController = new HashMap<>();

        private void removeController(long controller) {
            Set<Long> shells = shellsByController.remove(controller);
            if (shells != null) {
                shells.forEach(shell -> controllersByShell.remove(shell, controller));
            }
        }
    }
}
