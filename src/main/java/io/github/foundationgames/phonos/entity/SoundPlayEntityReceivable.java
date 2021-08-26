package io.github.foundationgames.phonos.entity;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;

public interface SoundPlayEntityReceivable {
    @Environment(EnvType.CLIENT)
    void onRecievedSoundClient(ClientWorld world, Entity entity, int channel, float volume, float pitch);
}
