package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.Instrument;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Properties;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NoteBlockTunerItem extends Item {
    public static final int APPLY_MODE = 0;
    public static final int COPY_MODE = 1;
    public static final int ADJUST_MODE = 2;

    public NoteBlockTunerItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        if(ctx.getPlayer().isSneaking()) return ActionResult.PASS;
        BlockState state = ctx.getWorld().getBlockState(ctx.getBlockPos());
        World world = ctx.getWorld();
        BlockPos pos = ctx.getBlockPos();
        ItemStack stack = ctx.getStack();
        int tunerMode = stack.getOrCreateSubTag("TunerData").getInt("Mode");
        int note = stack.getOrCreateSubTag("TunerData").getInt("Note");
        if(state.isOf(PhonosBlocks.RADIO_NOTE_BLOCK) || state.isOf(Blocks.NOTE_BLOCK)) {
            if(tunerMode == APPLY_MODE) {
                world.setBlockState(pos, state.with(Properties.NOTE, note));
                ctx.getPlayer().sendMessage(new TranslatableText("message.phonos.apply_note_tune_success").formatted(Formatting.GREEN), true);
            } else if(tunerMode == COPY_MODE) {
                stack.getOrCreateSubTag("TunerData").putInt("Note", state.get(Properties.NOTE));
                stack.getOrCreateSubTag("TunerData").putInt("Mode", APPLY_MODE);
                ctx.getPlayer().sendMessage(new TranslatableText("message.phonos.copy_note_tune_success").formatted(Formatting.AQUA), true);
            }
            return ActionResult.success(ctx.getWorld().isClient());
        } else {
            SoundEvent sound = Instrument.fromBlockState(state).getSound();
            if(tunerMode == ADJUST_MODE) {
                note += 1;
                if(note > 24) note = 0;
                stack.getOrCreateSubTag("TunerData").putInt("Note", note);
                playNote(sound, note, world, pos);
            } else if(tunerMode == APPLY_MODE) {
                playNote(sound, note, world, pos);
            }
            return ActionResult.success(ctx.getWorld().isClient());
        }
    }

    private static void playNote(SoundEvent sound, int note, World world, BlockPos pos) {
        if(!world.isClient()) for(ServerPlayerEntity player : ((ServerWorld)world).getPlayers()) player.networkHandler.sendPacket(new PlaySoundS2CPacket(sound, SoundCategory.BLOCKS, pos.getX()+0.5, pos.getY()+0.5, pos.getZ()+0.5,  1.5f, (float)Math.pow(2.0D, (double)(note - 12) / 12.0D)));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        int tunerMode = stack.getOrCreateSubTag("TunerData").getInt("Mode");
        tunerMode += 1;
        if(tunerMode > 2) tunerMode = 0;
        stack.getOrCreateSubTag("TunerData").putInt("Mode", tunerMode);
        user.sendMessage(new TranslatableText("message.phonos.mode_prefix").append(new TranslatableText("message.phonos.mode_"+tunerMode+"_title").formatted(Formatting.AQUA)), true);
        return world.isClient() ? TypedActionResult.consume(stack) : TypedActionResult.pass(stack);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if(group == this.getGroup()) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateSubTag("TunerData").putInt("Mode", APPLY_MODE);
            stack.getOrCreateSubTag("TunerData").putInt("Note", 0);
            stacks.add(stack);
        }
    }
}
