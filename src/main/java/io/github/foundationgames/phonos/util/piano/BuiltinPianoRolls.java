package io.github.foundationgames.phonos.util.piano;

import io.github.foundationgames.phonos.util.PhonosUtil;

import java.sql.Date;
import java.time.Instant;
import java.time.Month;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public final class BuiltinPianoRolls {
    private static final Map<String, PianoRoll> REGISTRY = new HashMap<>();

    private BuiltinPianoRolls() {}

    public static final PianoRoll CHOPSTICKS = register("chopsticks", chopsticks());
    private static final int CHOPSTICKS_SPEED = 5;

    public static PianoRoll register(String name, PianoRoll roll) {
        REGISTRY.put(name, roll);

        return roll;
    }

    public static void forEach(BiConsumer<String, PianoRoll> action) {
        REGISTRY.forEach(action);
    }

    private static PianoRoll chopsticks() {
        var stages = new ArrayList<PianoRoll.Stage>();

        chopsticksCommonSection(stages);

        for (int i = 0; i < 4; i++) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(6),  // C4
                    PhonosUtil.pitchFromNote(18)  // C5
            }, CHOPSTICKS_SPEED));
        }
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(8),  // D4
                PhonosUtil.pitchFromNote(17)  // B5
        }, CHOPSTICKS_SPEED));
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(10), // E4
                PhonosUtil.pitchFromNote(15)  // A4
        }, CHOPSTICKS_SPEED));

        chopsticksCommonSection(stages);

        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(6),  // C4
                PhonosUtil.pitchFromNote(18)  // C5
        }, CHOPSTICKS_SPEED));
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(13),  // G4
        }, CHOPSTICKS_SPEED));
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(10),  // E4
        }, CHOPSTICKS_SPEED));
        stages.add(new PianoRoll.Stage(new float[] {
                PhonosUtil.pitchFromNote(6),  // C4
        }, CHOPSTICKS_SPEED));

        int halfSpeed = CHOPSTICKS_SPEED / 2;
        if (Calendar.getInstance().get(Calendar.MONTH) == 3 && Calendar.getInstance().get(Calendar.DAY_OF_MONTH) == 1) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(1),
            }, halfSpeed));
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(3),
            }, halfSpeed));
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(6),
            }, halfSpeed));
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(3),
            }, halfSpeed));
            for (int i = 0; i < 3; i++) {
                stages.add(new PianoRoll.Stage(new float[] {
                        PhonosUtil.pitchFromNote(6), PhonosUtil.pitchFromNote(10)
                }, CHOPSTICKS_SPEED));
            }
            for (int i = 0; i < 3; i++) {
                stages.add(new PianoRoll.Stage(new float[] {
                        PhonosUtil.pitchFromNote(5), PhonosUtil.pitchFromNote(13)
                }, CHOPSTICKS_SPEED));
            }
            for (int i = 0; i < 3; i++) {
                stages.add(new PianoRoll.Stage(new float[] {
                        PhonosUtil.pitchFromNote(8), PhonosUtil.pitchFromNote(13)
                }, CHOPSTICKS_SPEED));
            }
            for (int i = 0; i < 3; i++) {
                stages.add(new PianoRoll.Stage(new float[] {
                        PhonosUtil.pitchFromNote(6), PhonosUtil.pitchFromNote(13)
                }, CHOPSTICKS_SPEED));
            }
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(6), PhonosUtil.pitchFromNote(10), PhonosUtil.pitchFromNote(15)
            }, CHOPSTICKS_SPEED));
        }

        return new PianoRoll(stages);
    }

    private static void chopsticksCommonSection(List<PianoRoll.Stage> stages) {
        for (int i = 0; i < 6; i++) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(11), // F4
                    PhonosUtil.pitchFromNote(13)  // G4
            }, CHOPSTICKS_SPEED));
        }
        for (int i = 0; i < 6; i++) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(10), // E4
                    PhonosUtil.pitchFromNote(13)  // G4
            }, CHOPSTICKS_SPEED));
        }
        for (int i = 0; i < 6; i++) {
            stages.add(new PianoRoll.Stage(new float[] {
                    PhonosUtil.pitchFromNote(8),  // D4
                    PhonosUtil.pitchFromNote(17)  // B5
            }, CHOPSTICKS_SPEED));
        }
    }
}
