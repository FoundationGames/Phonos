package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.client.ClientRecieverLocationStorage;
import io.github.foundationgames.phonos.mixin.ClientWorldAccess;
import io.github.foundationgames.phonos.mixin.WorldRendererAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.Map;

public final class ClientPayloadPackets {
    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientSidePacketRegistry.INSTANCE.register(Phonos.id("radio_channel_sound"), ((ctx, buf) -> {
            BlockPos pos = buf.readBlockPos();
            SoundEvent sound = Registry.SOUND_EVENT.get(buf.readIdentifier());
            int channel = buf.readInt();
            float volume = buf.readFloat();
            float pitch = buf.readFloat();
            boolean stoppable = buf.readBoolean();
            ctx.getTaskQueue().execute(() -> {
                if(stoppable) ClientRecieverLocationStorage.playStoppableSound(pos, sound, channel, volume, pitch);
                else ClientRecieverLocationStorage.playSound(sound, channel, volume, pitch);
            });
        }));

        ClientSidePacketRegistry.INSTANCE.register(Phonos.id("radio_channel_sound_by_id"), ((ctx, buf) -> {
            BlockPos pos = buf.readBlockPos();
            Identifier sound = buf.readIdentifier();
            int channel = buf.readInt();
            float volume = buf.readFloat();
            float pitch = buf.readFloat();
            boolean stoppable = buf.readBoolean();
            ctx.getTaskQueue().execute(() -> {
                if(stoppable) ClientRecieverLocationStorage.playStoppableSound(pos, sound, channel, volume, pitch);
                else ClientRecieverLocationStorage.playSound(sound, channel, volume, pitch);
            });
        }));

        ClientSidePacketRegistry.INSTANCE.register(Phonos.id("jukebox_song_by_id"), ((ctx, buf) -> {
            Identifier sound = buf.readIdentifier();
            BlockPos pos = buf.readBlockPos();
            ctx.getTaskQueue().execute(() -> {
                if(ctx.getPlayer().getEntityWorld() instanceof ClientWorld) {
                    ClientWorld world = (ClientWorld)ctx.getPlayer().getEntityWorld();
                    Map<BlockPos, SoundInstance> songs = ((WorldRendererAccess)(((ClientWorldAccess)world).getWorldRenderer())).getPlayingSongs();
                    SoundInstance soundI = new PositionedSoundInstance(sound, SoundCategory.RECORDS, 1.8f, 1.0f, false, 0, SoundInstance.AttenuationType.LINEAR, pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, false);
                    MinecraftClient.getInstance().getSoundManager().play(soundI);
                    songs.put(pos, soundI);
                }
            });
        }));

        ClientSidePacketRegistry.INSTANCE.register(Phonos.id("radio_channel_stop"), ((ctx, buf) -> {
            BlockPos pos = buf.readBlockPos();
            int channel = buf.readInt();
            ctx.getTaskQueue().execute(() -> ClientRecieverLocationStorage.tryStopSound(pos, channel));
        }));

        ClientSidePacketRegistry.INSTANCE.register(Phonos.id("update_receivers"), ((ctx, buf) -> {
            RecieverStorageOperation operation = RecieverStorageOperation.fromByte(buf.readByte());
            int channel = buf.readInt();
            long[] positions = buf.readLongArray(new long[] {});
            ctx.getTaskQueue().execute(() -> {
                for(long l : positions) {
                    if(operation == RecieverStorageOperation.ADD) ClientRecieverLocationStorage.addReciever(channel, l);
                    else if(operation == RecieverStorageOperation.REMOVE) ClientRecieverLocationStorage.removeReciever(channel, l);
                    else if(operation == RecieverStorageOperation.CLEAR) ClientRecieverLocationStorage.clear();
                }
            });
        }));
    }
}
