package io.github.foundationgames.phonos.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.foundationgames.phonos.screen.ExtendedBackgroundPainter;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

public class WBasicButton extends WWidget {
    private ClickFunction clicked = (button, x, y, mbutton) -> {};
    @Environment(EnvType.CLIENT)
    private ExtendedBackgroundPainter backgroundPainter = null;
    private ScrollFunction scroll = ((button, x, y, amount) -> InputResult.IGNORED);
    public boolean enabled = true;

    public WBasicButton(int width, int height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public void paint(MatrixStack matrices, int x, int y, int mouseX, int mouseY) {
        super.paint(matrices, x, y, mouseX, mouseY);
        if (this.backgroundPainter != null) {
            this.backgroundPainter.paintBackground(matrices, x, y, mouseX, mouseY, this);
        }
    }

    public void setWhenClicked(ClickFunction f) {
        this.clicked = f;
    }

    @Environment(EnvType.CLIENT)
    public ExtendedBackgroundPainter getBackgroundPainter() {
        return this.backgroundPainter;
    }

    @Environment(EnvType.CLIENT)
    public void setBackgroundPainter(ExtendedBackgroundPainter painter) {
        this.backgroundPainter = painter;
    }

    public void setWhenScrolledOver(ScrollFunction f) {
        this.scroll = f;
    }

    @Override
    public InputResult onMouseDown(int x, int y, int button) {
        if (isWithinBounds(x, y) && enabled) this.clicked.apply(this, x, y, button);
        return super.onMouseDown(x, y, button);
    }

    @Override
    public InputResult onMouseScroll(int x, int y, double amount) {
        super.onMouseScroll(x, y, amount);
        if(isWithinBounds(x, y)) {
            return scroll.apply(this, x, y, amount);
        }
        return super.onMouseScroll(x, y, amount);
    }

    @FunctionalInterface
    public interface ClickFunction {
        void apply(WBasicButton button, int x, int y, int mouseButton);
    }

    @FunctionalInterface
    public interface ScrollFunction {
        InputResult apply(WBasicButton button, int x, int y, double amount);
    }

    @FunctionalInterface
    public interface PaintFunction {
        void apply(WBasicButton button, MatrixStack matrices, int x, int y, int mouseX, int mouseY);
    }
}
