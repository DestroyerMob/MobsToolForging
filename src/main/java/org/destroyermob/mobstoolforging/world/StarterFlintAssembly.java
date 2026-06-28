package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;

public final class StarterFlintAssembly {
    private StarterFlintAssembly() {
    }

    public static boolean isStarterPrimaryPart(ItemStack stack) {
        return target(stack).filter(KnappingTarget::primaryPart).isPresent();
    }

    public static boolean canAdd(List<ItemStack> existingStacks, ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        List<ItemStack> candidate = new ArrayList<>(existingStacks.stream().map(ItemStack::copy).toList());
        candidate.add(stack.copyWithCount(1));
        return state(candidate).isValidPartial();
    }

    public static boolean isComplete(List<ItemStack> stacks) {
        return state(stacks).isComplete();
    }

    public static ItemStack assemble(List<ItemStack> stacks, HolderLookup.Provider registries) {
        if (!isComplete(stacks)) {
            return ItemStack.EMPTY;
        }
        ItemStack output = ToolmakerBenchAssembly.assemble(stacks, registries);
        if (output.isEmpty()) {
            return output;
        }
        applyPlantFiberBinding(output);
        List<ItemStack> assemblyParts = new ArrayList<>(stacks.stream().map(stack -> stack.copyWithCount(1)).toList());
        assemblyParts.add(new ItemStack(ModItems.PLANT_FIBER.get()));
        output.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(assemblyParts));
        return output;
    }

    private static void applyPlantFiberBinding(ItemStack output) {
        ToolConstructionData construction = output.get(ModDataComponents.TOOL_CONSTRUCTION.get());
        if (construction == null || construction.bindingMaterial().isPresent()) {
            return;
        }
        ToolConstructionData updated = new ToolConstructionData(
                construction.toolType(),
                construction.headMaterial(),
                construction.handleMaterial(),
                Optional.of(MaterialCatalog.PLANT_FIBER),
                construction.wrapMaterial(),
                construction.focusMaterial(),
                construction.treatment(),
                construction.quality()
        );
        output.set(ModDataComponents.TOOL_CONSTRUCTION.get(), updated);
        ToolTypeRegistry.toolType(updated.toolType()).ifPresent(definition -> ToolStatBuilder.apply(output, definition, updated));
    }

    private static Optional<KnappingTarget> target(ItemStack stack) {
        return KnappingTarget.byPartData(stack.get(ModDataComponents.TOOL_PART.get()));
    }

    private static AssemblyState state(List<ItemStack> stacks) {
        KnappingTarget primary = null;
        boolean hasHandle = false;
        boolean hasSwordGuard = false;
        boolean failed = false;

        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            Optional<KnappingTarget> target = target(stack);
            if (target.isPresent()) {
                KnappingTarget knappingTarget = target.get();
                if (knappingTarget.primaryPart()) {
                    if (primary != null) {
                        failed = true;
                    }
                    primary = knappingTarget;
                    continue;
                }
                if (knappingTarget == KnappingTarget.SWORD_GUARD && !hasSwordGuard) {
                    hasSwordGuard = true;
                    continue;
                }
                failed = true;
                continue;
            }
            if (stack.is(Items.STICK) && !hasHandle) {
                hasHandle = true;
                continue;
            }
            failed = true;
        }

        if (primary != KnappingTarget.SWORD_BLADE && hasSwordGuard) {
            failed = true;
        }
        return new AssemblyState(failed, primary, hasHandle, hasSwordGuard);
    }

    private record AssemblyState(boolean failed, KnappingTarget primary, boolean hasHandle, boolean hasSwordGuard) {
        private boolean isValidPartial() {
            return !failed && primary != null;
        }

        private boolean isComplete() {
            if (!isValidPartial() || !hasHandle) {
                return false;
            }
            return primary == KnappingTarget.SWORD_BLADE ? hasSwordGuard : !hasSwordGuard;
        }
    }
}
