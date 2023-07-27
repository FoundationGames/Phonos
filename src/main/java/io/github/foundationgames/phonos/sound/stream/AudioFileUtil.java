package io.github.foundationgames.phonos.sound.stream;

import net.minecraft.client.sound.OggAudioStream;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class AudioFileUtil {
    public static @Nullable AudioDataQueue dataOfVorbis(InputStream ogg) throws IOException {
        var oggStream = new OggAudioStream(ogg);
        var format = oggStream.getFormat();

        if (format.getChannels() > 1) {
            return null;
        }

        int sampleRate = (int) format.getSampleRate();
        int oggSamplesPerAudSample = 1;
        while (sampleRate > 22050) {
            sampleRate /= 2;
            oggSamplesPerAudSample *= 2;
        }

        int fadeOutPeriod = sampleRate / 4;

        var aud = new AudioDataQueue(sampleRate);
        var oggBuf = oggStream.getBuffer().rewind();

        while (oggBuf.remaining() >= 1 + oggSamplesPerAudSample) {
            int cap = Math.min(oggBuf.remaining(), AudioDataQueue.SAMPLE_SECTION);

            var audBuf = ByteBuffer.allocate(cap);

            for (int i = 0; i < cap; i++) {
                int combinedSample = 0;
                int s = 0;
                for (; s < oggSamplesPerAudSample && oggBuf.remaining() >= 1 + oggSamplesPerAudSample; s++) {
                    combinedSample += MathHelper.clamp((oggBuf.getShort() >> 8) + 127, 0, 0xFF);
                }
                combinedSample /= oggSamplesPerAudSample;

                if (oggBuf.remaining() < fadeOutPeriod) {
                    combinedSample -= 127;
                    double fade = (double) oggBuf.remaining() / fadeOutPeriod;
                    combinedSample = (int) Math.min(0, combinedSample * fade);
                    combinedSample += 127 * fade;
                }

                audBuf.put((byte) combinedSample);
            }

            aud.data.addLast(audBuf.rewind());
        }

        aud.data.addLast(ByteBuffer.allocate(16));

        oggStream.close();

        return aud;
    }
}
