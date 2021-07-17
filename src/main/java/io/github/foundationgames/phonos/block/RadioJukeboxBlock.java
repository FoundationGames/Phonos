package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.entity.RadioJukeboxBlockEntity;
import io.github.foundationgames.phonos.item.ChannelTunerItem;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.RadioChannelState;
import net.fabricmc.fabric.api.container.ContainerProviderRegistry;
import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class RadioJukeboxBlock extends BlockWithEntity implements RadioChannelBlock {
    public static final IntProperty CHANNEL = IntProperty.of("channel", 0, 19);
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final BooleanProperty PLAYING = BooleanProperty.of("playing");

    public RadioJukeboxBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(CHANNEL, 0).with(POWERED, false).with(PLAYING, false));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getStackInHand(hand).getItem() instanceof ChannelTunerItem) return ActionResult.PASS;
        if (hit.getSide() == Direction.UP) {
            world.setBlockState(pos, state.cycle(CHANNEL));
            return ActionResult.success(world.isClient());
        }
        player.openHandledScreen(state.createScreenHandlerFactory(world, pos));
        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (!newState.isOf(this) || (newState.isOf(this) && !state.get(CHANNEL).equals(newState.get(CHANNEL))) && blockEntity instanceof RadioJukeboxBlockEntity) {
            if(!world.isClient() && ((RadioJukeboxBlockEntity)blockEntity).isPlaying()) {
                RadioChannelState pstate = PhonosUtil.getRadioState((ServerWorld)world);
                pstate.tryStopSound(pos, state.get(CHANNEL));
                ((RadioJukeboxBlockEntity)blockEntity).stop();
            }
        }
        if (state.getBlock() != newState.getBlock()) {
            if (blockEntity instanceof RadioJukeboxBlockEntity) {
                ItemScatterer.spawn(world, pos, ((RadioJukeboxBlockEntity)blockEntity));
                world.updateNeighbors(pos, this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }

    @Override
    public int getComparatorOutput(BlockState state, World world, BlockPos pos) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof RadioJukeboxBlockEntity) {
            return ((RadioJukeboxBlockEntity)blockEntity).getComparatorOutput();
        }
        return 0;
    }

    @Override
    public boolean hasComparatorOutput(BlockState state) {
        return true;
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);
        boolean wp = world.isReceivingRedstonePower(pos);
        boolean sp = state.get(POWERED);
        if(wp != sp) {
            if(world.getBlockEntity(pos) instanceof RadioJukeboxBlockEntity && wp) ((RadioJukeboxBlockEntity)world.getBlockEntity(pos)).playOrStop();
            world.setBlockState(pos, state.with(POWERED, wp));
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHANNEL, POWERED, PLAYING);
    }

    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new RadioJukeboxBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return checkType(type, PhonosBlocks.RADIO_JUKEBOX_ENTITY, RadioJukeboxBlockEntity::tick);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public IntProperty getChannelProperty() {
        return CHANNEL;
    }
}
