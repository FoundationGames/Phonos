package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.util.piano.BuiltinPianoRolls;
import io.github.foundationgames.phonos.util.piano.PianoRoll;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PianoRollItem extends Item {
    public PianoRollItem(Settings settings) {
        super(settings);
    }

    public ItemStack create(PianoRoll roll) {
        var stack = new ItemStack(this);
        stack.setSubNbt("PianoRoll", roll.toNbt());
        return stack;
    }

    public static PianoRoll getRoll(ItemStack stack) {
        return PianoRoll.fromNbt(stack.getSubNbt("PianoRoll"));
    }

    public static int getDuration(ItemStack stack) {
        var rollNbt = stack.getSubNbt("PianoRoll");
        if (rollNbt != null) {
            return rollNbt.getInt("duration");
        }

        return 0;
    }

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (group == this.getGroup() || group == ItemGroup.SEARCH) {
            BuiltinPianoRolls.forEach((name, roll) -> {
                var stack = this.create(roll);
                stack.setCustomName(Text.translatable(getTranslationKey() + "." + name).styled(style -> style.withItalic(false)));
                stacks.add(stack);
            });
        }
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        int duration = getDuration(stack);
        int sec = (int) Math.floor((double) duration / 20);
        int min = (int) Math.floor((double) sec / 60);
        sec = sec % 60;
        tooltip.add(Text.translatable("tooltip.phonos.duration", String.format("%02d", min), String.format("%02d", sec)).formatted(Formatting.GRAY));

        super.appendTooltip(stack, world, tooltip, context);
    }
}
