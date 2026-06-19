package org.destroyermob.mobstoolforging.item;

import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.world.item.Item;
import org.destroyermob.mobstoolforging.world.ForgeTemplate;
import org.destroyermob.mobstoolforging.world.WorkstationKind;

public class ToolTemplateItem extends Item {
    private final ForgeTemplate template;
    @Nullable
    private final WorkstationKind workstationKind;

    public ToolTemplateItem(ForgeTemplate template, Properties properties) {
        this(template, null, properties);
    }

    public ToolTemplateItem(ForgeTemplate template, @Nullable WorkstationKind workstationKind, Properties properties) {
        super(properties);
        this.template = template;
        this.workstationKind = workstationKind;
    }

    public ForgeTemplate template() {
        return template;
    }

    public Optional<WorkstationKind> compatibleWorkstation() {
        return Optional.ofNullable(workstationKind);
    }

    public boolean canUseOn(WorkstationKind kind) {
        return workstationKind == null || workstationKind == kind;
    }
}
