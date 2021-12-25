package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.PhonosClient;
import io.github.foundationgames.phonos.client.ClientReceiverStorage;
import io.github.foundationgames.phonos.mixin.ClientWorldAccess;
import io.github.foundationgames.phonos.mixin.WorldRendererAccess;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
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
                if(stoppable) ClientReceiverStorage.playStoppableSound(pos, sound, channel, volume, pitch);
                else ClientReceiverStorage.playSound(sound, channel, volume, pitch);
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
                if(stoppable) ClientReceiverStorage.playStoppableSound(pos, sound, channel, volume, pitch);
                else ClientReceiverStorage.playSound(sound, channel, volume, pitch);
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("jukebox_song_by_id"), (client, handler, buf, sender) -> {
            Identifier sound = buf.readIdentifier();
            BlockPos pos = buf.readBlockPos();
            client.execute(() -> {
                if(client.player.getEntityWorld() instanceof ClientWorld) {
                    ClientWorld world = (ClientWorld)client.player.getEntityWorld();
                    Map<BlockPos, SoundInstance> songs = ((WorldRendererAccess)(((ClientWorldAccess)world).phonos$getWorldRenderer())).phonos$getPlayingSongs();
                    SoundInstance soundI = null;
                    if(sound != null) soundI = new PositionedSoundInstance(sound, SoundCategory.RECORDS, 1.8f, 1.0f, false, 0, SoundInstance.AttenuationType.LINEAR, pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, false);
                    MinecraftClient.getInstance().getSoundManager().play(soundI);
                    Text subtitle = null;
                    if(soundI != null) try { // also lets try to read the subtitle from the item
                        subtitle = soundI.getSoundSet(MinecraftClient.getInstance().getSoundManager()).getSubtitle();
                        if(subtitle == null) throw new Exception(); // fail if subtitle is null
                    } catch (Exception e) { // if it fails, lets just make it a "custom music disc"
                        subtitle = new TranslatableText("item.phonos.custom_music_disc");
                    }
                    if(subtitle != null) MinecraftClient.getInstance().inGameHud.setOverlayMessage(new TranslatableText("record.nowPlaying", subtitle), true);
                    songs.put(pos, soundI);
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("radio_channel_stop"), (client, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            int channel = buf.readInt();
            client.execute(() -> ClientReceiverStorage.tryStopSound(pos, channel));
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("update_receivers"), (client, handler, buf, sender) -> {
            ReceiverStorageOperation operation = ReceiverStorageOperation.fromByte(buf.readByte());
            int channel = buf.readInt();
            long[] positions = buf.readLongArray();
            int[] entities = buf.readIntArray();
            client.execute(() -> {
                if(operation == ReceiverStorageOperation.CLEAR) {
                    ClientReceiverStorage.clear();
                    return;
                }
                for(long l : positions) {
                    if(operation == ReceiverStorageOperation.ADD) ClientReceiverStorage.addReceiver(channel, BlockPos.fromLong(l));
                    else if(operation == ReceiverStorageOperation.REMOVE) ClientReceiverStorage.removeReceiver(channel, BlockPos.fromLong(l));
                }
                for(int i : entities) {
                    if(operation == ReceiverStorageOperation.ADD) ClientReceiverStorage.addEntityReceiver(channel, client.world.getEntityById(i));
                    else if(operation == ReceiverStorageOperation.REMOVE) ClientReceiverStorage.removeEntityReceiver(channel, client.world.getEntityById(i));
                }
            });
        });
    }
}
