package io.github.foundationgames.phonos.util.piano;

public class PianoRoll {
    /*
    private final List<Stage> stages;

    public PianoRoll(List<Stage> stages) {
        this.stages = stages;
    }

    public Player createPlayable(Consumer<Float> player) {
        return new Player(new ArrayDeque<>(stages), player);
    }

    public int duration() {
        int dur = 0;
        for (var stage : this.stages) {
            dur += 1 + stage.delay;
        }

        return dur;
    }

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();
        var stageList = new NbtList();

        this.stages.forEach(s -> stageList.add(s.toNbt()));
        nbt.put("stages", stageList);
        nbt.putInt("duration", this.duration());

        return nbt;
    }

    public static @Nullable PianoRoll fromNbt(@Nullable NbtCompound nbt) {
        if (nbt != null) {
            if (!nbt.contains("stages")) return null;

            var stageList = nbt.getList("stages", 10);
            var stages = new ArrayList<Stage>();

            for (var el : stageList) {
                if (el instanceof NbtCompound com) {
                    stages.add(Stage.fromNbt(com));
                }
            }

            return new PianoRoll(stages);
        }

        return null;
    }

    public static class Player {
        private final Deque<Stage> stages;
        private final Consumer<Float> player;
        private int delay = 0;

        public Player(Deque<Stage> stages, Consumer<Float> player) {
            this.stages = stages;
            this.player = player;
        }

        public void tick() {
            if (!this.done()) {
                if (this.delay <= 0) {
                    var stage = this.stages.removeFirst();

                    this.delay = stage.delay;
                    for (float pitch : stage.pitches) {
                        this.player.accept(pitch);
                    }
                } else {
                    this.delay--;
                }
            }
        }

        public boolean done() {
            return this.stages.size() <= 0;
        }
    }

    public static class Recorder {
        private final Set<Float> currentPitches = new HashSet<>();
        private int currentDelay = 0;

        private final List<Stage> stages = new ArrayList<>();

        public void pushStage() {
            this.stages.add(new Stage(PhonosUtil.arrFromList(currentPitches), Math.max(currentDelay - 1, 0)));

            this.currentPitches.clear();
            this.currentDelay = 0;
        }

        public void tick() {
            if (this.currentPitches.size() > 0) {
                this.currentDelay++;
            }
        }

        public void applyNote(float pitch) {
            if (currentDelay > 0) {
                pushStage();
            }

            this.currentPitches.add(pitch);
        }

        public int size() {
            return this.stages.size();
        }

        public PianoRoll build() {
            if (currentPitches.size() > 0) {
                pushStage();
            }

            return new PianoRoll(stages);
        }
    }

    public record Stage(float[] pitches, int delay) {
        public NbtCompound toNbt() {
            var nbt = new NbtCompound();
            nbt.putIntArray("pitches", PhonosUtil.toIntBytesArr(this.pitches));
            nbt.putInt("delay", this.delay);
            return nbt;
        }

        public static Stage fromNbt(NbtCompound nbt) {
            return new Stage(PhonosUtil.fromIntBytesArr(nbt.getIntArray("pitches")), nbt.getInt("delay"));
        }
    }

     */
}
