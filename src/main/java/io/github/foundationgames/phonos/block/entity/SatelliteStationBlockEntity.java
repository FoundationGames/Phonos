package io.github.foundationgames.phonos.block.entity;

import io.github.foundationgames.phonos.block.PhonosBlocks;
import io.github.foundationgames.phonos.item.PhonosItems;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;

public class SatelliteStationBlockEntity extends AbstractOutputBlockEntity {
    public static final int ACTION_CRASH = 0;
    public static final int ACTION_LAUNCH = 1;

    public static final BlockConnectionLayout OUTPUT_LAYOUT = new BlockConnectionLayout()
            .addPoint(-8, -5, 0, Direction.WEST)
            .addPoint(8, -5, 0, Direction.EAST)
            .addPoint(0, -5, 8, Direction.SOUTH);
    public static final int SCREEN_LAUNCH = 0;
    public static final int SCREEN_CRASH = 1;

    public final long streamId;

    private String error = null;
    private Vec3d launchpadPos = null;
    private @Nullable SoundEmitterTree playingSound = null;
    private Status status = Status.NONE;

    private int playDuration = 0;
    private int playingTimer = 0;

    private Rocket rocket = null;

    public SatelliteStationBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, OUTPUT_LAYOUT);

        this.streamId = UniqueId.obf(this.emitterId());
    }

    public SatelliteStationBlockEntity(BlockPos pos, BlockState state) {
        this(PhonosBlocks.SATELLITE_STATION_ENTITY, pos, state);
    }

    public void play() {
        if (world instanceof ServerWorld sWorld && ServerCustomAudio.loaded() && ServerCustomAudio.hasSaved(this.streamId)) {
            var aud = ServerCustomAudio.loadSaved(this.streamId);
            ServerOutgoingStreamHandler.startStream(this.streamId, aud, sWorld.getServer());

            this.playingSound = new SoundEmitterTree(this.emitterId);
            SoundStorage.getInstance(this.world).play(this.world,
                    new StreamSoundData(SoundDataTypes.STREAM, this.emitterId(), this.streamId, SoundCategory.MASTER, 2, 1),
                    this.playingSound);

            this.playingTimer = this.playDuration = (int) ((aud.originalSize * 20f) / aud.sampleRate);
        }
    }

    public void stop() {
        if (world instanceof ServerWorld sWorld && playingSound != null) {
            ServerOutgoingStreamHandler.endStream(this.streamId, sWorld.getServer());
            SoundStorage.getInstance(this.world).stop(this.world, this.emitterId());
            this.playingSound = null;
        }

        this.playDuration = this.playingTimer = 0;
    }

    public boolean addRocket() {
        if (this.rocket != null || this.status != Status.NONE) {
            return false;
        }

        this.rocket = dormantRocket();
        sync();
        markDirty();

        return true;
    }

    @Override
    public void tick(World world, BlockPos pos, BlockState state) {
        super.tick(world, pos, state);

        if (this.rocket != null) {
            this.rocket.tick();

            if (this.rocket.removed) {
                this.rocket = null;
                this.status = Status.IN_ORBIT;

                sync();
            }
        }

        if (!world.isClient()) {
            if (this.playingTimer > 0) {
                this.playingTimer--;

                if (this.getComparatorOutput(this.playingTimer + 1) != this.getComparatorOutput()) {
                    world.updateComparators(this.pos, this.getCachedState().getBlock());
                }
            }

            if (this.playingSound != null) {
                var delta = this.playingSound.updateServer(world);

                if (delta.hasChanges() && world instanceof ServerWorld sWorld) for (var player : sWorld.getPlayers()) {
                    PayloadPackets.sendSoundUpdate(player, delta);
                }
            }

            if (ServerCustomAudio.ERRORS.containsKey(this.streamId)) {
                if (status == Status.IN_ORBIT) this.performAction(ACTION_CRASH);
                this.error = ServerCustomAudio.ERRORS.remove(this.streamId);

                sync();
            }

            if (ServerCustomAudio.loaded()) {
                if (status == Status.NONE && (ServerCustomAudio.UPLOADING.containsKey(this.streamId) || ServerCustomAudio.SAVED.containsKey(this.streamId))) {
                    this.performAction(ACTION_LAUNCH);
                } else if (status == Status.IN_ORBIT) {
                    if (!ServerCustomAudio.SAVED.containsKey(this.streamId)) {
                        this.performAction(ACTION_CRASH);
                    }
                }
            }
        } else {
            if (this.rocket != null) {
                this.rocket.addParticles();
            }
        }
    }

    public void performAction(int action) {
        if (world instanceof ServerWorld sWorld) {
            for (var player : sWorld.getPlayers()) {
                sWorld.sendToPlayerIfNearby(player,
                        true, this.getPos().getX(), this.getPos().getY(), this.getPos().getZ(),
                        PayloadPackets.pktSatelliteAction(this, action));
            }
        }

        switch (action) {
            case ACTION_LAUNCH -> {
                if (this.rocket == null) {
                    this.rocket = dormantRocket();
                }

                this.status = Status.LAUNCHING;
                this.rocket.inFlight = true;
            }
            case ACTION_CRASH -> {
                this.status = Status.NONE;

                if (world instanceof ServerWorld sWorld) {
                    ServerCustomAudio.deleteSaved(sWorld.getServer(), this.streamId);

                    this.stop();
                }

                spawnCrashingSatellite();
            }
        }

        if (!world.isClient()) {
            markDirty();
        }
    }

    public @Nullable Rocket getRocket() {
        return this.rocket;
    }

    public @Nullable String getError() {
        return this.error;
    }

    public Status getStatus() {
        return this.status;
    }

    public int getComparatorOutput() {
        return getComparatorOutput(this.playingTimer);
    }

    protected int getComparatorOutput(int timer) {
        if (this.playDuration == 0) {
            return 0;
        }

        return MathHelper.clamp((int) Math.ceil(15f * timer / this.playDuration), 0, 15);
    }

    public Vec3d launchpadPos() {
        if (this.launchpadPos == null) {
            this.launchpadPos = Vec3d.ofBottomCenter(this.getPos())
                    .add(new Vec3d(-0.21875, 0.4375, -0.21875)
                            .rotateY((float) Math.toRadians(180 - this.getRotation().asRotation())));
        }

        return this.launchpadPos;
    }

    @Override
    public void onDestroyed() {
        super.onDestroyed();

        if (world instanceof ServerWorld sWorld) {
            ServerCustomAudio.deleteSaved(sWorld.getServer(), this.streamId);

            if (status == Status.IN_ORBIT) {
                spawnCrashingSatellite();
            } else if (this.rocket != null) {
                var launchpad = this.launchpadPos();

                double rX = launchpad.x + rocket.x;
                double rY = launchpad.y + rocket.y;
                double rZ = launchpad.z + rocket.z;

                var item = new ItemEntity(world, rX, rY, rZ, PhonosItems.SATELLITE.getDefaultStack());
                world.spawnEntity(item);
            }
        }
    }

    public void spawnCrashingSatellite() {
        var pos = this.somewhereInTheSky().add(this.getPos().toCenterPos()).subtract(0, 100, 0);

        // TODO: Special crashing entity
        var item = new ItemEntity(world, pos.x, pos.y, pos.z, PhonosItems.SATELLITE.getDefaultStack());
        world.spawnEntity(item);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);

        this.status = Status.values()[nbt.getInt("status")];
        this.playingTimer = nbt.getInt("playing_timer");
        this.playDuration = nbt.getInt("play_duration");

        if (nbt.contains("error")) {
            this.error = nbt.getString("error");
        }

        if (nbt.contains("Rocket")) {
            this.rocket = rocketFromNbt(nbt.getCompound("Rocket"));
        } else {
            this.rocket = null;
        }
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);

        nbt.putInt("status", this.status.ordinal());
        nbt.putInt("playing_timer", this.playingTimer);
        nbt.putInt("play_duration", this.playDuration);

        if (this.error != null) {
            nbt.putString("error", this.error);
        }

        if (this.rocket != null) {
            var rocketNbt = new NbtCompound();
            this.rocket.writeNbt(rocketNbt);
            nbt.put("Rocket", rocketNbt);
        }
    }

    public boolean canUpload(ServerPlayerEntity player) {
        return player.getPos().squaredDistanceTo(this.getPos().toCenterPos()) < 96 && player.canModifyBlocks();
    }

    public boolean canCrash(ServerPlayerEntity player) {
        return player.getPos().squaredDistanceTo(this.getPos().toCenterPos()) < 96 && player.canModifyBlocks();
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

    public void tryOpenScreen(ServerPlayerEntity player) {
        if (this.status == Status.LAUNCHING) {
            player.sendMessageToClient(Text.translatable("message.phonos.satellite_launching").formatted(Formatting.GOLD), true);
        } else if (this.status == Status.IN_ORBIT) {
            PayloadPackets.sendOpenSatelliteStationScreen(player, pos, SCREEN_CRASH);
        } else if (this.rocket == null) {
            player.sendMessageToClient(Text.translatable("message.phonos.no_satellite").formatted(Formatting.RED), true);
        } else {
            PayloadPackets.sendOpenSatelliteStationScreen(player, pos, SCREEN_LAUNCH);
        }
    }

    public enum Status {
        NONE, IN_ORBIT, LAUNCHING;
    }

    public Vec3d somewhereInTheSky() {
        return new Vec3d(world.random.nextBetween(-5, 5), 200, world.random.nextBetween(-5, 5));
    }

    public Rocket dormantRocket() {
        return new Rocket(this.world.random.nextFloat() * 0.2, somewhereInTheSky());
    }

    public Rocket rocketFromNbt(NbtCompound nbt) {
        var rocket = new Rocket(nbt.getDouble("drift"), new Vec3d(
                nbt.getDouble("targX"), nbt.getDouble("targY"), nbt.getDouble("targZ")
        ));

        rocket.prevX = nbt.getDouble("prevX");
        rocket.prevY = nbt.getDouble("prevY");
        rocket.prevZ = nbt.getDouble("prevZ");
        rocket.x = nbt.getDouble("x");
        rocket.y = nbt.getDouble("y");
        rocket.z = nbt.getDouble("z");
        rocket.vel = nbt.getDouble("vel");
        rocket.inFlight = nbt.getBoolean("flying");

        return rocket;
    }

    // All coordinates are relative to the block's "launchpad", not world coords
    public class Rocket {
        private final Vector3d calc = new Vector3d();

        public double prevX, prevY, prevZ;
        public double x, y, z;
        public double vel;
        public boolean inFlight;
        public boolean removed;

        public final double drift;
        private final Vector3d target;

        public Rocket(double drift, Vec3d target) {
            this.drift = drift;
            this.target = new Vector3d(target.x, target.y, target.z);
        }

        public void writeNbt(NbtCompound nbt) {
            nbt.putDouble("prevX", prevX);
            nbt.putDouble("prevY", prevY);
            nbt.putDouble("prevZ", prevZ);
            nbt.putDouble("x", x);
            nbt.putDouble("y", y);
            nbt.putDouble("z", z);
            nbt.putDouble("vel", vel);
            nbt.putBoolean("flying", inFlight);
            nbt.putDouble("drift", drift);
            nbt.putDouble("targX", target.x);
            nbt.putDouble("targY", target.y);
            nbt.putDouble("targZ", target.z);
        }

        public float getX(float delta) {
            return (float) MathHelper.lerp(delta, prevX, x);
        }

        public float getY(float delta) {
            return (float) MathHelper.lerp(delta, prevY, y);
        }

        public float getZ(float delta) {
            return (float) MathHelper.lerp(delta, prevZ, z);
        }

        public void tick() {
            if (inFlight) {
                this.prevX = x;
                this.prevY = y;
                this.prevZ = z;

                this.vel = Math.min(this.vel + 0.25, 3.25);

                calc.set(target.x - x, target.y - y, target.z - z).normalize(vel);

                this.x += calc.x;
                this.y += calc.y;
                this.z += calc.z;

                this.target.add(1 * drift, 0, 1 * drift);

                if (this.y > target.y) {
                    this.removed = true;
                }
            }
        }

        public void addParticles() {
            if (inFlight) {
                var be = SatelliteStationBlockEntity.this;

                if (be.world.isClient()) {
                    double x = be.launchpadPos().x + this.x;
                    double y = be.launchpadPos().y + this.y;
                    double z = be.launchpadPos().z + this.z;

                    MinecraftClient.getInstance().particleManager.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0, -0.03 ,0);
                }
            }
        }
    }
}
