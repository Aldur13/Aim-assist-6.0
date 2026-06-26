package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class AimAssistFeature {

    // Minimum smoothness applied while circling behind a target.
    // Overrides the user's setting if it's lower, so the aim tracks
    // through the strafe circle without lagging onto the ground.
    private static final float SNEAK_BEHIND_MIN_SMOOTHNESS = 0.6f;

    public static void tick(Minecraft minecraft) {
        AimAssistState state = AimAssistState.getInstance();

        if (!state.isEnabled() || minecraft.player == null || minecraft.level == null) {
            state.setCurrentTarget(null);
            return;
        }

        Player target = findNearest(minecraft);
        state.setCurrentTarget(target);

        if (target != null) {
            aimAt(minecraft, target, state.getConfig().aimSmoothness);
        }
    }

    private static Player findNearest(Minecraft minecraft) {
        return minecraft.level.players().stream()
                .filter(p -> p != minecraft.player)
                .min(Comparator.comparingDouble(p -> p.distanceToSqr(minecraft.player)))
                .orElse(null);
    }

    public static void aimAt(Minecraft minecraft, Player target, float smoothness) {
        if (minecraft.player == null) return;

        Vec3 from = minecraft.player.getEyePosition();
        Vec3 to   = getAimPosition(minecraft, target);

        float[] angles = calcAngles(from, to);
        float targetYaw   = angles[0];
        float targetPitch = angles[1];

        // Save pitch before update so LocalPlayerMixin can interpolate per-frame
        AimAssistState.getInstance().setPrevPitch(minecraft.player.getXRot());

        // While circling behind a blocking target, snap the aim faster so it
        // doesn't lag behind the strafe and drift onto the ground.
        float effective = ShieldBreakerFeature.isSneakBehindActive()
                ? Math.max(smoothness, SNEAK_BEHIND_MIN_SMOOTHNESS)
                : smoothness;

        float newYaw   = lerpAngle(minecraft.player.getYRot(), targetYaw, effective);
        float newPitch = Mth.lerp(effective, minecraft.player.getXRot(), targetPitch);

        minecraft.player.setYRot(newYaw);
        minecraft.player.setXRot(Mth.clamp(newPitch, -90f, 90f));
    }

    /**
     * Returns the world position to aim at.
     *
     * Adjustments applied in order:
     * 1. Elytra prediction — leads the aim ahead of a gliding target.
     * 2. Sneak-behind lift — raises the aim point above the eye when circling
     *    behind a blocking target, counteracting the downward drift that
     *    happens at close range during the strafe.
     */
    private static Vec3 getAimPosition(Minecraft minecraft, Player target) {
        Vec3 eye = target.getEyePosition();

        // ── elytra prediction ────────────────────────────────────────────────
        if (AimAssistState.getInstance().getConfig().elytraPredict && target.isFallFlying()) {
            Vec3 velocity = target.getDeltaMovement();
            double speed  = velocity.length();
            if (speed > 0.05) {
                double predictionTicks = Math.min(speed * 3.5, 10.0);
                return eye.add(velocity.scale(predictionTicks));
            }
        }

        // ── sneak-behind upward lift ──────────────────────────────────────────
        if (ShieldBreakerFeature.isSneakBehindActive()) {
            // Scale the vertical lift with distance: more correction up close
            // (where ground-drift is worst), tapering off at range.
            double dist   = minecraft.player == null ? 2.0 : minecraft.player.distanceTo(target);
            double lift   = Mth.clamp(0.4 - dist * 0.06, 0.05, 0.35);
            return eye.add(0, lift, 0);
        }

        return eye;
    }

    public static float[] calcAngles(Vec3 from, Vec3 to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, hDist));
        return new float[]{yaw, pitch};
    }

    private static float lerpAngle(float current, float target, float factor) {
        float diff = Mth.wrapDegrees(target - current);
        return current + diff * factor;
    }
}
