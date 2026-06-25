package dev.gamingartum.aimassist.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AimAssistConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("aimassist.json");

    /** Whether mace auto-hit is active. Requires: holding mace + falling + aim assist on. */
    public boolean maceMode = true;

    /**
     * How fast the aim snaps to the target.
     * 1.0 = instant lock. 0.1 = very smooth pull. Default: 1.0.
     */
    public float aimSmoothness = 1.0f;

    public static AimAssistConfig load() {
        if (Files.exists(CONFIG_PATH)) {
            try {
                String json = Files.readString(CONFIG_PATH);
                AimAssistConfig cfg = GSON.fromJson(json, AimAssistConfig.class);
                return cfg != null ? cfg : new AimAssistConfig();
            } catch (IOException e) {
                return new AimAssistConfig();
            }
        }
        AimAssistConfig cfg = new AimAssistConfig();
        cfg.save();
        return cfg;
    }

    public void save() {
        try {
            Files.writeString(CONFIG_PATH, GSON.toJson(this));
        } catch (IOException ignored) {
        }
    }
}
