package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.network.PayloadPackets;
import io.github.foundationgames.phonos.sound.SoundStorage;
import io.github.foundationgames.phonos.sound.custom.ServerCustomAudio;
import io.github.foundationgames.phonos.sound.emitter.SoundEmitterTree;
import io.github.foundationgames.phonos.sound.stream.ServerOutgoingStreamHandler;
import io.github.foundationgames.phonos.util.UniqueId;
import io.github.foundationgames.phonos.world.sound.InputPlugPoint;
import io.github.foundationgames.phonos.world.sound.block.BlockConnectionLayout;
import io.github.foundationgames.phonos.world.sound.data.SoundDataTypes;
import io.github.foundationgames.phonos.world.sound.data.StreamSoundData;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class SatelliteStationBlockEntity extends AbstractOutputBlockEntity {
    public static final BlockConnectionLayout OUTPUT_LAYOUT = new BlockConnectionLayout()
            .addPoint(-8, -5, 0, Direction.WEST)
            .addPoint(8, -5, 0, Direction.EAST)
            .addPoint(0, -5, 8, Direction.SOUTH);

    public final long streamId;
    private @Nullable SoundEmitterTree playingSound = null;

    public SatelliteStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, OUTPUT_LAYOUT);

        this.streamId = UniqueId.obf(this.emitterId());
    }

    public SatelliteStationBlockEntity(BlockPos pos, BlockState state) {
        this(PhonosBlocks.SATELLITE_STATION_ENTITY, pos, state);
    }

    public void play() {
        if (world instanceof ServerWorld sWorld && ServerCustomAudio.hasSaved(this.streamId)) {
            ServerOutgoingStreamHandler.startStream(this.streamId, ServerCustomAudio.loadSaved(this.streamId), sWorld.getServer());

            this.playingSound = new SoundEmitterTree(this.emitterId);
            SoundStorage.getInstance(this.world).play(this.world,
                    new StreamSoundData(SoundDataTypes.STREAM, this.emitterId(), this.streamId, 2, 1),
                    this.playingSound);
        }
    }

    public void stop() {
        if (world instanceof ServerWorld sWorld && playingSound != null) {
            ServerOutgoingStreamHandler.endStream(this.streamId, sWorld.getServer());
            SoundStorage.getInstance(this.world).stop(this.world, this.emitterId());
            this.playingSound = null;
        }
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state) {
        super.tick(world, pos, state);

        if (!world.isClient()) {
            if (this.playingSound != null) {
                var delta = this.playingSound.updateServer(world);

                if (delta.hasChanges() && world instanceof ServerWorld sWorld) for (var player : sWorld.getPlayers()) {
                    PayloadPackets.sendSoundUpdate(player, delta);
                }
            }
        }
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();

        if (world instanceof ServerWorld sWorld) {
            ServerCustomAudio.deleteSaved(sWorld.getServer(), this.streamId);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
    }

    public boolean canUpload(ServerPlayerEntity player) {
        return player.canModifyBlocks();
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
    public Direction getRotation() {
        return this.getCachedState().get(Properties.HORIZONTAL_FACING);
    }

    @Override
    public boolean forwards() {
        return false;
    }

    public enum Status {
        NOT_LAUNCHED, LAUNCHING, IN_ORBIT, CRASHED;
    }
}
