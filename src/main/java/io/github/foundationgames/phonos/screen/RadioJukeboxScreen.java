package io.github.foundationgames.phonos.screen;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public class RadioJukeboxScreen extends CottonInventoryScreen<RadioJukeboxGuiDescription> {
    public RadioJukeboxScreen(RadioJukeboxGuiDescription description, PlayerEntity player) {
        super(description, player);
    }
}
