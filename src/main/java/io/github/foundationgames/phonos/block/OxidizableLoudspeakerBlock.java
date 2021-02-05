package io.github.foundationgames.phonos.block;

import net.minecraft.block.BlockState;
import net.minecraft.block.Oxidizable;
import net.minecraft.class_5810;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;

import java.util.Random;

// Yes, i did this with incomplete Yarn

public class OxidizableLoudspeakerBlock extends LoudspeakerBlock implements class_5810 {
    private final BlockState nextOxidizedState;
    private final class_5811 stage;

    public OxidizableLoudspeakerBlock(Settings settings, class_5811 stage, BlockState nextState) {
        super(settings);
        this.nextOxidizedState = nextState != null ? nextState : this.getDefaultState();
        this.stage = stage;
    }

    @Override
    public BlockState getOxidationResult(BlockState state) {
        return nextOxidizedState;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        this.method_33621(state, world, pos, random);
    }

    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public class_5811 method_33622() {
        return this.stage;
    }
}
