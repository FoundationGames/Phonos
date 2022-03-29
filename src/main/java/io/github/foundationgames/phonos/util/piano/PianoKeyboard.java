package io.github.foundationgames.phonos.util.piano;

public class PianoKeyboard {
    private static final int ANIMATION_LENGTH = 3;

    private final int[] keyTimers = new int[25];

    public void tick() {
        for (int i = 0; i < keyTimers.length; i++) {
            if (keyTimers[i] > 0) {
                keyTimers[i]--;
            }
        }
    }

    public void press(int key) {
        this.keyTimers[key] = ANIMATION_LENGTH;
    }

    public float getAnimationProgress(int key, float tickDelta) {
        return Math.max((float) this.keyTimers[key] - tickDelta, 0) / ANIMATION_LENGTH;
    }
}
