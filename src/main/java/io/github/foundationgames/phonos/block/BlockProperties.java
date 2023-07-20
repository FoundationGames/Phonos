package io.github.foundationgames.phonos.block;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Property;

public final class BlockProperties {
    private static final Int2ObjectMap<BooleanProperty> INPUT_INDEX_PROPERTIES = new Int2ObjectArrayMap<>();

    private BlockProperties() {}

    public static BooleanProperty[] pluggableInputs(int count) {
        var properties = new BooleanProperty[count];

        for (int i = 0; i < count; i++) {
            properties[i] = INPUT_INDEX_PROPERTIES
                    .computeIfAbsent(i, idx -> BooleanProperty.of("input_" + idx + "_plugged"));
        }
        return properties;
    }

    public static <T extends Comparable<T>> BlockState withAll(BlockState state, Property<T>[] properties, T value) {
        for (var property : properties) {
            state = state.with(property, value);
        }

        return state;
    }
}
