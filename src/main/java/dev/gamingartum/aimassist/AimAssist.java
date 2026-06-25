package dev.gamingartum.aimassist;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AimAssist implements ModInitializer {
    public static final String MOD_ID = "aimassist";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Aim Assist loaded.");
    }
}
