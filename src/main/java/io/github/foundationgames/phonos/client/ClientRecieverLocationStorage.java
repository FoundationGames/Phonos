package io.github.foundationgames.phonos.client;

import com.google.common.collect.Maps;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.sound.MultiPositionedSoundInstance;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ClientRecieverLocationStorage {
    private static final Int2ObjectMap<ArrayList<Long>> channelStorage = new Int2ObjectOpenHashMap<>();
    private static final Int2ObjectMap<Map<BlockPos, SoundInstance>> stoppableSoundStorage = new Int2ObjectOpenHashMap<>();
    private static final List<SoundConsumer> playSoundCallbacks = new ArrayList<>();

    public static void addReciever(int channel, long pos) {
        channelStorage.putIfAbsent(channel, new ArrayList<>());
        ArrayList<Long> l = channelStorage.get(channel);
        if(!l.contains(pos)) l.add(pos);
    }

    public static void removeReciever(int channel, long pos) {
        channelStorage.putIfAbsent(channel, new ArrayList<>());
        channelStorage.get(channel).remove(pos);
    }

    public static void clear() {
        channelStorage.clear();
    }

    public static void playSound(SoundEvent sound, int channel, float volume, float pitch) {
        playSound(sound.getId(), channel, volume, pitch);
    }

    public static void playSound(Identifier sound, int channel, float volume, float pitch) {
        MinecraftClient.getInstance().getSoundManager().play(new MultiPositionedSoundInstance(channelStorage.get(channel), sound, volume, pitch));
        for(SoundConsumer c : playSoundCallbacks) c.apply(sound, channelStorage.get(channel), channel, volume, pitch, false);
    }

    public static void playStoppableSound(BlockPos pos, SoundEvent sound, int channel, float volume, float pitch) {
        playStoppableSound(pos, sound.getId(), channel, volume, pitch);
    }

    public static void playStoppableSound(BlockPos pos, Identifier sound, int channel, float volume, float pitch) {
        SoundManager manager = MinecraftClient.getInstance().getSoundManager();
        tryStopSound(pos, channel);
        SoundInstance instance = new MultiPositionedSoundInstance(channelStorage.get(channel), sound, volume, pitch);
        manager.play(instance);
        if(!stoppableSoundStorage.containsKey(channel)) stoppableSoundStorage.put(channel, new HashMap<>());
        stoppableSoundStorage.get(channel).put(pos, instance);
        for(SoundConsumer c : playSoundCallbacks) c.apply(sound, channelStorage.get(channel), channel, volume, pitch, true);
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
        void apply(Identifier sound, List<Long> positions, int channel, float volume, float pitch, boolean stoppable);
    }
}
