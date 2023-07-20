package io.github.foundationgames.phonos.block.entity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface Ticking {
    void tick(World world, BlockPos pos, BlockState state);

    static <E extends BlockEntity & Ticking> void ticker(World world, BlockPos pos, BlockState state, E entity) {
        entity.tick(world, pos, state);
    }
}
