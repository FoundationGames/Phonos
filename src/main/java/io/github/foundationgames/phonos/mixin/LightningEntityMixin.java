package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.util.CopperStateMap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LightningEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import java.util.Iterator;
import java.util.Optional;

@Mixin(LightningEntity.class)
public class LightningEntityMixin {
    @Inject(method = "cleanOxidization", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void phonos$setCustomUnaffectedBlock(World world, BlockPos pos, CallbackInfo ci, BlockState state, BlockPos strikePos) {
        var strikePosState = world.getBlockState(strikePos);
        world.setBlockState(strikePos, CopperStateMap.getOriginalStage(strikePosState.getBlock()).getStateWithProperties(strikePosState));
    }

    @Inject(method = "cleanOxidizationAround(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILEXCEPTION)
    private static void phonos$setCustomUnoxidizedBlock(World world, BlockPos pos, CallbackInfoReturnable<Optional<BlockPos>> cir, Iterator<?> iter, BlockPos cleanPos, BlockState cleanState) {
        CopperStateMap.getDecrease(cleanState.getBlock()).ifPresent(b -> world.setBlockState(cleanPos, b.getStateWithProperties(cleanState)));
    }
}
