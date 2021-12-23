package io.github.foundationgames.phonos.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import io.github.foundationgames.phonos.item.PortableSpeakerItem;
import io.github.foundationgames.phonos.world.RadioChannelState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.ClientConnection;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {
    // Players should be able to play two items at the same time, because why not.
    private ItemStack offhandSpeaker = null;
    private ItemStack mainhandSpeaker = null;

    @Inject(method = "tick", at = @At(value = "TAIL")) // , locals = LocalCapture.PRINT)
    private void playerHoldingTick(CallbackInfo info) {
        if(!(((PlayerEntity)(Object)(this)) instanceof ServerPlayerEntity)) return;
        ItemStack offhand = ((PlayerEntity)(Object)(this)).getEquippedStack(EquipmentSlot.OFFHAND);
        ItemStack mainhand = ((PlayerEntity)(Object)(this)).getEquippedStack(EquipmentSlot.MAINHAND);
        if(offhand.getItem() instanceof PortableSpeakerItem) {
            if(this.offhandSpeaker != null && offhand != this.offhandSpeaker) PortableSpeakerItem.stopPlaying(this.offhandSpeaker, ((ServerPlayerEntity)(Object)(this)));
            this.offhandSpeaker = offhand;
            PortableSpeakerItem.keepPlaying(this.offhandSpeaker, ((ServerPlayerEntity)(Object)(this)));
        } else if(this.offhandSpeaker != null) {
            PortableSpeakerItem.stopPlaying(this.offhandSpeaker, ((ServerPlayerEntity)(Object)(this)));
            this.offhandSpeaker = null;
        }
        if(mainhand.getItem() instanceof PortableSpeakerItem) {
            if(this.mainhandSpeaker != null && mainhand != this.mainhandSpeaker) PortableSpeakerItem.stopPlaying(this.mainhandSpeaker, ((ServerPlayerEntity)(Object)(this)));
            this.mainhandSpeaker = mainhand;
            PortableSpeakerItem.keepPlaying(this.mainhandSpeaker, ((ServerPlayerEntity)(Object)(this)));
        } else if(this.mainhandSpeaker != null) {
            PortableSpeakerItem.stopPlaying(this.mainhandSpeaker, ((ServerPlayerEntity)(Object)(this)));
            this.mainhandSpeaker = null;
        }
    }
}
