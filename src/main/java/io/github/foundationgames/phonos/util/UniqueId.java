package io.github.foundationgames.phonos.util;

import net.minecraft.util.math.BlockPos;

import java.util.Random;

public final class UniqueId {
    public static long random() {
        return new Random().nextLong();
    }

    public static long ofBlock(BlockPos pos) {
        return obf(pos.asLong() + 0xABCDEF);
    }

    public static long ofRadioChannel(int channel) {
        return obf(channel + 0xFADECAB);
    }

    public static long obf(long uniqueId) {
        return new Random(uniqueId).nextLong();
    }
}
