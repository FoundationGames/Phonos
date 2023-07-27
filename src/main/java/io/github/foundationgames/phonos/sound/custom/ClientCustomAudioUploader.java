package io.github.foundationgames.phonos.sound.custom;

import io.github.foundationgames.phonos.network.ClientPayloadPackets;
import io.github.foundationgames.phonos.sound.stream.AudioDataQueue;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

public class ClientCustomAudioUploader {
    public static final Long2ObjectMap<AudioDataQueue> UPLOAD_QUEUE = new Long2ObjectOpenHashMap<>();

    public static void queueForUpload(long id, AudioDataQueue audio) {
        UPLOAD_QUEUE.put(id, audio);
    }

    public static void sendUploadPackets(long id) {
        if (UPLOAD_QUEUE.containsKey(id)) {
            var aud = UPLOAD_QUEUE.get(id);

            while (!aud.data.isEmpty()) {
                ClientPayloadPackets.sendAudioUploadPacket(id, aud.sampleRate, aud.data.removeFirst().rewind(), aud.data.isEmpty());
            }

            UPLOAD_QUEUE.remove(id);
        }
    }

    public static void reset() {
        UPLOAD_QUEUE.clear();
    }
}
