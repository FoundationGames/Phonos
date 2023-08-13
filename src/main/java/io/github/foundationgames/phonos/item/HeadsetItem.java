package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.Phonos;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.DyeableItem;
import net.minecraft.item.Equipment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class HeadsetItem extends Item implements DyeableItem, GlowableItem, Equipment {
    public static final Text NOISE_CANCELLING = Text.translatable("tooltip.phonos.item.noise_cancelling").formatted(Formatting.YELLOW);
    public static final Text GLOWING = Text.translatable("tooltip.phonos.item.glowing").formatted(Formatting.GRAY, Formatting.ITALIC);

    public static final Identifier DEFAULT_TEXTURE = Phonos.id("textures/entity/headset.png");
    public static final Identifier NOISE_CANCELLING_TEXTURE = Phonos.id("textures/entity/headset_noise_cancelling.png");

    public HeadsetItem(Settings settings) {
        super(settings);
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return this.equipAndSwap(this, world, user, hand);
    }

    public boolean isNoiseCancelling(ItemStack stack) {
        return stack.hasNbt() && stack.getNbt().getBoolean("noise_cancelling");
    }

    public void setNoiseCancelling(ItemStack stack, boolean noiseCancelling) {
        stack.getOrCreateNbt().putBoolean("noise_cancelling", noiseCancelling);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT && otherStack.isEmpty()) {
            setNoiseCancelling(stack, !isNoiseCancelling(stack));

            return true;
        }

        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public EquipmentSlot getSlotType() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
        super.appendTooltip(stack, world, tooltip, context);

        if (isNoiseCancelling(stack)) {
            tooltip.add(NOISE_CANCELLING);
        }

        if (isGlowing(stack)) {
            tooltip.add(GLOWING);
        }
    }

    public Identifier getTexture(ItemStack stack) {
        return isNoiseCancelling(stack) ? NOISE_CANCELLING_TEXTURE : DEFAULT_TEXTURE;
    }
}
