package dev.gamingartum.aimassist.client;

import dev.gamingartum.aimassist.client.config.AimAssistConfig;
import net.minecraft.world.entity.player.Player;

public class AimAssistState {
    private static final AimAssistState INSTANCE = new AimAssistState();

    private boolean enabled = false;
    private Player currentTarget = null;
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
        if (!enabled) currentTarget = null;
    }

    public Player getCurrentTarget() {
        return currentTarget;
    }

    public void setCurrentTarget(Player target) {
        this.currentTarget = target;
    }

    public AimAssistConfig getConfig() {
        return config;
    }
}
