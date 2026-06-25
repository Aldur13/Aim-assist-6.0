package dev.gamingartum.aimassist.client;

import dev.gamingartum.aimassist.client.config.AimAssistConfig;
import net.minecraft.entity.player.PlayerEntity;

public class AimAssistState {
    private static final AimAssistState INSTANCE = new AimAssistState();

    private boolean enabled = false;
    private PlayerEntity currentTarget = null;
    private final AimAssistConfig config;

    private AimAssistState() {
        this.config = AimAssistConfig.load();
    }

    public static AimAssistState getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void toggle() {
        enabled = !enabled;
        if (!enabled) {
            currentTarget = null;
        }
    }

    public PlayerEntity getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(PlayerEntity target) {
        this.currentTarget = target;
    }

    public AimAssistConfig getConfig() {
        return config;
    }
}
