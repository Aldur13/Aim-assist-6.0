package dev.gamingartum.atelier.client.screen;

import dev.gamingartum.atelier.network.ScribeSpellPayload;
import dev.gamingartum.atelier.spell.GlyphScorer;
import dev.gamingartum.atelier.spell.SpellGlyphs;
import dev.gamingartum.atelier.spell.SpellGlyphs.SpellTemplate;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

public class DrawingScreen extends Screen {

    private static final int CANVAS_SIZE = 200;
    private static final int TEMPLATE_COLOR = 0x44FFFFFF;
    private static final int TEMPLATE_RING_COLOR = 0x33FFFFFF;
    private static final int STROKE_COLOR = 0xFF1A1AFF;
    private static final int CANVAS_BG = 0xFFEEEECC;
    private static final int CANVAS_BORDER = 0xFF222222;
    private static final int PANEL_BG = 0xCC000000;

    private int canvasX, canvasY;
    private int selectedSpell = 0;

    private final List<List<float[]>> strokes = new ArrayList<>();
    private List<float[]> currentStroke = null;
    private boolean isDrawing = false;

    private String feedbackMessage = null;
    private int feedbackTimer = 0;

    public DrawingScreen() {
        super(Component.translatable("screen.atelier.drawing"));
    }

    @Override
    protected void init() {
        canvasX = (this.width - CANVAS_SIZE) / 2;
        canvasY = 50;

        int btnY = canvasY + CANVAS_SIZE + 10;

        // Spell selector arrows
        this.addRenderableWidget(
            Button.builder(Component.literal("<"), btn -> {
                selectedSpell = (selectedSpell - 1 + SpellGlyphs.ALL.length) % SpellGlyphs.ALL.length;
                strokes.clear();
            }).bounds(canvasX, 20, 20, 20).build()
        );
        this.addRenderableWidget(
            Button.builder(Component.literal(">"), btn -> {
                selectedSpell = (selectedSpell + 1) % SpellGlyphs.ALL.length;
                strokes.clear();
            }).bounds(canvasX + CANVAS_SIZE - 20, 20, 20, 20).build()
        );

        // Confirm
        this.addRenderableWidget(
            Button.builder(Component.translatable("screen.atelier.confirm"), btn -> onConfirm())
                .bounds(canvasX, btnY, 60, 20).build()
        );
        // Clear
        this.addRenderableWidget(
            Button.builder(Component.translatable("screen.atelier.clear"), btn -> strokes.clear())
                .bounds(canvasX + 65, btnY, 60, 20).build()
        );
        // Undo
        this.addRenderableWidget(
            Button.builder(Component.translatable("screen.atelier.undo"), btn -> {
                if (!strokes.isEmpty()) strokes.remove(strokes.size() - 1);
            }).bounds(canvasX + 130, btnY, 60, 20).build()
        );
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        // Dim background
        g.fill(0, 0, this.width, this.height, PANEL_BG);

        super.extractRenderState(g, mouseX, mouseY, delta);

        SpellTemplate tmpl = SpellGlyphs.ALL[selectedSpell];

        // Spell name (centered above canvas)
        g.centeredText(this.font,
            Component.translatable(tmpl.nameKey()),
            this.width / 2, canvasY - 18, tmpl.color());

        // Canvas background + border
        g.fill(canvasX - 1, canvasY - 1, canvasX + CANVAS_SIZE + 1, canvasY + CANVAS_SIZE + 1, CANVAS_BORDER);
        g.fill(canvasX, canvasY, canvasX + CANVAS_SIZE, canvasY + CANVAS_SIZE, CANVAS_BG);

        // Template overlay (inner segments)
        for (float[] seg : tmpl.innerSegments()) {
            drawLine(g,
                canvasX + (int)(seg[0] * CANVAS_SIZE),
                canvasY + (int)(seg[1] * CANVAS_SIZE),
                canvasX + (int)(seg[2] * CANVAS_SIZE),
                canvasY + (int)(seg[3] * CANVAS_SIZE),
                TEMPLATE_COLOR, 1);
        }
        // Template ring
        for (float[] seg : SpellGlyphs.RING) {
            drawLine(g,
                canvasX + (int)(seg[0] * CANVAS_SIZE),
                canvasY + (int)(seg[1] * CANVAS_SIZE),
                canvasX + (int)(seg[2] * CANVAS_SIZE),
                canvasY + (int)(seg[3] * CANVAS_SIZE),
                TEMPLATE_RING_COLOR, 1);
        }

        // Player strokes (confirmed)
        for (List<float[]> stroke : strokes) {
            renderStroke(g, stroke, STROKE_COLOR);
        }
        // Current stroke being drawn
        if (currentStroke != null) {
            renderStroke(g, currentStroke, STROKE_COLOR);
        }

        // Feedback / hint text
        if (feedbackMessage != null && feedbackTimer > 0) {
            g.centeredText(this.font, Component.literal(feedbackMessage),
                this.width / 2, canvasY + CANVAS_SIZE + 35, 0xFFFF4444);
        } else {
            g.centeredText(this.font,
                Component.translatable("screen.atelier.hint"),
                this.width / 2, canvasY + CANVAS_SIZE + 35, 0xFFAAAAAA);
        }
    }

    private void renderStroke(GuiGraphicsExtractor g, List<float[]> stroke, int color) {
        for (int i = 1; i < stroke.size(); i++) {
            float[] p0 = stroke.get(i - 1), p1 = stroke.get(i);
            drawLine(g,
                canvasX + (int)(p0[0] * CANVAS_SIZE),
                canvasY + (int)(p0[1] * CANVAS_SIZE),
                canvasX + (int)(p1[0] * CANVAS_SIZE),
                canvasY + (int)(p1[1] * CANVAS_SIZE),
                color, 2);
        }
    }

    // Bresenham line with a radius-r square brush via fill()
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
    public boolean mouseClicked(MouseButtonEvent event, boolean pConsumed) {
        if (event.button() == 0 && isOnCanvas((int)event.x(), (int)event.y())) {
            isDrawing = true;
            currentStroke = new ArrayList<>();
            currentStroke.add(toCanvasCoords(event.x(), event.y()));
            return true;
        }
        return super.mouseClicked(event, pConsumed);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (isDrawing && event.button() == 0 && currentStroke != null) {
            if (isOnCanvas((int)event.x(), (int)event.y())) {
                currentStroke.add(toCanvasCoords(event.x(), event.y()));
            }
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (isDrawing && event.button() == 0 && currentStroke != null) {
            if (currentStroke.size() >= 2) {
                strokes.add(currentStroke);
            }
            currentStroke = null;
            isDrawing = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    private boolean isOnCanvas(int mx, int my) {
        return mx >= canvasX && mx < canvasX + CANVAS_SIZE
            && my >= canvasY && my < canvasY + CANVAS_SIZE;
    }

    private float[] toCanvasCoords(double mx, double my) {
        return new float[]{
            (float)((mx - canvasX) / CANVAS_SIZE),
            (float)((my - canvasY) / CANVAS_SIZE)
        };
    }

    private void onConfirm() {
        GlyphScorer.ScoreResult result = GlyphScorer.score(strokes, SpellGlyphs.ALL[selectedSpell]);
        if (!result.isValid()) {
            feedbackMessage = "The ring is not closed — the glyph fails!";
            feedbackTimer = 80;
            return;
        }
        SpellTemplate tmpl = SpellGlyphs.ALL[selectedSpell];
        ClientPlayNetworking.send(new ScribeSpellPayload(
            Identifier.parse(tmpl.spellId()),
            result.power()
        ));
        this.onClose();
    }

    @Override
    public void tick() {
        super.tick();
        if (feedbackTimer > 0) feedbackTimer--;
        else feedbackMessage = null;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return true;
    }
}
