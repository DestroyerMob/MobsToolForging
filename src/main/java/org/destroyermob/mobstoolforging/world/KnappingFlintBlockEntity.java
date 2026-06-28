package org.destroyermob.mobstoolforging.world;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.destroyermob.mobstoolforging.registry.ModBlockEntities;

public class KnappingFlintBlockEntity extends BlockEntity {
    public static final int REQUIRED_HITS = 4;
    private static final String TARGET_TAG = "Target";
    private static final String HIT_COUNT_TAG = "HitCount";

    private KnappingTarget target = KnappingTarget.SWORD_BLADE;
    private int hitCount;

    public KnappingFlintBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.KNAPPING_FLINT.get(), pos, blockState);
    }

    public KnappingTarget target() {
        return target;
    }

    public int hitCount() {
        return hitCount;
    }

    public void cycleTarget(int delta) {
        target = target.cycle(delta);
        hitCount = 0;
        sync();
    }

    public boolean hit() {
        if (hitCount < REQUIRED_HITS) {
            hitCount++;
            sync();
        }
        return hitCount >= REQUIRED_HITS;
    }

    public ItemStack outputStack() {
        return target.createOutput();
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
        tag.putString(TARGET_TAG, target.id());
        tag.putInt(HIT_COUNT_TAG, hitCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        target = tag.contains(TARGET_TAG) ? KnappingTarget.byId(tag.getString(TARGET_TAG)) : KnappingTarget.SWORD_BLADE;
        hitCount = Mth.clamp(tag.getInt(HIT_COUNT_TAG), 0, REQUIRED_HITS);
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
