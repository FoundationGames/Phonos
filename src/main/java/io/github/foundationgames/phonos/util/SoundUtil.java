package io.github.foundationgames.phonos.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.BlockPos;

public enum SoundUtil {;
    public static void playPositionedSound(SoundEvent event, SoundCategory category, float volume, float pitch, BlockPos pos) {
        if(FabricLoader.getInstance().getEnvironmentType() != EnvType.SERVER) MinecraftClient.getInstance().getSoundManager().play(new PositionedSoundInstance(event, category, volume, pitch, pos));
    }
}
