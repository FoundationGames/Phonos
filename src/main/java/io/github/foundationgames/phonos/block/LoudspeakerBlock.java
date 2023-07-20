package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import io.github.foundationgames.phonos.world.sound.block.InputBlock;
import io.github.foundationgames.phonos.world.sound.block.SoundDataHandler;
import io.github.foundationgames.phonos.world.sound.data.NoteBlockSoundData;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class LoudspeakerBlock extends HorizontalFacingBlock implements SoundDataHandler, InputBlock {
    public static final BooleanProperty[] INPUTS = BlockProperties.pluggableInputs(4);

    public final BlockConnectionLayout inputLayout = new BlockConnectionLayout()
            .addPoint(1.5, -2.5, 8, Direction.SOUTH)
            .addPoint(-1.5, -2.5, 8, Direction.SOUTH)
            .addPoint(4.5, -2.5, 8, Direction.SOUTH)
            .addPoint(-4.5, -2.5, 8, Direction.SOUTH);

    public LoudspeakerBlock(Settings settings) {
        super(settings);

        setDefaultState(BlockProperties.withAll(getDefaultState(), INPUTS, false).with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
        builder.add(INPUTS);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient() && player.canModifyBlocks() && hit.getSide().equals(state.get(FACING).getOpposite())) {
            if (!PhonosUtil.holdingAudioCable(player)) {
                return tryRemoveConnection(state, world, pos, hit);
            } else {
                return ActionResult.PASS;
            }
        }

        return ActionResult.success(hit.getSide().equals(state.get(FACING).getOpposite()));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    public boolean canInputConnect(ItemUsageContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var state = world.getBlockState(pos);
        var facing = state.get(FACING);

        if (ctx.getSide().getOpposite() == facing) {
            int index = this.getInputLayout().getClosestIndexClicked(ctx.getHitPos(), pos, facing);

            return !this.isInputPluggedIn(index, state, world, pos);
        }

        return false;
    }

    @Override
    public boolean playsSound(World world, BlockPos pos) {
        return true;
    }

    @Override
    public Direction getRotation(BlockState state) {
        return state.get(FACING);
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
            if (!world.getBlockState(pos.up()).isSolidBlock(world, pos.up())) {
                world.addParticle(ParticleTypes.NOTE,
                        pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5,
                        note, 0, 0);
            } else {
                var facing = state.get(FACING);
                for (var dir : Direction.Type.HORIZONTAL) if (dir != facing.getOpposite()) {
                    if (!world.getBlockState(pos.offset(dir)).isSolidBlock(world, pos.offset(dir))) {
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

    @Override
    public boolean isInputPluggedIn(int inputIndex, BlockState state, World world, BlockPos pos) {
        inputIndex = MathHelper.clamp(inputIndex, 0, 3);
        return state.get(INPUTS[inputIndex]);
    }

    @Override
    public void setInputPluggedIn(int inputIndex, boolean pluggedIn, BlockState state, World world, BlockPos pos) {
        inputIndex = MathHelper.clamp(inputIndex, 0, 3);
        world.setBlockState(pos, state.with(INPUTS[inputIndex], pluggedIn));
    }

    @Override
    public BlockConnectionLayout getInputLayout() {
        return this.inputLayout;
    }
}
