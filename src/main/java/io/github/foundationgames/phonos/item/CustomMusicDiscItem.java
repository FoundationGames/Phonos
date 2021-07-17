package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.screen.CustomMusicDiscGuiDescription;
import io.github.foundationgames.phonos.util.PhonosUtil;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.JukeboxBlock;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.stat.Stats;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.List;

public class CustomMusicDiscItem extends Item {
    public CustomMusicDiscItem(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (state.isOf(Blocks.JUKEBOX) && !state.get(JukeboxBlock.HAS_RECORD)) {
            ItemStack stack = context.getStack();
            if (!world.isClient) {
                ((JukeboxBlock)Blocks.JUKEBOX).setRecord(world, pos, state, stack);
                for(ServerPlayerEntity player : ((ServerWorld)world).getPlayers()) {
                    Identifier soundId = Identifier.tryParse(stack.getOrCreateSubNbt("MusicData").getString("SoundId"));
                    PayloadPackets.sendJukeboxIdSound(player, soundId, pos);
                    //ServerSidePacketRegistry.INSTANCE.sendToPlayer(player, new PlaySoundIdS2CPacket(soundId, SoundCategory.BLOCKS, Vec3d.of(pos).add(0.5, 0.5, 0.5), 1.8f, 1.0f));
                }
                stack.decrement(1);
                PlayerEntity playerEntity = context.getPlayer();
                if (playerEntity != null) {
                    playerEntity.incrementStat(Stats.PLAY_RECORD);
                }
            }

            return ActionResult.success(world.isClient);
        } else {
            return ActionResult.PASS;
        }
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        if(user.isCreative()) {
            user.openHandledScreen(new DiscGuiFactory(PhonosUtil.slotOf(user.getInventory(), user.getStackInHand(hand))));
            return world.isClient() ? TypedActionResult.success(user.getStackInHand(hand)) : TypedActionResult.pass(user.getStackInHand(hand));
        }
        else return super.use(world, user, hand);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if(group == this.getGroup() || group == ItemGroup.SEARCH) {
            ItemStack s = new ItemStack(this);
            s.getOrCreateSubNbt("MusicData").putString("SoundId", "minecraft:empty");
            s.getOrCreateSubNbt("MusicData").putInt("ComparatorSignal", 1);
            stacks.add(s);
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);
        tooltip.add(new LiteralText(stack.getOrCreateSubNbt("MusicData").getString("SoundId")).formatted(Formatting.BLUE));
    }

    public static class DiscGuiFactory implements ExtendedScreenHandlerFactory {
        private final int slot;

        public DiscGuiFactory(int slot) {
            this.slot = slot;
        }

        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
            buf.writeInt(slot);
        }

        @Override
        public Text getDisplayName() {
            return new LiteralText("");
        }

        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            return new CustomMusicDiscGuiDescription(syncId, inv, slot);
        }
    }
}
