package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.block.entity.PlayerPianoBlockEntity;
import io.github.foundationgames.phonos.block.entity.RadioRecorderBlockEntity;
import io.github.foundationgames.phonos.item.PhonosItems;
import io.github.foundationgames.phonos.item.PianoRollItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RadioRecorderBlock extends LoudspeakerBlock implements BlockEntityProvider, NotePlayReceivable {
    public static final BooleanProperty POWERED = Properties.POWERED;

    public RadioRecorderBlock(Settings settings) {
        super(settings);

        this.setDefaultState(this.getDefaultState().with(POWERED, false));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RadioRecorderBlockEntity(pos, state);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (hit.getSide() != Direction.UP && world.getBlockEntity(pos) instanceof RadioRecorderBlockEntity recorder) {
            var taken = recorder.takeItem();
            var holding = player.getStackInHand(hand);

            if (!taken.isEmpty()) {
                if (!world.isClient()) {
                    var entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, taken);
                    world.spawnEntity(entity);
                }

                return ActionResult.SUCCESS;
            }

            if (holding.isOf(PhonosItems.EMPTY_PIANO_ROLL)) {
                if (!world.isClient()) {
                    recorder.setItem(holding.copy());
                    holding.decrement(1);
                }

                return ActionResult.SUCCESS;
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);

        boolean powered = world.isReceivingRedstonePower(pos);

        if (powered != state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, powered));
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(this) && world.getBlockEntity(pos) instanceof RadioRecorderBlockEntity recorder) {
            var stack = recorder.takeItem();
            if (!stack.isEmpty()) {
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return RadioJukeboxBlock.typeCheck(type, PhonosBlocks.RADIO_RECORDER_ENTITY, RadioRecorderBlockEntity::tick);
    }

    @Override
    public IntProperty getChannelProperty() {
        return super.getChannelProperty();
    }

    @Override
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @Override
    public boolean isShapeFullCube(BlockState state, BlockView world, BlockPos pos) {
        return false;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        builder.add(POWERED);
    }

    @Override
    public void onNotePlayed(ServerWorld world, BlockPos pos, float pitch) {
        if (world.getBlockEntity(pos) instanceof RadioRecorderBlockEntity recorder) {
            recorder.receiveNote(pitch);
        }
    }
}
