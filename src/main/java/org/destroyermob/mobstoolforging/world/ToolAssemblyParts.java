package org.destroyermob.mobstoolforging.world;

import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

public record ToolAssemblyParts(List<ItemStack> stacks) {
    public static final Codec<ToolAssemblyParts> CODEC = ItemStack.OPTIONAL_CODEC.listOf()
            .xmap(ToolAssemblyParts::new, ToolAssemblyParts::stacks);
    public static final StreamCodec<RegistryFriendlyByteBuf, ToolAssemblyParts> STREAM_CODEC = StreamCodec.composite(
            ItemStack.OPTIONAL_LIST_STREAM_CODEC,
            ToolAssemblyParts::stacks,
            ToolAssemblyParts::new
    );

    public ToolAssemblyParts {
        stacks = stacks.stream()
                .filter(stack -> !stack.isEmpty())
                .map(stack -> stack.copyWithCount(1))
                .toList();
    }

    public static ToolAssemblyParts from(List<ItemStack> stacks) {
        return new ToolAssemblyParts(stacks);
    }

    public List<ItemStack> copyStacks() {
        return stacks.stream()
                .map(ItemStack::copy)
                .toList();
    }
}
