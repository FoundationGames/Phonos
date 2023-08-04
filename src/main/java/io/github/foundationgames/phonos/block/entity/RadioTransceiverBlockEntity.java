package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.block.RadioTransceiverBlock;
import io.github.foundationgames.phonos.radio.RadioDevice;
import io.github.foundationgames.phonos.radio.RadioStorage;
import io.github.foundationgames.phonos.world.RadarPoints;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongConsumer;

public class RadioTransceiverBlockEntity extends AbstractConnectionHubBlockEntity implements RadioDevice.Transmitter, RadioDevice.Receiver {
    public static final BlockConnectionLayout OUTPUT_LAYOUT = new BlockConnectionLayout()
            .addPoint(-8, -5, 0, Direction.WEST)
            .addPoint(8, -5, 0, Direction.EAST)
            .addPoint(0, -5, 8, Direction.SOUTH);

    private int channel = 0;
    private boolean needsAdd = false;

    public RadioTransceiverBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, OUTPUT_LAYOUT, new boolean[2]);
    }

    public RadioTransceiverBlockEntity(BlockPos pos, BlockState state) {
        this(PhonosBlocks.RADIO_TRANSCEIVER_ENTITY, pos, state);
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
    public void onDestroyed() {
        super.onDestroyed();

        if (world instanceof ServerWorld sWorld) {
            RadarPoints.get(sWorld).remove(this.channel, this.pos);
        }
    }

    @Override
    public void forEachChild(LongConsumer action) {
        RadioDevice.Transmitter.super.forEachChild(action);

        super.forEachChild(action);
    }

    @Override
    public int getChannel() {
        return channel;
    }

    @Override
    public boolean canConnect(ItemUsageContext ctx) {
        var side = ctx.getSide();
        var facing = getRotation();

        if (side != Direction.UP && side != Direction.DOWN && side != getCachedState().get(Properties.HORIZONTAL_FACING)) {
            return !this.outputs.isOutputPluggedIn(OUTPUT_LAYOUT.getClosestIndexClicked(ctx.getHitPos(), this.getPos(), facing));
        }

        return false;
    }

    @Override
    public boolean addConnection(Vec3d hitPos, @Nullable DyeColor color, InputPlugPoint destInput, ItemStack cable) {
        int index = OUTPUT_LAYOUT.getClosestIndexClicked(hitPos, this.getPos(), getRotation());

        if (this.outputs.tryPlugOutputIn(index, color, destInput, cable)) {
            this.markDirty();
            this.sync();
            return true;
        }

        return false;
    }

    @Override
    public boolean forwards() {
        return true;
    }

    @Override
    public Direction getRotation() {
        if (this.getCachedState().getBlock() instanceof RadioTransceiverBlock block) {
            return block.getRotation(this.getCachedState());
        }

        return Direction.NORTH;
    }

    @Override
    public void setAndUpdateChannel(int channel) {
        channel = Math.floorMod(channel, RadioStorage.CHANNEL_COUNT);

        if (this.world instanceof ServerWorld sWorld) for (boolean in : this.inputs) if (in) {
            RadarPoints.get(sWorld).remove(this.channel, this.pos);
            RadarPoints.get(sWorld).add(channel, this.pos);
            break;
        }

        var radio = RadioStorage.getInstance(this.world);

        radio.removeReceivingEmitter(this.channel, this.emitterId());
        this.channel = channel;
        radio.addReceivingEmitter(channel, this);

        sync();
    }

    @Override
    public void addReceiver() {
        setAndUpdateChannel(getChannel());
    }

    @Override
    public void removeReceiver() {
        var radio = RadioStorage.getInstance(this.world);
        radio.removeReceivingEmitter(this.getChannel(), this.emitterId());
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state) {
        super.tick(world, pos, state);

        if (this.needsAdd) {
            this.addReceiver();
            this.needsAdd = false;
        }
    }
}
