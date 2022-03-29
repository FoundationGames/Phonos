package io.github.foundationgames.phonos.util.piano;

import io.github.foundationgames.phonos.util.PhonosUtil;

import java.util.ArrayList;
import java.util.List;

public final class BuiltinPianoRolls {
    private BuiltinPianoRolls() {}

    public static final PianoRoll CHOPSTICKS;

    private static void chopsticksCommonSection(List<PianoRoll.Stage> stages) {
        for (int i = 0; i < 6; i++) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(11), // F4
                    PhonosUtil.pitchFromNote(13)  // G4
            }, 5));
        }
        for (int i = 0; i < 6; i++) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(10), // E4
                    PhonosUtil.pitchFromNote(13)  // G4
            }, 5));
        }
        for (int i = 0; i < 6; i++) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(8),  // D4
                    PhonosUtil.pitchFromNote(17)  // B5
            }, 5));
        }
    }

    static {
        var stages = new ArrayList<PianoRoll.Stage>();

        chopsticksCommonSection(stages);

        for (int i = 0; i < 4; i++) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(6),  // C4
                    PhonosUtil.pitchFromNote(18)  // C5
            }, 5));
        }
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(8),  // D4
                PhonosUtil.pitchFromNote(17)  // B5
        }, 5));
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(10), // E4
                PhonosUtil.pitchFromNote(15)  // A4
        }, 5));

        chopsticksCommonSection(stages);

        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(6),  // C4
                PhonosUtil.pitchFromNote(18)  // C5
        }, 5));
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(13),  // G4
        }, 5));
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(10),  // E4
        }, 5));
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(6),  // C4
        }, 5));

        CHOPSTICKS = new PianoRoll(stages);
    }
}
