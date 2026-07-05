package org.destroyermob.mobstoolforging.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public record GroundAssemblyRecipe(
        ResourceLocation id,
        List<Entry> assembly,
        List<Entry> consumables,
        Result result
) {
    public GroundAssemblyRecipe {
        assembly = List.copyOf(assembly);
        consumables = List.copyOf(consumables);
        if (assembly.isEmpty()) {
            throw new IllegalArgumentException("Ground assembly recipe needs at least one assembly entry.");
        }
    }

    public boolean canStart(ItemStack stack) {
        return !stack.isEmpty() && assembly.stream().anyMatch(entry -> entry.matches(stack));
    }

    public boolean canAccept(List<ItemStack> existingStacks, ItemStack stack) {
        if (stack.isEmpty() || existingStacks.size() >= assembly.size() + consumables.size()) {
            return false;
        }
        List<ItemStack> candidate = new ArrayList<>(existingStacks.stream().map(ItemStack::copy).toList());
        candidate.add(stack.copyWithCount(1));
        return match(candidate, false).isPresent();
    }

    public ItemStack assemble(List<ItemStack> stacks, HolderLookup.Provider registries) {
        return match(stacks, true)
                .map(match -> result.assemble(match.assemblyStacks(), registries))
                .orElse(ItemStack.EMPTY);
    }

    private Optional<Match> match(List<ItemStack> stacks, boolean requireComplete) {
        if (stacks.isEmpty() || stacks.size() > assembly.size() + consumables.size()) {
            return Optional.empty();
        }
        boolean[] assemblyUsed = new boolean[assembly.size()];
        boolean[] consumablesUsed = new boolean[consumables.size()];
        ItemStack[] assignedAssembly = new ItemStack[assembly.size()];
        if (!assign(stacks, 0, assemblyUsed, consumablesUsed, assignedAssembly)) {
            return Optional.empty();
        }
        if (requireComplete && (!allUsed(assemblyUsed) || !allUsed(consumablesUsed))) {
            return Optional.empty();
        }
        List<ItemStack> assemblyStacks = new ArrayList<>();
        for (ItemStack stack : assignedAssembly) {
            if (stack != null && !stack.isEmpty()) {
                assemblyStacks.add(stack.copyWithCount(1));
            }
        }
        return Optional.of(new Match(List.copyOf(assemblyStacks)));
    }

    private boolean assign(List<ItemStack> stacks, int index, boolean[] assemblyUsed, boolean[] consumablesUsed, ItemStack[] assignedAssembly) {
        if (index >= stacks.size()) {
            return true;
        }
        ItemStack stack = stacks.get(index);
        for (int slot = 0; slot < assembly.size(); slot++) {
            if (!assemblyUsed[slot] && assembly.get(slot).matches(stack)) {
                assemblyUsed[slot] = true;
                assignedAssembly[slot] = stack.copyWithCount(1);
                if (assign(stacks, index + 1, assemblyUsed, consumablesUsed, assignedAssembly)) {
                    return true;
                }
                assignedAssembly[slot] = null;
                assemblyUsed[slot] = false;
            }
        }
        for (int slot = 0; slot < consumables.size(); slot++) {
            if (!consumablesUsed[slot] && consumables.get(slot).matches(stack)) {
                consumablesUsed[slot] = true;
                if (assign(stacks, index + 1, assemblyUsed, consumablesUsed, assignedAssembly)) {
                    return true;
                }
                consumablesUsed[slot] = false;
            }
        }
        return false;
    }

    private static boolean allUsed(boolean[] values) {
        for (boolean value : values) {
            if (!value) {
                return false;
            }
        }
        return true;
    }

    public static GroundAssemblyRecipe fromJson(ResourceLocation id, JsonObject json) {
        List<Entry> assembly = entries(GsonHelper.getAsJsonArray(json, json.has("assembly") ? "assembly" : "ingredients"));
        List<Entry> consumables = json.has("consumables") ? entries(GsonHelper.getAsJsonArray(json, "consumables")) : List.of();
        Result result = Result.fromJson(json.has("result") ? GsonHelper.getAsJsonObject(json, "result") : new JsonObject());
        return new GroundAssemblyRecipe(id, assembly, consumables, result);
    }

    private static List<Entry> entries(JsonArray array) {
        List<Entry> entries = new ArrayList<>();
        for (JsonElement element : array) {
            entries.add(Entry.fromJson(GsonHelper.convertToJsonObject(element, "ground assembly entry")));
        }
        return entries;
    }

    private record Match(List<ItemStack> assemblyStacks) {
    }

    public record Entry(
            Optional<ResourceLocation> itemId,
            Optional<TagKey<Item>> tag,
            Optional<String> partType,
            Optional<ResourceLocation> material
    ) {
        public boolean matches(ItemStack stack) {
            if (stack.isEmpty()) {
                return false;
            }
            if (itemId.isPresent() && !BuiltInRegistries.ITEM.getKey(stack.getItem()).equals(itemId.get())) {
                return false;
            }
            if (tag.isPresent() && !stack.is(tag.get())) {
                return false;
            }
            if (partType.isPresent() || material.isPresent()) {
                ToolPartData partData = stack.get(ModDataComponents.TOOL_PART.get());
                if (partData == null) {
                    return false;
                }
                if (partType.isPresent() && !partType.get().equals(partData.partType())) {
                    return false;
                }
                if (material.isPresent() && !material.get().equals(partData.materialId())) {
                    return false;
                }
            }
            return itemId.isPresent() || tag.isPresent() || partType.isPresent() || material.isPresent();
        }

        private static Entry fromJson(JsonObject json) {
            Optional<ResourceLocation> item = optionalLocation(json, "item");
            Optional<ResourceLocation> tag = optionalLocation(json, "tag");
            Optional<String> partType = optionalString(json, json.has("part_type") ? "part_type" : "part");
            Optional<ResourceLocation> material = optionalLocation(json, "material");
            return new Entry(item, tag.map(id -> TagKey.create(Registries.ITEM, id)), partType, material);
        }
    }

    public interface Result {
        ItemStack assemble(List<ItemStack> assemblyStacks, HolderLookup.Provider registries);

        static Result fromJson(JsonObject json) {
            String type = GsonHelper.getAsString(json, "type", json.has("item") || json.has("id") ? "item" : "toolmaker_assembly");
            if (type.equals("toolmaker_assembly") || type.equals("toolmaker") || type.equals("modular_tool")) {
                return (assemblyStacks, registries) -> ToolmakerBenchAssembly.assemble(assemblyStacks, registries);
            }
            if (type.equals("item")) {
                ResourceLocation itemId = ResourceLocation.parse(json.has("id") ? GsonHelper.getAsString(json, "id") : GsonHelper.getAsString(json, "item"));
                Item item = BuiltInRegistries.ITEM.get(itemId);
                if (item == Items.AIR) {
                    throw new IllegalArgumentException("Unknown ground assembly result item " + itemId);
                }
                ItemStack output = new ItemStack(item, Math.max(1, GsonHelper.getAsInt(json, "count", 1)));
                return (assemblyStacks, registries) -> output.copy();
            }
            throw new IllegalArgumentException("Unknown ground assembly result type " + type);
        }
    }

    private static Optional<ResourceLocation> optionalLocation(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? Optional.of(ResourceLocation.parse(GsonHelper.getAsString(json, key)))
                : Optional.empty();
    }

    private static Optional<String> optionalString(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull()
                ? Optional.of(GsonHelper.getAsString(json, key))
                : Optional.empty();
    }
}
