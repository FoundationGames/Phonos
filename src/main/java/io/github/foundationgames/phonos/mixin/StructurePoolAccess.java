package io.github.foundationgames.phonos.mixin;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.structure.pool.StructurePool;
import net.minecraft.structure.pool.StructurePoolElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(StructurePool.class)
public interface StructurePoolAccess {
    @Accessor(value = "elements")
    ObjectArrayList<StructurePoolElement> phonos$getElements();

    @Accessor(value = "elementCounts")
    List<Pair<StructurePoolElement, Integer>> phonos$getElementCounts();
}
