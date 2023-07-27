package io.github.foundationgames.phonos.world.sound.data;

import net.minecraft.network.PacketByteBuf;

public class StreamSoundData extends SoundData {
    public final long streamId;

    public StreamSoundData(Type<?> type, long emitterId, long streamId, float volume, float pitch) {
        super(type, emitterId, volume, pitch);

        this.streamId = streamId;
    }

    public StreamSoundData(Type<?> type, PacketByteBuf buf) {
        super(type, buf);

        this.streamId = buf.readLong();
    }

    public static StreamSoundData create(long id, long streamId, float volume, float pitch) {
        return new StreamSoundData(SoundDataTypes.STREAM, id, streamId, volume, pitch);
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        super.toPacket(buf);

        buf.writeLong(this.streamId);
    }
}
