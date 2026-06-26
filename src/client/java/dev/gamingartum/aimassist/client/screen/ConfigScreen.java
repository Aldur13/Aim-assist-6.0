package dev.gamingartum.aimassist.client.screen;

import dev.gamingartum.aimassist.client.AimAssistState;
import dev.gamingartum.aimassist.client.config.AimAssistConfig;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final AimAssistConfig config;

    public ConfigScreen(Screen parent) {
        super(Component.translatable("screen.aimassist.config"));
        this.parent = parent;
        this.config = AimAssistState.getInstance().getConfig();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y  = this.height / 4;

        // Mace Mode toggle
        addRenderableWidget(Button.builder(maceModeLabel(), btn -> {
            config.maceMode = !config.maceMode;
            btn.setMessage(maceModeLabel());
            config.save();
        }).bounds(cx - 100, y, 200, 20).build());

        // Aim Smoothness slider
        addRenderableWidget(new AbstractSliderButton(cx - 100, y + 28, 200, 20,
                Component.empty(), config.aimSmoothness) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Component.literal(String.format("Aim Smoothness: %.0f%%", value * 100)));
            }

            @Override
            protected void applyValue() {
                config.aimSmoothness = (float) Math.max(0.05, value);
                config.save();
            }
        });

        // Shield Breaker toggle
        addRenderableWidget(Button.builder(shieldBreakerLabel(), btn -> {
            config.shieldBreaker = !config.shieldBreaker;
            btn.setMessage(shieldBreakerLabel());
            config.save();
        }).bounds(cx - 100, y + 56, 200, 20).build());

        // Sneak Behind toggle
        addRenderableWidget(Button.builder(sneakBehindLabel(), btn -> {
            config.sneakBehind = !config.sneakBehind;
            btn.setMessage(sneakBehindLabel());
            config.save();
        }).bounds(cx - 100, y + 84, 200, 20).build());

        // Elytra Predict toggle
        addRenderableWidget(Button.builder(elytraPredictLabel(), btn -> {
            config.elytraPredict = !config.elytraPredict;
            btn.setMessage(elytraPredictLabel());
            config.save();
        }).bounds(cx - 100, y + 112, 200, 20).build());

        // Done
        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"), btn -> onClose()
        ).bounds(cx - 100, y + 140, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.height / 4 - 24, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        config.save();
        this.minecraft.setScreen(parent);
    }

    private Component maceModeLabel() {
        return Component.literal("Mace Mode: " + (config.maceMode ? "§aON" : "§cOFF"));
    }

    private Component shieldBreakerLabel() {
        return Component.literal("Shield Breaker: " + (config.shieldBreaker ? "§aON" : "§cOFF"));
    }

    private Component sneakBehindLabel() {
        return Component.literal("Sneak Behind: " + (config.sneakBehind ? "§aON" : "§cOFF"));
    }

    private Component elytraPredictLabel() {
        return Component.literal("Elytra Predict: " + (config.elytraPredict ? "§aON" : "§cOFF"));
    }
}
