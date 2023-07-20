package io.github.foundationgames.phonos.sound.emitter;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.function.Consumer;

public class SoundEmitterTree {
    public final long rootId;
    private final ArrayList<Level> levels;

    public SoundEmitterTree(long rootId) {
        this(rootId, new ArrayList<>());
        levels.add(new Level(new LongArrayList(new long[] {rootId}), new LongArrayList()));
    }

    private SoundEmitterTree(long rootId, ArrayList<Level> levels) {
        this.rootId = rootId;
        this.levels = levels;
    }

    public boolean contains(long value, int upUntil) {
        upUntil = Math.min(this.levels.size() - 1, upUntil);

        for (int i = 0; i < upUntil; i++) {
            var level = this.levels.get(i);
            if (level.active.contains(value) || level.inactive.contains(value)) {
                return true;
            }
        }

        return false;
    }

    public void update(World world) {
        var emitters = SoundEmitterStorage.getInstance(world);

        int index = 0;

        while (index < this.levels.size() && !this.levels.get(index).empty()) {
            if (index + 1 == this.levels.size()) {
                this.levels.add(new Level(new LongArrayList(), new LongArrayList()));
            }

            var level = this.levels.get(index);
            var nextLevel = this.levels.get(index + 1);

            nextLevel.inactive().addAll(nextLevel.active());
            nextLevel.active().clear();

            for (long emId : level.active()) {
                if (emitters.isLoaded(emId)) {
                    var emitter = emitters.getEmitter(emId);

                    final int searchUntil = index;
                    emitter.forEachChild(child -> {
                        if (this.contains(child, searchUntil)) {
                            return;
                        }

                        nextLevel.inactive().rem(child);

                        if (!nextLevel.active().contains(child)) {
                            nextLevel.active().add(child);
                        }
                    });
                }
            }

            index++;
        }
    }

    public void forEachSource(World world, Consumer<SoundSource> action) {
        var emitters = SoundEmitterStorage.getInstance(world);

        for (var level : this.levels)
            for (long em : level.active) {
            if (emitters.isLoaded(em)) {
                var emitter = emitters.getEmitter(em);
                emitter.forEachSource(action);
            }
        }
    }

    public record Level(LongArrayList active, LongArrayList inactive){
        public boolean empty() {
            return active.isEmpty() && inactive.isEmpty();
        }

        public static void toPacket(PacketByteBuf buf, Level level) {
            buf.writeCollection(level.active, PacketByteBuf::writeLong);
            buf.writeCollection(level.inactive, PacketByteBuf::writeLong);
        }

        public static Level fromPacket(PacketByteBuf buf) {
            var active = buf.readCollection(LongArrayList::new, PacketByteBuf::readLong);
            var inactive = buf.readCollection(LongArrayList::new, PacketByteBuf::readLong);

            return new Level(active, inactive);
        }
    }

    public void toPacket(PacketByteBuf buf) {
        buf.writeLong(this.rootId);
        buf.writeCollection(this.levels, Level::toPacket);
    }

    public static SoundEmitterTree fromPacket(PacketByteBuf buf) {
        return new SoundEmitterTree(buf.readLong(), buf.readCollection(ArrayList::new, Level::fromPacket));
    }
}
