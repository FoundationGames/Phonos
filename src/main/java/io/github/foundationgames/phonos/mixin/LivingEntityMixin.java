package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.item.ItemLoudspeaker;
import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.ItemStack;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "setStackInHand", at = @At(value = "HEAD"))
    private void phonos$onHeldItemChanged(Hand hand, ItemStack stack, CallbackInfo ci) {
        if ((Object)this instanceof ServerPlayerEntity) return;

        var self = (LivingEntity)(Object)this;
        ItemLoudspeaker.stackUpdatedForEntity(self, self.getStackInHand(hand), stack);
    }
}
