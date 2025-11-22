package badasintended.slotlink.mixin;

import badasintended.slotlink.network.NetworkState;
import badasintended.slotlink.network.NetworkStateHolder;
import java.util.List;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.progress.ChunkProgressListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin implements NetworkStateHolder {

    @Shadow
    public abstract DimensionDataStorage getDataStorage();

    @Unique
    private NetworkState networkState;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void slotlink$initNetworkState(MinecraftServer server, Executor workerExecutor, LevelStorageSource.LevelStorageAccess session, ServerLevelData properties, ResourceKey<?> worldKey, LevelStem dimensionOptions, ChunkProgressListener worldGenerationProgressListener, boolean debugWorld, long seed, List<?> spawners, boolean shouldTickTime, CallbackInfo ci) {
        ServerLevel self = (ServerLevel) (Object) this;
        networkState = getDataStorage().computeIfAbsent(nbt -> NetworkState.create(self, nbt), NetworkState::new, "slotlink");
    }

    @NotNull
    @Override
    public NetworkState slotlink$getNetworkState() {
        return networkState;
    }

}
