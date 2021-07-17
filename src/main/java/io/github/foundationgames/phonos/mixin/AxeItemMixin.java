package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.util.CopperStateMap;
import net.minecraft.block.BlockState;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

/**
 * This utter mess will continue to exist as long as Fabric's
 * Mixin continues to lack interface mixin support for whatever
 * ungodly reason
 */
@Mixin(AxeItem.class)
public class AxeItemMixin {
    private static ItemUsageContext cachedUseCtx = null;

    @Inject(method = "useOnBlock", at = @At("HEAD"))
    private void phonos$cacheUseCtx(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        cachedUseCtx = context;
    }

    @ModifyVariable(method = "useOnBlock", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemUsageContext;getStack()Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE), index = 7)
    private Optional<BlockState> phonos$modifyUnoxidizedState(Optional<BlockState> old) {
        var state = cachedUseCtx.getWorld().getBlockState(cachedUseCtx.getBlockPos());
        var block = CopperStateMap.getDecrease(state.getBlock());
        if (block.isPresent()) {
            return block.map(b -> b.getStateWithProperties(state));
        }
        return old;
    }

    @ModifyVariable(method = "useOnBlock", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemUsageContext;getStack()Lnet/minecraft/item/ItemStack;", shift = At.Shift.BEFORE), index = 8)
    private Optional<BlockState> phonos$modifyWaxedState(Optional<BlockState> old) {
        var state = cachedUseCtx.getWorld().getBlockState(cachedUseCtx.getBlockPos());
        var block = CopperStateMap.getUnwaxed(state.getBlock());
        if (block.isPresent()) {
            return block.map(b -> b.getStateWithProperties(state));
        }
        return old;
    }
}
