package io.github.foundationgames.phonos.world.sound;

import io.github.foundationgames.phonos.util.PhonosUtil;
import io.github.foundationgames.phonos.util.Pose3f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.joml.Vector4f;

public interface CablePlugPoint {
    default Vec3d calculatePos(World world, double extend) {
        var originPose = new Pose3f(new Vector3f(), new Quaternionf());
        var plugPose = new Pose3f(new Vector3f(), new Quaternionf());
        this.writeOriginPose(world, 0, originPose);
        this.writePlugPose(world, 0, plugPose);

        var pos = PhonosUtil.vec3to4(originPose.pos(), new Vector4f())
                .add(originPose.rotation()
                        .transform(PhonosUtil.vec3to4(plugPose.pos(), new Vector4f()))
                )
                .add(plugPose.rotation()
                        .transform(new Vector4f(0, 0, (float) -extend, 1))
                );

        return new Vec3d(pos.x(), pos.y(), pos.z());
    }

    void writePlugPose(World world, float delta, Pose3f out);

    void writeOriginPose(World world, float delta, Pose3f out);
}
