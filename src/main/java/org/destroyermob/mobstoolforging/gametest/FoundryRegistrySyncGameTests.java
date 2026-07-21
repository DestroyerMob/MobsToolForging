package org.destroyermob.mobstoolforging.gametest;

import io.netty.buffer.Unpooled;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.network.FoundryRegistrySyncPayload;

@GameTestHolder(MobsToolForging.MOD_ID)
@PrefixGameTestTemplate(false)
public final class FoundryRegistrySyncGameTests {
    private FoundryRegistrySyncGameTests() {
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void registrySnapshotCodecRoundTrips(GameTestHelper helper) {
        FoundryRegistrySyncPayload expected = FoundryRegistrySyncPayload.capture();
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            FoundryRegistrySyncPayload.STREAM_CODEC.encode(buffer, expected);
            FoundryRegistrySyncPayload decoded = FoundryRegistrySyncPayload.STREAM_CODEC.decode(buffer);
            helper.assertTrue(expected.equals(decoded), "Foundry registry snapshot changed during network encoding");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void incompatibleRegistrySnapshotIsRejectedBeforeApply(GameTestHelper helper) {
        FoundryRegistrySyncPayload current = FoundryRegistrySyncPayload.capture();
        FoundryRegistrySyncPayload incompatible = new FoundryRegistrySyncPayload(
                FoundryRegistrySyncPayload.FORMAT_VERSION + 1,
                current.meltingRecipes(),
                current.meltingPoints(),
                current.fuelRecipes(),
                current.alloyRecipes(),
                current.castRecipes()
        );
        boolean rejected = false;
        try {
            incompatible.apply();
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        helper.assertTrue(rejected, "Incompatible foundry registry snapshot was applied");
        helper.assertTrue(current.equals(FoundryRegistrySyncPayload.capture()),
                "Rejected foundry registry snapshot partially changed live registries");
        helper.succeed();
    }
}
