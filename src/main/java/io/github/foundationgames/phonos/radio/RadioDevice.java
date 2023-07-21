package io.github.foundationgames.phonos.radio;

import io.github.foundationgames.phonos.sound.emitter.SoundEmitter;
import io.github.foundationgames.phonos.util.UniqueId;

import java.util.function.LongConsumer;

public interface RadioDevice {
    int getChannel();

    interface Transmitter extends RadioDevice, SoundEmitter {
        @Override
        default void forEachChild(LongConsumer action) {
            action.accept(UniqueId.ofRadioChannel(getChannel()));
        }
    }

    interface Receiver extends RadioDevice {
        void setAndUpdateChannel(int channel);

        void addReceiver();

        void removeReceiver();
    }
}
