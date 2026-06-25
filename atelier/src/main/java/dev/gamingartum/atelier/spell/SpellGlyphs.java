package dev.gamingartum.atelier.spell;

import java.util.ArrayList;
import java.util.List;

public final class SpellGlyphs {

    private static final int RING_SEGMENTS = 24;
    private static final float RING_RADIUS = 0.42f;
    private static final float CX = 0.5f, CY = 0.5f;

    public static final float[][] RING = buildRing();

    private static float[][] buildRing() {
        float[][] segs = new float[RING_SEGMENTS][4];
        for (int i = 0; i < RING_SEGMENTS; i++) {
            double a1 = 2 * Math.PI * i / RING_SEGMENTS;
            double a2 = 2 * Math.PI * (i + 1) / RING_SEGMENTS;
            segs[i] = new float[]{
                CX + RING_RADIUS * (float) Math.cos(a1),
                CY + RING_RADIUS * (float) Math.sin(a1),
                CX + RING_RADIUS * (float) Math.cos(a2),
                CY + RING_RADIUS * (float) Math.sin(a2)
            };
        }
        return segs;
    }

    public record SpellTemplate(
        String spellId,
        String nameKey,
        float[][] innerSegments,
        int color
    ) {
        public List<float[]> allSegments() {
            List<float[]> all = new ArrayList<>();
            for (float[] s : innerSegments) all.add(s);
            for (float[] s : RING) all.add(s);
            return all;
        }
    }

    // ── Firebolt (fire) — upward triangle ─────────────────────────────────
    public static final SpellTemplate FIREBOLT = new SpellTemplate(
        "atelier:firebolt", "spell.atelier.firebolt",
        new float[][]{
            {0.50f, 0.12f, 0.85f, 0.82f},
            {0.85f, 0.82f, 0.15f, 0.82f},
            {0.15f, 0.82f, 0.50f, 0.12f}
        },
        0xFFFF5500
    );

    // ── Gust (wind) — Y-fork from center ──────────────────────────────────
    public static final SpellTemplate GUST = new SpellTemplate(
        "atelier:gust", "spell.atelier.gust",
        new float[][]{
            // stem up from center
            {0.50f, 0.55f, 0.50f, 0.12f},
            // two branches lower-left and lower-right
            {0.50f, 0.55f, 0.18f, 0.85f},
            {0.50f, 0.55f, 0.82f, 0.85f}
        },
        0xFF55DDFF
    );

    // ── Healing Spring (water) — equal cross ──────────────────────────────
    public static final SpellTemplate HEALING_SPRING = new SpellTemplate(
        "atelier:healing_spring", "spell.atelier.healing_spring",
        new float[][]{
            {0.50f, 0.10f, 0.50f, 0.90f},
            {0.10f, 0.50f, 0.90f, 0.50f}
        },
        0xFF0099FF
    );

    // ── Stone Barrier (earth) — diamond (rotated square) ──────────────────
    public static final SpellTemplate STONE_BARRIER = new SpellTemplate(
        "atelier:stone_barrier", "spell.atelier.stone_barrier",
        new float[][]{
            {0.50f, 0.12f, 0.88f, 0.50f},
            {0.88f, 0.50f, 0.50f, 0.88f},
            {0.50f, 0.88f, 0.12f, 0.50f},
            {0.12f, 0.50f, 0.50f, 0.12f}
        },
        0xFF886633
    );

    // ── Flash (light) — 6-pointed star (two overlapping triangles) ────────
    public static final SpellTemplate FLASH = new SpellTemplate(
        "atelier:flash", "spell.atelier.flash",
        new float[][]{
            // upward triangle
            {0.50f, 0.12f, 0.85f, 0.72f},
            {0.85f, 0.72f, 0.15f, 0.72f},
            {0.15f, 0.72f, 0.50f, 0.12f},
            // downward triangle
            {0.50f, 0.88f, 0.85f, 0.28f},
            {0.85f, 0.28f, 0.15f, 0.28f},
            {0.15f, 0.28f, 0.50f, 0.88f}
        },
        0xFFFFDD00
    );

    public static final SpellTemplate[] ALL = {
        FIREBOLT, GUST, HEALING_SPRING, STONE_BARRIER, FLASH
    };
}
