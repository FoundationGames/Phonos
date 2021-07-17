package io.github.foundationgames.phonos.util;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import net.minecraft.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CopperStateMap {
    private static final Map<Block, Block> INCREASES = new HashMap<>();

    private static final Map<Block, Block> DECREASES = new HashMap<>();

    private static final Map<Block, Block> WAXED = new HashMap<>();

    private static final Map<Block, Block> UNWAXED = new HashMap<>();

    public static void put(Block block, @Nullable Block oxidized, Block waxed) {
        if (oxidized != null) {
            INCREASES.put(block, oxidized);
            DECREASES.put(oxidized, block);
        }
        WAXED.put(block, waxed);
        UNWAXED.put(waxed, block);
    }

    public static Optional<Block> getIncrease(Block block) {
        return Optional.ofNullable(INCREASES.get(block));
    }

    public static Optional<Block> getDecrease(Block block) {
        return Optional.ofNullable(DECREASES.get(block));
    }

    public static Optional<Block> getWaxed(Block block) {
        return Optional.ofNullable(WAXED.get(block));
    }

    public static Optional<Block> getUnwaxed(Block block) {
        return Optional.ofNullable(UNWAXED.get(block));
    }

    public static Block getOriginalStage(Block block) {
        var result = block;
        for (var b = getDecrease(block); b.isPresent(); b = getDecrease(b.get())) {
            result = b.get();
        }
        return result;
    }

    static {
        put(PhonosBlocks.COPPER_SPEAKER, PhonosBlocks.EXPOSED_COPPER_SPEAKER, PhonosBlocks.WAXED_COPPER_SPEAKER);
        put(PhonosBlocks.EXPOSED_COPPER_SPEAKER, PhonosBlocks.WEATHERED_COPPER_SPEAKER, PhonosBlocks.WAXED_EXPOSED_COPPER_SPEAKER);
        put(PhonosBlocks.WEATHERED_COPPER_SPEAKER, PhonosBlocks.OXIDIZED_COPPER_SPEAKER, PhonosBlocks.WAXED_WEATHERED_COPPER_SPEAKER);
        put(PhonosBlocks.OXIDIZED_COPPER_SPEAKER, null, PhonosBlocks.WAXED_OXIDIZED_COPPER_SPEAKER);
    }
}
