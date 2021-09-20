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

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.client.ClientRecieverStorage;

public class MultiPositionedSoundInstance extends AbstractSoundInstance implements TickableSoundInstance {
    // sets of the end result type, so we don't need to iterate through all
    // entities, and also because sticking with BlockPos's is nice...
    private final Set<BlockPos> blocks;
    private final Set<Entity> entities;
    private final Entity player;

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
        this.volume = 0;
        this.player = MinecraftClient.getInstance().player;
        // no attenuation, we are controlling this entirely.
        this.attenuationType = SoundInstance.AttenuationType.NONE;
        this.updatePosition();
    }

    private void updatePosition() {
        if((this.entities == null || this.entities.size() == 0) && (this.blocks == null || this.blocks.size() == 0)) {
            // if the lists are empty or don't exist, dont run the code!
            this.volume = 0;
            return;
        }
        if(this.entities.contains(this.player) && this.player.isAlive()) {
            // if the player listening is producing audio, only play them from their POV.
            this.x = this.player.getX();
            this.y = -10000;
            this.z = this.player.getZ();
            this.volume = this.maxVol;
            return;
        }
        // Assume that there is no closest block position (volume)
        double closestDist = Double.MAX_VALUE;

        // The product of all of the weights (where one weight is (dist/64)^2)
        double totalWeight = 0.0;
        Vec3d dirWeight = new Vec3d(0, 0, 0);
        // The total amount of positions in the weight.
        for(BlockPos pos : this.blocks) {
            // for every block, lets get the square distance from that to the player.
            double dist = pos.getSquaredDistance(this.player.getEyePos(), true);
            // when within a 64 block radius in 3d space...
            if(dist <= 64 * 64) {
                // Lets convert the working position to a vector to make things a bit easier.
                Vec3d workingPos = new Vec3d(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
                // This will be the weight of a given source.
                double tempWeight = 64 * 64 / dist - 1;
                // If that weight is bigger than one, we can cube it
                if(tempWeight > 1) tempWeight *= tempWeight * tempWeight;
                // add the final weight to the total weight
                totalWeight += tempWeight;
                // and also add add the weighted vector to the total vector.
                dirWeight = dirWeight.add(workingPos.multiply(tempWeight));
                // and also, if we are now closer to a source than previously, lets set it as such.
                if(closestDist > dist) closestDist = dist;
            }
        }
        // Now, iterate through the entities too.
        for(Entity ent : this.entities) {
            if(ent == null || !ent.isAlive()) continue; // if the entity is dead, don't play it!
            // for every block, lets get the current position of it as a vector.
            Vec3d workingPos = ent.getPos();
            // And the square distance from that to the player.
            double dist = workingPos.squaredDistanceTo(this.player.getEyePos());
            // when within a 64 block radius in 3d space...
            if(dist <= 64 * 64) {
                // This will be the weight of a given source.
                double tempWeight = 64 * 64 / dist - 1;
                // If that weight is bigger than one, we can cube it
                if(tempWeight > 1) tempWeight *= tempWeight * tempWeight;
                // add the final weight to the total weight
                totalWeight += tempWeight;
                // and also add add the weighted vector to the total vector.
                dirWeight = dirWeight.add(workingPos.multiply(tempWeight));
                // and also, if we are now closer to a source than previously, lets set it as such.
                if(closestDist > dist) closestDist = dist;
            }
        }
        if(totalWeight == 0) {
            this.volume = 0;
            return;
        }
        dirWeight = dirWeight.multiply(1.0 / totalWeight);
        Vec3d result = dirWeight;
        // and center the block there.
        this.x = result.x;
        this.y = result.y;
        this.z = result.z;
        // also, if the closest distance is in range,
        if(closestDist < 64 * 64) {
            // volume is logarithmic, right? so this makes SENSE.
            this.volume = (float)((64*64/closestDist)-1)/64 * maxVol;
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
