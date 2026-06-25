package dev.gamingartum.aimassist.client.screen;

import dev.gamingartum.aimassist.client.AimAssistState;
import dev.gamingartum.aimassist.client.config.AimAssistConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

        // Done
        addRenderableWidget(Button.builder(
                Component.translatable("gui.done"), btn -> onClose()
        ).bounds(cx - 100, y + 80, 200, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(g, mouseX, mouseY, partialTick);
        g.centeredText(this.font, this.title, this.width / 2, this.height / 4 - 24, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        config.save();
        this.minecraft.gui.setScreen(parent);
    }

    private Component maceModeLabel() {
        return Component.literal("Mace Mode: " + (config.maceMode ? "§aON" : "§cOFF"));
    }
}
