package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class BoomboxItem extends BlockItem implements ItemLoudspeaker {
    public BoomboxItem(Block block, Settings settings) {
        super(block, settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        int oldChannel = getChannel(stack);
        int channel = Math.floorMod(oldChannel + (user.isSneaking() ? -1 : 1), 20);

        if (world instanceof ServerWorld sWorld) {
            var state = PhonosUtil.getRadioState(sWorld);
            state.removeEntityReceiver(oldChannel, user);
            state.addEntityReceiver(channel, user);
        }

        stack.getOrCreateSubNbt("RadioData").putInt("Channel", channel);
        return TypedActionResult.consume(stack);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if(group == this.getGroup() || group == ItemGroup.SEARCH) {
            ItemStack stack = createStack(0);
            stacks.add(stack);
        }
    }

    public ItemStack createStack(int channel) {
        ItemStack stack = new ItemStack(this);
        stack.getOrCreateSubNbt("RadioData").putInt("Channel", channel);
        return stack;
    }

    @Override
    public int getChannel(ItemStack stack) {
        return stack.getOrCreateSubNbt("RadioData").getInt("Channel");
    }
}
