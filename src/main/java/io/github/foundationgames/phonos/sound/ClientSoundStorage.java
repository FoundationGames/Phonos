package io.github.foundationgames.phonos.sound;

import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import io.github.foundationgames.phonos.world.sound.data.SoundDataTypes;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClientSoundStorage extends SoundStorage {
    private static final Map<SoundData.Type<?>, SoundInstanceFactory<SoundData>> CLIENT_SOUND_PROVIDERS = new HashMap<>();
    private final Long2ObjectMap<SoundInstance> playingSounds = new Long2ObjectOpenHashMap<>();
    private final Set<SoundEmitterTree> activeEmitterTrees = new HashSet<>();

    @SuppressWarnings("unchecked")
    public static <S extends SoundData> void registerProvider(SoundData.Type<S> type, SoundInstanceFactory<S> provider) {
        CLIENT_SOUND_PROVIDERS.put(type, (d, t, r) -> provider.create((S) d, t, r));
    }

    public static @Nullable SoundInstance provideSound(SoundData data, SoundEmitterTree tree, Random random) {
        return CLIENT_SOUND_PROVIDERS.get(data.type).create(data, tree, random);
    }

    @Override
    public void play(World world, SoundData data, SoundEmitterTree tree) {
        tree.updateClient(world);

        var inst = provideSound(data, tree, world.getRandom());
        MinecraftClient.getInstance().getSoundManager().play(inst);

        playingSounds.put(data.emitterId, inst);
        activeEmitterTrees.add(tree);

        this.notifySoundSourcesPlayed(world, data, tree);
    }

    @Override
    public void stop(World world, long soundUniqueId) {
        if (playingSounds.containsKey(soundUniqueId)) {
            var sound = playingSounds.get(soundUniqueId);
            var soundMgr = MinecraftClient.getInstance().getSoundManager();

            if (soundMgr.isPlaying(sound)) {
                soundMgr.stop(sound);
            }
            playingSounds.remove(soundUniqueId);
        }

        activeEmitterTrees.removeIf(tree -> tree.rootId == soundUniqueId);
    }

    @Override
    public void update(SoundEmitterTree.Delta delta) {
        for (var tree : activeEmitterTrees) {
            if (tree.rootId == delta.rootId()) {
                delta.apply(tree);
            }
        }
    }

    @Override
    public void tick(World world) {
        this.playingSounds.long2ObjectEntrySet().removeIf(e -> {
            if (!MinecraftClient.getInstance().getSoundManager().isPlaying(e.getValue())) {
                activeEmitterTrees.removeIf(tree -> tree.rootId == e.getLongKey());
                return true;
            }

            return false;
        });

        this.activeEmitterTrees.forEach(tree -> tree.updateClient(world));
    }

    public interface SoundInstanceFactory<S extends SoundData> {
        SoundInstance create(S data, SoundEmitterTree tree, Random random);
    }

    public static void initClient() {
        registerProvider(SoundDataTypes.SOUND_EVENT, (data, list, random) ->
                new MultiSourceSoundInstance(list, data.sound.value(), random, data.volume, data.pitch));
        registerProvider(SoundDataTypes.NOTE_BLOCK, (data, list, random) ->
                new MultiSourceSoundInstance(list, data.sound.value(), random, data.volume, data.pitch));
    }
}
