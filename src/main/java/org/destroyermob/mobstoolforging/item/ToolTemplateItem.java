package org.destroyermob.mobstoolforging.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.ForgeTemplateDefinition;
import org.destroyermob.mobstoolforging.world.ToolTypeRegistry;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public class ToolTemplateItem extends Item {
    private final ResourceLocation templateId;
    @Nullable
    private final WorkstationKind workstationKind;

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

    public ResourceLocation templateId() {
        return templateId;
    }

    public Optional<ForgeTemplateDefinition> template() {
        return ToolTypeRegistry.template(templateId);
    }

    public Optional<WorkstationKind> compatibleWorkstation() {
        return Optional.ofNullable(workstationKind);
    }

    public boolean canUseOn(WorkstationKind kind) {
        return workstationKind == null || workstationKind == kind;
    }
}
