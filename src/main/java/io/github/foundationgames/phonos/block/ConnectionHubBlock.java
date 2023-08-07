package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.block.entity.ConnectionHubBlockEntity;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import io.github.foundationgames.phonos.world.sound.block.InputBlock;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class ConnectionHubBlock extends FacingBlock implements BlockEntityProvider, InputBlock {
    public static final VoxelShape UP_SHAPE = createCuboidShape(4, 0, 4, 12, 4, 12);
    public static final VoxelShape DOWN_SHAPE = createCuboidShape(4, 12, 4, 12, 16, 12);
    public static final VoxelShape NORTH_SHAPE = createCuboidShape(4, 4, 12, 12, 12, 16);
    public static final VoxelShape SOUTH_SHAPE = createCuboidShape(4, 4, 0, 12, 12, 4);
    public static final VoxelShape EAST_SHAPE = createCuboidShape(0, 4, 4, 4, 12, 12);
    public static final VoxelShape WEST_SHAPE = createCuboidShape(12, 4, 4, 16, 12, 12);

    public final BlockConnectionLayout inputLayout = new BlockConnectionLayout()
        .addPoint(1.5, 1.5, 4, Direction.NORTH)
        .addPoint(-1.5, 1.5, 4, Direction.NORTH)
        .addPoint(-1.5, -1.5, 4, Direction.NORTH)
        .addPoint(1.5, -1.5, 4, Direction.NORTH);

    public ConnectionHubBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.UP));
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx).with(FACING, ctx.getSide());
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var side = hit.getSide();
        var facing = state.get(FACING);

        if (side == facing.getOpposite()) {
            return ActionResult.PASS;
        }

        if (player.canModifyBlocks()) {
            if (!world.isClient() && world.getBlockEntity(pos) instanceof ConnectionHubBlockEntity be) {
                if (PhonosUtil.holdingAudioCable(player)) {
                    return ActionResult.PASS;
                }

                if (side != facing) {
                    if (be.outputs.tryRemoveConnection(world, hit, !player.isCreative())) {
                        be.sync();
                        return ActionResult.SUCCESS;
                    }
                } else {
                    return tryRemoveConnection(state, world, pos, hit);
                }
            }
        }

        return ActionResult.success(side == facing);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (!newState.isOf(this) && world.getBlockEntity(pos) instanceof ConnectionHubBlockEntity be) {
            be.onDestroyed();
        }

        super.onStateReplaced(state, world, pos, newState, moved);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        builder.add(FACING);
    }

    @Override
    public Direction getRotation(BlockState state) {
        return state.get(FACING);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        var facing = state.get(FACING);

        return switch (facing) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            case DOWN -> DOWN_SHAPE;
            default -> UP_SHAPE;
        };
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new ConnectionHubBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        return PhonosUtil.blockEntityTicker(type, PhonosBlocks.CONNECTION_HUB_ENTITY);
    }

    @Override
    public boolean canInputConnect(ItemUsageContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var state = world.getBlockState(pos);
        var facing = state.get(FACING);
        var side = ctx.getSide();

        if (side == facing) {
            int index = this.getInputLayout().getClosestIndexClicked(ctx.getHitPos(), pos, getRotation(state));

            return !this.isInputPluggedIn(index, state, world, pos);
        }

        return false;
    }

    @Override
    public boolean playsSound(World world, BlockPos pos) {
        return false;
    }

    @Override
    public boolean isInputPluggedIn(int inputIndex, BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ConnectionHubBlockEntity be) {
            inputIndex = MathHelper.clamp(inputIndex, 0, be.inputs.length - 1);

            return be.inputs[inputIndex];
        }

        return false;
    }

    @Override
    public void setInputPluggedIn(int inputIndex, boolean pluggedIn, BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof ConnectionHubBlockEntity be) {
            inputIndex = MathHelper.clamp(inputIndex, 0, be.inputs.length - 1);
            be.inputs[inputIndex] = pluggedIn;
            be.sync();
            be.markDirty();
        }
    }

    @Override
    public BlockConnectionLayout getInputLayout() {
        return inputLayout;
    }
}
