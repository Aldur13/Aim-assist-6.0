package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import dev.gamingartum.aimassist.client.util.HumanizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class ShieldBreakerFeature {

    private static final int    AXE_COOLDOWN      = 10;  // ticks between axe swings while shield is up
    private static final int    FOLLOW_UP_WINDOW   = 80;  // ticks of fast follow-ups after shield drops (~4s)
    private static final double ATTACK_RANGE       = 3.5;
    private static final float  MIN_CHARGE         = 0.90f; // only swing when weapon is ≥90% charged

    private static int   axeCooldown    = 0;
    private static int   followUpWindow = 0;
    private static int   savedSlot      = -1;  // hotbar slot we held before switching to axe
    private static float pendingStrafe  = 0f;
    private static int   equipmentDelay = 0;   // delay for equipment animations
    private static int   equipmentTarget = -1; // target slot to switch to

    public static void tick(Minecraft minecraft) {
        if (axeCooldown > 0) axeCooldown--;
        if (equipmentDelay > 0) equipmentDelay--;

        // Process delayed equipment switch
        if (equipmentDelay == 0 && equipmentTarget >= 0) {
            if (minecraft.player != null) {
                minecraft.player.getInventory().setSelectedSlot(equipmentTarget);
            }
            equipmentTarget = -1;
        }

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
            restoreSlot(player);
            return;
        }

        if (target.isBlocking()) {
            followUpWindow = 0; // abort any follow-up while shield is back up

            // Remember original slot so we can restore it the moment the shield drops
            if (savedSlot < 0) {
                savedSlot = player.getInventory().getSelectedSlot();
            }

            if (axeCooldown <= 0) {
                tryAxeAttack(minecraft, player, target);
            }

            if (state.getConfig().sneakBehind) {
                pendingStrafe = calcStrafeDir(player, target);
            }
        } else {
            pendingStrafe = 0f;

            // The instant the shield goes down, restore the original weapon and open follow-up window
            if (savedSlot >= 0) {
                player.getInventory().setSelectedSlot(savedSlot);
                savedSlot = -1;
                followUpWindow = FOLLOW_UP_WINDOW;
            }

            // Hammer the target with follow-up attacks while the window is open
            if (followUpWindow > 0) {
                followUpWindow--;
                followUpAttack(minecraft, player, target);
            }
        }
    }

    // ─── axe hit to break shield ─────────────────────────────────────────────

    private static void tryAxeAttack(Minecraft minecraft, Player player, Player target) {
        int axeSlot = findBestAxeSlot(player);
        if (axeSlot < 0) return;

        if (equipmentDelay <= 0 && equipmentTarget < 0) {
            equipmentTarget = axeSlot;
            equipmentDelay = 3 + (int)(Math.random() * 5);
        }

        if (player.getInventory().getSelectedSlot() == axeSlot && player.distanceTo(target) <= ATTACK_RANGE) {
            minecraft.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
            axeCooldown = HumanizationUtils.getVariableCooldown(AXE_COOLDOWN, 0.3f);
        }
    }

    // ─── rapid follow-up after shield is disabled ─────────────────────────────

    private static void followUpAttack(Minecraft minecraft, Player player, Player target) {
        if (player.distanceTo(target) > ATTACK_RANGE) return;
        // Only swing when the weapon charge is high enough to deal real damage
        if (player.getAttackStrengthScale(0.5f) >= MIN_CHARGE) {
            minecraft.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

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

    private static void restoreSlot(Player player) {
        if (savedSlot >= 0 && player != null) {
            player.getInventory().setSelectedSlot(savedSlot);
        }
        savedSlot = -1;
        pendingStrafe = 0f;
        followUpWindow = 0;
    }

    /**
     * Returns left/right strafe impulse to circle behind the blocking target.
     * Positive = strafe left, negative = strafe right (KeyboardInput convention).
     */
    private static float calcStrafeDir(Player player, Player target) {
        Vec3 targetBack = target.position().subtract(target.getLookAngle().scale(2.0));
        Vec3 toBack     = targetBack.subtract(player.position()).normalize();

        double yawRad = Math.toRadians(player.getYRot());
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad));

        double dot = toBack.dot(right);
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
