package dev.gamingartum.aimassist.client.util;

import java.util.Random;

public class BehaviorSimulator {

    private static final Random RANDOM = new Random();

    private static int hesitationCounter = 0;
    private static int hesitationTarget = 0;
    private static int lastSuccessCount = 0;
    private static long lastBehaviorShift = System.currentTimeMillis();

    public static float getHumanLikeAccuracy(float targetAccuracy, int consecutiveHits) {
        float confidence = Math.min(consecutiveHits / 5f, 1f);
        float naturalVariance = (float) (Math.random() - 0.5) * 0.3f;
        return Math.max(0.3f, Math.min(targetAccuracy + naturalVariance, 0.95f));
    }

    public static boolean shouldMissIntentionally(int consecutiveHits) {
        if (consecutiveHits < 3) return false;
        if (consecutiveHits > 15) return Math.random() < 0.3f;
        if (consecutiveHits > 8) return Math.random() < 0.15f;
        return Math.random() < 0.05f;
    }

    public static float getAdaptiveReactionTime(float baseReaction) {
        if (RANDOM.nextFloat() < 0.1f) {
            return baseReaction * 1.5f;
        }
        return baseReaction * (0.9f + (float) Math.random() * 0.2f);
    }

    public static boolean shouldHesitate() {
        if (hesitationCounter > 0) {
            hesitationCounter--;
            return true;
        }

        if (Math.random() < 0.05f) {
            hesitationTarget = 3 + RANDOM.nextInt(8);
            hesitationCounter = hesitationTarget;
            return true;
        }

        return false;
    }

    public static float getMovementPredictionConfidence(int targetDistance) {
        if (targetDistance > 30) return 0.6f;
        if (targetDistance > 20) return 0.75f;
        if (targetDistance > 10) return 0.85f;
        return 0.95f;
    }

    public static boolean shouldAdaptBehavior() {
        long timeSinceLastShift = System.currentTimeMillis() - lastBehaviorShift;
        if (timeSinceLastShift > 30000 && Math.random() < 0.3f) {
            lastBehaviorShift = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    public static float getAngleEstimationError() {
        return (float) (Math.random() - 0.5f) * 0.5f;
    }

    public static int getActionDelayFromState(int fromState, int toState) {
        if (fromState == toState) return 0;
        return 2 + RANDOM.nextInt(4);
    }

    public static boolean shouldMissAttackAtDistance(double distance) {
        if (distance > 15) return Math.random() < 0.3f;
        if (distance > 10) return Math.random() < 0.2f;
        if (distance > 5) return Math.random() < 0.1f;
        return Math.random() < 0.05f;
    }

    public static float getTargetLeadingError(float perfectLead) {
        float error = (float) (Math.random() - 0.5f) * 0.2f;
        return perfectLead * (1f + error);
    }
}
