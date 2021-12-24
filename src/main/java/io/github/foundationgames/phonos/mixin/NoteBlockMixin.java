package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.item.NoteBlockTunerItem;
import net.minecraft.block.BlockState;
import net.minecraft.block.NoteBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoteBlock.class)
public class NoteBlockMixin {
    @Inject(method = "onUse", at = @At("HEAD"), cancellable = true)
    public void phonos$cancelUseActionIfTuner(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit, CallbackInfoReturnable<ActionResult> cir) {
        if(player.getStackInHand(hand).getItem() instanceof NoteBlockTunerItem) cir.setReturnValue(ActionResult.PASS);
    }
}
