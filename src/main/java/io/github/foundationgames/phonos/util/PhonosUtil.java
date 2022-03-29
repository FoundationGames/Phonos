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

import java.util.Collection;
import java.util.function.Supplier;

public enum PhonosUtil {;
    public static RadioChannelState getRadioState(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(tag -> {
            var state = new RadioChannelState(world);
            state.readNbt(tag);
            return state;
        }, () -> new RadioChannelState(world), RadioChannelState.ID);
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

    public static void tryAddElementToPool(Identifier targetPool, StructurePool pool, String elementId, StructurePool.Projection projection, int weight) {
        if(targetPool.equals(pool.getId())) {
            var element = StructurePoolElement.ofProcessedLegacySingle(elementId, StructureProcessorLists.EMPTY).apply(projection);
            for (int i = 0; i < weight; i++) {
                ((StructurePoolAccess)pool).phonos$getElements().add(element);
            }
            ((StructurePoolAccess)pool).phonos$getElementCounts().add(Pair.of(element, weight));
        }
    }

    public static float pitchFromNote(int note) {
        return (float) Math.pow(2, (double)(note - 12) / 12);
    }
    public static int noteFromPitch(float pitch) {
        return (int) Math.round(17.3123404907 * Math.log(pitch) + 12);
    }

    public static float[] arrFromList(Collection<Float> list) {
        var result = new float[list.size()];
        int i = 0;
        for (Float f : list) {
            result[i] = f;
            i++;
        }
        return result;
    }

    public static float[] fromIntBytesArr(int[] arr) {
        var result = new float[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = Float.intBitsToFloat(arr[i]);
        }
        return result;
    }

    public static int[] toIntBytesArr(float[] arr) {
        var result = new int[arr.length];
        for (int i = 0; i < arr.length; i++) {
            result[i] = Float.floatToIntBits(arr[i]);
        }
        return result;
    }
}
