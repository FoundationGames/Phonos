package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.radio.RadioDevice;
import io.github.foundationgames.phonos.radio.RadioStorage;
import io.github.foundationgames.phonos.sound.emitter.SoundSource;
import io.github.foundationgames.phonos.world.sound.block.SoundDataHandler;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class RadioLoudspeakerBlockEntity extends BlockEntity implements Syncing, Ticking, RadioDevice.Receiver, SoundSource {
    private int channel = 0;
    private boolean needsAdd = false;

    public RadioLoudspeakerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public RadioLoudspeakerBlockEntity(BlockPos pos, BlockState state) {
        this(PhonosBlocks.RADIO_LOUDSPEAKER_ENTITY, pos, state);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        if (this.world == null) {
            this.needsAdd = true;
            this.channel = nbt.getInt("channel");
        } else {
            this.setAndUpdateChannel(nbt.getInt("channel"));
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putInt("channel", this.getChannel());
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
        return this.getPacket();
    }

    @Override
    public int getChannel() {
        return channel;
    }

    @Override
    public void setAndUpdateChannel(int channel) {
        channel = Math.floorMod(channel, RadioStorage.CHANNEL_COUNT);

        var radio = RadioStorage.getInstance(this.world);

        radio.removeReceivingSource(this.channel, this);
        this.channel = channel;
        radio.addReceivingSource(channel, this);

        sync();
    }

    @Override
    public void addReceiver() {
        setAndUpdateChannel(getChannel());
    }

    @Override
    public void removeReceiver() {
        var radio = RadioStorage.getInstance(this.world);
        radio.removeReceivingSource(this.getChannel(), this);
    }

    @Override
    public double x() {
        return this.getPos().getX() + 0.5;
    }

    @Override
    public double y() {
        return this.getPos().getY() + 0.5;
    }

    @Override
    public double z() {
        return this.getPos().getZ() + 0.5;
    }

    @Override
    public void onSoundPlayed(World world, SoundData sound) {
        var pos = this.getPos();
        var state = world.getBlockState(pos);

        if (state.getBlock() instanceof SoundDataHandler block) {
            block.receiveSound(state, world, pos, sound);
        }
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state) {
        if (this.needsAdd) {
            this.addReceiver();
            this.needsAdd = false;
        }
    }

    public Direction getRotation() {
        return this.getCachedState().get(Properties.HORIZONTAL_FACING);
    }
}
