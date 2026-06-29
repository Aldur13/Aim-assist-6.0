package dev.gamingartum.aimassist.client.util;

import java.util.Random;

public class HumanizationUtils {

    private static final Random RANDOM = new Random();

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
}
