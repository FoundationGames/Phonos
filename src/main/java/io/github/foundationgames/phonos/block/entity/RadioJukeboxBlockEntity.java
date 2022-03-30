package io.github.foundationgames.phonos.block.entity;

import com.mojang.datafixers.util.Pair;
import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.block.RadioJukeboxBlock;
import io.github.foundationgames.phonos.item.CustomMusicDiscItem;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.screen.RadioJukeboxGuiDescription;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.world.RadioChannelState;
import io.netty.buffer.Unpooled;
import it.unimi.dsi.fastutil.ints.Int2ByteMap;
import it.unimi.dsi.fastutil.ints.Int2ByteOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RadioJukeboxBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, SidedInventory, Syncing {
    private final DefaultedList<ItemStack> items = DefaultedList.ofSize(6, ItemStack.EMPTY);
    public float pitch = 1.0f;
    public boolean doShuffle = false;

    private boolean isPlaying = false;
    private int songProgress = 0;
    private int playingSong = 0;

    public int disc1Duration = 1;
    public int disc2Duration = 1;
    public int disc3Duration = 1;
    public int disc4Duration = 1;
    public int disc5Duration = 1;
    public int disc6Duration = 1;

    public RadioJukeboxBlockEntity(BlockPos pos, BlockState state) {
        super(PhonosBlocks.RADIO_JUKEBOX_ENTITY, pos, state);
    }

    public void onDiscRemoved(int slot) {
        if(isPlaying) playOrStop();
    }

    public void playOrStop() {
        if(!isPlaying) {
            if(doShuffle) shuffle();
            pushDiscs();
            playSong(0);
            this.isPlaying = true;
            if(world != null && !world.isClient()) {
                sync();
            }
        } else {
            stop();
        }
        world.updateComparators(pos, world.getBlockState(pos).getBlock());
    }

    public void stop() {
        if(isPlaying) {
            if(!world.isClient()) {
                RadioChannelState pstate = PhonosUtil.getRadioState((ServerWorld)world);
                pstate.tryStopSound(pos, getChannel());
            }
            this.playingSong = 0;
            this.songProgress = 0;
            this.isPlaying = false;
            if(world != null && !world.isClient()) {
                sync();
            }
            world.updateComparators(pos, world.getBlockState(pos).getBlock());
        }
    }

    private void playSong(int slot) {
        slot = Math.max(0, Math.min(slot, 5));
        if(items.get(slot).isEmpty()) {
            this.songProgress = 0;
            this.isPlaying = false;
            return;
        }
        this.playingSong = slot;
        int d = getDuration(slot);
        this.songProgress = d * 20;
        isPlaying = true;
        if(!world.isClient()) {
            RadioChannelState pstate = PhonosUtil.getRadioState((ServerWorld)world);
            ItemStack disc = items.get(slot);
            if(disc.getItem() instanceof MusicDiscItem) {
                pstate.playSound(pos, ((MusicDiscItem)disc.getItem()).getSound(), getChannel(), 1.8f, pitch, true);
            }
            if(disc.getItem() instanceof CustomMusicDiscItem) {
                Identifier id = Identifier.tryParse(disc.getOrCreateSubNbt("MusicData").getString("SoundId"));
                if(id == null) id = new Identifier("empty");
                pstate.playSound(pos, id, getChannel(), 1.8f, pitch, true);
            }
            sync();
        }
        world.updateComparators(pos, world.getBlockState(pos).getBlock());
    }

    public int getComparatorOutput() {
        if(!isPlaying) return 0;
        Item item = items.get(playingSong).getItem();
        if(item instanceof MusicDiscItem) return ((MusicDiscItem)item).getComparatorOutput();
        if(item instanceof CustomMusicDiscItem) {
            ItemStack stack = items.get(playingSong);
            return stack.getOrCreateSubNbt("MusicData").getInt("ComparatorSignal");
        }
        return 0;
    }

    public static void tick(World world, BlockPos pos, BlockState state, RadioJukeboxBlockEntity self) {
        if(self.isPlaying) {
            if(self.songProgress <= 0) {
                int nextSong = self.playingSong + 1;
                if(nextSong <= 5 && !self.items.get(nextSong).isEmpty()) self.playSong(nextSong);
                else self.playOrStop();
            }
            self.songProgress--;
        }
        boolean s = world.getBlockState(pos).getBlock() instanceof RadioJukeboxBlock && world.getBlockState(pos).get(RadioJukeboxBlock.PLAYING);
        if(s != self.isPlaying && world.getBlockState(pos).getBlock() instanceof RadioJukeboxBlock) {
            world.setBlockState(pos, world.getBlockState(pos).with(RadioJukeboxBlock.PLAYING, !s));
        }
    }

    public void nextSong() {
        if(isPlaying && playingSong < 5) {
            int nextSong = playingSong + 1;
            if(!items.get(nextSong).isEmpty()) playSong(nextSong);
            else playOrStop();
            if(world != null && !world.isClient()) {
                sync();
            }
        }
    }

    public void prevSong() {
        if(isPlaying && playingSong > 0) {
            int nextSong = playingSong - 1;
            if(!items.get(nextSong).isEmpty()) playSong(nextSong);
            else playOrStop();
            if(world != null && !world.isClient()) {
                sync();
            }
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.pitch = nbt.getFloat("Pitch");
        this.doShuffle = nbt.getBoolean("DoShuffle");
        items.clear();
        NbtCompound durations = nbt.getCompound("Durations");
        disc1Duration = durations.getInt("Track1");
        disc2Duration = durations.getInt("Track2");
        disc3Duration = durations.getInt("Track3");
        disc4Duration = durations.getInt("Track4");
        disc5Duration = durations.getInt("Track5");
        disc6Duration = durations.getInt("Track6");
        Inventories.readNbt(nbt, items);
        NbtCompound playingMusic = nbt.getCompound("PlayingMusic");
        isPlaying = playingMusic.getBoolean("Playing");
        playingSong = playingMusic.getInt("Track");
        songProgress = playingMusic.getInt("Progress");
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        nbt.putFloat("Pitch", pitch);
        nbt.putBoolean("DoShuffle", doShuffle);
        Inventories.writeNbt(nbt, items);
        NbtCompound durations = new NbtCompound();
        durations.putInt("Track1", disc1Duration);
        durations.putInt("Track2", disc2Duration);
        durations.putInt("Track3", disc3Duration);
        durations.putInt("Track4", disc4Duration);
        durations.putInt("Track5", disc5Duration);
        durations.putInt("Track6", disc6Duration);
        nbt.put("Durations", durations);
        NbtCompound playingMusic = new NbtCompound();
        playingMusic.putBoolean("Playing", isPlaying);
        playingMusic.putInt("Track", playingSong);
        playingMusic.putInt("Progress", songProgress);
        nbt.put("PlayingMusic", playingMusic);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public float getProgress() {
        int d = 1;
        d = (playingSong == 0 ? disc1Duration : d);
        d = (playingSong == 1 ? disc2Duration : d);
        d = (playingSong == 2 ? disc3Duration : d);
        d = (playingSong == 3 ? disc4Duration : d);
        d = (playingSong == 4 ? disc5Duration : d);
        d = (playingSong == 5 ? disc6Duration : d);
        return ((float)songProgress / 20) / d;
    }

    public int getPlayingSong() {
        return playingSong;
    }

    private void shuffle() {
        List<Pair<ItemStack, Integer>> discs = new ArrayList<>();
        for(int i = 0; i < 6; i++) {
            int d = getDuration(i);
            discs.add(Pair.of(items.get(i), d));
        }
        Collections.shuffle(discs);
        items.clear();
        for(int i = 0; i < discs.size(); i++) {
            items.set(i, discs.get(i).getFirst());
            setDuration(i, discs.get(i).getSecond());
        }
        if(!world.isClient()) sync();
    }

    private void pushDiscs() {
        List<Pair<ItemStack, Integer>> discs = new ArrayList<>();
        for(int i = 0; i < 6; i++) {
            int d = getDuration(i);
            discs.add(Pair.of(items.get(i), d));
        }
        List<Pair<ItemStack, Integer>> fdiscs = new ArrayList<>();
        for(Pair<ItemStack, Integer> p : discs) {
            if(!p.getFirst().isEmpty()) {
                fdiscs.add(p);
            }
        }
        for(Pair<ItemStack, Integer> p : discs) {
            if(p.getFirst().isEmpty()) {
                fdiscs.add(p);
            }
        }
        items.clear();
        for(int i = 0; i < fdiscs.size(); i++) {
            items.set(i, fdiscs.get(i).getFirst());
            setDuration(i, fdiscs.get(i).getSecond());
        }
        if(!world.isClient()) sync();
    }

    public int getDuration(int slot) {
        int d = 1;
        d = (slot == 0 ? disc1Duration : d);
        d = (slot == 1 ? disc2Duration : d);
        d = (slot == 2 ? disc3Duration : d);
        d = (slot == 3 ? disc4Duration : d);
        d = (slot == 4 ? disc5Duration : d);
        d = (slot == 5 ? disc6Duration : d);
        return d;
    }

    public void setDuration(int slot, int duration) {
        if(slot == 0) disc1Duration = duration;
        else if(slot == 1) disc2Duration = duration;
        else if(slot == 2) disc3Duration = duration;
        else if(slot == 3) disc4Duration = duration;
        else if(slot == 4) disc5Duration = duration;
        else if(slot == 5) disc6Duration = duration;
    }

    @Environment(EnvType.CLIENT)
    public void performSyncedOperation(byte operation, int data) {
        doOperation(this, operation, data);
        PacketByteBuf buf = new PacketByteBuf(Unpooled.buffer());
        buf.writeBlockPos(pos);
        buf.writeByte(operation);
        buf.writeInt(data);
        ClientPlayNetworking.send(Phonos.id("update_radio_jukebox"), buf);
    }

    public static void registerServerPackets() {
        ServerPlayNetworking.registerGlobalReceiver(Phonos.id("update_radio_jukebox"), (server, player, handler, buf, sender) -> {
            BlockPos pos = buf.readBlockPos();
            byte operation = buf.readByte();
            int data = buf.readInt();
            server.execute(() -> {
                BlockEntity b = player.world.getBlockEntity(pos);
                if(b instanceof RadioJukeboxBlockEntity && pos.isWithinDistance(player.getPos(), 90)) {
                    RadioJukeboxBlockEntity be = (RadioJukeboxBlockEntity)b;
                    doOperation(be, operation, data);
                }
            });
        });
    }

    public static void doOperation(RadioJukeboxBlockEntity be, byte op, int data) {
        if(op == Ops.SET_PITCH) {
            be.pitch = (float)Math.min(20, Math.max(data, 1)) / 10;
        } else if(op == Ops.SET_SHUFFLE) {
            be.doShuffle = data > 0;
        } else if(op == Ops.PLAY_STOP) {
            be.playOrStop();
        } else if(op == Ops.SET_DURATION_1) {
            be.disc1Duration = Math.max(1, Math.min(data, 599));
        } else if(op == Ops.SET_DURATION_2) {
            be.disc2Duration = Math.max(1, Math.min(data, 599));
        } else if(op == Ops.SET_DURATION_3) {
            be.disc3Duration = Math.max(1, Math.min(data, 599));
        } else if(op == Ops.SET_DURATION_4) {
            be.disc4Duration = Math.max(1, Math.min(data, 599));
        } else if(op == Ops.SET_DURATION_5) {
            be.disc5Duration = Math.max(1, Math.min(data, 599));
        } else if(op == Ops.SET_DURATION_6) {
            be.disc6Duration = Math.max(1, Math.min(data, 599));
        } else if(op == Ops.NEXT_SONG) {
            be.nextSong();
        } else if(op == Ops.PREV_SONG) {
            be.prevSong();
        }
        be.markDirty();
    }

    public static final Int2ByteMap SLOT_2_OP = PhonosUtil.create(() -> {
        Int2ByteMap m = new Int2ByteOpenHashMap();
        m.put(0, Ops.SET_DURATION_1);
        m.put(1, Ops.SET_DURATION_2);
        m.put(2, Ops.SET_DURATION_3);
        m.put(3, Ops.SET_DURATION_4);
        m.put(4, Ops.SET_DURATION_5);
        m.put(5, Ops.SET_DURATION_6);
        return m;
    });

    private int getChannel() {
        if(world.getBlockState(pos).isOf(PhonosBlocks.RADIO_JUKEBOX)) {
            return world.getBlockState(pos).get(RadioJukeboxBlock.CHANNEL);
        }
        return 0;
    }

    public static class Ops {
        public static final byte SET_PITCH = 0x00;
        public static final byte SET_SHUFFLE = 0x01;
        public static final byte SET_DURATION_1 = 0x02;
        public static final byte SET_DURATION_2 = 0x03;
        public static final byte SET_DURATION_3 = 0x04;
        public static final byte SET_DURATION_4 = 0x05;
        public static final byte SET_DURATION_5 = 0x06;
        public static final byte SET_DURATION_6 = 0x07;
        public static final byte PLAY_STOP = 0x08;
        public static final byte NEXT_SONG = 0x09;
        public static final byte PREV_SONG = 0x0A;
    }

    // --------------------------------------------------------------------

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public int[] getAvailableSlots(Direction side) {
        return new int[] {0, 1, 2, 3, 4, 5};
    }

    @Override
    public boolean canInsert(int slot, ItemStack stack, Direction dir) {
        return stack.getItem() instanceof MusicDiscItem || stack.getItem() instanceof CustomMusicDiscItem;
    }

    @Override
    public boolean canExtract(int slot, ItemStack stack, Direction dir) {
        return true;
    }

    @Override
    public int size() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack s : this.items) {
            if(!s.isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getStack(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        this.onDiscRemoved(slot);
        return items.get(slot).split(amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return items.remove(slot);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        items.set(slot, stack);
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        items.clear();
    }

    @Override
    public Text getDisplayName() {
        return new LiteralText("");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
        sync();
        return new RadioJukeboxGuiDescription(syncId, inv, ScreenHandlerContext.create(world, pos), this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
        buf.writeBlockPos(pos);
    }
}
