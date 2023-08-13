package io.github.foundationgames.phonos.item;

import net.minecraft.item.ItemStack;

public interface GlowableItem {
    default void setGlowing(ItemStack stack, boolean glowing) {
        stack.getOrCreateSubNbt("display").putBoolean("glowing", glowing);
    }

    default boolean isGlowing(ItemStack stack) {
        var display = stack.getSubNbt("display");
        return display != null && display.contains("glowing") && display.getBoolean("glowing");
    }
}
