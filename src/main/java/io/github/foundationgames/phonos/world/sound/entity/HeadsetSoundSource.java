package io.github.foundationgames.phonos.world.sound.entity;

import io.github.foundationgames.phonos.item.HeadsetItem;
import io.github.foundationgames.phonos.item.SoundEmitterItem;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitter;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterStorage;
import io.github.foundationgames.phonos.sound.emitter.SoundSource;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.world.World;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class HeadsetSoundSource implements SoundSource {
    public static final HeadsetSoundSource INSTANCE = new HeadsetSoundSource();

    public LongSet parentEmitters = new LongOpenHashSet();
    public Set<SoundEmitter> createdEmitters = new HashSet<>();

    private double x, y = 999999, z;

    private HeadsetSoundSource() {
    }

    @Override
    public double x() {
        return x;
    }

    @Override
    public double y() {
        return y;
    }

    @Override
    public double z() {
        return z;
    }

    @Override
    public void onSoundPlayed(World world, SoundData sound) {
    }

    public void tick(MinecraftClient client) {
        if (client.world == null) {
            return;
        }

        var createdEmittersToRemove = new LongOpenHashSet();
        createdEmitters.forEach(em -> createdEmittersToRemove.add(em.emitterId()));

        parentEmitters.clear();
        var player = client.player;
        var emitters = SoundEmitterStorage.getInstance(client.world);

        if (player != null) {
            x = player.getX();
            y = player.getY() + 2.25;
            z = player.getZ();

            if (player.getEquippedStack(EquipmentSlot.HEAD).getItem() instanceof HeadsetItem) {
                var inv = player.getInventory();

                for (int i = 0; i < inv.size(); i++) {
                    var stack = inv.getStack(i);

                    if (stack.getItem() instanceof SoundEmitterItem emitter && emitter.hasParentEmitter(stack)) {
                        long emitterId = emitter.getParentEmitter(stack);
                        parentEmitters.add(emitterId);

                        if (emitter.createsEmitter(stack) && createdEmitters.stream().noneMatch(em -> em.emitterId() == emitterId)) {
                            var newEm = new InvToHeadsetSoundEmitter(emitterId, this);
                            createdEmitters.add(newEm);

                            if (!emitters.isLoaded(emitterId)) {
                                emitters.addEmitter(newEm);
                            }
                        }

                        createdEmittersToRemove.remove(emitterId);
                    }
                }
            }
        }

        for (long toRemove : createdEmittersToRemove) {
            emitters.removeEmitter(toRemove);
        }
        createdEmitters.removeIf(em -> createdEmittersToRemove.contains(em.emitterId()));
    }

    public static class InvToHeadsetSoundEmitter implements SoundEmitter {
        private final long emitterId;
        private final HeadsetSoundSource headset;

        public InvToHeadsetSoundEmitter(long emitterId, HeadsetSoundSource headset) {
            this.emitterId = emitterId;
            this.headset = headset;
        }

        @Override
        public long emitterId() {
            return emitterId;
        }

        @Override
        public void forEachSource(Consumer<SoundSource> action) {
            action.accept(this.headset);
        }

        @Override
        public void forEachChild(LongConsumer action) {
        }
    }
}
