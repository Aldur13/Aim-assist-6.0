package dev.gamingartum.atelier.client.screen;

import dev.gamingartum.atelier.spell.SpellGlyphs;
import dev.gamingartum.atelier.spell.SpellGlyphs.SpellTemplate;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class HandbookScreen extends Screen {

    private static final int PAGE_WIDTH = 280;
    private static final int PAGE_HEIGHT = 200;
    private static final int GLYPH_SIZE = 100;

    private int pageX, pageY;
    private int selectedSpell = 0;

    public HandbookScreen() {
        super(Component.translatable("screen.atelier.handbook"));
    }

    @Override
    protected void init() {
        pageX = (this.width - PAGE_WIDTH) / 2;
        pageY = (this.height - PAGE_HEIGHT) / 2;

        // Prev / Next
        this.addRenderableWidget(
            Button.builder(Component.literal("<"), btn -> {
                selectedSpell = (selectedSpell - 1 + SpellGlyphs.ALL.length) % SpellGlyphs.ALL.length;
            }).bounds(pageX, pageY + PAGE_HEIGHT - 24, 20, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(Component.literal(">"), btn -> {
                selectedSpell = (selectedSpell + 1) % SpellGlyphs.ALL.length;
            }).bounds(pageX + PAGE_WIDTH - 20, pageY + PAGE_HEIGHT - 24, 20, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(Component.literal("✕"), btn -> this.onClose())
                .bounds(pageX + PAGE_WIDTH - 20, pageY, 20, 20).build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Parchment background
        g.fill(pageX, pageY, pageX + PAGE_WIDTH, pageY + PAGE_HEIGHT, 0xFFF5E6C8);
        g.fill(pageX - 1, pageY - 1, pageX + PAGE_WIDTH + 1, pageY + PAGE_HEIGHT + 1, 0xFF8B6914);

        super.extractRenderState(g, mouseX, mouseY, delta);

        SpellTemplate tmpl = SpellGlyphs.ALL[selectedSpell];

        // Spell name header
        g.centeredText(this.font,
            Component.translatable(tmpl.nameKey()),
            pageX + PAGE_WIDTH / 2, pageY + 10, tmpl.color());

        // Divider
        g.fill(pageX + 8, pageY + 24, pageX + PAGE_WIDTH - 8, pageY + 25, 0xFF8B6914);

        // Glyph rendering (left column)
        int glyphX = pageX + 20;
        int glyphY = pageY + 35;

        // Glyph background
        g.fill(glyphX - 2, glyphY - 2, glyphX + GLYPH_SIZE + 2, glyphY + GLYPH_SIZE + 2, 0xFF222200);
        g.fill(glyphX, glyphY, glyphX + GLYPH_SIZE, glyphY + GLYPH_SIZE, 0xFF1A1A00);

        // Template inner segments
        for (float[] seg : tmpl.innerSegments()) {
            drawLine(g,
                glyphX + (int)(seg[0] * GLYPH_SIZE),
                glyphY + (int)(seg[1] * GLYPH_SIZE),
                glyphX + (int)(seg[2] * GLYPH_SIZE),
                glyphY + (int)(seg[3] * GLYPH_SIZE),
                tmpl.color() | 0xFF000000, 1);
        }
        // Ring
        int ringColor = (tmpl.color() & 0x00FFFFFF) | 0x88000000;
        for (float[] seg : SpellGlyphs.RING) {
            drawLine(g,
                glyphX + (int)(seg[0] * GLYPH_SIZE),
                glyphY + (int)(seg[1] * GLYPH_SIZE),
                glyphX + (int)(seg[2] * GLYPH_SIZE),
                glyphY + (int)(seg[3] * GLYPH_SIZE),
                ringColor | 0xFF000000, 1);
        }

        // Description text (right column)
        int textX = glyphX + GLYPH_SIZE + 16;
        int textY = glyphY;
        int textW = PAGE_WIDTH - GLYPH_SIZE - 50;

        g.textWithWordWrap(this.font,
            Component.translatable(tmpl.nameKey() + ".desc"),
            textX, textY, textW, 0xFF4A3000);

        // Page counter
        String pageLabel = (selectedSpell + 1) + " / " + SpellGlyphs.ALL.length;
        g.centeredText(this.font, Component.literal(pageLabel),
            pageX + PAGE_WIDTH / 2, pageY + PAGE_HEIGHT - 18, 0xFF8B6914);

        // Hint
        g.centeredText(this.font,
            Component.translatable("screen.atelier.handbook.hint"),
            pageX + PAGE_WIDTH / 2, pageY + PAGE_HEIGHT - 6, 0xAA8B6914);
    }

    private void drawLine(GuiGraphicsExtractor g, int x1, int y1, int x2, int y2, int color, int r) {
        int dx = Math.abs(x2 - x1), sx = x1 < x2 ? 1 : -1;
        int dy = -Math.abs(y2 - y1), sy = y1 < y2 ? 1 : -1;
        int err = dx + dy;
        while (true) {
            g.fill(x1 - r, y1 - r, x1 + r, y1 + r, color);
            if (x1 == x2 && y1 == y2) break;
            int e2 = 2 * err;
            if (e2 >= dy) { err += dy; x1 += sx; }
            if (e2 <= dx) { err += dx; y1 += sy; }
        }
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
