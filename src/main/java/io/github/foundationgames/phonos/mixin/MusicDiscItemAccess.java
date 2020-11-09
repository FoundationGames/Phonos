package io.github.foundationgames.phonos.mixin;

import net.minecraft.item.MusicDiscItem;
import net.minecraft.sound.SoundEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(MusicDiscItem.class)
public interface MusicDiscItemAccess {
    @Accessor("sound")
    SoundEvent getSoundEvent();
}
