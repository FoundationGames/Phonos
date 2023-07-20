package io.github.foundationgames.phonos.world.sound;

import com.mojang.serialization.Lifecycle;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitter;
import io.github.foundationgames.phonos.sound.emitter.SoundSource;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.SimpleRegistry;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public abstract class InputPlugPoint implements WirePlugPoint {
    public static final RegistryKey<Registry<Type>> REGISTRY_KEY = RegistryKey.ofRegistry(Phonos.id("wire_plug_point"));
    public static final Registry<Type> REGISTRY = new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.stable());

    public static final Type BLOCK_TYPE = register(Phonos.id("block"), BlockConnectionLayout::blockInputPlugPoint);

    public final Type type;

    protected InputPlugPoint(Type type) {
        this.type = type;
    }

    public void writeNbt(NbtCompound nbt) {
        nbt.putString("type", this.type.id().toString());
    }

    public abstract void setConnected(World world, boolean connected);

    public abstract boolean canPlugExist(World world);

    public abstract @Nullable SoundSource asSource(World world);

    public abstract @Nullable SoundEmitter forward(World world);

    public static @Nullable InputPlugPoint readInputPoint(NbtCompound nbt, World world) {
        var id = Identifier.tryParse(nbt.getString("type"));

        if (id != null) {
            var entry = REGISTRY.get(id);

            if (entry != null) {
                return entry.constructor().create(entry, world, nbt);
            }
        }

        return null;
    }

    public static Type register(Identifier id, Factory factory) {
        return Registry.register(REGISTRY, id, new Type(id, factory));
    }

    public static void init() {
    }

    public record Type(Identifier id, Factory constructor) {}

    public interface Factory {
        @Nullable InputPlugPoint create(Type type, World world, NbtCompound nbt);
    }
}
