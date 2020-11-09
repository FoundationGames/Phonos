package io.github.foundationgames.phonos.block;

import io.github.foundationgames.phonos.item.ChannelTunerItem;
import io.github.foundationgames.phonos.item.NoteBlockTunerItem;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.RadioChannelState;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.stat.Stats;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.awt.*;

public class RadioNoteBlock extends Block implements RadioChannelBlock {
    //public static final EnumProperty<Instrument> INSTRUMENT = Properties.INSTRUMENT; ~~ instrument is gone ~~
    public static final BooleanProperty POWERED = Properties.POWERED;
    public static final IntProperty NOTE = Properties.NOTE;
    public static final IntProperty CHANNEL = IntProperty.of("channel", 0, 19);

    public RadioNoteBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(getDefaultState()/*.with(INSTRUMENT, Instrument.HARP)*/.with(NOTE, 0).with(POWERED, false).with(CHANNEL, 0));
    }

    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()/*.with(INSTRUMENT, Instrument.fromBlockState(ctx.getWorld().getBlockState(ctx.getBlockPos().down())))*/;
    }

    public BlockState getStateForNeighborUpdate(BlockState state, Direction direction, BlockState newState, WorldAccess world, BlockPos pos, BlockPos posFrom) {
        return /*direction == Direction.DOWN ? state.with(INSTRUMENT, Instrument.fromBlockState(newState)) : */super.getStateForNeighborUpdate(state, direction, newState, world, pos, posFrom);
    }

    public void neighborUpdate(BlockState state, World world, BlockPos pos, Block block, BlockPos fromPos, boolean notify) {
        boolean powered = world.isReceivingRedstonePower(pos);
        if (powered != state.get(POWERED)) {
            if (powered) {
                this.playNote(world, pos);
            }
            world.setBlockState(pos, state.with(POWERED, powered), 3);
        }
    }

    private void playNote(World world, BlockPos pos) {
        if(!world.isClient()) {
            RadioChannelState pstate = PhonosUtil.getRadioState((ServerWorld)world);
            pstate.playSound(pos, Instrument.fromBlockState(world.getBlockState(pos.down())).getSound(), world.getBlockState(pos).get(CHANNEL), 1.8f, (float)Math.pow(2.0D, (double)(world.getBlockState(pos).get(NOTE) - 12) / 12.0D), false);
        }
    }

    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if(player.getStackInHand(hand).getItem() instanceof ChannelTunerItem || player.getStackInHand(hand).getItem() instanceof NoteBlockTunerItem) return ActionResult.PASS;
        if (world.isClient) {
            return ActionResult.SUCCESS;
        } else if(hit.getSide() == Direction.UP) {
            world.setBlockState(pos, state.cycle(CHANNEL));
            return ActionResult.CONSUME;
        } else {
            world.setBlockState(pos, state.cycle(NOTE), 3);
            world.addSyncedBlockEvent(pos, this, 0, 0);
            player.incrementStat(Stats.TUNE_NOTEBLOCK);
            return ActionResult.CONSUME;
        }
    }

    public void onBlockBreakStart(BlockState state, World world, BlockPos pos, PlayerEntity player) {
        if (!world.isClient) {
            if (world.getBlockState(pos.up()).isAir()) {
                world.addSyncedBlockEvent(pos, this, 0, 0);
            }
            player.incrementStat(Stats.PLAY_NOTEBLOCK);
        }
    }

    public boolean onSyncedBlockEvent(BlockState state, World world, BlockPos pos, int type, int data) {
        int i = state.get(NOTE);
        float f = (float)Math.pow(2.0D, (double)(i - 12) / 12.0D);
        world.playSound(null, pos, Instrument.fromBlockState(world.getBlockState(pos.down())).getSound(), SoundCategory.RECORDS, 3.0F, f);
        world.addParticle(ParticleTypes.NOTE, (double)pos.getX() + 0.5D, (double)pos.getY() + 1.2D, (double)pos.getZ() + 0.5D, (double)i / 24.0D, 0.0D, 0.0D);
        return true;
    }

    public static int getColorFromNote(int note) {
        float d = (float)note/24;
        float r = Math.max(0.0F, MathHelper.sin((d + 0.0F) * 6.2831855F) * 0.65F + 0.35F);
        float g = Math.max(0.0F, MathHelper.sin((d + 0.33333334F) * 6.2831855F) * 0.65F + 0.35F);
        float b = Math.max(0.0F, MathHelper.sin((d + 0.6666667F) * 6.2831855F) * 0.65F + 0.35F);
        Color c = new Color(r, g, b);
        return c.getRGB();
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED, NOTE, CHANNEL);
    }

    @Override
    public IntProperty getChannelProperty() {
        return CHANNEL;
    }
}
