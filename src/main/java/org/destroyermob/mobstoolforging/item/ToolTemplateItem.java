package org.destroyermob.mobstoolforging.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.registry.ModItems;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public class ToolTemplateItem extends Item {
    @Nullable
    private final ResourceLocation templateId;
    @Nullable
    private final WorkstationKind workstationKind;

    public ToolTemplateItem(Properties properties) {
        this((ResourceLocation) null, null, properties);
    }

    public ToolTemplateItem(ForgeTemplate template, Properties properties) {
        this(template, null, properties);
    }

    public ToolTemplateItem(ForgeTemplate template, @Nullable WorkstationKind workstationKind, Properties properties) {
        this(template.registryId(), workstationKind, properties);
    }

    public ToolTemplateItem(ResourceLocation templateId, Properties properties) {
        this(templateId, null, properties);
    }

    public ToolTemplateItem(ResourceLocation templateId, @Nullable WorkstationKind workstationKind, Properties properties) {
        super(properties);
        this.templateId = templateId;
        this.workstationKind = workstationKind;
    }

    @Nullable
    public ResourceLocation templateId() {
        return templateId;
    }

    public Optional<ResourceLocation> templateId(ItemStack stack) {
        ResourceLocation stackTemplate = stack.get(ModDataComponents.FORGE_TEMPLATE.get());
        return Optional.ofNullable(stackTemplate == null ? templateId : stackTemplate);
    }

    public Optional<ForgeTemplateDefinition> template() {
        if (templateId == null) {
            return Optional.empty();
        }
        return ToolTypeRegistry.template(templateId);
    }

    public Optional<ForgeTemplateDefinition> template(ItemStack stack) {
        return templateId(stack).flatMap(ToolTypeRegistry::template);
    }

    public Optional<WorkstationKind> compatibleWorkstation() {
        return Optional.ofNullable(workstationKind);
    }

    public boolean canUseOn(WorkstationKind kind) {
        return workstationKind == null || workstationKind == kind;
    }

    public static ItemStack createPatternStack(ForgeTemplateDefinition template) {
        return createPatternStack(template.id());
    }

    public static ItemStack createPatternStack(ResourceLocation id) {
        if (id.equals(ForgeTemplate.PICKAXE_HEAD.registryId())) {
            return new ItemStack(ModItems.PICKAXE_HEAD_PATTERN.get());
        }
        if (id.equals(ForgeTemplate.AXE_HEAD.registryId())) {
            return new ItemStack(ModItems.AXE_HEAD_PATTERN.get());
        }
        if (id.equals(ForgeTemplate.SHOVEL_HEAD.registryId())) {
            return new ItemStack(ModItems.SHOVEL_HEAD_PATTERN.get());
        }
        if (id.equals(ForgeTemplate.HOE_HEAD.registryId())) {
            return new ItemStack(ModItems.HOE_HEAD_PATTERN.get());
        }
        if (id.equals(ForgeTemplate.SWORD_BLADE.registryId())) {
            return new ItemStack(ModItems.SWORD_BLADE_PATTERN.get());
        }
        if (id.equals(ForgeTemplate.SWORD_GUARD.registryId())) {
            return new ItemStack(ModItems.SWORD_GUARD_PATTERN.get());
        }
        if (id.equals(ToolTypeRegistry.SMITHING_HAMMER_HEAD_TEMPLATE)) {
            return new ItemStack(ModItems.SMITHING_HAMMER_HEAD_PATTERN.get());
        }
        if (id.equals(ToolTypeRegistry.SCREWDRIVER_HEAD_TEMPLATE)) {
            return new ItemStack(ModItems.SCREWDRIVER_HEAD_PATTERN.get());
        }
        if (id.equals(ToolTypeRegistry.GEM_CUTTERS_BLADE_TEMPLATE)) {
            return new ItemStack(ModItems.GEM_CUTTERS_BLADE_PATTERN.get());
        }
        ItemStack pattern = new ItemStack(ModItems.TEMPLATE_PATTERN.get());
        pattern.set(ModDataComponents.FORGE_TEMPLATE.get(), id);
        return pattern;
    }

    @Override
    public Component getName(ItemStack stack) {
        Optional<ForgeTemplateDefinition> template = template(stack);
        return template.isPresent()
                ? Component.translatable("item.mobstoolforging.template_pattern.named", template.get().displayName())
                : super.getName(stack);
    }
}
