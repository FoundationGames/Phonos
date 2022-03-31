package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.block.entity.PlayerPianoBlockEntity;
import io.github.foundationgames.phonos.block.entity.RadioRecorderBlockEntity;
import io.github.foundationgames.phonos.item.PianoRollItem;
import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlayerPianoBlock extends PianoBlock implements BlockEntityProvider {
    public PlayerPianoBlock(Settings settings, Block neighbor) {
        super(settings, Side.LEFT, neighbor);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var hitPos = hit.getPos();
        double midOffset = switch (state.get(FACING)) {
            case SOUTH -> hitPos.x - (pos.getX() + 1);
            case WEST -> hitPos.z - (pos.getZ() + 1);
            case EAST -> pos.getZ() - hitPos.z;
            default -> pos.getX() - hitPos.x;
        };
        double heightOffset = hitPos.y - pos.getY();

        if (heightOffset > 0.49 && heightOffset < 0.51 &&
                midOffset > -0.6875 && midOffset < 0.6875 &&
                hit.getSide() == Direction.UP) {
            int note = (int) Math.round(((midOffset + 0.6875) / 1.375) * 25);

            if (world.getBlockEntity(pos) instanceof PlayerPianoBlockEntity piano) {
                if (!world.isClient()) {
                    piano.playNote(PhonosUtil.pitchFromNote(note));
                }

                return ActionResult.SUCCESS;
            }
        }

        var holding = player.getStackInHand(hand);

        if (hit.getSide() != Direction.UP && world.getBlockEntity(pos) instanceof PlayerPianoBlockEntity piano) {
            var taken = piano.takeItem();

            if (!taken.isEmpty()) {
                if (!world.isClient()) {
                    var entity = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5, taken);
                    world.spawnEntity(entity);
                }

                return ActionResult.SUCCESS;
            }

            if (holding.getItem() instanceof PianoRollItem) {
                if (!world.isClient()) {
                    piano.setItem(holding.copy());
                    holding.decrement(1);
                }

                return ActionResult.SUCCESS;
            }

            return this.onUseOther(state, world, pos, player, hand, hit);
        }

        return ActionResult.PASS;
    }

    protected ActionResult onUseOther(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        return ActionResult.PASS;
    }

    @Override
    public void onPowered(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof PlayerPianoBlockEntity pianoEntity) {
            pianoEntity.togglePianoRoll();
        }
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(this) && world.getBlockEntity(pos) instanceof PlayerPianoBlockEntity piano) {
            var stack = piano.takeItem();
            if (!stack.isEmpty()) {
                world.spawnEntity(new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, stack));
            }
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new PlayerPianoBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return RadioJukeboxBlock.typeCheck(type, PhonosBlocks.PLAYER_PIANO_ENTITY, PlayerPianoBlockEntity::tick);
    }
}
