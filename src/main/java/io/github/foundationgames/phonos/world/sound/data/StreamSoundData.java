package io.github.foundationgames.phonos.world.sound.data;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundCategory;

public class StreamSoundData extends SoundData {
    public final long streamId;

    public StreamSoundData(Type<?> type, long emitterId, long streamId, SoundCategory category, float volume, float pitch) {
        super(type, emitterId, category, volume, pitch);

        this.streamId = streamId;
    }

    public StreamSoundData(Type<?> type, PacketByteBuf buf) {
        super(type, buf);

        this.streamId = buf.readLong();
    }

    public static StreamSoundData create(long id, long streamId, SoundCategory category, float volume, float pitch) {
        return new StreamSoundData(SoundDataTypes.STREAM, id, streamId, category, volume, pitch);
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        super.toPacket(buf);

        buf.writeLong(this.streamId);
    }
}
