package org.destroyermob.mobstoolforging.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
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

    @Override
    public Component getName(ItemStack stack) {
        Optional<ForgeTemplateDefinition> template = template(stack);
        return template.isPresent()
                ? Component.translatable("item.mobstoolforging.template_pattern.named", template.get().displayName())
                : super.getName(stack);
    }
}
