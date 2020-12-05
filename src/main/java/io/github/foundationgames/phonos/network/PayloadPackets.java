package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.RadioJukeboxBlockEntity;
import io.github.foundationgames.phonos.screen.CustomMusicDiscGuiDescription;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.network.ServerSidePacketRegistry;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

public final class PayloadPackets {
    public static void initCommon() {
        RadioJukeboxBlockEntity.registerServerPackets();
        CustomMusicDiscGuiDescription.registerServerPackets();
    }

    public static void sendJukeboxIdSound(PlayerEntity player, Identifier sound, BlockPos pos) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeIdentifier(sound);
        buf.writeBlockPos(pos);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Phonos.id("jukebox_song_by_id"), buf);
    }

    public static void sendRadioChannelSound(PlayerEntity player, BlockPos origin, Instrument instrument, int channel, float volume, float pitch, boolean stoppable) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(origin);
        buf.writeString(instrument.toString());
        buf.writeInt(channel);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeBoolean(stoppable);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Phonos.id("radio_channel_sound_by_instrument"), buf);
    }

    public static void sendRadioChannelSound(PlayerEntity player, BlockPos origin, int itemID, int channel, float volume, float pitch, boolean stoppable) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(origin);
        buf.writeInt(itemID);
        buf.writeInt(channel);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeBoolean(stoppable);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Phonos.id("radio_channel_sound_by_id"), buf);
    }

    public static void sendRadioChannelSound(PlayerEntity player, BlockPos origin, Identifier sound, int channel, float volume, float pitch, boolean stoppable) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(origin);
        buf.writeIdentifier(sound);
        buf.writeInt(channel);
        buf.writeFloat(volume);
        buf.writeFloat(pitch);
        buf.writeBoolean(stoppable);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Phonos.id("radio_channel_sound"), buf);
    }

    public static void sendStopSound(PlayerEntity player, BlockPos pos, int channel) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(channel);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Phonos.id("radio_channel_stop"), buf);
    }

    public static void sendRecieversUpdate(PlayerEntity player, RecieverStorageOperation operation, int channel, long[] positions) {
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeByte(operation.asByte());
        buf.writeInt(channel);
        buf.writeLongArray(positions);
        ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, Phonos.id("update_receivers"), buf);
    }
}
