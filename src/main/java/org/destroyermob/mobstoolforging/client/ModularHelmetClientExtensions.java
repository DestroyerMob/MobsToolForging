package org.destroyermob.mobstoolforging.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.destroyermob.mobstoolforging.client.model.ModularHelmetModel;

public final class ModularHelmetClientExtensions implements IClientItemExtensions {
    public static final ModularHelmetClientExtensions INSTANCE = new ModularHelmetClientExtensions();
    private HumanoidModel<?> blankArmorModel;

    private ModularHelmetClientExtensions() {
    }

    @Override
    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        if (blankArmorModel == null) {
            blankArmorModel = new HumanoidModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModularHelmetModel.BLANK_ARMOR_LAYER));
        }
        return blankArmorModel;
    }

    @Override
    public Model getGenericArmorModel(LivingEntity livingEntity, ItemStack itemStack, EquipmentSlot equipmentSlot, HumanoidModel<?> original) {
        return getHumanoidArmorModel(livingEntity, itemStack, equipmentSlot, original);
    }

    @Override
    public int getArmorLayerTintColor(ItemStack stack, LivingEntity entity, ArmorMaterial.Layer layer, int layerIdx, int fallbackColor) {
        return 0;
    }
}
