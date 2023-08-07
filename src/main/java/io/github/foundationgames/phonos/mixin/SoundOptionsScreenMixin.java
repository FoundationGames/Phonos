package io.github.foundationgames.phonos.mixin;

import io.github.foundationgames.phonos.Phonos;
import io.github.foundationgames.phonos.config.PhonosClientConfig;
import io.github.foundationgames.phonos.config.PhonosSoundVolumeOption;
import net.minecraft.client.gui.screen.option.SoundOptionsScreen;
import net.minecraft.client.option.SimpleOption;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import java.io.IOException;

@Mixin(SoundOptionsScreen.class)
public class SoundOptionsScreenMixin {
    @ModifyArg(
            method = "init",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/gui/widget/OptionListWidget;addAll([Lnet/minecraft/client/option/SimpleOption;)V",
                    ordinal = 0
            ),
            index = 0
    )
    private SimpleOption<?>[] phonos$addVolumeSliders(SimpleOption<?>[] old) {
        var options = new SimpleOption<?>[old.length + 2];
        System.arraycopy(old, 0, options, 0, old.length);

        var config = PhonosClientConfig.get();

        options[options.length - 2] = PhonosSoundVolumeOption.create(
                "phonosMasterVolume", config.phonosMasterVolume,
                val -> {
                    config.phonosMasterVolume = val;
                    try {config.save();}
                    catch (IOException e) {Phonos.LOG.error("Error saving config from sounds menu", e);}
                });
        options[options.length - 1] = PhonosSoundVolumeOption.create(
                "streamVolume", config.streamVolume,
                val -> {
                    config.streamVolume = val;
                    try {config.save();}
                    catch (IOException e) {Phonos.LOG.error("Error saving config from sounds menu", e);}
                });

        return options;
    }
}
