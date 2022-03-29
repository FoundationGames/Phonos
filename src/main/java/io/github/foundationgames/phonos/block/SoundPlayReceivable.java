package io.github.foundationgames.phonos.block;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface SoundPlayReceivable {
    @Environment(EnvType.CLIENT)
    void onReceivedSoundClient(ClientWorld world, BlockState state, BlockPos pos, int channel, float volume, float pitch);

    default void onReceivedNote(World world, BlockPos pos, BlockState state, float pitch) {}
}
