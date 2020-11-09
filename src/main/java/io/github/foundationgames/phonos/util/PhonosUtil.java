package io.github.foundationgames.phonos.util;

import com.mojang.datafixers.util.Pair;
import io.github.foundationgames.phonos.mixin.StructurePoolAccess;
import io.github.foundationgames.phonos.world.RadioChannelState;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import net.minecraft.structure.processor.StructureProcessorLists;
import net.minecraft.util.Identifier;

import java.util.function.Supplier;

public class PhonosUtil {
    public static RadioChannelState getRadioState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(() -> new RadioChannelState(world), "radio_channel_state");
    }

    public static <T> T create(Supplier<T> creator) {
        return creator.get();
    }

    public static int slotOf(Inventory inv, ItemStack stack) {
        for (int i = 0; i < inv.size(); i++) {
            if(inv.getStack(i) == stack) return i;
        }
        return -1;
    }

    public static StructurePool tryAddElementToPool(Identifier targetPool, StructurePool pool, String elementId, StructurePool.Projection projection, int weight) {
        if(targetPool.equals(pool.getId())) {
            StructurePoolElement element = StructurePoolElement.method_30426(elementId, StructureProcessorLists.EMPTY).apply(projection);
            for (int i = 0; i < weight; i++) {
                ((StructurePoolAccess)pool).getElements().add(element);
            }
            ((StructurePoolAccess)pool).getElementCounts().add(Pair.of(element, weight));
        }
        return pool;
    }
}
