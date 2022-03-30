package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.util.piano.PianoRoll;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class RadioRecorderBlockEntity extends BlockEntity implements Syncing {
    private PianoRoll.Recorder currentRecorder = null;

    public RadioRecorderBlockEntity(BlockPos pos, BlockState state) {
        super(null, pos, state);
    }

    public void startRecording() {

    }

    public void stopRecording() {

    }
}
