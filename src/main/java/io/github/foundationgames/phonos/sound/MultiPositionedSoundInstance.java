package io.github.foundationgames.phonos.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.AbstractSoundInstance;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.TickableSoundInstance;
import net.minecraft.entity.Entity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MultiPositionedSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    // sets because each thing should only be considered once.
    private Set<BlockPos> blocks = new LinkedHashSet<BlockPos>();
    private Set<Entity> entities = new LinkedHashSet<Entity>();

    private boolean done;

    private float maxVol;

    public MultiPositionedSoundInstance(Set<BlockPos> blocks, Set<Entity> entities, SoundEvent sound, float volume, float pitch) {
        this(blocks, entities, sound.getId(), volume, pitch);
    }

    public MultiPositionedSoundInstance(Set<BlockPos> blocks, Set<Entity> entities, Identifier sound, float volume, float pitch) {
        super(sound, SoundCategory.RECORDS);
        this.blocks = blocks;
        this.entities = entities;
        this.maxVol = volume;
        // no attenuation, we are controlling this entirely.
        this.attenuationType = SoundInstance.AttenuationType.NONE;
    }

    public MultiPositionedSoundInstance(List<Long> blocks, SoundEvent sound, float volume, float pitch) {
        this(blocks, sound.getId(), volume, pitch);
    }

    public MultiPositionedSoundInstance(List<Long> blocks, Identifier sound, float volume, float pitch) {
        super(sound, SoundCategory.RECORDS);
        this.addBlockLongList(blocks);
        // no attenuation, we control this entierly.
        this.attenuationType = SoundInstance.AttenuationType.NONE;
        this.maxVol = volume;
    }

    public void addEntityList(List<Entity> entities) {
        if(entities != null) {
            for(Entity e : entities) {
                this.addEntity(e);
            }
        }
    }

    public void addEntityIntList(List<Integer> entities) {
        if(entities != null) {
            for(int i : entities) {
                this.addEntityInt(i);
            }
        }
    }

    public void addEntityInt(int entity) { // Ideally, this should not be needed, as we will convert ahead of time.
        Entity e = MinecraftClient.getInstance().world.getEntityById(entity);
        this.addEntity(e);
    }

    public void addEntity(Entity entity) {
        if(entity != null) this.entities.add(entity);
    }

    public void removeEntityInt(int entity) {
        Entity e = MinecraftClient.getInstance().world.getEntityById(entity);
        this.removeEntity(e);
    }

    public void removeEntity(Entity entity) {
        if(entity != null) this.entities.remove(entity);
    }

    public void addBlockLongList(List<Long> blocks) {
        if(blocks != null) {
            for(Long l : blocks) {
                this.addBlockLong(l);
            }
        }
    }

    public void addBlockLong(Long block) {
        BlockPos p = BlockPos.fromLong(block);
        if(p != null) this.blocks.add(p);
    }

    public void removeBlockLong(Long block) {
        BlockPos p = BlockPos.fromLong(block);
        if(p != null) this.blocks.remove(p);
    }

    private void updatePosition() {
        Entity player = MinecraftClient.getInstance().player;
        if(this.entities.contains(player)) {
            // if the player listening is producing audio, only play them from their POV.
            this.x = player.getX();
            this.y = player.getEyeY() + 32; // add 32 for better centering.
            this.z = player.getZ();
            this.volume = this.maxVol;
            return;
        }
        // Assume that there is no closest block position (volume)
        double closestDist = Double.MAX_VALUE;

        // The product of all of the weights (where one weight is (dist/64)^2)
        double productWeight = 1.0;
        // The total amount of positions in the weight.
        int weighedPositions = 0;
        for(BlockPos pos : this.blocks) {
            // for every block, lets get the current position of it as a vector.
            Vec3d workingPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            // And the square distance from that to the player.
            double dist = workingPos.squaredDistanceTo(player.getEyePos());
            // when within a 64 block radius in 3d space...
            if(dist <= 64 * 64) {
                // go ahead and add the distance to our weight product.
                productWeight *= dist / (64 * 64);
                // increment how many we have
                weighedPositions++;
                // and see if its a new lowest distance.
                if(closestDist > dist) {
                    closestDist = dist;
                }
            }
        }
        // Now, iterate through the entities too.
        for(Entity ent : this.entities) {
            // for every block, lets get the current position of it as a vector.
            Vec3d workingPos = ent.getPos();
            // And the square distance from that to the player.
            double dist = workingPos.squaredDistanceTo(player.getEyePos());
            // when within a 64 block radius in 3d space...
            if(dist <= 64 * 64) {
                // go ahead and add the distance to our weight product.
                productWeight *= dist / (64 * 64);
                // increment how many we have
                weighedPositions++;
                // and see if its a new lowest distance.
                if(closestDist > dist) {
                    closestDist = dist;
                }
            }
        }
        // if nothing is being weighed, lets just skip this iteration for now.
        if(weighedPositions == 0) {
            this.x = 0;
            this.y = 0;
            this.z = 0;
            this.volume = 0;
            return;
        }
        // otherwise...
        // the product weight will be x^(n*2), so to cancel out that n, root it by such.
        productWeight = Math.pow(productWeight, 1.0 / (weighedPositions));

        // sum of weighed directions \sum(wi*veci)
        Vec3d dirWeight = new Vec3d(0, 0, 0);
        // the sum of the weight \sum(wi)
        double trueWeight = 0;
        // now, we iterate AGAIN to get the real weight of each block.
        for(BlockPos pos : this.blocks) {
            // get the block position as a vector.
            Vec3d workingPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            // distance to player again.
            double dist = workingPos.squaredDistanceTo(player.getEyePos());
            // if its in range...
            if(dist <= 64 * 64) {
                // we go ahead and calculate the current blocks weight (wi)
                double blockWeight = (productWeight / (dist));
                // square it (to get a better fall off of the weight)
                blockWeight *= blockWeight;
                // add it to the total weight
                trueWeight += blockWeight;
                // scale the block position by the weight
                Vec3d weightedPosition = workingPos.multiply(blockWeight);
                // and add that scaled vector.
                dirWeight = dirWeight.add(weightedPosition);
            }
        }
        // and entity iteration again.
        for(Entity ent : this.entities) {
            // get the entity position.
            Vec3d workingPos = ent.getPos();
            // distance to player again.
            double dist = workingPos.squaredDistanceTo(player.getEyePos());
            // if its in range...
            if(dist <= 64 * 64) {
                // we go ahead and calculate the current blocks weight (wi)
                double blockWeight = (productWeight / (dist));
                // square it (to get a better fall off of the weight)
                blockWeight *= blockWeight;
                // add it to the total weight
                trueWeight += blockWeight;
                // scale the block position by the weight
                Vec3d weightedPosition = workingPos.multiply(blockWeight);
                // and add that scaled vector.
                dirWeight = dirWeight.add(weightedPosition);
            }
        }
        // now we go ahead and average the weights (\sum(wi*veci)/\sum(wi))
        Vec3d result = dirWeight.multiply(1.0 / trueWeight);
        // and center the block there.
        this.x = result.x;
        this.y = result.y;
        this.z = result.z;
        // also, if the closest distance is in range,
        if(closestDist < 64 * 64) {
            // volume is logarithmic, right? so this makes SENSE.
            this.volume = ((float)-Math.log(closestDist / (64 * 64)) / 8) * maxVol;
        } else this.volume = 0.0f;
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
