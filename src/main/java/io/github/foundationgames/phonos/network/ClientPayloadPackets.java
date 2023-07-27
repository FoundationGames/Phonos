package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.SatelliteStationBlockEntity;
import io.github.foundationgames.phonos.client.screen.SatelliteStationScreen;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.custom.ClientCustomAudioUploader;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.sound.stream.ClientIncomingStreamHandler;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.PacketByteBuf;

import java.nio.ByteBuffer;

public final class ClientPayloadPackets {
    @Environment(EnvType.CLIENT)
    public static void initClient() {
        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("sound_play"), (client, handler, buf, responseSender) -> {
           var data = SoundData.fromPacket(buf);
           var tree = SoundEmitterTree.fromPacket(buf);

           client.execute(() -> SoundStorage.getInstance(client.world).play(client.world, data, tree));
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("sound_stop"), (client, handler, buf, responseSender) -> {
            long id = buf.readLong();

            client.execute(() -> SoundStorage.getInstance(client.world).stop(client.world, id));
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("sound_update"), (client, handler, buf, responseSender) -> {
            SoundEmitterTree.Delta delta = SoundEmitterTree.Delta.fromPacket(buf);

            client.execute(() -> SoundStorage.getInstance(client.world).update(delta));
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("open_satellite_station_screen"), (client, handler, buf, responseSender) -> {
            var pos = buf.readBlockPos();

            client.execute(() -> {
                if (client.world.getBlockEntity(pos) instanceof SatelliteStationBlockEntity sat) {
                    client.setScreen(new SatelliteStationScreen(sat));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("audio_upload_status"), (client, handler, buf, responseSender) -> {
            long id = buf.readLong();
            boolean ok = buf.readBoolean();

            client.execute(() -> {
                if (ok) {
                    ClientCustomAudioUploader.sendUploadPackets(id);
                } else {
                    Phonos.LOG.warn("Denied upload for sound " + Long.toHexString(id));
                }
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("audio_stream_data"), (client, handler, buf, responseSender) -> {
            long id = buf.readLong();
            int sampleRate = buf.readInt();
            var samples = PhonosUtil.readBufferFromPacket(buf, ByteBuffer::allocate);

            client.execute(() -> ClientIncomingStreamHandler.receiveStream(id, sampleRate, samples));
        });

        ClientPlayNetworking.registerGlobalReceiver(Phonos.id("audio_stream_end"), (client, handler, buf, responseSender) -> {
            long id = buf.readLong();

            client.execute(() -> ClientIncomingStreamHandler.endStream(id));
        });
    }

    public static void sendRequestSatelliteUploadSession(SatelliteStationBlockEntity entity) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(entity.getPos());
        ClientPlayNetworking.send(Phonos.id("request_satellite_upload_session"), buf);
    }

    public static void sendAudioUploadPacket(long streamId, int sampleRate, ByteBuffer samples, boolean last) {
        var buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeLong(streamId);
        buf.writeInt(sampleRate);
        PhonosUtil.writeBufferToPacket(buf, samples);
        buf.writeBoolean(last);

        ClientPlayNetworking.send(Phonos.id("audio_upload"), buf);
    }
}
