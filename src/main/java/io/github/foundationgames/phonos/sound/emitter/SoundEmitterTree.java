package io.github.foundationgames.phonos.sound.emitter;

import io.github.foundationgames.phonos.radio.RadioStorage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
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

    // Updates the tree on the server and provides a list of changes to be sent to the client
    // More accurate than the updateClient() method, as far as what the server is aware of (loaded chunks)
    public Delta updateServer(World world) {
        var emitters = SoundEmitterStorage.getInstance(world);
        var delta = new Delta(this.rootId, new Int2ObjectOpenHashMap<>());

        int index = 0;

        while (index < this.levels.size() && !this.levels.get(index).empty()) {
            if (index + 1 == this.levels.size()) {
                this.levels.add(new Level(new LongArrayList(), new LongArrayList()));
            }

            var level = this.levels.get(index);
            var nextLevel = this.levels.get(index + 1);

            var nextLevelChanges = new ChangeList(new LongArrayList(), new LongArrayList(nextLevel.active()));

            for (long emId : level.active()) {
                if (emitters.isLoaded(emId)) {
                    var emitter = emitters.getEmitter(emId);

                    final int searchUntil = index;
                    emitter.forEachChild(child -> {
                        if (this.contains(child, searchUntil)) {
                            return;
                        }

                        nextLevelChanges.remove().rem(child);

                        if (!nextLevel.active().contains(child)) {
                            nextLevelChanges.add().add(child);
                        }
                    });
                }
            }

            if (!nextLevelChanges.add().isEmpty() || !nextLevelChanges.remove().isEmpty()) {
                nextLevelChanges.apply(nextLevel);
                delta.deltas().put(index + 1, new Level(
                        new LongArrayList(nextLevel.active()),
                        new LongArrayList(nextLevel.inactive())
                ));
            }

            index++;
        }

        return delta;
    }

    // Updates the tree on the client side
    // Not entirely accurate, but enough so that most modifications of sound networks will have
    // immediate effects regardless of server speed/latency
    public void updateClient(World world) {
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
            for (long l : nextLevel.inactive()) if (RadioStorage.RADIO_EMITTERS.contains(l)) {
                nextLevel.active().add(l);
            }
            nextLevel.inactive().removeAll(nextLevel.active());

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

        if (index < this.levels.size() && this.levels.get(index).empty()) {
            while (index < this.levels.size()) {
                this.levels.remove(this.levels.size() - 1);
            }
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

    public record Level(LongArrayList active, LongArrayList inactive) {
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

    public record Delta(long rootId, Int2ObjectMap<Level> deltas) {
        public static void toPacket(PacketByteBuf buf, Delta delta) {
            buf.writeLong(delta.rootId);
            buf.writeMap(delta.deltas, PacketByteBuf::writeInt, Level::toPacket);
        }

        public static Delta fromPacket(PacketByteBuf buf) {
            var id = buf.readLong();
            var deltas = buf.readMap(Int2ObjectOpenHashMap::new, PacketByteBuf::readInt, Level::fromPacket);

            return new Delta(id, deltas);
        }

        public boolean hasChanges() {
            return this.deltas.size() > 0;
        }

        public void apply(SoundEmitterTree tree) {
            for (var entry : this.deltas().int2ObjectEntrySet()) {
                int idx = entry.getIntKey();
                if (idx < tree.levels.size()) {
                    tree.levels.set(idx, entry.getValue());
                } else {
                    tree.levels.add(idx, entry.getValue());
                }
            }
        }
    }

    public record ChangeList(LongList add, LongList remove) {
        public static void toPacket(PacketByteBuf buf, ChangeList level) {
            buf.writeCollection(level.add, PacketByteBuf::writeLong);
            buf.writeCollection(level.remove, PacketByteBuf::writeLong);
        }

        public static ChangeList fromPacket(PacketByteBuf buf) {
            var add = buf.readCollection(LongArrayList::new, PacketByteBuf::readLong);
            var rem = buf.readCollection(LongArrayList::new, PacketByteBuf::readLong);

            return new ChangeList(add, rem);
        }

        public void apply(Level level) {
            level.active().removeAll(this.remove);
            level.inactive().removeAll(this.remove);

            for (long l : this.add) {
                if (!level.active().contains(l)) {
                    level.active().add(l);
                }
                level.inactive().rem(l);
            }
        }
    }
}
