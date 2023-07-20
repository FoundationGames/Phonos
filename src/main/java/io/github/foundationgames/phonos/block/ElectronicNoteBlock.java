package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.block.entity.ElectronicJukeboxBlockEntity;
import io.github.foundationgames.phonos.block.entity.ElectronicNoteBlockEntity;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import io.github.foundationgames.phonos.world.sound.block.InputBlock;
import io.github.foundationgames.phonos.world.sound.data.NoteBlockSoundData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ElectronicNoteBlock extends NoteBlock implements BlockEntityProvider, InputBlock {
    public final BlockConnectionLayout inputLayout = new BlockConnectionLayout()
            .addPoint(-8, 3.5, -3.5, Direction.WEST)
            .addPoint(8, 3.5, 3.5, Direction.EAST)
            .addPoint(-3.5, 3.5, 8, Direction.SOUTH)
            .addPoint(3.5, 3.5, -8, Direction.NORTH);

    public ElectronicNoteBlock(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (player.canModifyBlocks()) {
            if (!world.isClient() && world.getBlockEntity(pos) instanceof ElectronicNoteBlockEntity be) {
                if (PhonosUtil.holdingAudioCable(player)) {
                    return ActionResult.PASS;
                } else if (hit.getSide() != Direction.UP) {
                    var relHitPos = hit.getPos().subtract(pos.toCenterPos());
                    if (relHitPos.getY() < 0) {
                        if (be.outputs.tryRemoveConnection(world, hit, !player.isCreative())) {
                            be.sync();
                            return ActionResult.SUCCESS;
                        }
                    } else if (tryRemoveConnection(state, world, pos, hit) == ActionResult.SUCCESS) {
                        return ActionResult.SUCCESS;
                    }
                }
            }
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        boolean powered = world.isReceivingRedstonePower(pos);
        if (powered != state.get(POWERED)) {
            if (powered && !world.isClient()) {
                this.emitNote(state, world, pos);
            }

            world.setBlockState(pos, state.with(POWERED, powered), 3);
        }

    }

    private void emitNote(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ElectronicNoteBlockEntity be) {
            long id = be.emitterId();
            var instrument = state.get(INSTRUMENT);
            int note = state.get(NOTE);
            float pitch = instrument.shouldSpawnNoteParticles() ? getNotePitch(note) : 1;
            if (instrument.isNotBaseBlock() || world.getBlockState(pos.up()).isAir()) {
                SoundStorage.getInstance(world).play(world,
                        NoteBlockSoundData.create(id, instrument.getSound(), 2, pitch, instrument, note),
                        new SoundEmitterTree(id));
            }
        }
    }

    @Override
    public boolean canInputConnect(ItemUsageContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var state = world.getBlockState(pos);

        if (ctx.getSide() != Direction.UP && ctx.getSide() != Direction.DOWN) {
            var relHitPos = ctx.getHitPos().subtract(pos.toCenterPos());

            if (relHitPos.getY() < 0) {
                return false;
            }

            int index = this.getInputLayout().getClosestIndexClicked(ctx.getHitPos(), pos);

            return !this.isInputPluggedIn(index, state, world, pos);
        }

        return false;
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(this) && world.getBlockEntity(pos) instanceof ElectronicNoteBlockEntity be) {
            be.onDestroyed();
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    public boolean playsSound(World world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isInputPluggedIn(int inputIndex, BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ElectronicNoteBlockEntity be) {
            inputIndex = MathHelper.clamp(inputIndex, 0, be.inputs.length - 1);

            return be.inputs[inputIndex];
        }

        return false;
    }

    @Override
    public void setInputPluggedIn(int inputIndex, boolean pluggedIn, BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ElectronicNoteBlockEntity be) {
            inputIndex = MathHelper.clamp(inputIndex, 0, be.inputs.length - 1);
            be.inputs[inputIndex] = pluggedIn;
            be.sync();
        }
    }

    @Override
    public BlockConnectionLayout getInputLayout() {
        return this.inputLayout;
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ElectronicNoteBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return PhonosUtil.blockEntityTicker(type, PhonosBlocks.ELECTRONIC_NOTE_BLOCK_ENTITY);
    }
}
