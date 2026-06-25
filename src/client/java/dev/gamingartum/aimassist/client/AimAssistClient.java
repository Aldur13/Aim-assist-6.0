package dev.gamingartum.aimassist.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.gamingartum.aimassist.client.feature.AimAssistFeature;
import dev.gamingartum.aimassist.client.feature.MaceHitFeature;
import dev.gamingartum.aimassist.client.screen.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class AimAssistClient implements ClientModInitializer {

    public static KeyMapping toggleKey;
    public static KeyMapping configKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.aimassist.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                KeyMapping.Category.MISC
        ));

        configKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.aimassist.config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_SEMICOLON,
                KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            while (toggleKey.consumeClick()) {
                AimAssistState.getInstance().toggle();
                if (minecraft.player != null) {
                    boolean on = AimAssistState.getInstance().isEnabled();
                    minecraft.player.sendOverlayMessage(
                            Component.literal("Aim Assist: " + (on ? "§aON" : "§cOFF"))
                    );
                }
            }

            while (configKey.consumeClick()) {
                if (minecraft.gui.screen() == null) {
                    minecraft.gui.setScreen(new ConfigScreen(null));
                }
            }

            AimAssistFeature.tick(minecraft);
            MaceHitFeature.tick(minecraft);
        });
    }
}
