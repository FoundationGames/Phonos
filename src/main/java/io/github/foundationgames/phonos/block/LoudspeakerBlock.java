package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.client.ClientRecieverStorage;
import io.github.foundationgames.phonos.item.ChannelTunerItem;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.RadioChannelState;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.IntProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import java.util.Random;

public class LoudspeakerBlock extends Block implements SoundPlayReceivable, RadioChannelBlock {
    public static final IntProperty CHANNEL = IntProperty.of("channel", 0, 19);

    public LoudspeakerBlock(Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState().with(CHANNEL, 0));
    }

    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getStackInHand(hand).getItem() instanceof ChannelTunerItem) return ActionResult.PASS;
        if(hit.getSide() == Direction.UP) {
            world.setBlockState(pos, state.cycle(CHANNEL));
            return ActionResult.success(world.isClient());
        }
        return super.onUse(state, world, pos, player, hand, hit);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        super.onStateReplaced(state, world, pos, newState, moved);
        if(world instanceof ServerWorld) {
            RadioChannelState pstate = PhonosUtil.getRadioState((ServerWorld)world);
            if(state.isOf(this)) pstate.removeReciever(state.get(CHANNEL), pos);
            if(newState.isOf(this)) pstate.addReciever(newState.get(CHANNEL), pos);
        }
    }

    @Override
    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        super.onBlockAdded(state, world, pos, oldState, notify);
        if(!oldState.isOf(this)) this.onStateReplaced(oldState, world, pos, state, false);
    }

    @Override
    public boolean hasRandomTicks(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.randomTick(state, world, pos, random);
        RadioChannelState s = PhonosUtil.getRadioState(world);
        if(!s.hasReciever(state.get(CHANNEL), pos)) s.addReciever(state.get(CHANNEL), pos);
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(CHANNEL);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void onRecievedSoundClient(ClientWorld world, BlockState state, BlockPos pos, int channel, float volume, float pitch) {
        noteParticle(world, pos);
    }

    @Override
    @Environment(EnvType.CLIENT)
    public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
        if(ClientRecieverStorage.isChannelPlaying(state.get(CHANNEL))) noteParticle(world, pos);
    }

    protected void noteParticle(World world, BlockPos pos) {
        double t = (double) world.getTime() / 80;
        float c = (float)(t - Math.floor(t)) * 2;
        if(!world.getBlockState(pos.north()).isSolidBlock(world, pos)) world.addParticle(ParticleTypes.NOTE,
                (double)pos.getX() + 0.5D,
                (double)pos.getY() + 0.35D,
                (double)pos.getZ() - 0.1D,
                c, 0.0D, 0.0D);
        if(!world.getBlockState(pos.south()).isSolidBlock(world, pos)) world.addParticle(ParticleTypes.NOTE,
                (double)pos.getX() + 0.5D,
                (double)pos.getY() + 0.35D,
                (double)pos.getZ() + 1.1D,
                c, 0.0D, 0.0D);
        if(!world.getBlockState(pos.east()).isSolidBlock(world, pos)) world.addParticle(ParticleTypes.NOTE,
                (double)pos.getX() + 1.1D,
                (double)pos.getY() + 0.35D,
                (double)pos.getZ() + 0.5D,
                c, 0.0D, 0.0D);
        if(!world.getBlockState(pos.west()).isSolidBlock(world, pos)) world.addParticle(ParticleTypes.NOTE,
                (double)pos.getX() - 0.1D,
                (double)pos.getY() + 0.35D,
                (double)pos.getZ() + 0.5D,
                c, 0.0D, 0.0D);
    }

    @Override
    public IntProperty getChannelProperty() {
        return CHANNEL;
    }
}
