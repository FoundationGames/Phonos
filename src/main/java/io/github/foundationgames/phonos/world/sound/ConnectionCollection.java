package io.github.foundationgames.phonos.world.sound;

import java.util.function.BiConsumer;

public interface ConnectionCollection {
    void forEach(BiConsumer<Integer, CableConnection> action);
}
