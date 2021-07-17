package io.github.foundationgames.phonos.screen;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.util.math.MatrixStack;

@FunctionalInterface
public interface ExtendedBackgroundPainter {
    void paintBackground(MatrixStack matrices, int x, int y, int mouseX, int mouseY, WWidget wWidget);
}
