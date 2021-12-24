package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.item.ItemLoudspeaker;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    private ItemStack phonos$lastMainHandStack = ItemStack.EMPTY;
    private ItemStack phonos$lastOffHandStack = ItemStack.EMPTY;

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void phonos$checkHeldItemChange(CallbackInfo ci) {
        var self = (ServerPlayerEntity)(Object)this;

        // ItemStack#canCombine is a poorly named equality checking method, it is purely used for comparisons here
        if (ItemStack.canCombine(phonos$lastMainHandStack, self.getOffHandStack()) && ItemStack.canCombine(phonos$lastOffHandStack, self.getMainHandStack())) {
            return; // Stacks have simply been swapped, nothing should change
        }

        if (!ItemStack.canCombine(phonos$lastMainHandStack, self.getMainHandStack())) {
            ItemLoudspeaker.stackUpdatedForEntity(self, phonos$lastMainHandStack, self.getMainHandStack());

            phonos$lastMainHandStack = self.getMainHandStack().copy();
        }
        if (!ItemStack.canCombine(phonos$lastOffHandStack, self.getOffHandStack())) {
            ItemLoudspeaker.stackUpdatedForEntity(self, phonos$lastOffHandStack, self.getOffHandStack());

            phonos$lastOffHandStack = self.getOffHandStack().copy();
        }
    }
}
