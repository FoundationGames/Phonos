package io.github.foundationgames.phonos.world.sound.data;

import com.mojang.serialization.Lifecycle;
import io.github.foundationgames.phonos.Phonos;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

public abstract class SoundData {
    public static final RegistryKey<Registry<SoundData.Type<?>>> REGISTRY_KEY = RegistryKey.ofRegistry(Phonos.id("sound_data"));
    public static final Registry<SoundData.Type<?>> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.stable());

    public final Type<?> type;
    public final long emitterId;
    public final float volume, pitch;

    public SoundData(Type<?> type, long emitterId, float volume, float pitch) {
        this.type = type;
        this.emitterId = emitterId;
        this.volume = volume;
        this.pitch = pitch;
    }

    public SoundData(Type<?> type, PacketByteBuf buf) {
        this(type, buf.readLong(), buf.readFloat(), buf.readFloat());
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeIdentifier(type.id());
        buf.writeLong(emitterId);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
    }

    @SuppressWarnings("unchecked")
    public static @Nullable SoundData fromPacket(PacketByteBuf buf) {
        var id = buf.readIdentifier();
        var entry = REGISTRY.get(id);

        if (entry != null) {
            return entry.constructor().create(entry, buf);
        }

        return null;
    }

    public static <S extends SoundData> Type<S> register(Identifier id, Factory<S> factory) {
        return Registry.register(REGISTRY, id, new Type<>(id, factory));
    }

    public record Type<S extends SoundData>(Identifier id, Factory<S> constructor) {}

    public interface Factory<S extends SoundData> {
        S create(Type<?> type, PacketByteBuf buf);
    }
}
