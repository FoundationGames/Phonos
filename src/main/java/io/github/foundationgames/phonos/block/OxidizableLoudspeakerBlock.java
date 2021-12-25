package io.github.foundationgames.phonos.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

public class OxidizableLoudspeakerBlock extends LoudspeakerBlock implements Oxidizable {
    private final OxidizationLevel stage;

    public OxidizableLoudspeakerBlock(OxidizationLevel stage, Settings settings) {
        super(settings);
        this.stage = stage;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.tickDegradation(state, world, pos, random);
    }

    public boolean hasRandomTicks(BlockState state) {
        return Oxidizable.getIncreasedOxidationBlock(state.getBlock()).isPresent();
    }

    @Override
    public OxidizationLevel getDegradationLevel() {
        return this.stage;
    }
}
