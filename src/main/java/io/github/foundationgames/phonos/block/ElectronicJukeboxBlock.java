package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.block.entity.ElectronicJukeboxBlockEntity;
import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

public class ElectronicJukeboxBlock extends JukeboxBlock implements BlockEntityProvider {
    protected ElectronicJukeboxBlock(Settings settings) {
        super(settings);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ElectronicJukeboxBlockEntity(pos, state);
    }

    public ActionResult useMusicDisc(World world, BlockPos pos, BlockState state, ItemStack stack, PlayerEntity user) {
        if (!world.isClient) {
            if (world.getBlockEntity(pos) instanceof JukeboxBlockEntity jukebox) {
                jukebox.setStack(stack.copy());
                world.emitGameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Emitter.of(user, state));
            }

            stack.decrement(1);
            if (user != null) {
                user.incrementStat(Stats.PLAY_RECORD);
            }
        }

        return ActionResult.success(world.isClient);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var side = hit.getSide();
        if (side == Direction.UP) {
            var stack = player.getStackInHand(hand);
            if (!state.get(HAS_RECORD) && stack.getItem() instanceof MusicDiscItem) {
                return this.useMusicDisc(world, pos, state, stack, player);
            }

            return super.onUse(state, world, pos, player, hand, hit);
        }
        if (side == Direction.DOWN) {
            return ActionResult.PASS;
        }

        if (player.canModifyBlocks()) {
            if (!world.isClient() && world.getBlockEntity(pos) instanceof ElectronicJukeboxBlockEntity be) {
                if (!PhonosUtil.holdingAudioCable(player) && be.outputs.tryRemoveConnection(world, hit, !player.isCreative())) {
                    be.sync();
                    return ActionResult.SUCCESS;
                } else {
                    return ActionResult.PASS;
                }
            }
        }

        return ActionResult.SUCCESS;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(this) && world.getBlockEntity(pos) instanceof ElectronicJukeboxBlockEntity jukebox) {
            jukebox.onDestroyed();
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return PhonosUtil.blockEntityTicker(type, PhonosBlocks.ELECTRONIC_JUKEBOX_ENTITY);
    }
}
