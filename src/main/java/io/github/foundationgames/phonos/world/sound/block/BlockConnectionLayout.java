package io.github.foundationgames.phonos.world.sound.block;

import io.github.foundationgames.phonos.sound.emitter.SoundEmitter;
import io.github.foundationgames.phonos.sound.emitter.SoundSource;
import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.util.Pose3f;
import io.github.foundationgames.phonos.util.UniqueId;
import io.github.foundationgames.phonos.world.sound.CablePlugPoint;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.data.SoundData;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationAxis;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BlockConnectionLayout {
    private final List<ConnectionPose> connectionPoints = new ArrayList<>();

    public BlockConnectionLayout addPoint(ConnectionPose pose) {
        this.connectionPoints.add(pose);
        return this;
    }

    public BlockConnectionLayout addPoint(double x, double y, double z, Direction rotation) {
        return this.addPoint(new ConnectionPose(new Vec3d(x / 16, y / 16, z / 16), PhonosUtil.rotationTo(rotation)));
    }

    public int getConnectionCount() {
        return connectionPoints.size();
    }

    public @Nullable BlockConnectionLayout.BlockInput inputOfConnection(InputPlugPoint.Type type, BlockPos pos, int index) {
        if (index >= 0 && index < connectionPoints.size()) {
            return new BlockInput(type, connectionPoints.get(index), pos, index);
        }

        return null;
    }

    public @Nullable BlockConnectionLayout.BlockOutput outputOfConnection(BlockPos pos, int index) {
        if (index >= 0 && index < connectionPoints.size()) {
            return new BlockOutput(connectionPoints.get(index), pos, index);
        }

        return null;
    }

    public int getClosestIndexClicked(Vec3d clickPos, BlockPos blockPos) {
        return this.getClosestIndexClicked(clickPos, blockPos, Direction.NORTH);
    }

    public int getClosestIndexClicked(Vec3d clickPos, BlockPos blockPos, Direction rotation) {
        var pos = PhonosUtil.rotateTo(
                clickPos.subtract(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5),
                rotation
        );

        return connectionPoints.stream()
                .min(Comparator.comparingDouble(pose -> pose.pos().squaredDistanceTo(pos)))
                .map(connectionPoints::indexOf)
                .orElse(-1);
    }

    public static @Nullable BlockConnectionLayout.BlockInput blockInputPlugPoint(InputPlugPoint.Type type, World world, NbtCompound nbt) {
        var pos = BlockPos.fromLong(nbt.getLong("pos"));
        var state = world.getBlockState(pos);
        var index = nbt.getInt("index");

        if (state.getBlock() instanceof InputBlock input) {
             return input.getInputLayout().inputOfConnection(type, pos, index);
        }

        return null;
    }

    public record ConnectionPose(Vec3d pos, Quaternionf rot) {
    }

    public static class BlockInput extends InputPlugPoint implements SoundSource {
        private final BlockPos blockPos;
        private final Pose3f pose;
        private final long uniqueId;
        public final int connectionIndex;

        public BlockInput(Type type, ConnectionPose pose, BlockPos pos, int connectionIndex) {
            super(type);
            this.blockPos = pos;
            this.uniqueId = UniqueId.ofBlock(pos);
            this.pose = new Pose3f(new Vector3f(
                    (float) pose.pos().x, (float) pose.pos().y, (float) pose.pos().z
            ), new Quaternionf().set(pose.rot()));
            this.connectionIndex = connectionIndex;
        }

        @Override
        public void writePlugPose(World world, float delta, Pose3f out) {
            out.set(this.pose);
        }

        @Override
        public void writeOriginPose(World world, float delta, Pose3f out) {
            out.pos().set(this.blockPos.getX() + 0.5f, this.blockPos.getY() + 0.5f, this.blockPos.getZ() + 0.5f);

            if (world.isPosLoaded(this.blockPos.getX(), this.blockPos.getZ())) {
                if (world.getBlockState(this.blockPos).getBlock() instanceof InputBlock block) {
                    out.rotation().set(PhonosUtil.rotationTo(block.getRotation(world.getBlockState(blockPos))));
                    return;
                }
            }
            out.rotation().set(RotationAxis.POSITIVE_Y.rotation(0));
        }

        @Override
        public boolean canPlugExist(World world) {
            if (world.isPosLoaded(this.blockPos.getX(), this.blockPos.getZ())) {
                var state = world.getBlockState(this.blockPos);
                return state.getBlock() instanceof InputBlock input &&
                        input.isInputPluggedIn(this.connectionIndex, state, world, this.blockPos);
            }

            return true;
        }

        @Override
        public @Nullable SoundSource asSource(World world) {
            if (world.isPosLoaded(this.blockPos.getX(), this.blockPos.getZ())) {
                var state = world.getBlockState(this.blockPos);
                return state.getBlock() instanceof InputBlock input &&
                        input.playsSound(world, this.blockPos) ? this : null;
            }

            return null;
        }

        @Override
        public @Nullable SoundEmitter forward(World world) {
            if (world.isPosLoaded(this.blockPos.getX(), this.blockPos.getZ()) &&
                    world.getBlockEntity(this.blockPos) instanceof OutputBlockEntity out &&
                    out.forwards()) {
                return out;
            }

            return null;
        }

        @Override
        public void writeNbt(NbtCompound nbt) {
            super.writeNbt(nbt);

            nbt.putLong("pos", this.blockPos.asLong());
            nbt.putInt("index", this.connectionIndex);
        }

        @Override
        public void onSoundPlayed(World world, SoundData sound) {
            if (world.isPosLoaded(this.blockPos.getX(), this.blockPos.getZ())) {
                var state = world.getBlockState(this.blockPos);
                if (state.getBlock() instanceof SoundDataHandler block) {
                    block.receiveSound(state, world, this.blockPos, sound);
                }
            }
        }

        @Override
        public void setConnected(World world, boolean connected) {
            var state = world.getBlockState(this.blockPos);
            if (state.getBlock() instanceof InputBlock input) {
                input.setInputPluggedIn(this.connectionIndex, connected, state, world, this.blockPos);
            }
        }

        @Override
        public double x() {
            return this.blockPos.getX() + 0.5;
        }

        @Override
        public double y() {
            return this.blockPos.getY() + 0.5;
        }

        @Override
        public double z() {
            return this.blockPos.getZ() + 0.5;
        }
    }

    public static class BlockOutput implements CablePlugPoint {
        private final BlockPos blockPos;
        private final Pose3f pose;
        public final int connectionIndex;

        public BlockOutput(ConnectionPose pose, BlockPos pos, int connectionIndex) {
            this.blockPos = pos;
            this.pose = new Pose3f(new Vector3f(
                    (float) pose.pos().x, (float) pose.pos().y, (float) pose.pos().z
            ), new Quaternionf().set(pose.rot()));
            this.connectionIndex = connectionIndex;
        }

        @Override
        public void writePlugPose(World world, float delta, Pose3f out) {
            out.set(this.pose);
        }

        @Override
        public void writeOriginPose(World world, float delta, Pose3f out) {
            out.pos().set(this.blockPos.getX() + 0.5f, this.blockPos.getY() + 0.5f, this.blockPos.getZ() + 0.5f);

            if (world.isPosLoaded(this.blockPos.getX(), this.blockPos.getZ())) {
                if (world.getBlockEntity(this.blockPos) instanceof OutputBlockEntity be) {
                    out.rotation().set(PhonosUtil.rotationTo(be.getRotation()));
                    return;
                }
            }
            out.rotation().set(RotationAxis.POSITIVE_Y.rotation(0));
        }
    }
}
