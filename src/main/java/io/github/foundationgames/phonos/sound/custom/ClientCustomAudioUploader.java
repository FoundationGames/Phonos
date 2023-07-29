package io.github.foundationgames.phonos.sound.custom;

import io.github.foundationgames.phonos.network.ClientPayloadPackets;
import io.github.foundationgames.phonos.sound.stream.AudioDataQueue;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientCustomAudioUploader {
    public static final ExecutorService UPLOAD_POOL = Executors.newFixedThreadPool(1);

    public static final Long2ObjectMap<AudioDataQueue> UPLOAD_QUEUE = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

    public static void queueForUpload(long id, AudioDataQueue audio) {
        UPLOAD_QUEUE.put(id, audio);
    }

    public static void sendUploadPackets(long id) {
        if (UPLOAD_QUEUE.containsKey(id)) {
            UPLOAD_POOL.submit(() -> sendAudioDataPackets(id));
        }
    }

    private static void sendAudioDataPackets(long id) {
        var aud = UPLOAD_QUEUE.get(id);

        while (!aud.data.isEmpty() && UPLOAD_QUEUE.containsKey(id)) {
            ClientPayloadPackets.sendAudioUploadPacket(id, aud.sampleRate, aud.data.removeFirst().rewind(), aud.data.isEmpty());
        }

        UPLOAD_QUEUE.remove(id);
    }

    public static void cancelUpload(long id) {
        UPLOAD_QUEUE.remove(id);
    }

    public static void reset() {
        UPLOAD_QUEUE.clear();
    }
}
