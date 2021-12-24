package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.util.PhonosUtil;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePools;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(StructurePools.class)
public class StructurePoolsMixin {
    @Inject(method = "register", at = @At("HEAD"), cancellable = true)
    private static void phonos$addVillageStructures(StructurePool pool, CallbackInfoReturnable<StructurePool> cir) {
        PhonosUtil.tryAddElementToPool(new Identifier("village/plains/houses"), pool, "phonos:village/plains/houses/plains_music_stage", StructurePool.Projection.RIGID, 4);
        PhonosUtil.tryAddElementToPool(new Identifier("village/desert/houses"), pool, "phonos:village/desert/houses/desert_music_stage", StructurePool.Projection.RIGID, 17);
        PhonosUtil.tryAddElementToPool(new Identifier("village/savanna/houses"), pool, "phonos:village/savanna/houses/savanna_music_stage", StructurePool.Projection.RIGID, 4);
        PhonosUtil.tryAddElementToPool(new Identifier("village/taiga/houses"), pool, "phonos:village/taiga/houses/taiga_music_stage", StructurePool.Projection.RIGID, 4);
        PhonosUtil.tryAddElementToPool(new Identifier("village/snowy/houses"), pool, "phonos:village/snowy/houses/snowy_music_stage", StructurePool.Projection.RIGID, 4);

        if(BuiltinRegistries.STRUCTURE_POOL.getId(pool) == null) {
            cir.setReturnValue(BuiltinRegistries.add(BuiltinRegistries.STRUCTURE_POOL, pool.getId(), pool));
        }
    }
}
