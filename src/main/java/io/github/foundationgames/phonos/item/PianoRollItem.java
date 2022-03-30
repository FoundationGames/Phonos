package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.util.piano.BuiltinPianoRolls;
import io.github.foundationgames.phonos.util.piano.PianoRoll;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.collection.DefaultedList;

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

    @Override
    public void appendStacks(ItemGroup group, DefaultedList<ItemStack> stacks) {
        if (group == this.getGroup() || group == ItemGroup.SEARCH) {
            var chopsticks = this.create(BuiltinPianoRolls.CHOPSTICKS);
            chopsticks.setCustomName(new TranslatableText(getTranslationKey() + ".chopsticks").styled(style -> style.withItalic(false)));
            stacks.add(chopsticks);
        }
    }
}
