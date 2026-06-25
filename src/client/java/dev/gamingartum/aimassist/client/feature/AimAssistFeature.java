package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.Comparator;

public class AimAssistFeature {

    /**
     * Called every client tick. Finds the nearest player and locks the local
     * player's camera onto them when aim assist is enabled.
     */
    public static void tick(MinecraftClient client) {
        AimAssistState state = AimAssistState.getInstance();

        if (!state.isEnabled() || client.player == null || client.world == null) {
            state.setCurrentTarget(null);
            return;
        }

        PlayerEntity target = findNearest(client);
        state.setCurrentTarget(target);

        if (target != null) {
            aimAt(client, target, state.getConfig().aimSmoothness);
        }
    }

    private static PlayerEntity findNearest(MinecraftClient client) {
        return client.world.getPlayers().stream()
                .filter(p -> p != client.player)
                .min(Comparator.comparingDouble(p -> p.squaredDistanceTo(client.player)))
                .orElse(null);
    }

    /**
     * Rotates the player's view towards {@code target}.
     * {@code smoothness} controls how fast: 1.0 = instant snap, lower = gradual pull.
     */
    public static void aimAt(MinecraftClient client, PlayerEntity target, float smoothness) {
        if (client.player == null) return;

        Vec3d from = client.player.getEyePos();
        Vec3d to   = target.getEyePos();

        float[] angles = calcAngles(from, to);
        float targetYaw   = angles[0];
        float targetPitch = angles[1];

        float newYaw   = lerpAngle(client.player.getYaw(), targetYaw, smoothness);
        float newPitch = MathHelper.lerp(smoothness, client.player.getPitch(), targetPitch);

        client.player.setYaw(newYaw);
        client.player.setPitch(MathHelper.clamp(newPitch, -90f, 90f));
    }

    /** Returns {yaw, pitch} in degrees pointing from {@code from} toward {@code to}. */
    private static float[] calcAngles(Vec3d from, Vec3d to) {
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double dz = to.z - from.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        float yaw   = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, hDist));
        return new float[]{yaw, pitch};
    }

    /** Shortest-path angle lerp that handles the 360→0 wrap correctly. */
    private static float lerpAngle(float current, float target, float factor) {
        float diff = MathHelper.wrapDegrees(target - current);
        return current + diff * factor;
    }
}
