package io.github.foundationgames.phonos.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;

public interface SoundPlayReceivable {
    @Environment(EnvType.CLIENT)
    void onRecievedSoundClient(ClientWorld world, BlockState state, BlockPos pos, int channel, float volume, float pitch);
}
