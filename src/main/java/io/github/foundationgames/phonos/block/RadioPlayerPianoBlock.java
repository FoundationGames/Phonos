package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.block.entity.PlayerPianoBlockEntity;
import io.github.foundationgames.phonos.item.ChannelTunerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RadioPlayerPianoBlock extends PlayerPianoBlock implements RadioChannelBlock {
    public static final IntProperty CHANNEL = IntProperty.of("channel", 0, 19);

    public RadioPlayerPianoBlock(Settings settings, Block neighbor) {
        super(settings, neighbor);
        this.setDefaultState(this.getDefaultState().with(CHANNEL, 0));
    }

    @Override
    protected ActionResult onUseOther(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getStackInHand(hand).getItem() instanceof ChannelTunerItem) {
            return ActionResult.PASS;
        }

        world.setBlockState(pos, state.cycle(CHANNEL));
        return ActionResult.success(world.isClient());
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PlayerPianoBlockEntity.Radio(pos, state);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return RadioJukeboxBlock.typeCheck(type, PhonosBlocks.RADIO_PLAYER_PIANO_ENTITY, PlayerPianoBlockEntity::tick);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        builder.add(CHANNEL);
    }

    @Override
    public IntProperty getChannelProperty() {
        return CHANNEL;
    }
}
