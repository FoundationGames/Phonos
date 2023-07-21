package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import io.github.foundationgames.phonos.world.sound.block.InputBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class LoudspeakerBlock extends AbstractLoudspeakerBlock implements InputBlock {
    public static final BooleanProperty[] INPUTS = BlockProperties.pluggableInputs(4);

    public final BlockConnectionLayout inputLayout = new BlockConnectionLayout()
            .addPoint(1.5, -2.5, 8, Direction.SOUTH)
            .addPoint(-1.5, -2.5, 8, Direction.SOUTH)
            .addPoint(4.5, -2.5, 8, Direction.SOUTH)
            .addPoint(-4.5, -2.5, 8, Direction.SOUTH);

    public LoudspeakerBlock(Settings settings) {
        super(settings);

        setDefaultState(BlockProperties.withAll(getDefaultState(), INPUTS, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

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
