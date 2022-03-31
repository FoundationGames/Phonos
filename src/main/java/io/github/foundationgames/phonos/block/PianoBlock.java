package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.util.ShapeDefinition;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PianoBlock extends HorizontalFacingBlock {
    public static final BooleanProperty POWERED = Properties.POWERED;

    public static final ShapeDefinition SHAPE = new ShapeDefinition()
            .cuboid(0, 0, 7, 16, 16, 15)
            .cuboid(0, 16, 6, 16, 18, 16)
            .cuboid(0, 6, 2, 16, 8, 7);

    public static final VoxelShape NORTH_SHAPE = SHAPE.toShape(Direction.NORTH);
    public static final VoxelShape SOUTH_SHAPE = SHAPE.toShape(Direction.SOUTH);
    public static final VoxelShape EAST_SHAPE = SHAPE.toShape(Direction.EAST);
    public static final VoxelShape WEST_SHAPE = SHAPE.toShape(Direction.WEST);

    public final Side side;
    protected final @Nullable Block neighbor;

    public PianoBlock(Settings settings, Side side, @Nullable Block neighbor) {
        super(settings);
        this.side = side;
        this.neighbor = neighbor;

        this.setDefaultState(this.getDefaultState().with(FACING, Direction.NORTH).with(POWERED, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING, POWERED);
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        var neighborPos = pos.offset(this.side.neighborDirection(state.get(FACING)));
        var neighborState = world.getBlockState(neighborPos);

        if (neighborState.getBlock() instanceof PianoBlock) {
            return neighborState.onUse(world, player, hand, hit.withBlockPos(neighborPos));
        }

        return super.onUse(state, world, pos, player, hand, hit);
    }

    public void onPowered(BlockState state, World world, BlockPos pos) {}

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);

        if (!newState.isOf(this)) {
            var neighborPos = pos.offset(this.side.neighborDirection(state.get(FACING)));

            if (world.getBlockState(neighborPos).getBlock() instanceof PianoBlock piano && piano.side == this.side.opposite()) {
                world.breakBlock(neighborPos, true);
            }
        } else {
            if (newState.get(POWERED)) {
                this.onPowered(state, world, pos);
            }
        }
    }

    @Override
    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        super.neighborUpdate(state, world, pos, block, fromPos, notify);

        var neighborPos = pos.offset(this.side.neighborDirection(state.get(FACING)));
        boolean hasPower = world.isReceivingRedstonePower(pos) || world.isReceivingRedstonePower(neighborPos);

        if (hasPower != state.get(POWERED)) {
            world.setBlockState(pos, state.with(POWERED, hasPower));
        }
    }

    public BlockState getPlacementState(Direction playerFacing) {
        return this.getDefaultState().with(FACING, playerFacing.getOpposite());
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var state = this.getPlacementState(ctx.getPlayerFacing());
        var neighborPos = ctx.getBlockPos().offset(this.side.neighborDirection(state.get(FACING)));

        return ctx.getWorld().getBlockState(neighborPos).canReplace(ctx) ? state : null;
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);

        var neighborPos = pos.offset(this.side.neighborDirection(state.get(FACING)));
        if (neighbor != null && neighbor instanceof PianoBlock neighborPiano) {
            world.setBlockState(neighborPos, neighborPiano.getPlacementState(placer != null ? placer.getHorizontalFacing() : Direction.NORTH));
        }
    }

    public Instrument getInstrument(float pitch, BlockState state, World world, BlockPos pos) {
        var neighborPos = pos.offset(this.side.neighborDirection(state.get(FACING)));
        var relevantPos = switch (this.side) {
            case LEFT -> pitch < 1f ? pos : neighborPos;
            case RIGHT -> pitch >= 1f ? pos : neighborPos;
        };

        return Instrument.fromBlockState(world.getBlockState(relevantPos.down()));
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            default -> NORTH_SHAPE;
        };
    }

    public enum Side {
        LEFT, RIGHT;

        public Direction neighborDirection(Direction facing) {
            return switch (this) {
                case LEFT -> facing.rotateYCounterclockwise();
                default -> facing.rotateYClockwise();
            };
        }

        public Side opposite() {
            return switch (this) {
                case LEFT -> RIGHT;
                default -> LEFT;
            };
        }
    }
}
