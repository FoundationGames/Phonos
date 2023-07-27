package io.github.foundationgames.phonos.sound.stream;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.client.sound.AudioStream;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class ClientIncomingStreamHandler {
    public static final Long2ObjectMap<AudioDataQueue> STREAMS = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    private static final Long2ObjectMap<CompletableFuture<AudioStream>> WAITING = new Long2ObjectOpenHashMap<>();

    public static CompletableFuture<AudioStream> getStream(long id) {
        if (STREAMS.containsKey(id)) {
            return CompletableFuture.completedFuture(new QueueAudioStream(STREAMS.get(id)));
        }

        var future = new CompletableFuture<AudioStream>();
        WAITING.put(id, future);
        return future;
    }

    public static void receiveStream(long id, int sampleRate, ByteBuffer samples) {
        if (!STREAMS.containsKey(id)) {
            STREAMS.put(id, new AudioDataQueue(sampleRate));
        }

        var queue = STREAMS.get(id);
        samples.rewind();
        queue.data.addLast(samples);

        if (WAITING.containsKey(id)) {
            WAITING.remove(id).complete(new QueueAudioStream(queue));
        }
    }

    public static void endStream(long id) {
        STREAMS.remove(id);
    }

    public static void reset() {
        STREAMS.clear();
    }
}
