package io.github.foundationgames.phonos.world.sound.block;

import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SoundDataHandler {
    void receiveSound(BlockState state, World world, BlockPos pos, SoundData sound);
}
