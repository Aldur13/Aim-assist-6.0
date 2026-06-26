package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ShieldBreakerFeature {

    private static final int    ATTACK_COOLDOWN = 10;
    private static final double ATTACK_RANGE    = 3.5;

    private static int   attackCooldown  = 0;
    private static float pendingStrafe   = 0f;

    public static void tick(Minecraft minecraft) {
        if (attackCooldown > 0) attackCooldown--;

        AimAssistState state = AimAssistState.getInstance();
        if (!state.isEnabled() || !state.getConfig().shieldBreaker) {
            pendingStrafe = 0f;
            return;
        }
        if (minecraft.player == null || minecraft.level == null) {
            pendingStrafe = 0f;
            return;
        }

        Player player = minecraft.player;
        Player target = state.getCurrentTarget();
        if (target == null) {
            pendingStrafe = 0f;
            return;
        }

        if (target.isBlocking()) {
            if (attackCooldown <= 0) {
                tryAxeAttack(minecraft, player, target);
            }
            if (state.getConfig().sneakBehind) {
                pendingStrafe = calcStrafeDir(player, target);
            }
        } else {
            pendingStrafe = 0f;
        }
    }

    private static void tryAxeAttack(Minecraft minecraft, Player player, Player target) {
        int axeSlot = findBestAxeSlot(player);
        if (axeSlot < 0) return;

        player.getInventory().setSelectedSlot(axeSlot);

        if (player.distanceTo(target) <= ATTACK_RANGE) {
            minecraft.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
            attackCooldown = ATTACK_COOLDOWN;
        }
    }

    private static int findBestAxeSlot(Player player) {
        int current = player.getInventory().getSelectedSlot();
        if (player.getInventory().getItem(current).is(ItemTags.AXES)) {
            return current;
        }
        for (int i = 0; i < 9; i++) {
            if (player.getInventory().getItem(i).is(ItemTags.AXES)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Calculates the left/right strafe impulse to circle behind the target.
     * Positive = strafe left, negative = strafe right (matches KeyboardInput convention).
     */
    private static float calcStrafeDir(Player player, Player target) {
        // Point behind target (opposite to their facing)
        Vec3 targetBack = target.position().subtract(target.getLookAngle().scale(2.0));
        Vec3 toBack = targetBack.subtract(player.position()).normalize();

        // Player's right direction in world space
        double yawRad = Math.toRadians(player.getYRot());
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad));

        double dot = toBack.dot(right);
        // dot > 0 → target-back is to our right → strafe right (leftImpulse = -1)
        // dot < 0 → target-back is to our left  → strafe left  (leftImpulse = +1)
        return dot > 0 ? -1f : 1f;
    }

    public static float getPendingStrafe() {
        return pendingStrafe;
    }

    public static boolean isSneakBehindActive() {
        AimAssistState state = AimAssistState.getInstance();
        return state.isEnabled()
                && state.getConfig().sneakBehind
                && pendingStrafe != 0f;
    }
}
