package io.github.foundationgames.phonos.client;

import io.github.foundationgames.phonos.sound.MultiPositionedSoundInstance;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Environment(EnvType.CLIENT)
public class ClientRecieverStorage {
    private static final Int2ObjectMap<LinkedHashSet<BlockPos>> blockStorage = new Int2ObjectOpenHashMap<>();
    // TODO:
    private static final Int2ObjectMap<LinkedHashSet<Entity>> entityStorage = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Map<BlockPos, SoundInstance>> stoppableSoundStorage = new Int2ObjectOpenHashMap<>();
    private static final List<SoundConsumer> playSoundCallbacks = new ArrayList<>();

    public static void addReciever(int channel, BlockPos pos) {
        blockStorage.putIfAbsent(channel, new LinkedHashSet<>());
        blockStorage.get(channel).add(pos);
    }

    public static void removeReciever(int channel, BlockPos pos) {
        blockStorage.putIfAbsent(channel, new LinkedHashSet<>());
        blockStorage.get(channel).remove(pos);
    }

    public static void addEntityReciever(int channel, Entity ent) {
        entityStorage.putIfAbsent(channel, new LinkedHashSet<>());
        entityStorage.get(channel).add(ent);
    }

    public static void removeEntityReciever(int channel, Entity ent) {
        entityStorage.putIfAbsent(channel, new LinkedHashSet<>());
        entityStorage.get(channel).remove(ent);
    }

    public static void clear() {
        blockStorage.clear();
        entityStorage.clear();
    }

    public static void playSound(SoundEvent sound, int channel, float volume, float pitch) {
        playSound(sound.getId(), channel, volume, pitch);
    }

    public static void playSound(Identifier sound, int channel, float volume, float pitch) {
        blockStorage.putIfAbsent(channel, new LinkedHashSet<>());
        entityStorage.putIfAbsent(channel, new LinkedHashSet<>());
        MinecraftClient.getInstance().getSoundManager().play(new MultiPositionedSoundInstance(blockStorage.get(channel), entityStorage.get(channel), sound, volume, pitch));
        for(SoundConsumer c : playSoundCallbacks) c.apply(sound, blockStorage.get(channel), entityStorage.get(channel), channel, volume, pitch, false);
    }

    public static void playStoppableSound(BlockPos pos, SoundEvent sound, int channel, float volume, float pitch) {
        playStoppableSound(pos, sound.getId(), channel, volume, pitch);
    }

    public static void playStoppableSound(BlockPos pos, Identifier sound, int channel, float volume, float pitch) {
        blockStorage.putIfAbsent(channel, new LinkedHashSet<>());
        entityStorage.putIfAbsent(channel, new LinkedHashSet<>());
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();
        tryStopSound(pos, channel);
        SoundInstance instance = new MultiPositionedSoundInstance(blockStorage.get(channel), entityStorage.get(channel), sound, volume, pitch);
        manager.play(instance);
        if(!stoppableSoundStorage.containsKey(channel)) stoppableSoundStorage.put(channel, new HashMap<>());
        stoppableSoundStorage.get(channel).put(pos, instance);
        for(SoundConsumer c : playSoundCallbacks) c.apply(sound, blockStorage.get(channel), entityStorage.get(channel), channel, volume, pitch, true);
    }

    public static void tryStopSound(BlockPos pos, int channel) {
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();
        if(stoppableSoundStorage.containsKey(channel)) {
            manager.stop(stoppableSoundStorage.get(channel).get(pos));
            stoppableSoundStorage.get(channel).remove(pos);
        }
    }

    public static boolean isStoppablePlaying(BlockPos pos, int channel) {
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();
        return manager.isPlaying(stoppableSoundStorage.get(channel).get(pos));
    }

    public static boolean isChannelPlaying(int channel) {
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();
        if(stoppableSoundStorage.containsKey(channel)) {
            for (BlockPos pos : stoppableSoundStorage.get(channel).keySet()) {
                if (manager.isPlaying(stoppableSoundStorage.get(channel).get(pos))) return true;
            }
        }
        return false;
    }

    @Environment(EnvType.CLIENT)
    public static void registerPlaySoundCallback(SoundConsumer consumer) {
        playSoundCallbacks.add(consumer);
    }

    public static void init() {}

    @FunctionalInterface
    public interface SoundConsumer {
        void apply(Identifier sound, Set<BlockPos> blocks, Set<Entity> entities, int channel, float volume, float pitch, boolean stoppable);
    }
}
