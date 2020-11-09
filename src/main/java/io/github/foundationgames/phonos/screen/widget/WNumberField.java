package io.github.foundationgames.phonos.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.util.math.MatrixStack;

import java.util.function.Consumer;
import java.util.function.Function;

public class WNumberField extends WWidget {
    private PaintFunction paint = (numberField, matrices, x, y, mx, my) -> {};
    private Consumer<WNumberField> valueChanged = (integer -> {});

    public final int minimum;
    public final int maximum;
    private final int increment;
    public int value;

    public WNumberField(int min, int max, int defaultInt, int increment, int width, int height) {
        minimum = min;
        maximum = max;
        value = defaultInt;
        this.width = width;
        this.height = height;
        this.increment = increment;
    }

    public void setPainter(PaintFunction f) {
        this.paint = f;
    }

    public void whenValueChanged(Consumer<WNumberField> process) {this.valueChanged = process;}

    @Override
    public void onMouseScroll(int x, int y, double amount) {
        super.onMouseScroll(x, y, amount);
        if(isWithinBounds(x, y)) {
            value += increment * (amount > 0 ? 1 : -1);
            value = Math.min(Math.max(minimum, value), maximum);
            valueChanged.accept(this);
        }
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);
        this.paint.apply(this, matrices, x, y, mouseX, mouseY);
    }

    @FunctionalInterface
    public interface PaintFunction {
        void apply(WNumberField numberField, MatrixStack matrices, int x, int y, int mouseX, int mouseY);
    }
}
