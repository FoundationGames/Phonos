package io.github.foundationgames.phonos.util;

import net.minecraft.util.math.BlockPos;

import java.util.Random;

public final class UniqueId {
    public static long random() {
        return new Random().nextLong();
    }

    public static long ofBlock(BlockPos pos) {
        return new Random(pos.asLong() + 0xABCDEF).nextLong();
    }
}
