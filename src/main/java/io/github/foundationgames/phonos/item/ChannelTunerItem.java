package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.block.RadioChannelBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.state.property.IntProperty;
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
        int s = stack.getOrCreateSubTag("TunerData").getInt("Channel");
        s += user.isSneaking() ? -1 : 1; if(s > 19) s = 0; if(s < 0) s = 19;
        stack.getOrCreateSubTag("TunerData").putInt("Channel", s);
        return TypedActionResult.consume(stack);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext ctx) {
        if(ctx.getWorld().getBlockState(ctx.getBlockPos()).getBlock() instanceof RadioChannelBlock) {
            RadioChannelBlock b = (RadioChannelBlock)ctx.getWorld().getBlockState(ctx.getBlockPos()).getBlock();
            IntProperty property = b.getChannelProperty();
            int s = ctx.getStack().getOrCreateSubTag("TunerData").getInt("Channel");
            s = Math.min(20, Math.max(s, 0));
            ctx.getWorld().setBlockState(ctx.getBlockPos(), ctx.getWorld().getBlockState(ctx.getBlockPos()).with(property, s));
            return ActionResult.success(ctx.getWorld().isClient());
        }
        return super.useOnBlock(ctx);
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if(group == this.getGroup()) {
            ItemStack stack = new ItemStack(this);
            stack.getOrCreateSubTag("TunerData").putInt("Channel", 0);
            stacks.add(stack);
        }
    }
}
