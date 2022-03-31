package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.block.PianoBlock;
import io.github.foundationgames.phonos.block.RadioChannelBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;

public class ChannelTunerItem extends Item {
    public ChannelTunerItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        int s = stack.getOrCreateSubNbt("TunerData").getInt("Channel");
        s += user.isSneaking() ? -1 : 1; if(s > 19) s = 0; if(s < 0) s = 19;
        stack.getOrCreateSubNbt("TunerData").putInt("Channel", s);
        return TypedActionResult.consume(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        var world = ctx.getWorld();
        var pos = ctx.getBlockPos();
        var state = world.getBlockState(pos);

        if (!(state.getBlock() instanceof RadioChannelBlock) && state.getBlock() instanceof PianoBlock piano) {
            pos = pos.offset(piano.side.neighborDirection(state.get(Properties.HORIZONTAL_FACING)));
            state = world.getBlockState(pos);
        }

        if(state.getBlock() instanceof RadioChannelBlock radio) {
            IntProperty property = radio.getChannelProperty();
            int channel = ctx.getStack().getOrCreateSubNbt("TunerData").getInt("Channel");
            channel = Math.min(20, Math.max(channel, 0));
            world.setBlockState(pos, state.with(property, channel));
            return ActionResult.success(world.isClient());
        }
        return super.useOnBlock(ctx);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if(group == this.getGroup() || group == ItemGroup.SEARCH) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateSubNbt("TunerData").putInt("Channel", 0);
            stacks.add(stack);
        }
    }
}
