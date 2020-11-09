package io.github.foundationgames.phonos.screen;

import io.github.cottonmc.cotton.gui.client.CottonInventoryScreen;
import net.minecraft.entity.player.PlayerEntity;

public class CustomMusicDiscScreen extends CottonInventoryScreen<CustomMusicDiscGuiDescription> {
    public CustomMusicDiscScreen(CustomMusicDiscGuiDescription description, PlayerEntity player) {
        super(description, player);
    }
}
