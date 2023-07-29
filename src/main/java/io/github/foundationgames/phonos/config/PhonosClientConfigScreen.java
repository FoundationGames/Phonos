package io.github.foundationgames.phonos.config;

import io.github.foundationgames.phonos.Phonos;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.OptionListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class PhonosClientConfigScreen extends Screen {
    private static final Text TITLE = Text.translatable("text.config.phonos.title");

    private final Screen parent;
    private final List<SimpleOption<?>> options = new ArrayList<>();
    private final Runnable save;

    public PhonosClientConfigScreen(Screen parent, Runnable save) {
        super(TITLE);

        this.parent = parent;
        this.save = save;
    }

    public static PhonosClientConfigScreen create(PhonosClientConfig config, Screen parent) {
        var copy = config.copyTo(new PhonosClientConfig());

        var screen = new PhonosClientConfigScreen(parent, () -> {
            copy.copyTo(config);
            try {
                config.save();
            } catch (IOException e) {
                Phonos.LOG.error("Could not save config!", e);
            }
        });

        screen.addPercentage("phonosMasterVolume", val -> copy.phonosMasterVolume = val, copy.phonosMasterVolume);
        screen.addPercentage("streamVolume", val -> copy.streamVolume = val, copy.streamVolume);
        screen.addBoolean("cableLODs", val -> copy.cableLODs = val, copy.cableLODs);
        screen.addPercentage("cableLODNearDetail", val -> copy.cableLODNearDetail = val, copy.cableLODNearDetail);
        screen.addPercentage("cableLODFarDetail", val -> copy.cableLODFarDetail = val, copy.cableLODFarDetail);

        return screen;
    }

    public void addBoolean(String key, BooleanConsumer setter, boolean currentValue) {
        this.options.add(SimpleOption.ofBoolean("text.config.phonos.option." + key, currentValue, setter));
    }

    public void addPercentage(String key, DoubleConsumer setter, double currentValue) {
        addIntRange(key, val -> setter.accept(val * 0.01D), (int)(currentValue * 100), 0, 100);
    }

    public void addIntRange(String key, IntConsumer setter, int currentValue, int min, int max) {
        this.options.add(new SimpleOption<>("text.config.phonos.option." + key,
                SimpleOption.emptyTooltip(),
                (optionText, value) -> GameOptions.getGenericValueText(optionText, Text.literal(value.toString())),
                new SimpleOption.ValidatingIntSliderCallbacks(min, max),
                currentValue, setter::accept)
        );
    }

    public void close() {
        this.client.setScreen(this.parent);
    }

    @Override
    protected void init() {
        var buttons = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        for (SimpleOption<?> option : this.options) {
            buttons.addSingleOptionEntry(option);
        }
        this.addDrawableChild(buttons);

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            this.save.run();
            this.close();
        }).dimensions(this.width / 2 + 2, this.height - 27, 150, 20).build());

        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.CANCEL, (button) -> this.close())
                .dimensions(this.width / 2 - 152, this.height - 27, 150, 20).build());
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 13, 0xFFFFFF);
    }
}
