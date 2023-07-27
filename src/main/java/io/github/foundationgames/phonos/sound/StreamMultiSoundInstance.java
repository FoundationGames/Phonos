package io.github.foundationgames.phonos.sound;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.sound.stream.ClientIncomingStreamHandler;
import net.fabricmc.fabric.api.client.sound.v1.FabricSoundInstance;
import net.minecraft.client.sound.AudioStream;
import net.minecraft.client.sound.SoundLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

import java.util.concurrent.CompletableFuture;

public class StreamMultiSoundInstance extends MultiSourceSoundInstance implements FabricSoundInstance {
    public final long streamId;

    public StreamMultiSoundInstance(SoundEmitterTree tree, long streamId, Random random, float volume, float pitch) {
        super(tree, Phonos.STREAMED_SOUND, random, volume, pitch);

        this.streamId = streamId;
    }

    @Override
    public CompletableFuture<AudioStream> getAudioStream(SoundLoader loader, Identifier id, boolean repeatInstantly) {
        return ClientIncomingStreamHandler.getStream(this.streamId);
    }
}
