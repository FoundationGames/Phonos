package io.github.foundationgames.phonos.world;

import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.network.RecieverStorageOperation;
import io.github.foundationgames.phonos.util.PhonosUtil;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.LinkedHashSet;
import java.util.Set;

public class RadioChannelState extends PersistentState {

    private final Int2ObjectMap<LinkedHashSet<Long>> blockStorage = new Int2ObjectOpenHashMap<>();
    private final Int2ObjectMap<LinkedHashSet<Entity>> entityStorage = new Int2ObjectOpenHashMap<>();
    private final ServerWorld world;

    public RadioChannelState(ServerWorld world) {
        super();
        this.world = world;
    }

    public void readNbt(NbtCompound tag) {
        blockStorage.clear();
        entityStorage.clear();
        Set<String> channels = tag.getKeys();
        for(String c : channels) {
            int i = Integer.parseInt(c);
            long[] la = tag.getLongArray(c);
            LinkedHashSet<Long> lt = new LinkedHashSet<>();
            for(long l : la) {
                lt.add(l);
            }
            blockStorage.put(i, lt);
        }
        //Phonos.LOG.info("FROM TAG: "+channelStorage);
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        //Phonos.LOG.info("TO TAG: "+channelStorage);
        for(int k : blockStorage.keySet()) {
            long[] la = blockStorage.get(k).stream().mapToLong(l -> l).toArray();
            tag.putLongArray(Integer.toString(k), la);
        }
        return tag;
    }

    public void addReciever(int channel, BlockPos pos) {
        blockStorage.putIfAbsent(channel, new LinkedHashSet<>());
        LinkedHashSet<Long> l = blockStorage.get(channel);
        if(l.add(pos.asLong())) {
            for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.ADD, channel, new long[] { pos.asLong() }, new int[] {});
        }
        this.setDirty(true);
    }

    public void removeReciever(int channel, BlockPos pos) {
        blockStorage.putIfAbsent(channel, new LinkedHashSet<>());
        blockStorage.get(channel).remove(pos.asLong());
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.REMOVE, channel, new long[] { pos.asLong() }, new int[] {});
        this.setDirty(true);
    }
    
    public void addEntityReciever(int channel, Entity ent) {
        entityStorage.putIfAbsent(channel, new LinkedHashSet<>());
        LinkedHashSet<Entity> l = entityStorage.get(channel);
        if(l.add(ent)) {
            for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.ADD, channel, new long[] {}, new int[] { ent.getId() });
        }
        this.setDirty(true);
    }

    public void removeEntityReciever(int channel, Entity ent) {
        entityStorage.putIfAbsent(channel, new LinkedHashSet<>());
        entityStorage.get(channel).remove(ent);
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.REMOVE, channel, new long[] { }, new int[] {ent.getId()});
        this.setDirty(true);
    }
    
    public boolean hasReciever(int channel, BlockPos pos) {
        if(blockStorage.containsKey(channel)) return blockStorage.get(channel).contains(pos.asLong());
        return false;
    }
    
    public boolean hasEntityReciever(int channel, Entity ent) {
        if(entityStorage.containsKey(channel)) return entityStorage.get(channel).contains(ent);
        return false;
    }

    public static void sendPlayerJoinPackets(ServerPlayerEntity player) {
        RadioChannelState state = PhonosUtil.getRadioState(player.getServerWorld());
        for(int channel : state.blockStorage.keySet()) {
            PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.CLEAR, channel, new long[]{}, new int[] {});
        }
        for(int channel : state.entityStorage.keySet()) {
            PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.CLEAR, channel, new long[]{}, new int[] {});
        }
        for(int channel : state.blockStorage.keySet()) {
            Long[] posList = state.blockStorage.get(channel).toArray(new Long[] {});
            for (int i = 0; i < Math.ceil((float)posList.length / 16); i++) {
                int repeats = Math.min(posList.length - (i * 16), 16);
                long[] positions = new long[repeats];
                for (int j = 0; j < repeats; j++) {
                    int index = (i*16)+j;
                    positions[j] = posList[index];
                }
                PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.ADD, channel, positions, new int[] {});
            }
        }
        for(int channel : state.entityStorage.keySet()) {
            Entity[] entityList = state.entityStorage.get(channel).toArray(new Entity[] {});
            for (int i = 0; i < Math.ceil((float)entityList.length / 16); i++) {
                int repeats = Math.min(entityList.length - (i * 16), 16);
                int[] entities = new int[repeats];
                for (int j = 0; j < repeats; j++) {
                    int index = (i*16)+j;
                    entities[j] = entityList[index].getId();
                }
                PayloadPackets.sendRecieversUpdate(player, RecieverStorageOperation.ADD, channel, new long[] {}, entities);
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
