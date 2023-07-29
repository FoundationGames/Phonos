package io.github.foundationgames.phonos.util.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.foundationgames.phonos.config.PhonosClientConfig;
import io.github.foundationgames.phonos.config.PhonosClientConfigScreen;

public class PhonosModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return parent -> PhonosClientConfigScreen.create(PhonosClientConfig.get(), parent);
    }
}