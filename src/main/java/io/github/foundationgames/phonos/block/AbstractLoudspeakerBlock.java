package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.world.sound.block.SoundDataHandler;
import io.github.foundationgames.phonos.world.sound.data.NoteBlockSoundData;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class AbstractLoudspeakerBlock extends HorizontalFacingBlock implements SoundDataHandler {
    public AbstractLoudspeakerBlock(Settings settings) {
        super(settings);

        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public void receiveSound(BlockState state, World world, BlockPos pos, SoundData sound) {
        if (!world.isClient()) {
            return;
        }

        if (MinecraftClient.getInstance().player.getPos().squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ()) > 5000) {
            return;
        }

        if (sound instanceof NoteBlockSoundData noteData) {
            double note = noteData.note / 24D;
            if (!world.getBlockState(pos.up()).isSideSolidFullSquare(world, pos.up(), Direction.DOWN)) {
                world.addParticle(ParticleTypes.NOTE,
                        pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                        note, 0, 0);
            } else {
                var facing = state.get(FACING);
                for (var dir : Direction.Type.HORIZONTAL) if (dir != facing.getOpposite()) {
                    if (!world.getBlockState(pos.offset(dir)).isSideSolidFullSquare(world, pos.offset(dir), dir.getOpposite())) {
                        world.addParticle(ParticleTypes.NOTE,
                                pos.getX() + 0.5 + dir.getVector().getX() * 0.6,
                                pos.getY() + 0.35,
                                pos.getZ() + 0.5 + dir.getVector().getZ() * 0.6,
                                note, 0, 0);
                    }
                }
            }
        }
    }
}
