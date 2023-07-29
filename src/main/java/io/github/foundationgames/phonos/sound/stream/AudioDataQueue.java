package io.github.foundationgames.phonos.sound.stream;

import io.github.foundationgames.phonos.util.PhonosUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.IntFunction;

public class AudioDataQueue {
    public static final int SAMPLE_SECTION = 7000;

    public final int sampleRate;
    public final Deque<ByteBuffer> data = new ArrayDeque<>();

    public int originalSize;

    public AudioDataQueue(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public void push(ByteBuffer buf) {
        this.data.addLast(buf);
        originalSize += buf.capacity();
    }

    public AudioDataQueue copy(IntFunction<ByteBuffer> bufferProvider) {
        var copy = new AudioDataQueue(this.sampleRate);

        for (var buf : this.data) {
            int pos = buf.position();
            copy.push(bufferProvider.apply(buf.capacity()).put(buf));
            buf.position(pos);
        }

        return copy;
    }

    public void write(OutputStream stream) throws IOException {
        PhonosUtil.writeInt(stream, sampleRate);
        PhonosUtil.writeInt(stream, this.data.size());

        for (var buf : this.data) {
            PhonosUtil.writeInt(stream, buf.capacity());
            int pos = buf.position();
            while (buf.hasRemaining()) {
                stream.write(buf.get());
            }
            buf.position(pos);
        }
    }

    public static AudioDataQueue read(InputStream stream, IntFunction<ByteBuffer> bufferProvider) throws IOException {
        var aud = new AudioDataQueue(PhonosUtil.readInt(stream));
        int dataCount = PhonosUtil.readInt(stream);

        for (int i = 0; i < dataCount; i++) {
            int bufSize = PhonosUtil.readInt(stream);
            var buf = bufferProvider.apply(bufSize);

            int cur = buf.position();
            for (int j = 0; j < bufSize; j++) {
                buf.put((byte) stream.read());
            }
            buf.position(cur);

            aud.push(buf);
        }

        return aud;
    }
}
