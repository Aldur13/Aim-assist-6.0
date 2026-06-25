package dev.gamingartum.aimassist.client;

import dev.gamingartum.aimassist.client.feature.AimAssistFeature;
import dev.gamingartum.aimassist.client.feature.MaceHitFeature;
import dev.gamingartum.aimassist.client.screen.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AimAssistClient implements ClientModInitializer {

    public static KeyBinding toggleKey;
    public static KeyBinding configKey;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aimassist.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.aimassist"
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.aimassist.config",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_SEMICOLON,
                "category.aimassist"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Toggle aim assist with J
            while (toggleKey.wasPressed()) {
                AimAssistState.getInstance().toggle();
                if (client.player != null) {
                    boolean on = AimAssistState.getInstance().isEnabled();
                    client.player.sendMessage(
                            Text.literal("Aim Assist: " + (on ? "§aON" : "§cOFF")),
                            true
                    );
                }
            }

            // Open config screen with ;
            while (configKey.wasPressed()) {
                if (client.currentScreen == null) {
                    client.setScreen(new ConfigScreen(null));
                }
            }

            AimAssistFeature.tick(client);
            MaceHitFeature.tick(client);
        });
    }
}
