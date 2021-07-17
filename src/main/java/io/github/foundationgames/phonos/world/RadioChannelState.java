package io.github.foundationgames.phonos.world;

import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.network.RecieverStorageOperation;
import io.github.foundationgames.phonos.util.PhonosUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.ArrayList;
import java.util.Set;

public class RadioChannelState extends PersistentState {

    private final Int2ObjectMap<ArrayList<Long>> channelStorage = new Int2ObjectOpenHashMap<>();
    private final ServerWorld world;

    public RadioChannelState(ServerWorld world) {
        super();
        this.world = world;
    }

    public void readNbt(NbtCompound tag) {
        channelStorage.clear();
        Set<String> channels = tag.getKeys();
        for(String c : channels) {
            int i = Integer.parseInt(c);
            long[] la = tag.getLongArray(c);
            ArrayList<Long> lt = new ArrayList<>();
            for(long l : la) lt.add(l);
            channelStorage.put(i, lt);
        }
        //Phonos.LOG.info("FROM TAG: "+channelStorage);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        //Phonos.LOG.info("TO TAG: "+channelStorage);
        for(int k : channelStorage.keySet()) {
            long[] la = new long[channelStorage.get(k).size()];
            for (int i = 0; i < channelStorage.get(k).size(); i++) {
                la[i] = channelStorage.get(k).get(i);
            }
            tag.putLongArray(Integer.toString(k), la);
        }
        return tag;
    }

    public void addReciever(int channel, BlockPos pos) {
        channelStorage.putIfAbsent(channel, new ArrayList<>());
        ArrayList<Long> l = channelStorage.get(channel);
        if(!l.contains(pos.asLong())) {
            l.add(pos.asLong());
            for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.ADD, channel, new long[] { pos.asLong() });
        }
        this.setDirty(true);
    }

    public void removeReciever(int channel, BlockPos pos) {
        channelStorage.putIfAbsent(channel, new ArrayList<>());
        channelStorage.get(channel).remove(pos.asLong());
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.REMOVE, channel, new long[] { pos.asLong() });
        this.setDirty(true);
    }

    public boolean hasReciever(int channel, BlockPos pos) {
        if(channelStorage.containsKey(channel)) return channelStorage.get(channel).contains(pos.asLong());
        return false;
    }

    public static void sendPlayerJoinPackets(ServerPlayerEntity player) {
        RadioChannelState state = PhonosUtil.getRadioState(player.getServerWorld());
        for(int channel : state.channelStorage.keySet()) {
            PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.CLEAR, channel, new long[]{});
            ArrayList<Long> posList = state.channelStorage.get(channel);
            for (int i = 0; i < Math.ceil((float)posList.size() / 16); i++) {
                int repeats = Math.min(posList.size() - (i * 16), 16);
                long[] positions = new long[repeats];
                for (int j = 0; j < repeats; j++) {
                    int index = (i*16)+j;
                    positions[j] = posList.get(index);
                }
                PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.ADD, channel, positions);
            }
        }
    }

    public void playSound(BlockPos origin, SoundEvent sound, int channel, float volume, float pitch, boolean stoppable) {
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRadioChannelSound(player, origin, sound, channel, volume, pitch, stoppable);
    }

    public void playSound(BlockPos origin, Identifier sound, int channel, float volume, float pitch, boolean stoppable) {
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRadioChannelSound(player, origin, sound, channel, volume, pitch, stoppable);
    }

    public void tryStopSound(BlockPos origin, int channel) {
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendStopSound(player, origin, channel);
    }
}
