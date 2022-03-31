package io.github.foundationgames.phonos.world;

import io.github.foundationgames.phonos.block.NotePlayReceivable;
import io.github.foundationgames.phonos.block.SoundPlayReceivable;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.network.ReceiverStorageOperation;
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

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RadioChannelState extends PersistentState {
    public static final String ID = "radio_channel_state";

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
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag) {
        for(int k : blockStorage.keySet()) {
            long[] la = blockStorage.get(k).stream().mapToLong(l -> l).toArray();
            tag.putLongArray(Integer.toString(k), la);
        }

        return tag;
    }

    public void addReceiver(int channel, BlockPos pos) {
        blockStorage.putIfAbsent(channel, new LinkedHashSet<>());
        LinkedHashSet<Long> l = blockStorage.get(channel);
        if(l.add(pos.asLong())) {
            for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendReceiversUpdate(player, ReceiverStorageOperation.ADD, channel, new long[] { pos.asLong() }, new int[] {});
        }
        this.setDirty(true);
    }

    public void removeReceiver(int channel, BlockPos pos) {
        blockStorage.putIfAbsent(channel, new LinkedHashSet<>());
        blockStorage.get(channel).remove(pos.asLong());
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendReceiversUpdate(player, ReceiverStorageOperation.REMOVE, channel, new long[] { pos.asLong() }, new int[] {});
        this.setDirty(true);
    }
    
    public void addEntityReceiver(int channel, Entity ent) {
        entityStorage.putIfAbsent(channel, new LinkedHashSet<>());
        LinkedHashSet<Entity> l = entityStorage.get(channel);
        if(l.add(ent)) {
            for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendReceiversUpdate(player, ReceiverStorageOperation.ADD, channel, new long[] {}, new int[] {ent.getId()});
        }
        this.setDirty(true);
    }

    public void removeEntityReceiver(int channel, Entity ent) {
        entityStorage.putIfAbsent(channel, new LinkedHashSet<>());
        entityStorage.get(channel).remove(ent);
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendReceiversUpdate(player, ReceiverStorageOperation.REMOVE, channel, new long[] {}, new int[] {ent.getId()});
        this.setDirty(true);
    }

    // Does not sync to client, as client will also garbage collect on its own
    public void garbageCollectEntities() {
        Set<Entity> removed = new HashSet<>();
        for (int channel : entityStorage.keySet()) {
            removed.clear();
            for (var entity : entityStorage.get(channel)) {
                if (entity.isRemoved()) removed.add(entity);
            }
            for (var entity : removed) {
                entityStorage.get(channel).remove(entity);
            }
        }
    }

    public void tick() {
        garbageCollectEntities();
    }
    
    public boolean hasReceiver(int channel, BlockPos pos) {
        if (blockStorage.containsKey(channel)) return blockStorage.get(channel).contains(pos.asLong());
        return false;
    }
    
    public boolean hasEntityReceiver(int channel, Entity ent) {
        if (entityStorage.containsKey(channel)) return entityStorage.get(channel).contains(ent);
        return false;
    }

    public static void sendPlayerJoinPackets(ServerPlayerEntity player) {
        RadioChannelState state = PhonosUtil.getRadioState(player.getWorld());
        for (int channel : state.blockStorage.keySet()) {
            PayloadPackets.sendReceiversUpdate(player, ReceiverStorageOperation.CLEAR, channel, new long[]{}, new int[] {});
        }
        for (int channel : state.entityStorage.keySet()) {
            PayloadPackets.sendReceiversUpdate(player, ReceiverStorageOperation.CLEAR, channel, new long[]{}, new int[] {});
        }
        for (int channel : state.blockStorage.keySet()) {
            Long[] posList = state.blockStorage.get(channel).toArray(new Long[] {});
            for (int i = 0; i < Math.ceil((float)posList.length / 16); i++) {
                int repeats = Math.min(posList.length - (i * 16), 16);
                long[] positions = new long[repeats];
                for (int j = 0; j < repeats; j++) {
                    int index = (i*16)+j;
                    positions[j] = posList[index];
                }
                PayloadPackets.sendReceiversUpdate(player, ReceiverStorageOperation.ADD, channel, positions, new int[] {});
            }
        }
        for (int channel : state.entityStorage.keySet()) {
            Entity[] entityList = state.entityStorage.get(channel).toArray(new Entity[] {});
            for (int i = 0; i < Math.ceil((float)entityList.length / 16); i++) {
                int repeats = Math.min(entityList.length - (i * 16), 16);
                int[] entities = new int[repeats];
                for (int j = 0; j < repeats; j++) {
                    int index = (i*16)+j;
                    entities[j] = entityList[index].getId();
                }
                PayloadPackets.sendReceiversUpdate(player, ReceiverStorageOperation.ADD, channel, new long[] {}, entities);
            }
        }
    }

    public void playSound(BlockPos origin, SoundEvent sound, int channel, float volume, float pitch, boolean stoppable) {
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRadioChannelSound(player, origin, sound, channel, volume, pitch, stoppable);
    }

    public void playSound(BlockPos origin, Identifier sound, int channel, float volume, float pitch, boolean stoppable) {
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendRadioChannelSound(player, origin, sound, channel, volume, pitch, stoppable);
    }

    public void alertNotePlayed(int channel, float pitch) {
        var pos = new BlockPos.Mutable();

        for (long l : this.blockStorage.get(channel)) {
            pos.set(l);

            if (this.world.isChunkLoaded(pos) && this.world.getBlockState(pos).getBlock() instanceof NotePlayReceivable receiver) {
                receiver.onNotePlayed(this.world, pos, pitch);
            }
        }
    }

    public void tryStopSound(BlockPos origin, int channel) {
        for(ServerPlayerEntity player : world.getPlayers()) PayloadPackets.sendStopSound(player, origin, channel);
    }
}
