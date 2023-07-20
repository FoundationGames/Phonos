package io.github.foundationgames.phonos.world.sound.block;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public interface InputBlock {
    boolean canInputConnect(ItemUsageContext ctx);

    boolean playsSound(World world, BlockPos pos);

    default ActionResult tryRemoveConnection(BlockState state, World world, BlockPos pos, BlockHitResult hit) {
        int clickIndex = getInputLayout().getClosestIndexClicked(hit.getPos(), pos, getRotation(state));

        if (clickIndex >= 0 && isInputPluggedIn(clickIndex, state, world, pos)) {
            setInputPluggedIn(clickIndex, false, state, world, pos);
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    default Direction getRotation(BlockState state) {
        return Direction.NORTH;
    }

    boolean isInputPluggedIn(int inputIndex, BlockState state, World world, BlockPos pos);

    void setInputPluggedIn(int inputIndex, boolean pluggedIn, BlockState state, World world, BlockPos pos);

    BlockConnectionLayout getInputLayout();
}
