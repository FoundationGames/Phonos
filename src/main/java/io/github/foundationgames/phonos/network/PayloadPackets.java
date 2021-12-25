package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.RadioJukeboxBlockEntity;
import io.github.foundationgames.phonos.screen.CustomMusicDiscGuiDescription;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public final class PayloadPackets {
    public static void initCommon() {
        RadioJukeboxBlockEntity.registerServerPackets();
        CustomMusicDiscGuiDescription.registerServerPackets();
    }

    public static void sendJukeboxIdSound(ServerPlayerEntity player, Identifier sound, BlockPos pos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(sound);
        buf.writeBlockPos(pos);
        ServerPlayNetworking.send(player, Phonos.id("jukebox_song_by_id"), buf);
    }

    public static void sendRadioChannelSound(ServerPlayerEntity player, BlockPos origin, SoundEvent sound, int channel, float volume, float pitch, boolean stoppable) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(origin);
        buf.writeIdentifier(Registry.SOUND_EVENT.getId(sound));
        buf.writeInt(channel);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeBoolean(stoppable);
        ServerPlayNetworking.send(player, Phonos.id("radio_channel_sound_by_id"), buf);
    }

    public static void sendRadioChannelSound(ServerPlayerEntity player, BlockPos origin, Identifier sound, int channel, float volume, float pitch, boolean stoppable) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(origin);
        buf.writeIdentifier(sound);
        buf.writeInt(channel);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeBoolean(stoppable);
        ServerPlayNetworking.send(player, Phonos.id("radio_channel_sound"), buf);
    }

    public static void sendStopSound(ServerPlayerEntity player, BlockPos pos, int channel) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(channel);
        ServerPlayNetworking.send(player, Phonos.id("radio_channel_stop"), buf);
    }

    public static void sendReceiversUpdate(ServerPlayerEntity player, ReceiverStorageOperation operation, int channel, long[] positions, int[] entities) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeByte(operation.asByte());
        buf.writeInt(channel);
        buf.writeLongArray(positions);
        buf.writeIntArray(entities);
        ServerPlayNetworking.send(player, Phonos.id("update_receivers"), buf);
    }
}
