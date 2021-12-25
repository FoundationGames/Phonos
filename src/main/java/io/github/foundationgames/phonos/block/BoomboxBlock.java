package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.item.BoomboxItem;
import io.github.foundationgames.phonos.item.ChannelTunerItem;
import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class BoomboxBlock extends LoudspeakerBlock {
    public static final VoxelShape NORTH_SOUTH_SHAPE = createCuboidShape(0, 0, 6, 16, 8, 10);
    public static final VoxelShape EAST_WEST_SHAPE = createCuboidShape(6, 0, 0, 10, 8, 16);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public static final BlockSoundGroup SOUNDS = new BlockSoundGroup(1, 1,
            SoundEvents.ENTITY_PLAYER_ATTACK_WEAK,
            SoundEvents.BLOCK_METAL_STEP,
            SoundEvents.BLOCK_METAL_PLACE,
            SoundEvents.BLOCK_METAL_HIT,
            SoundEvents.BLOCK_METAL_FALL
    );

    public BoomboxBlock(Settings settings) {
        super(settings);
        setDefaultState(getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getStackInHand(hand).getItem() instanceof ChannelTunerItem) return ActionResult.PASS;
        if(world.getBlockState(pos.offset(state.get(FACING).getOpposite())).isSideSolidFullSquare(world, pos, state.get(FACING))) {
            world.playSound(player, pos, SoundEvents.BLOCK_CHEST_LOCKED, SoundCategory.BLOCKS, 0.2f, 1.6f);
            return ActionResult.CONSUME;
        }
        if(hit.getSide() == state.get(FACING).getOpposite()) {
            world.setBlockState(pos, state.cycle(CHANNEL));
            return ActionResult.success(world.isClient());
        }
        return ActionResult.PASS;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        int channel = 0;
        if (ctx.getStack().getItem() instanceof BoomboxItem boombox) {
            channel = boombox.getChannel(ctx.getStack());
        }
        return getDefaultState().with(CHANNEL, channel).with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return switch (state.get(FACING)) {
            case EAST, WEST -> EAST_WEST_SHAPE;
            default -> NORTH_SOUTH_SHAPE;
        };
    }

    @Override
    public void onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        super.onBreak(world, pos, state, player);

        if (!player.isCreative()) {
            int channel = state.get(CHANNEL);
            var stack = new ItemStack(this);

            if (this.asItem() instanceof BoomboxItem item) {
                stack = item.createStack(channel);
            }

            var item = new ItemEntity(world, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5, stack);

            world.spawnEntity(item);
        }
    }

    @Override
    public ItemStack getPickStack(BlockView world, BlockPos pos, BlockState state) {
        if (this.asItem() instanceof BoomboxItem item) {
            item.createStack(state.get(CHANNEL));
        }
        return super.getPickStack(world, pos, state);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(FACING);
    }

    @Override
    protected void noteParticle(World world, BlockPos pos) {
        double t = (double) world.getTime() / 80;
        float c = (float)(t - Math.floor(t)) * 2;
        world.addParticle(ParticleTypes.NOTE,
                (double)pos.getX() + 0.5D,
                (double)pos.getY() + 0.73D,
                (double)pos.getZ() + 0.5D,
                c, 0.0D, 0.0D);
    }
}
