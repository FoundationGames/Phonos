package io.github.foundationgames.phonos.sound.emitter;

import io.github.foundationgames.phonos.Phonos;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class SoundEmitterStorage {
    private static SoundEmitterStorage CLIENT;
    private static final Map<RegistryKey<World>, SoundEmitterStorage> SERVER = new HashMap<>();

    public static final List<Function<World, SoundEmitter>> DEFAULT_EMITTERS = new ArrayList<>();

    private static final SoundEmitterStorage INVALID = new SoundEmitterStorage(null) {
        @Override
        public boolean isLoaded(long uniqueId) {
            Phonos.LOG.error("Tried to query emitter " + Long.toHexString(uniqueId) + " in invalid world");
            return false;
        }

        @Override
        public void addEmitter(SoundEmitter emitter) {
            Phonos.LOG.error("Tried to add emitter " + Long.toHexString(emitter.emitterId()) + " in invalid world");
        }

        @Override
        public SoundEmitter getEmitter(long uniqueId) {
            Phonos.LOG.error("Tried to get emitter " + Long.toHexString(uniqueId) + " in invalid world");
            return null;
        }

        @Override
        public void removeEmitter(long uniqueId) {
            Phonos.LOG.error("Tried to remove emitter " + Long.toHexString(uniqueId) + " in invalid world");
        }
    };

    private final Long2ObjectMap<SoundEmitter> emitters = new Long2ObjectOpenHashMap<>();

    protected SoundEmitterStorage(World world) {
        if (world != null) for (var factory : DEFAULT_EMITTERS) {
            this.addEmitter(factory.apply(world));
        }
    }

    public boolean isLoaded(long uniqueId) {
        return emitters.containsKey(uniqueId);
    }

    public void addEmitter(SoundEmitter emitter) {
        emitters.put(emitter.emitterId(), emitter);
    }

    public SoundEmitter getEmitter(long uniqueId) {
        return emitters.get(uniqueId);
    }

    public void removeEmitter(long uniqueId) {
        emitters.remove(uniqueId);
    }

    public void removeEmitter(SoundEmitter emitter) {
        removeEmitter(emitter.emitterId());
    }

    public static SoundEmitterStorage getInstance(World world) {
        if (world.isClient()) {
            if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
                if (CLIENT == null) {
                    CLIENT = new SoundEmitterStorage(world);
                }

                return CLIENT;
            }
        }

        if (world instanceof ServerWorld sWorld) {
            return SERVER.computeIfAbsent(sWorld.getRegistryKey(), w -> new SoundEmitterStorage(world));
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
