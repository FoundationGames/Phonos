package io.github.foundationgames.phonos.world.sound.block;

import io.github.foundationgames.phonos.world.sound.CableConnection;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class BlockEntityOutputs {
    private final boolean[] skip; // Will not be purged on first tick of existing
    protected final CableConnection[] connections;
    protected final BlockConnectionLayout connectionLayout;
    protected final BlockEntity blockEntity;

    public BlockEntityOutputs(BlockConnectionLayout layout, BlockEntity blockEntity) {
        this.connectionLayout = layout;
        this.connections = new CableConnection[layout.getConnectionCount()];
        this.skip = new boolean[layout.getConnectionCount()];
        this.blockEntity = blockEntity;
    }

    public boolean isOutputPluggedIn(int outputIndex) {
        return getOutputConnection(outputIndex) != null;
    }

    public boolean tryPlugOutputIn(int outputIndex, @Nullable DyeColor color, InputPlugPoint destInput, ItemStack cable) {
        outputIndex = MathHelper.clamp(outputIndex, 0, connections.length - 1);

        if (isOutputPluggedIn(outputIndex)) {
            return false;
        }

        var srcOutput = connectionLayout.outputOfConnection(this.blockEntity.getPos(), outputIndex);
        this.connections[outputIndex] = new CableConnection(srcOutput, destInput, color, cable);
        this.skip[outputIndex] = true;

        return true;
    }

    public boolean tryRemoveConnection(World world, BlockHitResult hit, boolean drop) {
        int clickIndex = this.connectionLayout.getClosestIndexClicked(hit.getPos(), this.blockEntity.getPos(), this.getRotation());

        if (clickIndex >= 0 && isOutputPluggedIn(clickIndex)) {
            if (drop) {
                this.dropConnectionItem(world, connections[clickIndex], false);
            }

            connections[clickIndex].end.setConnected(world, false);
            connections[clickIndex] = null;
            return true;
        }

        return false;
    }

    public @Nullable CableConnection getOutputConnection(int outputIndex) {
        outputIndex = MathHelper.clamp(outputIndex, 0, connections.length - 1);
        return connections[outputIndex];
    }

    public void dropConnectionItem(World world, CableConnection conn, boolean atInput) {
        var point = atInput ? conn.end : conn.start;
        var pos = point.calculatePos(world, 0.25);
        var item = new ItemEntity(world, pos.x, pos.y, pos.z, conn.drop);

        world.spawnEntity(item);
    }

    public int getOutputCount() {
        int count = 0;
        for (var conn : connections) {
            if (conn != null) {
                count++;
            }
        }
        return count;
    }

    public void forEach(BiConsumer<Integer, CableConnection> action) {
        for (int i = 0; i < connections.length; i++) {
            if (connections[i] != null) {
                action.accept(i, connections[i]);
            }
        }
    }

    public boolean purge(Consumer<CableConnection> purgeAction) {
        boolean changed = false;
        for (int i = 0; i < connections.length; i++) {
            if (skip[i]) {
                skip[i] = false;
                continue;
            }

            if (connections[i] != null && connections[i].shouldRemove(this.blockEntity.getWorld())) {
                purgeAction.accept(connections[i]);
                connections[i] = null;
                changed = true;
            }
        }

        return changed;
    }

    public void writeNbt(NbtCompound nbt) {
        for (int i = 0; i < connections.length; i++) {
            var conn = connections[i];
            if (conn != null) {
                var connNbt = new NbtCompound();
                conn.writeNbt(connNbt);

                nbt.put(Integer.toString(i), connNbt);
            }
        }
    }

    public @Nullable NbtCompound consumeNbt(NbtCompound nbt) {
        NbtCompound result = null;

        for (int i = 0; i < connections.length; i++) {
            var key = Integer.toString(i);

            if (nbt.contains(key)) {
                var connNbt = nbt.getCompound(key);
                var outputPoint = this.connectionLayout.outputOfConnection(this.blockEntity.getPos(), i);

                var conn = CableConnection.readNbt(this.blockEntity.getWorld(), outputPoint, connNbt);

                if (conn != null) {
                    nbt.remove(key);
                    connections[i] = conn;
                } else {
                    result = nbt;
                }
            } else {
                connections[i] = null;
            }
        }

        return result;
    }

    private Direction getRotation() {
        return this.blockEntity instanceof OutputBlockEntity be ? be.getRotation() : Direction.NORTH;
    }
}
