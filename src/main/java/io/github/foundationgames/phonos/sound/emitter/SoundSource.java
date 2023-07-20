package io.github.foundationgames.phonos.sound.emitter;

import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.minecraft.world.World;

public interface SoundSource {
    double x();
    double y();
    double z();

    void onSoundPlayed(World world, SoundData sound);
}
