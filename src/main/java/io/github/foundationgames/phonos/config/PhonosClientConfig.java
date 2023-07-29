package io.github.foundationgames.phonos.config;

import com.google.gson.Gson;
import io.github.foundationgames.phonos.Phonos;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class PhonosClientConfig {
    private static PhonosClientConfig config = null;

    public double phonosMasterVolume = 1;
    public double streamVolume = 1;

    public boolean cableLODs = true;
    public double cableLODNearDetail = 1;
    public double cableLODFarDetail = 0.25;

    public PhonosClientConfig() {
    }

    public PhonosClientConfig copyTo(PhonosClientConfig copy) {
        for (var f : PhonosClientConfig.class.getDeclaredFields()) {
            try {
                f.set(copy, f.get(this));
            } catch (IllegalAccessException ignored) {}
        }

        return copy;
    }

    public static PhonosClientConfig get() {
        if (config == null) {
            config = new PhonosClientConfig();

            try {
                config.load();
            } catch (IOException ex) {
                Phonos.LOG.error("Error loading Phonos client config!", ex);
            }
        }

        return config;
    }

    private static Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("phonos.json");
    }

    public void load() throws IOException {
        var path = configPath();

        try (var in = Files.newBufferedReader(path)) {
            var fileCfg = new Gson().fromJson(in, PhonosClientConfig.class);
            fileCfg.copyTo(this);
        }
    }

    public void save() throws IOException {
        var path = configPath();

        var gson = new Gson();
        try (var writer = gson.newJsonWriter(Files.newBufferedWriter(path))) {
            writer.setIndent("    ");

            gson.toJson(gson.toJsonTree(this, PhonosClientConfig.class), writer);
        }
    }
}
