package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public class EntityMixin {
    @Shadow public World world;

    @Inject(method = "setRemoved", at = @At("TAIL"))
    private void phonos$clearReceiver(Entity.RemovalReason reason, CallbackInfo ci) {
        if (this.world instanceof ServerWorld world) {
            PhonosUtil.getRadioState(world).purgeEntityReceiver((Entity)(Object)this);
        }
    }
}
