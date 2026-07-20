package org.destroyermob.mobstoolforging.world;

import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class FoundryFaucetBlockEntity extends BlockEntity {
    private static final String POURING_MATERIAL_TAG = "PouringMaterial";

    @Nullable
    private ResourceLocation pouringMaterial;

    public FoundryFaucetBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_FAUCET.get(), pos, state);
    }

    public Optional<ResourceLocation> pouringMaterial() {
        return Optional.ofNullable(pouringMaterial);
    }

    public void setPouringMaterial(@Nullable ResourceLocation material) {
        if (Objects.equals(pouringMaterial, material)) {
            return;
        }
        pouringMaterial = material;
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (pouringMaterial != null) {
            tag.putString(POURING_MATERIAL_TAG, pouringMaterial.toString());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        pouringMaterial = ResourceLocation.tryParse(tag.getString(POURING_MATERIAL_TAG));
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
