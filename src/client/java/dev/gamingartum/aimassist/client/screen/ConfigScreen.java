package dev.gamingartum.aimassist.client.screen;

import dev.gamingartum.aimassist.client.AimAssistState;
import dev.gamingartum.aimassist.client.config.AimAssistConfig;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.SliderWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class ConfigScreen extends Screen {

    private final Screen parent;
    private final AimAssistConfig config;

    public ConfigScreen(Screen parent) {
        super(Text.translatable("screen.aimassist.config"));
        this.parent = parent;
        this.config = AimAssistState.getInstance().getConfig();
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int y  = this.height / 4;

        // Mace Mode toggle
        addDrawableChild(ButtonWidget.builder(
                maceModeLabel(),
                btn -> {
                    config.maceMode = !config.maceMode;
                    btn.setMessage(maceModeLabel());
                    config.save();
                }
        ).dimensions(cx - 100, y, 200, 20).build());

        // Aim Smoothness slider (0.05 = very smooth, 1.0 = instant snap)
        addDrawableChild(new SliderWidget(cx - 100, y + 28, 200, 20,
                Text.empty(), config.aimSmoothness) {
            {
                updateMessage();
            }

            @Override
            protected void updateMessage() {
                setMessage(Text.literal(
                        String.format("Aim Smoothness: %.0f%%", value * 100)));
            }

            @Override
            protected void applyValue() {
                // Prevent value from going below 5% so snapping never fully disappears
                config.aimSmoothness = (float) Math.max(0.05, value);
                config.save();
            }
        });

        // Done button
        addDrawableChild(ButtonWidget.builder(ScreenTexts.DONE,
                btn -> close()
        ).dimensions(cx - 100, y + 80, 200, 20).build());
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        renderBackground(ctx, mouseX, mouseY, delta);
        ctx.drawCenteredTextWithShadow(textRenderer, title,
                this.width / 2, this.height / 4 - 24, 0xFFFFFF);
        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public void close() {
        config.save();
        assert this.client != null;
        this.client.setScreen(parent);
    }

    private Text maceModeLabel() {
        return Text.literal("Mace Mode: " + (config.maceMode ? "§aON" : "§cOFF"));
    }
}
