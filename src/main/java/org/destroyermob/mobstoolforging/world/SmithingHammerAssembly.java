package org.destroyermob.mobstoolforging.world;

import java.util.List;
import java.util.Optional;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.registry.ModTags;

/** Patternless Toolmaker's Bench assembly for stone and iron smithing hammers. */
public final class SmithingHammerAssembly {
    private SmithingHammerAssembly() {
    }

    public static boolean isHammer(ItemStack stack) {
        return stack.is(ModItems.SMITHING_HAMMER.get()) || stack.is(ModItems.IRON_SMITHING_HAMMER.get());
    }

    public static boolean isHeadIngredient(ItemStack stack) {
        return stack.is(Items.IRON_INGOT) || stack.is(ModTags.Items.HAMMER_STONES);
    }

    public static ItemStack assemble(List<ItemStack> stacks) {
        if (stacks.size() != 2) {
            return ItemStack.EMPTY;
        }
        ItemStack head = ItemStack.EMPTY;
        ItemStack handle = ItemStack.EMPTY;
        for (ItemStack stack : stacks) {
            if (stack.isEmpty() || WorkpieceHeat.hasHeat(stack)) {
                return ItemStack.EMPTY;
            }
            if (stack.is(ModTags.Items.TOOL_HANDLES)) {
                if (!handle.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                handle = stack;
            } else if (isHeadIngredient(stack)) {
                if (!head.isEmpty()) {
                    return ItemStack.EMPTY;
                }
                head = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        if (head.isEmpty() || handle.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack output = new ItemStack(head.is(Items.IRON_INGOT)
                ? ModItems.IRON_SMITHING_HAMMER.get()
                : ModItems.SMITHING_HAMMER.get());
        output.set(ModDataComponents.TOOL_ASSEMBLY_PARTS.get(), ToolAssemblyParts.from(List.of(head, handle)));
        CompositeAffixCompatibility.syncCompatibilityMirror(output);
        return output;
    }

    public static Optional<List<ItemStack>> disassemble(ItemStack hammer) {
        if (!isHammer(hammer)) {
            return Optional.empty();
        }
        ToolAssemblyParts storedParts = hammer.get(ModDataComponents.TOOL_ASSEMBLY_PARTS.get());
        if (storedParts != null && storedParts.stacks().size() == 2) {
            return Optional.of(storedParts.copyStacks());
        }
        ItemStack fallbackHead = hammer.is(ModItems.IRON_SMITHING_HAMMER.get())
                ? new ItemStack(Items.IRON_INGOT)
                : new ItemStack(Items.COBBLESTONE);
        return Optional.of(List.of(fallbackHead, new ItemStack(Items.STICK)));
    }
}
