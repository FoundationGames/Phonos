package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

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
    }
}
