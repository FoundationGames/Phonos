package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.client.ClientRecieverLocationStorage;
import io.github.foundationgames.phonos.mixin.ClientWorldAccess;
import io.github.foundationgames.phonos.mixin.WorldRendererAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
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
        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("radio_channel_sound"), (client, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            SoundEvent sound = Registry.SOUND_EVENT.get(buf.readIdentifier());
            int channel = buf.readInt();
            float volume = buf.readFloat();
            float pitch = buf.readFloat();
            boolean stoppable = buf.readBoolean();
            client.execute(() -> {
                if(stoppable) ClientRecieverLocationStorage.playStoppableSound(pos, sound, channel, volume, pitch);
                else ClientRecieverLocationStorage.playSound(sound, channel, volume, pitch);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("radio_channel_sound_by_id"), (client, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            Identifier sound = buf.readIdentifier();
            int channel = buf.readInt();
            float volume = buf.readFloat();
            float pitch = buf.readFloat();
            boolean stoppable = buf.readBoolean();
            client.execute(() -> {
                if(stoppable) ClientRecieverLocationStorage.playStoppableSound(pos, sound, channel, volume, pitch);
                else ClientRecieverLocationStorage.playSound(sound, channel, volume, pitch);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("jukebox_song_by_id"), (client, handler, buf, sender) -> {
            Identifier sound = buf.readIdentifier();
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> {
                if(client.player.getEntityWorld() instanceof ClientWorld) {
                    ClientWorld world = (ClientWorld)client.player.getEntityWorld();
                    Map<BlockPos, SoundInstance> songs = ((WorldRendererAccess)(((ClientWorldAccess)world).getWorldRenderer())).getPlayingSongs();
                    SoundInstance soundI = new PositionedSoundInstance(sound, SoundCategory.RECORDS, 1.8f, 1.0f, false, 0, SoundInstance.AttenuationType.LINEAR, pos.getX()+0.5D, pos.getY()+0.5D, pos.getZ()+0.5D, false);
                    MinecraftClient.getInstance().getSoundManager().play(soundI);
                    songs.put(pos, soundI);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("radio_channel_stop"), (client, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            int channel = buf.readInt();
            client.execute(() -> ClientRecieverLocationStorage.tryStopSound(pos, channel));
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("update_receivers"), (client, handler, buf, sender) -> {
            RecieverStorageOperation operation = RecieverStorageOperation.fromByte(buf.readByte());
            int channel = buf.readInt();
            long[] positions = buf.readLongArray(new long[] {});
            client.execute(() -> {
                for(long l : positions) {
                    if(operation == RecieverStorageOperation.ADD) ClientRecieverLocationStorage.addReciever(channel, l);
                    else if(operation == RecieverStorageOperation.REMOVE) ClientRecieverLocationStorage.removeReciever(channel, l);
                    else if(operation == RecieverStorageOperation.CLEAR) ClientRecieverLocationStorage.clear();
                }
            });
        });
    }
}
