package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.item.PhonosItems;
import io.github.foundationgames.phonos.util.piano.PianoRoll;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RadioRecorderBlockEntity extends BlockEntity implements Syncing {
    private ItemStack roll = ItemStack.EMPTY;
    private PianoRoll.Recorder currentRecorder = null;
    private boolean powered = false;

    public RadioRecorderBlockEntity(BlockPos pos, BlockState state) {
        super(PhonosBlocks.RADIO_RECORDER_ENTITY, pos, state);
    }

    public boolean hasRoll() {
        return !roll.isEmpty();
    }

    public void setItem(ItemStack rollStack) {
        this.roll = rollStack;

        this.sync();
    }

    public ItemStack takeItem() {
        var stack = this.roll;
        this.roll = ItemStack.EMPTY;
        this.sync();

        return stack;
    }

    public boolean powered() {
        return this.powered;
    }

    public void finishRecording() {
        if (currentRecorder != null) {
            if (this.hasRoll() && !world.isClient()) {
                var rollStack = new ItemStack(PhonosItems.PIANO_ROLL);
                rollStack.setNbt(this.roll.getNbt());
                rollStack.setSubNbt("PianoRoll", currentRecorder.build().toNbt());

                world.spawnEntity(new ItemEntity(this.world, this.pos.getX() + 0.5, this.pos.getY() + 1.1, this.pos.getZ() + 0.5, rollStack));
                this.setItem(ItemStack.EMPTY);
            }

            currentRecorder = null;
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, RadioRecorderBlockEntity self) {
        boolean powered = state.get(Properties.POWERED);

        if (powered != self.powered) {
            self.powered = powered;

            if (!powered && !world.isClient()) {
                self.finishRecording();

                self.sync();
            }
        }

        if (!world.isClient() && self.currentRecorder != null) {
            self.currentRecorder.tick();

            if (self.currentRecorder.size() > 512) {
                self.finishRecording();

                self.sync();
            }
        }
    }

    public void receiveNote(float pitch) {
        if (this.powered) {
            if (this.currentRecorder == null) {
                this.currentRecorder = new PianoRoll.Recorder();
            }

            currentRecorder.applyNote(pitch);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.roll = ItemStack.fromNbt(nbt.getCompound("item"));
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        nbt.put("item", this.roll.writeNbt(new NbtCompound()));
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
