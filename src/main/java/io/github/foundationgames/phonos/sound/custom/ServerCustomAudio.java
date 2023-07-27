package io.github.foundationgames.phonos.sound.custom;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.sound.stream.AudioDataQueue;
import io.github.foundationgames.phonos.util.PhonosUtil;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class ServerCustomAudio {
    public static final String FILE_EXT = ".phonosaud";

    public static final Long2ObjectMap<AudioDataQueue> UPLOADING = new Long2ObjectOpenHashMap<>();
    public static final Long2ObjectMap<AudioDataQueue> SAVED = new Long2ObjectOpenHashMap<>();

    private static final Object2LongMap<UUID> UPLOAD_SESSIONS = new Object2LongOpenHashMap<>();

    public static boolean hasSaved(long id) {
        return SAVED.containsKey(id);
    }

    public static @Nullable AudioDataQueue loadSaved(long id) {
        if (hasSaved(id)) {
            return SAVED.get(id).copy(ByteBuffer::allocate);
        }

        return null;
    }

    public static void onPlayerDisconnect(ServerPlayerEntity player) {
        endUploadSession(player.getUuid());
    }

    public static void beginUploadSession(ServerPlayerEntity player, long streamId) {
        UPLOAD_SESSIONS.put(player.getUuid(), streamId);
    }

    public static void endUploadSession(UUID player) {
        long id = UPLOAD_SESSIONS.removeLong(player);
        UPLOADING.remove(id);
    }

    public static void receiveUpload(MinecraftServer srv, ServerPlayerEntity player, long id, int sampleRate, ByteBuffer samples, boolean last) {
        if (UPLOAD_SESSIONS.containsKey(player.getUuid()) && UPLOAD_SESSIONS.getLong(player.getUuid()) == id) {
            var aud = UPLOADING.computeIfAbsent(id, k -> new AudioDataQueue(sampleRate));
            aud.data.addLast(samples);

            if (last) {
                SAVED.put(id, aud);
                endUploadSession(player.getUuid());

                try {
                    saveOnly(id, PhonosUtil.getCustomSoundFolder(srv));
                } catch (IOException ex) {
                    Phonos.LOG.error("Error saving uploaded sound", ex);
                }
            }
        }
    }

    public static void deleteSaved(MinecraftServer srv, long id) {
        SAVED.remove(id);

        try {
            deleteOnly(id, PhonosUtil.getCustomSoundFolder(srv));

            Phonos.LOG.info("Saved audio with ID {} was deleted.", Long.toHexString(id));
        } catch (IOException ex) {
            Phonos.LOG.error("Error saving uploaded sound", ex);
        }
    }

    public static void reset() {
        UPLOADING.clear();
        SAVED.clear();
        UPLOAD_SESSIONS.clear();
    }

    public static void saveOnly(long id, Path folder) throws IOException {
        var aud = SAVED.get(id);
        var filename = Long.toHexString(id) + FILE_EXT;
        var path = folder.resolve(filename);

        try (var out = Files.newOutputStream(path)) {
            aud.write(out);
        }
    }

    public static void deleteOnly(long id, Path folder) throws IOException {
        var filename = Long.toHexString(id) + FILE_EXT;
        var path = folder.resolve(filename);

        Files.deleteIfExists(path);
    }

    public static void save(Path folder) throws IOException {
        var doNotDelete = new LongArraySet();

        for (long id : SAVED.keySet()) {
            saveOnly(id, folder);
            doNotDelete.add(id);
        }

        try (var paths = Files.walk(folder, 1)) {
            for (var path : paths.toList()) {
                var filename = path.getFileName().toString();
                if (filename.endsWith(FILE_EXT)) {
                    var hexStr = filename.replace(FILE_EXT, "");

                    try {
                        long id = Long.parseUnsignedLong(hexStr, 16);
                        if (!doNotDelete.contains(id)) {
                            Files.deleteIfExists(path);
                        }

                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public static void load(Path folder) throws IOException {
        SAVED.clear();
        try (var paths = Files.walk(folder, 1)) {
            for (var path : paths.toList()) {
                var filename = path.getFileName().toString();
                if (filename.endsWith(FILE_EXT)) {
                    var hexStr = filename.replace(FILE_EXT, "");

                    try {
                        long id = Long.parseUnsignedLong(hexStr, 16);
                        try (var in = Files.newInputStream(path)) {
                            var aud = AudioDataQueue.read(in, ByteBuffer::allocate);
                            SAVED.put(id, aud);
                        }

                    } catch (NumberFormatException ex) {
                        Phonos.LOG.error("Audio data " + filename + " has invalid name");
                    }
                }
            }
        }
    }
}