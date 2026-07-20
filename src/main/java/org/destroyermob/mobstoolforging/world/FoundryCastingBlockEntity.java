package org.destroyermob.mobstoolforging.world;

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
    private static final String COOLING_TAG = "Cooling";
    private static final String OUTPUT_TAG = "Output";
    private static final String FORM_TAG = "Form";

    @Nullable
    private ResourceLocation material;
    private int amountMb;
    private int coolingTicks;
    private ItemStack output = ItemStack.EMPTY;
    private ItemStack form = ItemStack.EMPTY;

    public FoundryCastingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_CASTING.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryCastingBlockEntity casting) {
        if (casting.amountMb < casting.capacityMb() || !casting.output.isEmpty()) {
            return;
        }
        casting.coolingTicks++;
        if (casting.coolingTicks >= COOLING_TICKS && casting.material != null) {
            casting.output = casting.completedOutput(casting.material);
            if (!casting.output.isEmpty() && !(casting.form.getItem() instanceof CastingMoldItem)) {
                casting.form = ItemStack.EMPTY;
            }
            casting.material = null;
            casting.amountMb = 0;
            casting.coolingTicks = 0;
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
        return amountMb / (float) capacityMb();
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
        return material == null ? ItemStack.EMPTY : completedOutput(material).copy();
    }

    public boolean canInsertForm(ItemStack stack) {
        return !isBasin(getBlockState())
                && form.isEmpty()
                && output.isEmpty()
                && material == null
                && amountMb == 0
                && coolingTicks == 0
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
        if (!canProduce(offeredMaterial)) {
            return 0;
        }
        return capacityMb() - amountMb;
    }

    public int receive(ResourceLocation offeredMaterial, int offeredMb) {
        int accepted = Math.min(Math.max(0, offeredMb), remainingCapacity(offeredMaterial));
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
        if (form.isEmpty() || !output.isEmpty() || amountMb > 0 || coolingTicks > 0) {
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
            if (!output.isEmpty()) {
                Block.popResource(level, worldPosition, output);
                output = ItemStack.EMPTY;
            }
            if (!form.isEmpty()) {
                Block.popResource(level, worldPosition, form);
                form = ItemStack.EMPTY;
            }
        }
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

    private ItemStack completedOutput(ResourceLocation pouredMaterial) {
        Optional<FoundryCastRecipe> castRecipe = FoundryCastRegistry.findForCast(form);
        if (castRecipe.isPresent()) {
            return ToolTypeRegistry.template(castRecipe.get().template())
                    .filter(template -> template.allowsMaterial(pouredMaterial))
                    .map(template -> {
                        ItemStack castPart = template.outputStack(pouredMaterial, ForgingQuality.CRUDE.score());
                        if (!castPart.isEmpty() && (castPart.get(ModDataComponents.TOOL_PART.get()) != null
                                || castPart.get(ModDataComponents.ARMOR_PART.get()) != null)) {
                            castPart.set(ModDataComponents.METALLURGY.get(), MetallurgyData.cast(pouredMaterial, capacityMb()));
                        }
                        return castPart;
                    })
                    .orElse(ItemStack.EMPTY);
        }
        Optional<FoundryCastRecipe> creationRecipe = FoundryCastRegistry.findForInput(form);
        if (creationRecipe.isPresent() && MaterialCatalog.GOLD.equals(pouredMaterial)) {
            return CastingMoldItem.create(creationRecipe.get().template());
        }
        return FoundryCastingOutputs.output(pouredMaterial, isBasin(getBlockState())).orElse(ItemStack.EMPTY);
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
        tag.putInt(COOLING_TAG, coolingTicks);
        if (!output.isEmpty()) {
            tag.put(OUTPUT_TAG, output.saveOptional(registries));
        }
        if (!form.isEmpty()) {
            tag.put(FORM_TAG, form.saveOptional(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        form = ItemStack.parseOptional(registries, tag.getCompound(FORM_TAG));
        material = ResourceLocation.tryParse(tag.getString(MATERIAL_TAG));
        amountMb = Math.max(0, Math.min(capacityMb(), tag.getInt(AMOUNT_TAG)));
        coolingTicks = Math.max(0, Math.min(COOLING_TICKS, tag.getInt(COOLING_TAG)));
        output = ItemStack.parseOptional(registries, tag.getCompound(OUTPUT_TAG));
        if (amountMb == 0) {
            material = null;
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
