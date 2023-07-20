package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.sound.emitter.SoundSource;
import io.github.foundationgames.phonos.util.UniqueId;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import io.github.foundationgames.phonos.world.sound.block.BlockEntityOutputs;
import io.github.foundationgames.phonos.world.sound.block.OutputBlockEntity;
import io.github.foundationgames.phonos.world.sound.data.SoundEventSoundData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.JukeboxBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.MusicDiscItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.registry.Registries;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

public class ElectronicJukeboxBlockEntity extends JukeboxBlockEntity implements Syncing, Ticking, OutputBlockEntity {
    public static final BlockConnectionLayout OUTPUT_LAYOUT = new BlockConnectionLayout()
            .addPoint(-8, -4, 0, Direction.WEST)
            .addPoint(8, -4, 0, Direction.EAST)
            .addPoint(0, -4, 8, Direction.SOUTH)
            .addPoint(0, -4, -8, Direction.NORTH);

    public final BlockEntityOutputs outputs;

    private final BlockEntityType<?> type;
    private @Nullable NbtCompound pendingNbt = null;
    private final long emitterId;

    public ElectronicJukeboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(pos, state);
        this.type = type;
        this.emitterId = UniqueId.ofBlock(pos);

        this.outputs = new BlockEntityOutputs(OUTPUT_LAYOUT, this);
    }

    public ElectronicJukeboxBlockEntity(BlockPos pos, BlockState state) {
        this(PhonosBlocks.ELECTRONIC_JUKEBOX_ENTITY, pos, state);
    }

    @Override
    public BlockEntityType<?> getType() {
        return this.type;
    }

    @Override
    public void startPlaying() {
        this.recordStartTick = this.tickCount;
        this.isPlaying = true;
        this.world.updateNeighborsAlways(this.getPos(), this.getCachedState().getBlock());

        if (this.getStack().getItem() instanceof MusicDiscItem disc && !world.isClient()) {
            SoundStorage.getInstance(world).play(world, SoundEventSoundData.create(
                    emitterId, Registries.SOUND_EVENT.getEntry(disc.getSound()), 2, 1),
                    new SoundEmitterTree(this.emitterId));
            sync();
        }

        this.markDirty();
    }

    @Override
    protected void stopPlaying() {
        this.isPlaying = false;
        this.world.emitGameEvent(GameEvent.JUKEBOX_STOP_PLAY, this.getPos(), GameEvent.Emitter.of(this.getCachedState()));
        this.world.updateNeighborsAlways(this.getPos(), this.getCachedState().getBlock());

        if (!world.isClient()) {
            SoundStorage.getInstance(world).stop(world, emitterId);
            sync();
        }

        this.markDirty();
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state) {
        if (this.pendingNbt != null) {
            this.pendingNbt = this.outputs.consumeNbt(this.pendingNbt);
        }

        if (!world.isClient()) {
            super.tick(world, pos, state);

            if (this.outputs.purge(conn -> this.outputs.dropConnectionItem(world, conn, true))) {
                sync();
            }
        }
    }

    public void onDestroyed() {
        this.outputs.forEach((index, conn) -> {
            this.outputs.dropConnectionItem(world, conn, false);
            conn.end.setConnected(this.getWorld(), false);
        });
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.pendingNbt = nbt.getCompound("Outputs").copy();

        if (this.world != null) {
            this.pendingNbt = this.outputs.consumeNbt(this.pendingNbt);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        var outputsNbt = this.pendingNbt != null ? this.pendingNbt.copy() : new NbtCompound();
        outputs.writeNbt(outputsNbt);
        nbt.put("Outputs", outputsNbt);
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
    public boolean canConnect(ItemUsageContext ctx) {
        var side = ctx.getSide();
        if (side != Direction.UP && side != Direction.DOWN) {
            return !this.outputs.isOutputPluggedIn(OUTPUT_LAYOUT.getClosestIndexClicked(ctx.getHitPos(), this.getPos()));
        }

        return false;
    }

    @Override
    public boolean addConnection(Vec3d hitPos, @Nullable DyeColor color, InputPlugPoint destInput, ItemStack cable) {
        int index = OUTPUT_LAYOUT.getClosestIndexClicked(hitPos, this.getPos());

        if (this.outputs.tryPlugOutputIn(index, color, destInput, cable)) {
            this.markDirty();
            this.sync();
            return true;
        }

        return false;
    }

    @Override
    public boolean forwards() {
        return false;
    }

    @Override
    public BlockEntityOutputs getOutputs() {
        return this.outputs;
    }

    @Override
    public long emitterId() {
        return emitterId;
    }

    @Override
    public void forEachSource(Consumer<SoundSource> action) {
        this.outputs.forEach((i, conn) -> {
            var src = conn.end.asSource(this.world);

            if (src != null) {
                action.accept(src);
            }
        });
    }

    @Override
    public void forEachChild(LongConsumer action) {
        this.outputs.forEach((i, conn) -> {
            var emitter = conn.end.forward(this.world);

            if (emitter != null) {
                action.accept(emitter.emitterId());
            }
        });
    }
}
