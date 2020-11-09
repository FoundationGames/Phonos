package io.github.foundationgames.phonos.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import java.util.List;

public class MultiPositionedSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private final List<Long> positions;
    private double x;
    private double y;
    private double z;

    private boolean done;

    public MultiPositionedSoundInstance(List<Long> positions, SoundEvent sound, float volume, float pitch) {
        this(positions, sound.getId(), volume, pitch);
    }

    public MultiPositionedSoundInstance(List<Long> positions, Identifier sound, float volume, float pitch) {
        super(sound, SoundCategory.RECORDS);
        this.volume = volume;
        this.pitch = pitch;
        this.positions = positions;
        updatePosition();
    }

    @Override
    public double getX() {
        return x;
    }

    @Override
    public double getY() {
        return y;
    }

    @Override
    public double getZ() {
        return z;
    }

    private void updatePosition() {
        PlayerEntity player = MinecraftClient.getInstance().player;
        BlockPos.Mutable mpos = new BlockPos.Mutable();
        BlockPos pos = player.getBlockPos();
        double tx = 0;
        double ty = 0;
        double tz = 0;
        if(positions == null) {
            setAsFarPositions(player);
            return;
        }
        boolean near = false;
        double bd = -1;
        BlockPos bp = null;
        for(long l : positions) {
            mpos.set(l);
            double d = mpos.getSquaredDistance(pos);
            if(d < 950*(volume/2)) {
                if(bd < 0 || d < bd) {
                    bd = d;
                    bp = mpos.toImmutable();
                }
            }
            if(pos.isWithinDistance(mpos, 15*(volume/2))) near = true;
        }
        if(near) setAsPlayerPositions(player);
        else if(bp != null) {
            this.x = bp.getX()+0.5;
            this.y = bp.getY()+0.5;
            this.z = bp.getZ()+0.5;
        }
        else setAsFarPositions(player);
    }

    private void setAsFarPositions(PlayerEntity player) {
        this.x = player.getPos().getX();
        this.y = player.getPos().getY()+256;
        this.z = player.getPos().getZ();
    }

    private void setAsPlayerPositions(PlayerEntity player) {
        this.x = player.getPos().getX();
        this.y = player.getPos().getY()+1;
        this.z = player.getPos().getZ();
    }

    @Override
    public boolean isDone() {
        return done;
    }

    protected final void setDone() {
        this.done = true;
        this.repeat = false;
    }

    @Override
    public void tick() {
        updatePosition();
    }
}
