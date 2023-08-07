package io.github.foundationgames.phonos.sound.custom;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.sound.stream.AudioDataQueue;
import io.github.foundationgames.phonos.util.PhonosUtil;
import it.unimi.dsi.fastutil.longs.*;
import it.unimi.dsi.fastutil.objects.Object2LongMap;
import it.unimi.dsi.fastutil.objects.Object2LongMaps;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerCustomAudio {
    public static final String FILE_EXT = ".phonosaud";

    public static final ExecutorService UPLOAD_POOL = Executors.newFixedThreadPool(1);
    public static final ExecutorService FILESYS_POOL = Executors.newFixedThreadPool(4);

    public static final Long2ObjectMap<AudioDataQueue> UPLOADING = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    public static final Long2ObjectMap<AudioDataQueue> SAVED = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());

    private static int TOTAL_SAVED_SIZE = 0;

    public static final Long2ObjectMap<String> ERRORS = Long2ObjectMaps.synchronize(new Long2ObjectOpenHashMap<>());
    public static final LongSet SUCCESSES = new LongOpenHashSet();

    private static final Object2LongMap<UUID> UPLOAD_SESSIONS = Object2LongMaps.synchronize(new Object2LongOpenHashMap<>());

    private static volatile boolean LOADED = false;

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
        UPLOAD_POOL.submit(() -> handleUpload(srv, player, id, sampleRate, samples, last));
    }

    private static void handleUpload(MinecraftServer srv, ServerPlayerEntity player, long id, int sampleRate, ByteBuffer samples, boolean last) {
        if (UPLOAD_SESSIONS.containsKey(player.getUuid()) && UPLOAD_SESSIONS.getLong(player.getUuid()) == id) {
            var aud = UPLOADING.computeIfAbsent(id, k -> new AudioDataQueue(sampleRate));
            aud.push(samples);

            int maxAud = srv.getGameRules().getInt(Phonos.PHONOS_UPLOAD_LIMIT_KB) * 1000;
            if (maxAud > 0 && TOTAL_SAVED_SIZE + aud.originalSize > maxAud) {
                endUploadSession(player.getUuid());
                PayloadPackets.sendUploadStop(player, id);

                ERRORS.put(id, "upload_limit");
                return;
            }

            if (last) {
                SAVED.put(id, aud);
                SUCCESSES.add(id);
                endUploadSession(player.getUuid());

                try {
                    saveOnly(id, PhonosUtil.getCustomSoundFolder(srv));
                } catch (IOException ex) {
                    Phonos.LOG.error("Error saving uploaded sound", ex);
                }
            }
        }
    }

    public static boolean loaded() {
        return LOADED;
    }

    public static void deleteSaved(MinecraftServer srv, long id) {
        var aud = SAVED.remove(id);

        try {
            deleteOnly(id, PhonosUtil.getCustomSoundFolder(srv));

            if (aud != null) {
                Phonos.LOG.info("Saved audio with ID {} ({} bytes) was deleted.", Long.toHexString(id), aud.originalSize);
            }
        } catch (IOException ex) {
            Phonos.LOG.error("Error saving uploaded sound", ex);
        }
    }

    public static void reset() {
        UPLOADING.clear();
        SAVED.clear();
        UPLOAD_SESSIONS.clear();
        LOADED = false;
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

    public static void saveAll(Path folder) throws IOException {
        if (!LOADED) {
            Phonos.LOG.error("Tried to save custom uploaded audio before it was loaded!");

            return;
        }

        var doNotDelete = new LongArraySet();

        for (long id : SAVED.keySet()) {
            saveOnly(id, folder);
            doNotDelete.add(id);
        }

        Phonos.LOG.info("Saved all custom audio to <world>/phonos/");

        try (var paths = Files.walk(folder, 1)) {
            for (var path : paths.toList()) {
                var filename = path.getFileName().toString();
                if (filename.endsWith(FILE_EXT)) {
                    var hexStr = filename.replace(FILE_EXT, "");

                    try {
                        long id = Long.parseUnsignedLong(hexStr, 16);
                        if (!doNotDelete.contains(id)) {
                            Files.deleteIfExists(path);
                            Phonos.LOG.info("Deleted unused custom audio with ID {}", Long.toHexString(id));
                        }

                    } catch (NumberFormatException ignored) {}
                }
            }
        }
    }

    public static void load(Path folder) throws IOException {
        final var startTime = Instant.now();

        SAVED.clear();

        List<String> files = new ArrayList<>();
        try (var paths = Files.walk(folder, 1)) {
            for (var path : paths.toList()) {
                var filename = path.getFileName().toString();
                if (filename.endsWith(FILE_EXT)) {
                    var hexStr = filename.replace(FILE_EXT, "");

                    try {
                        Long.parseUnsignedLong(hexStr, 16);
                        files.add(hexStr);
                    } catch (NumberFormatException ex) {
                        Phonos.LOG.error("Audio data " + filename + " has invalid name");
                    }
                }
            }
        }

        final int foundDataCount = files.size();
        var loadedDataCount = new AtomicInteger(0);

        for (final var hexStr : files) {
            final var path = folder.resolve(hexStr + FILE_EXT);
            FILESYS_POOL.submit(() -> {
                long id = Long.parseUnsignedLong(hexStr, 16);

                try (var in = Files.newInputStream(path)) {
                    var aud = AudioDataQueue.read(in, ByteBuffer::allocate);

                    SAVED.put(id, aud);
                    TOTAL_SAVED_SIZE += aud.originalSize;
                } catch (IOException ex) {
                    Phonos.LOG.error("Error loading custom audio file {}", path.getFileName());
                }

                if (loadedDataCount.incrementAndGet() >= foundDataCount) {
                    LOADED = true;

                    var dur = Duration.between(startTime, Instant.now());
                    Phonos.LOG.info("Loaded {} bytes of saved audio from <world>/phonos/ in {} ms", TOTAL_SAVED_SIZE, dur.toMillis());
                }
            });
        }
    }
}
