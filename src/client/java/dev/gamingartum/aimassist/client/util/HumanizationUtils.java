package dev.gamingartum.aimassist.client.util;

import java.util.Random;

public class HumanizationUtils {

    private static final Random RANDOM = new Random();
    private static long lastJitterUpdate = 0;
    private static float jitterOffset = 0;

    public static float easeInOutQuintic(float t) {
        t = Math.max(0, Math.min(1, t));
        if (t < 0.5f) {
            return 16 * t * t * t * t * t;
        } else {
            float f = 2 * t - 2;
            return 0.5f * f * f * f * f * f + 1;
        }
    }

    public static float applyJitter(float value, float amplitude) {
        double tremor = Math.sin(System.nanoTime() / 1_000_000.0 * 0.012 * Math.PI) * amplitude;
        tremor += (RANDOM.nextDouble() - 0.5) * amplitude;
        return value + (float) tremor;
    }

    public static int getVariableCooldown(int baseCooldown, float variance) {
        int min = (int) (baseCooldown * (1.0f - variance));
        int max = (int) (baseCooldown * (1.0f + variance));
        return min + RANDOM.nextInt(max - min + 1);
    }

    public static boolean shouldMiss(float missChance) {
        return RANDOM.nextFloat() < missChance;
    }

    public static int getReactionDelay() {
        int min = 3;
        int max = 12;
        return min + RANDOM.nextInt(max - min + 1);
    }

    public static boolean hasReactionDelayPassed(int tickCounter, int delayTicks) {
        return tickCounter >= delayTicks;
    }

    public static float getPredictionVariance() {
        return 0.85f + (float) (Math.random() * 0.3);
    }

    public static int getEquipmentSwitchDelay() {
        return 3 + RANDOM.nextInt(5);
    }

    public static int getAnimationDelay(int baseDelay) {
        return baseDelay + RANDOM.nextInt(5) - 2;
    }

    public static float getAdaptiveAccuracy(float baseAccuracy, int successCount) {
        if (successCount > 10) return Math.min(baseAccuracy + 0.1f, 0.95f);
        if (successCount < 2) return Math.max(baseAccuracy - 0.15f, 0.60f);
        return baseAccuracy;
    }

    public static boolean shouldUseStateDelay(int currentState, int nextState) {
        if (currentState != nextState) {
            return RANDOM.nextFloat() < 0.6f;
        }
        return false;
    }

    public static float getSmoothnessVariance(float baseSmoothness) {
        return baseSmoothness * (0.8f + (float) Math.random() * 0.4f);
    }
}
