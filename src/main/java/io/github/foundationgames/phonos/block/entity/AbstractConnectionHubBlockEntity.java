package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.sound.emitter.SoundSource;
import io.github.foundationgames.phonos.util.UniqueId;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import io.github.foundationgames.phonos.world.sound.block.BlockEntityOutputs;
import io.github.foundationgames.phonos.world.sound.block.OutputBlockEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.LongConsumer;

public abstract class AbstractConnectionHubBlockEntity extends BlockEntity implements Syncing, Ticking, OutputBlockEntity {
    public final BlockEntityOutputs outputs;
    public final boolean[] inputs;
    protected @Nullable NbtCompound pendingNbt = null;
    protected final long emitterId;

    public AbstractConnectionHubBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, BlockConnectionLayout outputLayout, boolean[] inputs) {
        super(type, pos, state);

        this.emitterId = UniqueId.ofBlock(pos);
        this.outputs = new BlockEntityOutputs(outputLayout, this);
        this.inputs = inputs;
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state) {
        if (this.pendingNbt != null) {
            this.pendingNbt = this.outputs.consumeNbt(this.pendingNbt);
        }

        if (!world.isClient()) {
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

        for (int i = 0; i < this.inputs.length; i++) {
            this.inputs[i] = nbt.getBoolean("Input" + i);
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        var outputsNbt = this.pendingNbt != null ? this.pendingNbt.copy() : new NbtCompound();
        outputs.writeNbt(outputsNbt);
        nbt.put("Outputs", outputsNbt);

        for (int i = 0; i < this.inputs.length; i++) {
            nbt.putBoolean("Input" + i, this.inputs[i]);
        }
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
    public long emitterId() {
        return this.emitterId;
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

    @Override
    public BlockEntityOutputs getOutputs() {
        return this.outputs;
    }
}
