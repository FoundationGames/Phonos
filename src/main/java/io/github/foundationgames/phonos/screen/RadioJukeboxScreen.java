package io.github.foundationgames.phonos.screen;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;

public class RadioJukeboxScreen extends CottonInventoryScreen<RadioJukeboxGuiDescription> {
    public RadioJukeboxScreen(RadioJukeboxGuiDescription description, PlayerEntity player) {
        super(description, player);
    }
}
