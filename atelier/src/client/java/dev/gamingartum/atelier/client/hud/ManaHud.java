package dev.gamingartum.atelier.client.hud;

import dev.gamingartum.atelier.mana.ManaAttachment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;

public final class ManaHud implements HudElement {

    public static final Identifier ID = Identifier.fromNamespaceAndPath("atelier", "mana_bar");

    private static final int BAR_WIDTH = 81;
    private static final int BAR_HEIGHT = 5;
    private static final int BAR_X = 10;
    private static final int BAR_Y_FROM_BOTTOM = 39;

    private static final int COLOR_BG   = 0xFF333366;
    private static final int COLOR_FILL = 0xFF4488FF;
    private static final int COLOR_BORDER = 0xFF8888CC;

    public static void register() {
        HudElementRegistry.attachElementBefore(VanillaHudElements.HOTBAR, ID, new ManaHud());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, DeltaTracker dt) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        int screenH = g.guiHeight();
        int barY = screenH - BAR_Y_FROM_BOTTOM;

        float mana = ManaAttachment.get(mc.player);
        float ratio = Math.max(0, Math.min(1, mana / ManaAttachment.MAX_MANA));
        int filled = (int)(BAR_WIDTH * ratio);

        // Border
        g.fill(BAR_X - 1, barY - 1, BAR_X + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, COLOR_BORDER);
        // Background
        g.fill(BAR_X, barY, BAR_X + BAR_WIDTH, barY + BAR_HEIGHT, COLOR_BG);
        // Fill
        if (filled > 0) {
            g.fill(BAR_X, barY, BAR_X + filled, barY + BAR_HEIGHT, COLOR_FILL);
        }
        // "Mana" label
        g.text(mc.font, "Mana", BAR_X + BAR_WIDTH + 4, barY - 1, 0xFF8888FF);
    }
}
