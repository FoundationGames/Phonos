package io.github.foundationgames.phonos.item;

import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;

public interface ItemLoudspeaker {
    int getChannel(ItemStack stack);

    static void stackUpdatedForEntity(Entity entity, ItemStack oldStack, ItemStack newStack) {
        if (entity.getEntityWorld() instanceof ServerWorld sWorld) {
            var state = PhonosUtil.getRadioState(sWorld);

            if (oldStack.getItem() instanceof ItemLoudspeaker speaker) {
                state.removeEntityReceiver(speaker.getChannel(oldStack), entity);
            }
            if (newStack.getItem() instanceof ItemLoudspeaker speaker) {
                state.addEntityReceiver(speaker.getChannel(newStack), entity);
            }
        }
    }
}
