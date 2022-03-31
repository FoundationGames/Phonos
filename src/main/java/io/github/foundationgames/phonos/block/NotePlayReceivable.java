package io.github.foundationgames.phonos.block;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

public interface NotePlayReceivable {
    void onNotePlayed(ServerWorld world, BlockPos pos, float pitch);
}
