package dev.gamingartum.atelier.spell;

import java.util.ArrayList;
import java.util.List;

/**
 * Scores a player's drawn strokes against a SpellGlyphs template.
 *
 * Returns a power value in [0, 1] composed of:
 *  - Coverage  (40 %) — fraction of template segments that have a drawn point nearby
 *  - Accuracy  (40 %) — RMS deviation of drawn points from nearest template segment
 *  - Ring gate (20 %) — whether the ring appears closed (a stroke loops back near its start)
 *
 * A ring closure score below RING_GATE_THRESHOLD invalidates the spell entirely (returns 0).
 */
public final class GlyphScorer {

    /** Maximum allowed pixel-space deviation for a point to "cover" a template segment. */
    private static final float COVERAGE_THRESHOLD = 0.12f; // in normalized [0,1] coords

    /** Maximum RMS deviation for a perfect accuracy score. */
    private static final float ACCURACY_PERFECT_DIST = 0.04f;

    /** Maximum gap (normalized) between a ring stroke's first and last point. */
    private static final float RING_CLOSURE_THRESHOLD = 0.15f;

    /** Ring closure score below which the spell is invalidated. */
    private static final float RING_GATE_THRESHOLD = 0.1f;

    public enum PowerTier {
        CRUDE(0.0f, 0.4f, 0.4f),
        FAIR(0.4f, 0.65f, 0.65f),
        FINE(0.65f, 0.85f, 0.85f),
        MASTERFUL(0.85f, 1.01f, 1.0f);

        public final float minPower, maxPower, effectMultiplier;

        PowerTier(float min, float max, float mult) {
            this.minPower = min;
            this.maxPower = max;
            this.effectMultiplier = mult;
        }

        public static PowerTier forPower(float p) {
            for (PowerTier t : values()) {
                if (p >= t.minPower && p < t.maxPower) return t;
            }
            return CRUDE;
        }
    }

    public record ScoreResult(float power, PowerTier tier, boolean ringClosed) {
        public boolean isValid() { return ringClosed && power > 0; }
    }

    /**
     * @param strokes  list of strokes (each stroke is a list of [x,y] in normalized canvas coords)
     * @param template the glyph being scored
     */
    public static ScoreResult score(List<List<float[]>> strokes, SpellGlyphs.SpellTemplate template) {
        if (strokes == null || strokes.isEmpty()) {
            return new ScoreResult(0, PowerTier.CRUDE, false);
        }

        // Flatten all strokes into one point list
        List<float[]> allPoints = new ArrayList<>();
        for (List<float[]> stroke : strokes) allPoints.addAll(stroke);

        // ── Ring gate ──────────────────────────────────────────────────────
        float ringScore = scoreRingClosure(strokes, template);
        if (ringScore < RING_GATE_THRESHOLD) {
            return new ScoreResult(0, PowerTier.CRUDE, false);
        }

        // ── Coverage ───────────────────────────────────────────────────────
        float coverage = scoreCoverage(allPoints, template);

        // ── Accuracy ───────────────────────────────────────────────────────
        float accuracy = scoreAccuracy(allPoints, template);

        // ── Combine ────────────────────────────────────────────────────────
        float power = clamp(0.40f * coverage + 0.40f * accuracy + 0.20f * ringScore);

        PowerTier tier = PowerTier.forPower(power);
        return new ScoreResult(power, tier, true);
    }

    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Coverage: fraction of template segments that have at least one drawn point within
     * COVERAGE_THRESHOLD of any point on the segment.
     */
    private static float scoreCoverage(List<float[]> points, SpellGlyphs.SpellTemplate tmpl) {
        int hit = 0;
        int total = tmpl.innerSegments().length + SpellGlyphs.RING.length;
        for (float[] seg : tmpl.innerSegments()) {
            if (anyPointNearSegment(points, seg)) hit++;
        }
        for (float[] seg : SpellGlyphs.RING) {
            if (anyPointNearSegment(points, seg)) hit++;
        }
        return total == 0 ? 0 : (float) hit / total;
    }

    /**
     * Accuracy: for each drawn point find its distance to the nearest template segment,
     * compute RMS, map to [0,1] (0 dist → 1.0, ACCURACY_PERFECT_DIST*3 → ~0).
     */
    private static float scoreAccuracy(List<float[]> points, SpellGlyphs.SpellTemplate tmpl) {
        if (points.isEmpty()) return 0;
        List<float[]> all = tmpl.allSegments();
        double sumSq = 0;
        for (float[] p : points) {
            float minDist = Float.MAX_VALUE;
            for (float[] seg : all) {
                float d = distToSegment(p[0], p[1], seg[0], seg[1], seg[2], seg[3]);
                if (d < minDist) minDist = d;
            }
            sumSq += minDist * minDist;
        }
        float rms = (float) Math.sqrt(sumSq / points.size());
        return clamp(1.0f - rms / (ACCURACY_PERFECT_DIST * 3));
    }

    /**
     * Ring closure: checks if the stroke(s) collectively close the circular ring glyph.
     * We detect whether any stroke is roughly circular and returns near its start.
     * Returns a score in [0,1]; below RING_GATE_THRESHOLD the spell fails.
     */
    private static float scoreRingClosure(List<List<float[]>> strokes, SpellGlyphs.SpellTemplate tmpl) {
        for (List<float[]> stroke : strokes) {
            if (stroke.size() < 3) continue;
            float[] first = stroke.get(0);
            float[] last = stroke.get(stroke.size() - 1);
            float gap = dist(first[0], first[1], last[0], last[1]);
            if (gap < RING_CLOSURE_THRESHOLD) {
                float score = clamp(1.0f - gap / RING_CLOSURE_THRESHOLD);
                if (score >= RING_GATE_THRESHOLD) return score;
            }
        }
        return 0;
    }

    private static boolean anyPointNearSegment(List<float[]> points, float[] seg) {
        for (float[] p : points) {
            if (distToSegment(p[0], p[1], seg[0], seg[1], seg[2], seg[3]) < COVERAGE_THRESHOLD) {
                return true;
            }
        }
        return false;
    }

    // Perpendicular distance from point (px, py) to segment (x1,y1)-(x2,y2)
    private static float distToSegment(float px, float py, float x1, float y1, float x2, float y2) {
        float dx = x2 - x1, dy = y2 - y1;
        float lenSq = dx * dx + dy * dy;
        if (lenSq < 1e-8f) return dist(px, py, x1, y1);
        float t = clamp(((px - x1) * dx + (py - y1) * dy) / lenSq);
        float projX = x1 + t * dx, projY = y1 + t * dy;
        return dist(px, py, projX, projY);
    }

    private static float dist(float ax, float ay, float bx, float by) {
        float dx = ax - bx, dy = ay - by;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private static float clamp(float v) {
        return Math.max(0, Math.min(1, v));
    }
}
