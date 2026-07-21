package org.destroyermob.mobstoolforging.world;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;
import org.destroyermob.mobstoolforging.registry.ModBlocks;
import org.destroyermob.mobstoolforging.registry.ModDataComponents;
import org.destroyermob.mobstoolforging.item.CastingMoldItem;

public class FoundryCastingBlockEntity extends BlockEntity {
    public static final int COOLING_TICKS = 60;
    private static final String MATERIAL_TAG = "Material";
    private static final String AMOUNT_TAG = "Amount";
    private static final String CAPACITY_TAG = "Capacity";
    private static final String COOLING_TAG = "Cooling";
    private static final String OUTPUT_TAG = "Output";
    private static final String PENDING_OUTPUT_TAG = "PendingOutput";
    private static final String FORM_TAG = "Form";
    private static final String CONSUME_FORM_TAG = "ConsumeForm";
    private static final String UNRESOLVED_LEGACY_TAG = "UnresolvedLegacyJob";

    @Nullable
    private ResourceLocation material;
    private int amountMb;
    private int castingCapacityMb;
    private int coolingTicks;
    private ItemStack output = ItemStack.EMPTY;
    private ItemStack pendingOutput = ItemStack.EMPTY;
    private ItemStack form = ItemStack.EMPTY;
    private boolean consumeForm;
    private boolean unresolvedLegacyJob;

    public FoundryCastingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_CASTING.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryCastingBlockEntity casting) {
        if (casting.unresolvedLegacyJob) {
            casting.tryResolveLegacyJob();
        }
        if (casting.amountMb <= 0
                || casting.castingCapacityMb <= 0
                || casting.amountMb < casting.castingCapacityMb
                || !casting.output.isEmpty()
                || casting.pendingOutput.isEmpty()) {
            return;
        }
        casting.coolingTicks++;
        if (casting.coolingTicks >= COOLING_TICKS && casting.material != null) {
            casting.output = casting.pendingOutput.copy();
            if (casting.consumeForm) {
                casting.form = ItemStack.EMPTY;
            }
            casting.material = null;
            casting.amountMb = 0;
            casting.castingCapacityMb = 0;
            casting.coolingTicks = 0;
            casting.pendingOutput = ItemStack.EMPTY;
            casting.consumeForm = false;
            level.playSound(null, pos, SoundEvents.ANVIL_PLACE, SoundSource.BLOCKS, 0.35F, 1.6F);
        }
        casting.sync();
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, FoundryCastingBlockEntity casting) {
        if (casting.coolingTicks <= 0 || casting.amountMb < casting.capacityMb() || casting.material == null) {
            return;
        }
        if (level.random.nextFloat() < 0.08F) {
            double x = pos.getX() + 0.2D + level.random.nextDouble() * 0.6D;
            double y = pos.getY() + (isBasin(state) ? 0.84D : 0.78D);
            double z = pos.getZ() + 0.2D + level.random.nextDouble() * 0.6D;
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.015D, 0.0D);
        }
    }

    public int capacityMb() {
        if (castingCapacityMb > 0) {
            return castingCapacityMb;
        }
        return liveCapacityMb();
    }

    private int liveCapacityMb() {
        if (isBasin(getBlockState())) {
            return FoundryForgeBlockEntity.BLOCK_MB;
        }
        Optional<FoundryCastRecipe> castRecipe = FoundryCastRegistry.findForCast(form);
        if (castRecipe.isPresent()) {
            return castRecipe.get().amountMb();
        }
        return FoundryCastRegistry.findForInput(form)
                .map(FoundryCastRecipe::goldAmountMb)
                .orElse(FoundryForgeBlockEntity.INGOT_MB);
    }

    public int amountMb() {
        return amountMb;
    }

    public float fillFraction() {
        return Math.min(1.0F, amountMb / (float) capacityMb());
    }

    public Optional<ResourceLocation> material() {
        return Optional.ofNullable(material);
    }

    public ItemStack output() {
        return output.copy();
    }

    public ItemStack form() {
        return form.copy();
    }

    public int coolingTicks() {
        return coolingTicks;
    }

    public ItemStack previewOutput() {
        if (!pendingOutput.isEmpty()) {
            return pendingOutput.copy();
        }
        return material == null ? ItemStack.EMPTY : completedOutput(material, capacityMb()).copy();
    }

    public boolean canInsertForm(ItemStack stack) {
        return !isBasin(getBlockState())
                && form.isEmpty()
                && output.isEmpty()
                && material == null
                && amountMb == 0
                && castingCapacityMb == 0
                && coolingTicks == 0
                && pendingOutput.isEmpty()
                && (FoundryCastRegistry.findForInput(stack).isPresent() || FoundryCastRegistry.findForCast(stack).isPresent());
    }

    public boolean insertForm(ItemStack offered) {
        if (!canInsertForm(offered)) {
            return false;
        }
        form = offered.split(1);
        sync();
        return true;
    }

    public int remainingCapacity(ResourceLocation offeredMaterial) {
        if (!output.isEmpty() || coolingTicks > 0) {
            return 0;
        }
        if (material != null && !material.equals(offeredMaterial)) {
            return 0;
        }
        if (castingCapacityMb > 0) {
            return pendingOutput.isEmpty() ? 0 : Math.max(0, castingCapacityMb - amountMb);
        }
        if (!canProduce(offeredMaterial)) {
            return 0;
        }
        return liveCapacityMb();
    }

    public int receive(ResourceLocation offeredMaterial, int offeredMb) {
        if (offeredMaterial == null || offeredMb <= 0 || !output.isEmpty() || coolingTicks > 0) {
            return 0;
        }
        if (material != null && !material.equals(offeredMaterial)) {
            return 0;
        }
        if (castingCapacityMb <= 0) {
            if (!canProduce(offeredMaterial)) {
                return 0;
            }
            int capacityMb = liveCapacityMb();
            ItemStack plannedOutput = completedOutput(offeredMaterial, capacityMb);
            if (plannedOutput.isEmpty()) {
                return 0;
            }
            castingCapacityMb = capacityMb;
            pendingOutput = plannedOutput;
            consumeForm = !(form.getItem() instanceof CastingMoldItem);
            unresolvedLegacyJob = false;
        }
        int accepted = Math.min(offeredMb, Math.max(0, castingCapacityMb - amountMb));
        if (accepted <= 0) {
            return 0;
        }
        material = offeredMaterial;
        amountMb += accepted;
        sync();
        return accepted;
    }

    public boolean takeOutput(Player player) {
        if (output.isEmpty()) {
            return false;
        }
        ItemStack taken = output;
        output = ItemStack.EMPTY;
        if (!player.getInventory().add(taken)) {
            player.drop(taken, false);
        }
        sync();
        return true;
    }

    public boolean takeForm(Player player) {
        if (form.isEmpty() || !output.isEmpty() || amountMb > 0 || castingCapacityMb > 0
                || coolingTicks > 0 || !pendingOutput.isEmpty()) {
            return false;
        }
        ItemStack taken = form;
        form = ItemStack.EMPTY;
        if (!player.getInventory().add(taken)) {
            player.drop(taken, false);
        }
        sync();
        return true;
    }

    public void dropContents() {
        if (level != null) {
            takeFallbackItems().forEach(stack -> Block.popResource(level, worldPosition, stack));
        }
    }

    CompoundTag savePortableState(HolderLookup.Provider registries) {
        if (material == null
                && amountMb == 0
                && castingCapacityMb == 0
                && coolingTicks == 0
                && output.isEmpty()
                && pendingOutput.isEmpty()
                && form.isEmpty()
                && !consumeForm) {
            return new CompoundTag();
        }
        return saveWithoutMetadata(registries);
    }

    List<ItemStack> takeFallbackItems() {
        List<ItemStack> recovered = new ArrayList<>(2);
        if (!output.isEmpty()) {
            recovered.add(output);
            output = ItemStack.EMPTY;
        }
        if (!form.isEmpty()) {
            recovered.add(form);
            form = ItemStack.EMPTY;
        }
        if (!recovered.isEmpty()) {
            setChanged();
        }
        return recovered;
    }

    private boolean canProduce(ResourceLocation offeredMaterial) {
        if (isBasin(getBlockState())) {
            return FoundryCastingOutputs.output(offeredMaterial, true).isPresent();
        }
        Optional<FoundryCastRecipe> castRecipe = FoundryCastRegistry.findForCast(form);
        if (castRecipe.isPresent()) {
            return ToolTypeRegistry.template(castRecipe.get().template())
                    .filter(template -> template.allowsMaterial(offeredMaterial))
                    .map(template -> !template.outputStack(offeredMaterial).isEmpty())
                    .orElse(false);
        }
        Optional<FoundryCastRecipe> creationRecipe = FoundryCastRegistry.findForInput(form);
        if (creationRecipe.isPresent()) {
            return MaterialCatalog.GOLD.equals(offeredMaterial);
        }
        return form.isEmpty() && FoundryCastingOutputs.output(offeredMaterial, false).isPresent();
    }

    private ItemStack completedOutput(ResourceLocation pouredMaterial, int castAmountMb) {
        Optional<FoundryCastRecipe> castRecipe = FoundryCastRegistry.findForCast(form);
        if (castRecipe.isPresent()) {
            return ToolTypeRegistry.template(castRecipe.get().template())
                    .filter(template -> template.allowsMaterial(pouredMaterial))
                    .map(template -> {
                        ItemStack castPart = template.outputStack(pouredMaterial, ForgingQuality.CRUDE.score());
                        if (!castPart.isEmpty() && (castPart.get(ModDataComponents.TOOL_PART.get()) != null
                                || castPart.get(ModDataComponents.ARMOR_PART.get()) != null)) {
                            castPart.set(ModDataComponents.METALLURGY.get(), MetallurgyData.cast(pouredMaterial, castAmountMb));
                        }
                        return castPart;
                    })
                    .orElse(ItemStack.EMPTY);
        }
        Optional<FoundryCastRecipe> creationRecipe = FoundryCastRegistry.findForInput(form);
        if (creationRecipe.isPresent() && MaterialCatalog.GOLD.equals(pouredMaterial)) {
            return CastingMoldItem.create(creationRecipe.get().template());
        }
        if (!form.isEmpty()) {
            return ItemStack.EMPTY;
        }
        return FoundryCastingOutputs.output(pouredMaterial, isBasin(getBlockState())).orElse(ItemStack.EMPTY);
    }

    private void tryResolveLegacyJob() {
        if (material == null || amountMb <= 0 || !canProduce(material)) {
            return;
        }
        int liveCapacity = liveCapacityMb();
        if (amountMb > liveCapacity) {
            return;
        }
        ItemStack resolvedOutput = completedOutput(material, liveCapacity);
        if (resolvedOutput.isEmpty()) {
            return;
        }
        castingCapacityMb = liveCapacity;
        pendingOutput = resolvedOutput;
        consumeForm = !(form.getItem() instanceof CastingMoldItem);
        unresolvedLegacyJob = false;
        sync();
    }

    public static boolean isBasin(BlockState state) {
        return state.is(ModBlocks.FOUNDRY_CASTING_BASIN.get());
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (material != null) {
            tag.putString(MATERIAL_TAG, material.toString());
        }
        tag.putInt(AMOUNT_TAG, amountMb);
        tag.putInt(CAPACITY_TAG, castingCapacityMb);
        tag.putInt(COOLING_TAG, coolingTicks);
        if (!output.isEmpty()) {
            tag.put(OUTPUT_TAG, output.saveOptional(registries));
        }
        if (!pendingOutput.isEmpty()) {
            tag.put(PENDING_OUTPUT_TAG, pendingOutput.saveOptional(registries));
        }
        if (!form.isEmpty()) {
            tag.put(FORM_TAG, form.saveOptional(registries));
        }
        tag.putBoolean(CONSUME_FORM_TAG, consumeForm);
        tag.putBoolean(UNRESOLVED_LEGACY_TAG, unresolvedLegacyJob);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        form = ItemStack.parseOptional(registries, tag.getCompound(FORM_TAG));
        material = ResourceLocation.tryParse(tag.getString(MATERIAL_TAG));
        amountMb = Math.max(0, tag.getInt(AMOUNT_TAG));
        castingCapacityMb = Math.max(0, tag.getInt(CAPACITY_TAG));
        coolingTicks = Math.max(0, Math.min(COOLING_TICKS, tag.getInt(COOLING_TAG)));
        output = ItemStack.parseOptional(registries, tag.getCompound(OUTPUT_TAG));
        pendingOutput = ItemStack.parseOptional(registries, tag.getCompound(PENDING_OUTPUT_TAG));
        consumeForm = tag.getBoolean(CONSUME_FORM_TAG);
        unresolvedLegacyJob = tag.getBoolean(UNRESOLVED_LEGACY_TAG);
        if (amountMb == 0) {
            material = null;
            castingCapacityMb = 0;
            coolingTicks = 0;
            pendingOutput = ItemStack.EMPTY;
            consumeForm = false;
            unresolvedLegacyJob = false;
        } else if (castingCapacityMb == 0) {
            // Migrate casts saved before process snapshots existed. Never reduce a saved amount.
            castingCapacityMb = amountMb;
            pendingOutput = ItemStack.EMPTY;
            consumeForm = false;
            unresolvedLegacyJob = true;
        }
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
