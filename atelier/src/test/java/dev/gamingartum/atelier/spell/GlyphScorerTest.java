package dev.gamingartum.atelier.spell;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GlyphScorerTest {

    private static final SpellGlyphs.SpellTemplate TMPL = SpellGlyphs.FIREBOLT;

    // ── Helpers ────────────────────────────────────────────────────────────

    /** Sample points uniformly along a normalized segment. */
    private static List<float[]> sampleSegment(float x1, float y1, float x2, float y2, int n) {
        List<float[]> pts = new ArrayList<>();
        for (int i = 0; i <= n; i++) {
            float t = (float) i / n;
            pts.add(new float[]{x1 + t * (x2 - x1), y1 + t * (y2 - y1)});
        }
        return pts;
    }

    /** Build a nearly-closed circular stroke approximation for the ring. */
    private static List<float[]> closedRingStroke() {
        List<float[]> pts = new ArrayList<>();
        int n = 40;
        for (int i = 0; i <= n; i++) {
            double a = 2 * Math.PI * i / n;
            pts.add(new float[]{
                0.5f + 0.42f * (float) Math.cos(a),
                0.5f + 0.42f * (float) Math.sin(a)
            });
        }
        return pts;
    }

    /** Build a ring stroke that is open (not closed). */
    private static List<float[]> openRingStroke() {
        List<float[]> pts = new ArrayList<>();
        int n = 20;
        for (int i = 0; i <= n; i++) {
            double a = Math.PI * i / n; // only half a circle
            pts.add(new float[]{
                0.5f + 0.42f * (float) Math.cos(a),
                0.5f + 0.42f * (float) Math.sin(a)
            });
        }
        return pts;
    }

    // ── Tests ──────────────────────────────────────────────────────────────

    @Test
    void emptyStrokes_returnsZeroPower() {
        GlyphScorer.ScoreResult r = GlyphScorer.score(List.of(), TMPL);
        assertEquals(0, r.power());
        assertFalse(r.isValid());
    }

    @Test
    void openRing_isInvalid() {
        List<List<float[]>> strokes = new ArrayList<>();
        strokes.add(openRingStroke()); // half-circle, not closed
        GlyphScorer.ScoreResult r = GlyphScorer.score(strokes, TMPL);
        assertFalse(r.isValid(), "Open ring should fail the ring gate");
        assertEquals(0, r.power(), 0.001f);
    }

    @Test
    void closedRingWithInnerLines_isValid() {
        List<List<float[]>> strokes = new ArrayList<>();
        strokes.add(closedRingStroke());
        // Add the three triangle edges of the firebolt template
        for (float[] seg : TMPL.innerSegments()) {
            strokes.add(sampleSegment(seg[0], seg[1], seg[2], seg[3], 20));
        }
        GlyphScorer.ScoreResult r = GlyphScorer.score(strokes, TMPL);
        assertTrue(r.isValid(), "Perfect trace should be valid");
        assertTrue(r.power() > 0.6f, "Perfect trace should score above FINE threshold: " + r.power());
    }

    @Test
    void neatTraceScoresHigherThanNoisyTrace() {
        // Neat: trace exactly on the template lines
        List<List<float[]>> neatStrokes = new ArrayList<>();
        neatStrokes.add(closedRingStroke());
        for (float[] seg : TMPL.innerSegments()) {
            neatStrokes.add(sampleSegment(seg[0], seg[1], seg[2], seg[3], 20));
        }
        GlyphScorer.ScoreResult neat = GlyphScorer.score(neatStrokes, TMPL);

        // Noisy: trace with random offsets of ~0.1 in each axis
        List<List<float[]>> noisyStrokes = new ArrayList<>();
        noisyStrokes.add(closedRingStroke()); // keep ring closed
        for (float[] seg : TMPL.innerSegments()) {
            List<float[]> noisySeg = new ArrayList<>();
            for (float[] p : sampleSegment(seg[0], seg[1], seg[2], seg[3], 20)) {
                noisySeg.add(new float[]{
                    Math.max(0, Math.min(1, p[0] + 0.08f)),
                    Math.max(0, Math.min(1, p[1] + 0.08f))
                });
            }
            noisyStrokes.add(noisySeg);
        }
        GlyphScorer.ScoreResult noisy = GlyphScorer.score(noisyStrokes, TMPL);

        assertTrue(neat.power() > noisy.power(),
            "Neat power " + neat.power() + " should exceed noisy power " + noisy.power());
    }

    @Test
    void powerTierMapping() {
        assertEquals(GlyphScorer.PowerTier.CRUDE,     GlyphScorer.PowerTier.forPower(0.0f));
        assertEquals(GlyphScorer.PowerTier.CRUDE,     GlyphScorer.PowerTier.forPower(0.3f));
        assertEquals(GlyphScorer.PowerTier.FAIR,      GlyphScorer.PowerTier.forPower(0.5f));
        assertEquals(GlyphScorer.PowerTier.FINE,      GlyphScorer.PowerTier.forPower(0.7f));
        assertEquals(GlyphScorer.PowerTier.MASTERFUL, GlyphScorer.PowerTier.forPower(0.9f));
    }
}
