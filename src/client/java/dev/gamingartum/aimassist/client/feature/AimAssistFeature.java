package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public class AimAssistFeature {

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
        Vec3 to   = target.getEyePosition();

        float[] angles = calcAngles(from, to);
        float targetYaw   = angles[0];
        float targetPitch = angles[1];

        float newYaw   = lerpAngle(minecraft.player.getYRot(), targetYaw, smoothness);
        float newPitch = Mth.lerp(smoothness, minecraft.player.getXRot(), targetPitch);

        minecraft.player.setYRot(newYaw);
        minecraft.player.setXRot(Mth.clamp(newPitch, -90f, 90f));
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
