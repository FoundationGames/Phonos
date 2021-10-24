package io.github.foundationgames.phonos.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.Vec3d;

import java.util.Set;

public class MultiPositionedSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    private final Set<BlockPos> blocks;
    private final Set<Entity> entities;
    private double x;
    private double y;
    private double z;
    
    private boolean done;

    public MultiPositionedSoundInstance(Set<BlockPos> blocks, Set<Entity> entities, SoundEvent sound, float volume, float pitch) {
        this(blocks, entities, sound.getId(), volume, pitch);
    }

    public MultiPositionedSoundInstance(Set<BlockPos> blocks, Set<Entity> entities, Identifier sound, float volume, float pitch) {
        super(sound, SoundCategory.RECORDS);
        this.volume = volume;
        this.pitch = pitch;
        this.blocks = blocks;
        this.entities = entities;
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
    	MinecraftClient mc = MinecraftClient.getInstance();
        PlayerEntity player = mc.player;

        if(blocks == null) {
            setAsFarPositions(player);
            return;
        }
        boolean near = false;
        double bd = -1;
        Vec3d bp = null;
        for(BlockPos l : blocks) {
            double d = l.getSquaredDistance(player.getEyePos(), false);
            if(d < 950*(volume/2)) {
                if(bd < 0 || d < bd) {
                    bd = d;
                    bp = new Vec3d(l.getX()+.5, l.getY()+.5, l.getZ()+.5);
                }
            }
            if(l.isWithinDistance(player.getEyePos(), 15*(volume/2))) near = true;
        }
        for(Entity e : entities) {
            double d = e.squaredDistanceTo(player.getEyePos());
            if(d < 950*(volume/2)) {
                if(bd < 0 || d < bd) {
                    bd = d;
                    bp = e.getEyePos();
                }
            }
            if(d < (15*(volume/2)*15*(volume/2))) near = true;
        }
        if(near) setAsPlayerPositions(player);
        else if(bp != null) {
            this.x = bp.getX();
            this.y = bp.getY();
            this.z = bp.getZ();
        }
        else setAsFarPositions(player);
        
    }

    private void setAsFarPositions(PlayerEntity player) {
        this.x = player.getX();
        this.y = player.getEyeY()+256;
        this.z = player.getZ();
    }

    private void setAsPlayerPositions(PlayerEntity player) {
        this.x = player.getX();
        this.y = player.getEyeY()+1;
        this.z = player.getZ();
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