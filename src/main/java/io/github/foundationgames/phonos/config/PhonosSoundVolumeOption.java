package io.github.foundationgames.phonos.config;

import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

import java.util.function.DoubleConsumer;

public class PhonosSoundVolumeOption {
    public static SimpleOption<Integer> create(String key, double currentValue, DoubleConsumer setter) {
        return new SimpleOption<>("text.config.phonos.option.vanilla." + key,
                SimpleOption.emptyTooltip(),
                (optionText, value) -> Text.translatable("options.percent_value", optionText, value),
                new SimpleOption.ValidatingIntSliderCallbacks(0, 100),
                (int)(currentValue * 100), val -> setter.accept(val * 0.01D));
    }
}
