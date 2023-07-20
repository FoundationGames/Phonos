package io.github.foundationgames.phonos.sound;

import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

public class ServerSoundStorage extends SoundStorage {
    @Override
    public void play(World world, SoundData data, SoundEmitterTree tree) {
        tree.update(world);

        if (world instanceof ServerWorld sWorld) for (var player : sWorld.getPlayers()) {
            PayloadPackets.sendSoundPlay(player, data, tree);
        }

        this.notifySoundSourcesPlayed(world, data, tree);
    }

    @Override
    public void stop(World world, long soundUniqueId) {
        if (world instanceof ServerWorld sWorld) for (var player : sWorld.getPlayers()) {
            PayloadPackets.sendSoundStop(player, soundUniqueId);
        }
    }

    @Override
    public void tick(World world) {
    }
}
