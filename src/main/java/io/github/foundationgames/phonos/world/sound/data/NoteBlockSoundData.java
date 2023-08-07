package io.github.foundationgames.phonos.world.sound.data;

import net.minecraft.block.enums.Instrument;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class NoteBlockSoundData extends SoundEventSoundData {
    public final Instrument instrument;
    public final int note;

    public NoteBlockSoundData(Type<?> type, long id, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, Instrument instrument, int note) {
        super(type, id, sound, category, volume, pitch);

        this.instrument = instrument;
        this.note = note;
    }

    public NoteBlockSoundData(Type<?> type, PacketByteBuf buf) {
        super(type, buf);

        this.instrument = Instrument.valueOf(buf.readString());
        this.note = buf.readByte();
    }

    public static NoteBlockSoundData create(long id, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch, Instrument instrument, int note) {
        return new NoteBlockSoundData(SoundDataTypes.NOTE_BLOCK, id, sound, category, volume, pitch, instrument, note);
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        super.toPacket(buf);

        buf.writeString(instrument.name());
        buf.writeByte(note);
    }
}
