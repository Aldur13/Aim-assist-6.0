package dev.gamingartum.aimassist.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.gamingartum.aimassist.client.feature.AimAssistFeature;
import dev.gamingartum.aimassist.client.feature.MaceHitFeature;
import dev.gamingartum.aimassist.client.screen.ConfigScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class AimAssistClient implements ClientModInitializer {

    public static KeyMapping toggleKey;
    public static KeyMapping configKey;

    private static final KeyMapping.Category CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath("aimassist", "category"));

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.aimassist.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                CATEGORY
        ));

        configKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.aimassist.config",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_SEMICOLON,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            while (toggleKey.consumeClick()) {
                AimAssistState.getInstance().toggle();
                if (minecraft.player != null) {
                    boolean on = AimAssistState.getInstance().isEnabled();
                    minecraft.player.displayClientMessage(
                            Component.literal("Aim Assist: " + (on ? "§aON" : "§cOFF")), true
                    );
                }
            }

            while (configKey.consumeClick()) {
                if (minecraft.screen == null) {
                    minecraft.setScreen(new ConfigScreen(null));
                }
            }

            AimAssistFeature.tick(minecraft);
            MaceHitFeature.tick(minecraft);
        });
    }
}
