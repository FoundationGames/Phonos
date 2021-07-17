package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.util.CopperStateMap;
import net.minecraft.block.BlockState;
import net.minecraft.item.HoneycombItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * This utter mess will continue to exist as long as Fabric's
 * Mixin continues to lack interface mixin support for whatever
 * ungodly reason
 */
@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
    @Inject(method = "getWaxedState", at = @At("HEAD"), cancellable = true)
    private static void phonos$includeCustomWaxedStates(BlockState state, CallbackInfoReturnable<Optional<BlockState>> cir) {
        var block = CopperStateMap.getWaxed(state.getBlock());
        if (block.isPresent()) {
            cir.setReturnValue(block.map(b -> b.getStateWithProperties(state)));
        }
    }
}
