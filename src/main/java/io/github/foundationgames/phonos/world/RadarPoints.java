package io.github.foundationgames.phonos.world;

import io.github.foundationgames.phonos.radio.RadioStorage;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

public class RadarPoints extends PersistentState {
    private Int2ObjectMap<LongSet> channelToSources = new Int2ObjectOpenHashMap<>();

    public void add(int channel, BlockPos pos) {
        var set = channelToSources.computeIfAbsent(channel, LongOpenHashSet::new);
        set.add(pos.asLong());
        markDirty();
    }

    public void remove(int channel, BlockPos pos) {
        if (channelToSources.containsKey(channel)) {
            channelToSources.get(channel).remove(pos.asLong());
            markDirty();
        }
    }

    public LongSet getPoints(int channel) {
        return channelToSources.get(channel);
    }

    public static RadarPoints get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(RadarPoints::readNbt, RadarPoints::new, "phonos_radar_points");
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {
        for (var entry : channelToSources.int2ObjectEntrySet()) if (entry.getValue().size() > 0) {
            var packedPosSet = new int[entry.getValue().size() * 2];

            int idx = 0;
            for (long pos : entry.getValue()) {
                packedPosSet[idx] = (int) (pos >> 32);
                packedPosSet[idx + 1] = (int) pos;

                idx += 2;
            }

            nbt.putIntArray("ch" + entry.getIntKey(), packedPosSet);
        }

        return nbt;
    }

    public static RadarPoints readNbt(NbtCompound nbt) {
        var state = new RadarPoints();

        for (int ch = 0; ch < RadioStorage.CHANNEL_COUNT; ch++) {
            var key = "ch" + ch;

            if (nbt.contains(key)) {
                var packedPosSet = nbt.getIntArray(key);
                var posSet = new LongOpenHashSet();

                for (int i = 0; i < packedPosSet.length; i += 2) {
                    long upper = packedPosSet[i];
                    long lower = packedPosSet[i + 1];

                    posSet.add(lower | (upper << 32));
                }

                state.channelToSources.put(ch, posSet);
            }
        }

        return state;
    }
}
