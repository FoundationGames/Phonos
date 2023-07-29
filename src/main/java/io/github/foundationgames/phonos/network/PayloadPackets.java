package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.SatelliteStationBlockEntity;
import io.github.foundationgames.phonos.sound.custom.ServerCustomAudio;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;

import java.nio.ByteBuffer;

public final class PayloadPackets {
    public static void initCommon() {
        ServerPlayNetworking.registerGlobalReceiver(Phonos.id("request_satellite_upload_session"), (server, player, handler, buf, responseSender) -> {
           var pos = buf.readBlockPos();

           server.execute(() -> {
               var world = player.getWorld();

               if (world.getBlockEntity(pos) instanceof SatelliteStationBlockEntity entity) {
                   if (entity.canUpload(player)) {
                       ServerCustomAudio.beginUploadSession(player, entity.streamId);
                       sendUploadStatus(player, entity.streamId, true);

                       Phonos.LOG.info("Allowed player {} to upload audio at satellite station {}. Will be saved to <world>/phonos/{}",
                               player, pos, Long.toHexString(entity.streamId) + ServerCustomAudio.FILE_EXT);
                   } else {
                       sendUploadStatus(player, entity.streamId, false);
                   }
               }
           });
        });

        ServerPlayNetworking.registerGlobalReceiver(Phonos.id("request_satellite_crash"), (server, player, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();

            server.execute(() -> {
                var world = player.getWorld();

                if (world.getBlockEntity(pos) instanceof SatelliteStationBlockEntity entity && entity.canCrash(player)) {
                    entity.performAction(SatelliteStationBlockEntity.ACTION_CRASH);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(Phonos.id("audio_upload"), (server, player, handler, buf, responseSender) -> {
            long streamId = buf.readLong();
            int sampleRate = buf.readInt();
            var samples = PhonosUtil.readBufferFromPacket(buf, ByteBuffer::allocate);

            boolean last = buf.readBoolean();

            server.execute(() -> ServerCustomAudio.receiveUpload(server, player, streamId, sampleRate, samples, last));
        });

        ServerPlayNetworking.registerGlobalReceiver(Phonos.id("audio_upload"), (server, player, handler, buf, responseSender) -> {
            long streamId = buf.readLong();
            int sampleRate = buf.readInt();
            var samples = PhonosUtil.readBufferFromPacket(buf, ByteBuffer::allocate);

            boolean last = buf.readBoolean();

            server.execute(() -> ServerCustomAudio.receiveUpload(server, player, streamId, sampleRate, samples, last));
        });
    }

    public static void sendSoundPlay(ServerPlayerEntity player, SoundData data, SoundEmitterTree tree) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        data.toPacket(buf);
        tree.toPacket(buf);
        ServerPlayNetworking.send(player, Phonos.id("sound_play"), buf);
    }

    public static void sendSoundStop(ServerPlayerEntity player, long sourceId) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(sourceId);
        ServerPlayNetworking.send(player, Phonos.id("sound_stop"), buf);
    }

    public static void sendSoundUpdate(ServerPlayerEntity player, SoundEmitterTree.Delta delta) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        SoundEmitterTree.Delta.toPacket(buf, delta);
        ServerPlayNetworking.send(player, Phonos.id("sound_update"), buf);
    }

    public static void sendOpenSatelliteStationScreen(ServerPlayerEntity player, BlockPos pos, int screenType) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeInt(screenType);

        ServerPlayNetworking.send(player, Phonos.id("open_satellite_station_screen"), buf);
    }

    public static void sendUploadStop(ServerPlayerEntity player, long uploadId) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(uploadId);

        ServerPlayNetworking.send(player, Phonos.id("audio_upload_stop"), buf);
    }

    public static void sendUploadStatus(ServerPlayerEntity player, long uploadId, boolean ok) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(uploadId);
        buf.writeBoolean(ok);

        ServerPlayNetworking.send(player, Phonos.id("audio_upload_status"), buf);
    }

    public static void sendAudioStreamData(ServerPlayerEntity player, long streamId, int sampleRate, ByteBuffer samples) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(streamId);
        buf.writeInt(sampleRate);
        PhonosUtil.writeBufferToPacket(buf, samples);

        ServerPlayNetworking.send(player, Phonos.id("audio_stream_data"), buf);
    }

    public static void sendAudioStreamEnd(ServerPlayerEntity player, long streamId) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(streamId);

        ServerPlayNetworking.send(player, Phonos.id("audio_stream_end"), buf);
    }

    public static Packet<ClientPlayPacketListener> pktSatelliteAction(SatelliteStationBlockEntity be, int action) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(be.getPos());
        buf.writeInt(action);

        return ServerPlayNetworking.createS2CPacket(Phonos.id("satellite_action"), buf);
    }
}
