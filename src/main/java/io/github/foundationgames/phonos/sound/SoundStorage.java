package io.github.foundationgames.phonos.sound;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public abstract class SoundStorage {
    private static SoundStorage CLIENT;
    private static final Map<ServerWorld, SoundStorage> SERVER = new HashMap<>();
    private static final SoundStorage INVALID = new SoundStorage() {
        @Override
        public void play(World world, SoundData data, SoundEmitterTree tree) {
            Phonos.LOG.error("Playing " + data.emitterId + " of type " + data.type + " in an invalid world");
        }

        @Override
        public void stop(World world, long emitterId) {
            Phonos.LOG.error("Stopping " + Long.toHexString(emitterId) + " in an invalid world");
        }

        @Override
        public void tick(World world) {
        }
    };

    protected void notifySoundSourcesPlayed(World world, SoundData data, SoundEmitterTree tree) {
        tree.forEachSource(world, source -> {
            source.onSoundPlayed(world, data);
        });
    }

    public abstract void play(World world, SoundData data, SoundEmitterTree tree);

    public abstract void stop(World world, long soundUniqueId);

    public abstract void tick(World world);

    public static SoundStorage getInstance(World world) {
        if (world.isClient()) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                if (CLIENT == null) {
                    CLIENT = new ClientSoundStorage();
                }

                return CLIENT;
            }
        }

        if (world instanceof ServerWorld sWorld) {
            return SERVER.computeIfAbsent(sWorld, w -> new ServerSoundStorage());
        }

        return INVALID;
    }

    public static void serverReset() {
        SERVER.clear();
    }

    public static void clientReset() {
        CLIENT = null;
    }
}
