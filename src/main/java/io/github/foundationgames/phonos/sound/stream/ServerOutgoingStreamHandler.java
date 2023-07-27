package io.github.foundationgames.phonos.sound.stream;

import io.github.foundationgames.phonos.network.PayloadPackets;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ServerOutgoingStreamHandler {
    public static final Long2ObjectMap<Streaming> STREAMS = new Long2ObjectOpenHashMap<>();

    public static void startStream(long streamId, AudioDataQueue data, MinecraftServer server) {
        STREAMS.put(streamId, new Streaming(streamId, data, server));
    }

    public static void endStream(long streamId, MinecraftServer server) {
        STREAMS.remove(streamId);

        for (var player : server.getPlayerManager().getPlayerList()) {
            PayloadPackets.sendAudioStreamEnd(player, streamId);
        }
    }

    public static void tick(MinecraftServer server) {
        STREAMS.forEach((k, v) -> v.tick(server));
    }

    public static void reset() {
        STREAMS.clear();
    }

    public static class Streaming {
        private int tickDelay = -3;
        private final Set<UUID> listeners = new HashSet<>();
        public final long streamId;
        public final AudioDataQueue queue;

        public Streaming(long streamId, AudioDataQueue queue, MinecraftServer server) {
            this.streamId = streamId;
            this.queue = queue;
            server.getPlayerManager().getPlayerList().stream().map(Entity::getUuid).forEach(listeners::add);
        }

        void tick(MinecraftServer server) {
            listeners.removeIf(id -> server.getPlayerManager().getPlayer(id) == null);

            if (tickDelay <= 0) {
                int sampleRate = queue.sampleRate;

                for (int i = 0; i < 1 - tickDelay && !queue.data.isEmpty(); i++) {
                    var samples = queue.data.removeFirst();

                    for (var id : listeners) {
                        samples.rewind();
                        PayloadPackets.sendAudioStreamData(server.getPlayerManager().getPlayer(id), streamId, sampleRate, samples);
                    }
                }

                float sectionTimeSec = (float) AudioDataQueue.SAMPLE_SECTION / sampleRate;

                tickDelay = Math.min((int) Math.floor(20 * sectionTimeSec) - 5, 1);
            } else tickDelay--;
        }
    }
}
