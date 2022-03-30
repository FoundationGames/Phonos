package io.github.foundationgames.phonos.block.entity;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;

public interface Syncing {
    default void sync() {
        if (this instanceof BlockEntity be && be.getWorld() instanceof ServerWorld world) {
            world.getChunkManager().markForUpdate(be.getPos());
        }
    }
}
