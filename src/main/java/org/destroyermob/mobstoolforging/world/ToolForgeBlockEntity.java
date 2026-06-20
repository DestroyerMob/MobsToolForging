package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.MobsToolForgingConfig;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;

public class ToolForgeBlockEntity extends BlockEntity {
    private static final String TEMPLATE_TAG = "Template";
    private static final String MATERIAL_COUNT_TAG = "MaterialCount";
    private static final String HIT_COUNT_TAG = "HitCount";
    private static final String MATERIAL_ID_TAG = "MaterialId";
    private static final String MATERIAL_ITEM_ID_TAG = "MaterialItemId";
    private static final String DISPLAY_ROTATION_TAG = "DisplayRotation";
    private static final String MATERIAL_HEAT_EXPIRES_TAG = "MaterialHeatExpires";
    private static final String MATERIAL_HEAT_TEMPERATURE_TAG = "MaterialHeatTemperature";
    private static final String MATERIAL_HEAT_LAST_UPDATE_TAG = "MaterialHeatLastUpdate";
    private static final String MATERIAL_HEAT_WORKABLE_TAG = "MaterialHeatWorkable";

    @Nullable
    private ResourceLocation templateId;
    @Nullable
    private ResourceLocation materialId;
    @Nullable
    private ResourceLocation materialItemId;
    @Nullable
    private HeatedWorkpieceData materialHeatData;
    private int materialCount;
    private int hitCount;
    private float displayRotationDegrees;

    public ToolForgeBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.TOOL_WORKSTATION.get(), pos, blockState);
    }

    @Nullable
    public ForgeTemplateDefinition template() {
        return templateId == null ? null : ToolTypeRegistry.template(templateId).orElse(null);
    }

    public int materialCount() {
        return materialCount;
    }

    public int hitCount() {
        return hitCount;
    }

    public float displayRotationDegrees() {
        return displayRotationDegrees;
    }

    @Nullable
    public ResourceLocation materialId() {
        return materialId;
    }

    public float progress() {
        ForgeTemplateDefinition template = template();
        if (template == null) {
            return 0.0F;
        }
        return Math.min(1.0F, hitCount / (float) template.requiredHits());
    }

    public boolean isEmpty() {
        return templateId == null && materialId == null && materialItemId == null && materialHeatData == null && materialCount == 0 && hitCount == 0;
    }

    public boolean isComplete() {
        ForgeTemplateDefinition template = template();
        return template != null && materialCount >= template.requiredMaterials() && hitCount >= template.requiredHits();
    }

    public boolean canChangeTemplate() {
        return materialId == null && materialItemId == null && materialHeatData == null && materialCount == 0 && hitCount == 0;
    }

    public boolean selectTemplate(ForgeTemplateDefinition template) {
        return setTemplateFromItem(template);
    }

    public boolean setTemplateFromItem(ForgeTemplateDefinition template) {
        if (this.templateId != null && this.templateId.equals(template.id())) {
            return true;
        }
        if (!canChangeTemplate()) {
            return false;
        }
        this.templateId = template.id();
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        materialCount = 0;
        hitCount = 0;
        displayRotationDegrees = 0.0F;
        sync();
        return true;
    }

    public boolean clearTemplate() {
        if (templateId == null || !canChangeTemplate()) {
            return false;
        }
        templateId = null;
        displayRotationDegrees = 0.0F;
        sync();
        return true;
    }

    public int remainingMaterials() {
        ForgeTemplateDefinition template = template();
        return template == null ? 0 : Math.max(0, template.requiredMaterials() - materialCount);
    }

    public int acceptMaterials(ItemStack stack, ToolMaterialDefinition material) {
        if (template() == null || isComplete()) {
            return 0;
        }
        if (materialId != null && !materialId.equals(material.id())) {
            return 0;
        }
        int taken = Math.min(stack.getCount(), remainingMaterials());
        if (taken > 0) {
            if (materialId == null) {
                materialId = material.id();
                materialItemId = BuiltInRegistries.ITEM.getKey(material.displayItem());
            }
            captureMaterialHeat(stack);
            stack.shrink(taken);
            materialCount += taken;
            sync();
        }
        return taken;
    }

    public boolean canHammer() {
        ForgeTemplateDefinition template = template();
        return template != null && materialCount >= template.requiredMaterials() && !isComplete();
    }

    public boolean hammer() {
        if (!canHammer()) {
            return false;
        }
        hitCount++;
        randomizeDisplayRotation();
        sync();
        return true;
    }

    public ItemStack outputStack() {
        ForgeTemplateDefinition template = template();
        return isComplete() && template != null && materialId != null ? applyMaterialHeat(template.outputStack(materialId)) : ItemStack.EMPTY;
    }

    public ItemStack displayMaterialStack() {
        if (isComplete()) {
            return outputStack();
        }
        if (materialCount <= 0 || materialItemId == null) {
            return ItemStack.EMPTY;
        }
        return applyMaterialHeat(new ItemStack(BuiltInRegistries.ITEM.get(materialItemId)));
    }

    public ItemStack materialDropStack() {
        if (materialCount <= 0 || materialItemId == null) {
            return ItemStack.EMPTY;
        }
        return applyMaterialHeat(new ItemStack(BuiltInRegistries.ITEM.get(materialItemId), materialCount));
    }

    public ItemStack removeOutput() {
        ItemStack output = outputStack();
        if (!output.isEmpty()) {
            reset();
        }
        return output;
    }

    public void reset() {
        templateId = null;
        materialId = null;
        materialItemId = null;
        materialHeatData = null;
        materialCount = 0;
        hitCount = 0;
        displayRotationDegrees = 0.0F;
        sync();
    }

    public boolean materialIsHot() {
        return materialHeatData != null && level != null && materialHeatData.workable()
                && materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get()) > 0.0F;
    }

    private void captureMaterialHeat(ItemStack stack) {
        HeatedWorkpieceData heatData = stack.get(ModDataComponents.HEATED_WORKPIECE.get());
        if (heatData == null) {
            return;
        }
        if (level == null) {
            materialHeatData = heatData;
            return;
        }
        float stackTemperature = heatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
        float existingTemperature = materialHeatData == null ? 0.0F : materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
        boolean workable = heatData.workable() || materialHeatData != null && materialHeatData.workable();
        if (stackTemperature >= existingTemperature) {
            materialHeatData = refreshedHeatData(stackTemperature, workable);
        } else if (materialHeatData != null && workable && !materialHeatData.workable()) {
            materialHeatData = refreshedHeatData(existingTemperature, true);
        }
    }

    private ItemStack applyMaterialHeat(ItemStack stack) {
        if (stack.isEmpty() || materialHeatData == null) {
            return stack;
        }
        if (level == null) {
            stack.set(ModDataComponents.HEATED_WORKPIECE.get(), materialHeatData);
            return stack;
        }
        float temperature = materialHeatData.temperatureAt(level.getGameTime(), MobsToolForgingConfig.COOLING_TICKS.get());
        if (temperature > 0.0F) {
            stack.set(ModDataComponents.HEATED_WORKPIECE.get(), refreshedHeatData(temperature, materialHeatData.workable()));
        }
        return stack;
    }

    private HeatedWorkpieceData refreshedHeatData(float temperature, boolean workable) {
        long gameTime = level == null ? 0L : level.getGameTime();
        return new HeatedWorkpieceData(
                gameTime + Math.max(1L, Math.round(temperature * MobsToolForgingConfig.COOLING_TICKS.get())),
                temperature,
                gameTime,
                workable
        );
    }

    private void randomizeDisplayRotation() {
        if (level == null) {
            return;
        }
        float direction = level.random.nextBoolean() ? 1.0F : -1.0F;
        float amount = 4.0F + level.random.nextFloat() * 8.0F;
        displayRotationDegrees = wrapDegrees(displayRotationDegrees + direction * amount);
    }

    private static float wrapDegrees(float degrees) {
        float wrapped = degrees % 360.0F;
        if (wrapped >= 180.0F) {
            wrapped -= 360.0F;
        }
        if (wrapped < -180.0F) {
            wrapped += 360.0F;
        }
        return wrapped;
    }

    public void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (templateId != null) {
            tag.putString(TEMPLATE_TAG, templateId.toString());
        }
        if (materialId != null) {
            tag.putString(MATERIAL_ID_TAG, materialId.toString());
        }
        if (materialItemId != null) {
            tag.putString(MATERIAL_ITEM_ID_TAG, materialItemId.toString());
        }
        if (materialHeatData != null) {
            tag.putLong(MATERIAL_HEAT_EXPIRES_TAG, materialHeatData.expiresAtGameTime());
            tag.putFloat(MATERIAL_HEAT_TEMPERATURE_TAG, materialHeatData.temperature());
            tag.putLong(MATERIAL_HEAT_LAST_UPDATE_TAG, materialHeatData.lastUpdateGameTime());
            tag.putBoolean(MATERIAL_HEAT_WORKABLE_TAG, materialHeatData.workable());
        }
        tag.putInt(MATERIAL_COUNT_TAG, materialCount);
        tag.putInt(HIT_COUNT_TAG, hitCount);
        tag.putFloat(DISPLAY_ROTATION_TAG, displayRotationDegrees);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        templateId = tag.contains(TEMPLATE_TAG) ? ResourceLocation.parse(tag.getString(TEMPLATE_TAG)) : null;
        materialId = tag.contains(MATERIAL_ID_TAG) ? ResourceLocation.parse(tag.getString(MATERIAL_ID_TAG)) : null;
        materialItemId = tag.contains(MATERIAL_ITEM_ID_TAG) ? ResourceLocation.parse(tag.getString(MATERIAL_ITEM_ID_TAG)) : null;
        materialHeatData = tag.contains(MATERIAL_HEAT_TEMPERATURE_TAG)
                ? new HeatedWorkpieceData(
                tag.getLong(MATERIAL_HEAT_EXPIRES_TAG),
                tag.getFloat(MATERIAL_HEAT_TEMPERATURE_TAG),
                tag.getLong(MATERIAL_HEAT_LAST_UPDATE_TAG),
                tag.getBoolean(MATERIAL_HEAT_WORKABLE_TAG)
        )
                : null;
        materialCount = tag.getInt(MATERIAL_COUNT_TAG);
        hitCount = tag.getInt(HIT_COUNT_TAG);
        displayRotationDegrees = tag.getFloat(DISPLAY_ROTATION_TAG);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
