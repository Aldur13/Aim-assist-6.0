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
     * How fast the aim pulls toward the target per tick.
     * 1.0 = instant snap. 0.05 = very slow pull. Default: 0.15 (smooth).
     */
    public float aimSmoothness = 0.15f;

    /** Auto-switch to axe and attack when target is blocking with a shield. */
    public boolean shieldBreaker = true;

    /** Strafe around the target's back when they are blocking to bypass the shield. */
    public boolean sneakBehind = true;

    /** Predict elytra movement and lead the aim when the target is gliding. */
    public boolean elytraPredict = true;

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
