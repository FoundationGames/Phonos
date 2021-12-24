package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.world.RadioChannelState;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
    @Inject(method = "onPlayerConnect", at = @At(value = "TAIL"))
    private void phonos$sendRecievers(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
        RadioChannelState.sendPlayerJoinPackets(player);
    }
}
