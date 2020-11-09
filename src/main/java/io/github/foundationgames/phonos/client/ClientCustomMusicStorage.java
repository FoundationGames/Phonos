package io.github.foundationgames.phonos.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class ClientCustomMusicStorage {
    public static final Map<BlockPos, SoundInstance> PLAYING_CUSTOM_MUSIC_DISCS = new HashMap<>();
    public static SoundInstance GUI_CUSTOM_MUSIC = null;
}
