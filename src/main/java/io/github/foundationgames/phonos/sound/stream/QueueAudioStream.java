package io.github.foundationgames.phonos.sound.stream;

import net.minecraft.client.sound.AudioStream;
import org.lwjgl.BufferUtils;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

public class QueueAudioStream implements AudioStream {
    private AudioDataQueue stream = null;

    private AudioFormat format = new AudioFormat(20000, 8, 1, true, false);

    private ByteBuffer stall = null;
    private int signalDiv = 1;

    public QueueAudioStream() {
    }

    public QueueAudioStream(AudioDataQueue queue) {
        this.init(queue);
    }

    public void init(AudioDataQueue queue) {
        this.stream = queue;
        this.format = new AudioFormat(queue.sampleRate, 8, 1, true, false);
    }

    @Override
    public AudioFormat getFormat() {
        return this.format;
    }

    @Override
    public synchronized ByteBuffer getBuffer(int size) {
        var out = BufferUtils.createByteBuffer(size);

        if (stream != null && !stream.data.isEmpty()) {
            if (stream.data.size() == 1) {
                var top = stream.data.getFirst();

                if (!top.hasRemaining()) {
                    top.position(Math.max(0, top.limit() - 412));

                    while (top.hasRemaining() && out.hasRemaining()) {
                        out.put(top.get());
                    }

                    top.position(top.limit());
                } else {
                    while (top.hasRemaining() && out.hasRemaining()) {
                        out.put(top.get());
                    }
                }
            } else while (stream.data.size() > 1 && out.hasRemaining()) {
                var top = stream.data.getFirst();

                while (top.hasRemaining() && out.hasRemaining()) {
                    out.put(top.get());
                }

                if (!top.hasRemaining()) {
                    stream.data.pollFirst();
                }
            }

            out.flip();
        } else if (out.position() == 0) {
            out.rewind();
            out.limit(8);
        }

        return out;
    }

    @Override
    public void close() {
    }
}
