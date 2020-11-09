package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.item.ChannelTunerItem;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
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

public class PotatoSpeakerBlock extends LoudspeakerBlock {
    public static final VoxelShape SHAPE = createCuboidShape(6, 0, 6, 10, 6, 10);
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    public PotatoSpeakerBlock(Settings settings) {
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
        return getDefaultState().with(FACING, ctx.getPlayerFacing().getOpposite());
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
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
                (double)pos.getY() + 0.63D,
                (double)pos.getZ() + 0.5D,
                c, 0.0D, 0.0D);
    }
}
