package io.github.foundationgames.phonos.network;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;

public final class PayloadPackets {
    public static void initCommon() {
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
}
