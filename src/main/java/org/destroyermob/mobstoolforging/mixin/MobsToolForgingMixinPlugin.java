package org.destroyermob.mobstoolforging.mixin;

import java.util.List;
import java.util.Set;
import net.neoforged.fml.loading.LoadingModList;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public final class MobsToolForgingMixinPlugin implements IMixinConfigPlugin {
    private static final String APOTHEOSIS_AFFIX_LOOT_MIXIN =
            "org.destroyermob.mobstoolforging.mixin.compat.ApotheosisAffixLootModifierMixin";
    private static final String APOTHEOSIS_AFFIX_HELPER_MIXIN =
            "org.destroyermob.mobstoolforging.mixin.compat.ApotheosisAffixHelperMixin";
    private static final String APOTHEOSIS_REFORGING_RESULT_SLOT_MIXIN =
            "org.destroyermob.mobstoolforging.mixin.compat.ApotheosisReforgingResultSlotMixin";
    private static final String APOTHEOSIS_SALVAGING_MENU_MIXIN =
            "org.destroyermob.mobstoolforging.mixin.compat.ApotheosisSalvagingMenuMixin";
    private static final String APOTHIC_STACK_ATTRIBUTE_MODIFIERS_EVENT_MIXIN =
            "org.destroyermob.mobstoolforging.mixin.compat.ApothicStackAttributeModifiersEventMixin";
    private static final String APOTHEOSIS_MOD_ID = "apotheosis";
    private static final String APOTHIC_ATTRIBUTES_MOD_ID = "apothic_attributes";

    @Override
    public void onLoad(String mixinPackage) {
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (APOTHEOSIS_AFFIX_LOOT_MIXIN.equals(mixinClassName)
                || APOTHEOSIS_AFFIX_HELPER_MIXIN.equals(mixinClassName)
                || APOTHEOSIS_REFORGING_RESULT_SLOT_MIXIN.equals(mixinClassName)
                || APOTHEOSIS_SALVAGING_MENU_MIXIN.equals(mixinClassName)) {
            return isModPresent(APOTHEOSIS_MOD_ID);
        }
        if (APOTHIC_STACK_ATTRIBUTE_MODIFIERS_EVENT_MIXIN.equals(mixinClassName)) {
            return isModPresent(APOTHIC_ATTRIBUTES_MOD_ID);
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {
    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    private static boolean isModPresent(String modId) {
        try {
            LoadingModList modList = LoadingModList.get();
            return modList != null && modList.getModFileById(modId) != null;
        } catch (RuntimeException ignored) {
            return false;
        }
    }
}
