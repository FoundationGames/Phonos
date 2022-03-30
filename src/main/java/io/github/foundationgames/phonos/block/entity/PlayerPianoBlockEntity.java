package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.block.PianoBlock;
import io.github.foundationgames.phonos.item.PianoRollItem;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.util.piano.PianoKeyboard;
import io.github.foundationgames.phonos.util.piano.PianoRoll;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class PlayerPianoBlockEntity extends BlockEntity implements Syncing {
    public final PianoKeyboard keyboard = new PianoKeyboard();

    private boolean rollTurning = false;
    private ItemStack rollStack = ItemStack.EMPTY;
    private PianoRoll currentRoll = null;
    private PianoRoll.Player playingRoll = null;

    public PlayerPianoBlockEntity(BlockPos pos, BlockState state) {
        super(PhonosBlocks.PLAYER_PIANO_ENTITY, pos, state);
    }

    public boolean rollTurning() {
        return rollTurning;
    }

    public boolean hasRoll() {
        return currentRoll != null;
    }

    public void setItem(ItemStack rollStack) {
        this.rollStack = rollStack;
        this.currentRoll = PianoRollItem.getRoll(rollStack);
        sync();
    }

    public ItemStack takeItem() {
        var stack = this.rollStack;
        this.rollStack = ItemStack.EMPTY;
        this.currentRoll = null;
        this.playingRoll = null;
        sync();
        return stack;
    }

    public static void tick(World world, BlockPos pos, BlockState state, PlayerPianoBlockEntity self) {
        if (!world.isClient()) {
            boolean rollTurning = self.playingRoll != null && !self.playingRoll.done();

            if (rollTurning != self.rollTurning) {
                self.rollTurning = rollTurning;
                self.sync();
            }
        }

        if (self.playingRoll != null) {
            self.playingRoll.tick();
        }

        self.keyboard.tick();
    }

    public void togglePianoRoll() {
        if (this.playingRoll == null || this.playingRoll.done()) {
            if (this.currentRoll != null) {
                this.playingRoll = this.currentRoll.createPlayable(this::playNote);
            } else {
                this.playingRoll = null;
            }
        } else {
            this.playingRoll = null;
        }

        sync();
    }

    public void playNote(float pitch) {
        var state = this.world.getBlockState(this.pos);

        if (state.getBlock() instanceof PianoBlock piano) {
            int key = MathHelper.clamp(PhonosUtil.noteFromPitch(pitch), 0, 24);

            this.world.playSound(null, this.pos, piano.getInstrument(pitch, state, this.world, this.pos).getSound(), SoundCategory.RECORDS, 3.0F, pitch);
            this.keyboard.press(key);

            if (!world.isClient()) {
                world.getPlayers().forEach(player -> {
                    if (player instanceof ServerPlayerEntity sPlayer && player.getBlockPos().isWithinDistance(this.getPos(), 100)) {
                        PayloadPackets.sendPianoKeyPress(sPlayer, this, key);
                    }
                });
            }
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.rollTurning = nbt.getBoolean("running");
        this.setItem(ItemStack.fromNbt(nbt.getCompound("item")));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.putBoolean("running", this.rollTurning);
        nbt.put("item", this.rollStack.writeNbt(new NbtCompound()));
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }
}
