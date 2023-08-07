package io.github.foundationgames.phonos.world.sound.data;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;

public class SoundEventSoundData extends SoundData {
    public final RegistryEntry<SoundEvent> sound;

    protected SoundEventSoundData(Type<?> type, long id, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch) {
        super(type, id, category, volume, pitch);
        this.sound = sound;
    }

    public SoundEventSoundData(Type<?> type, PacketByteBuf buf) {
        super(type, buf);
        this.sound = buf.readRegistryEntry(Registries.SOUND_EVENT.getIndexedEntries(), SoundEvent::fromBuf);
    }

    public static SoundEventSoundData create(long id, RegistryEntry<SoundEvent> sound, SoundCategory category, float volume, float pitch) {
        return new SoundEventSoundData(SoundDataTypes.SOUND_EVENT, id, sound, category, volume, pitch);
    }

    @Override
    public void toPacket(PacketByteBuf buf) {
        super.toPacket(buf);
        buf.writeRegistryEntry(Registries.SOUND_EVENT.getIndexedEntries(), sound, (rbuf, sound) -> sound.writeBuf(rbuf));
    }
}
