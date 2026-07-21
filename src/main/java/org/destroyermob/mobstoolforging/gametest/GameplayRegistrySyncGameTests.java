package org.destroyermob.mobstoolforging.gametest;

import io.netty.buffer.Unpooled;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import org.destroyermob.mobstoolforging.MobsToolForging;
import org.destroyermob.mobstoolforging.network.GameplayRegistrySyncPayload;
import org.destroyermob.mobstoolforging.world.GameplayRegistrySyncStore;

@GameTestHolder(MobsToolForging.MOD_ID)
@PrefixGameTestTemplate(false)
public final class GameplayRegistrySyncGameTests {
    private GameplayRegistrySyncGameTests() {
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void gameplayRegistrySnapshotCodecRoundTrips(GameTestHelper helper) {
        GameplayRegistrySyncPayload expected = GameplayRegistrySyncPayload.capture();
        helper.assertTrue(expected.sections().size() == GameplayRegistrySyncStore.Section.values().length,
                "Gameplay synchronization snapshot omitted a registry section");
        helper.assertTrue(expected.entryCount() > 0, "Gameplay synchronization snapshot was unexpectedly empty");

        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), helper.getLevel().registryAccess());
        try {
            GameplayRegistrySyncPayload.STREAM_CODEC.encode(buffer, expected);
            GameplayRegistrySyncPayload decoded = GameplayRegistrySyncPayload.STREAM_CODEC.decode(buffer);
            helper.assertTrue(expected.equals(decoded), "Gameplay registry snapshot changed during network encoding");
        } finally {
            buffer.release();
        }
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void incompatibleGameplayRegistrySnapshotIsRejectedBeforeApply(GameTestHelper helper) {
        GameplayRegistrySyncPayload current = GameplayRegistrySyncPayload.capture();
        GameplayRegistrySyncPayload incompatible = new GameplayRegistrySyncPayload(
                GameplayRegistrySyncPayload.FORMAT_VERSION + 1,
                current.sections()
        );
        boolean rejected = false;
        try {
            incompatible.apply();
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        helper.assertTrue(rejected, "Incompatible gameplay registry snapshot was applied");
        helper.assertTrue(current.equals(GameplayRegistrySyncPayload.capture()),
                "Rejected gameplay registry snapshot changed the captured server data");
        helper.succeed();
    }

    @GameTest(template = "station_work_completion", timeoutTicks = 20)
    public static void incompleteGameplayRegistrySnapshotIsRejected(GameTestHelper helper) {
        EnumMap<GameplayRegistrySyncStore.Section, Map<ResourceLocation, String>> incomplete =
                new EnumMap<>(GameplayRegistrySyncStore.Section.class);
        incomplete.putAll(GameplayRegistrySyncPayload.capture().sections());
        incomplete.remove(GameplayRegistrySyncStore.Section.MATERIALS);
        boolean rejected = false;
        try {
            new GameplayRegistrySyncPayload(GameplayRegistrySyncPayload.FORMAT_VERSION, incomplete);
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        helper.assertTrue(rejected, "Gameplay snapshot without materials was accepted");
        helper.succeed();
    }
}
