package io.github.foundationgames.phonos.sound.emitter;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

public interface SoundEmitter {
    long emitterId();

    void forEachSource(Consumer<SoundSource> action);

    default boolean hasChildren() {
        boolean[] found = {false};
        this.forEachChild(l -> found[0] = true);
        return found[0];
    }

    void forEachChild(LongConsumer action);
}
