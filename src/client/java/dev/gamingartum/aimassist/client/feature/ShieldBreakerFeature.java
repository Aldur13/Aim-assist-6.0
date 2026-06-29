package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import dev.gamingartum.aimassist.client.util.HumanizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShieldBreakerFeature {

    private static final int    AXE_COOLDOWN      = 25;  // ticks between axe swings (was 10, too aggressive)
    private static final int    FOLLOW_UP_WINDOW   = 120; // ticks of fast follow-ups after shield drops (~6s, was 80)
    private static final double ATTACK_RANGE       = 3.5;
    private static final float  MIN_CHARGE         = 0.85f; // only swing when weapon is ≥85% charged
    private static final double SNEAK_RANGE        = 3.2;  // max distance to attempt strafe

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
            float charge = player.getAttackStrengthScale(0.5f);
            if (charge >= MIN_CHARGE && canRaycastHit(player, target)) {
                minecraft.gameMode.attack(player, target);
                player.swing(InteractionHand.MAIN_HAND);
                axeCooldown = HumanizationUtils.getVariableCooldown(AXE_COOLDOWN, 0.3f);
            }
        }
    }

    private static boolean canRaycastHit(Player player, Player target) {
        Vec3 playerEyes = player.getEyePosition();
        Vec3 targetCenter = target.getEyePosition().add(0, -target.getEyeHeight() / 2, 0);
        return player.level().clip(new ClipContext(playerEyes, targetCenter,
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS;
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
        double distance = player.distanceTo(target);
        if (distance > SNEAK_RANGE) return 0f;

        Vec3 velocity = target.getDeltaMovement();
        double predictTicks = Math.min(distance / 2.0, 3.0);
        Vec3 predictedPos = target.position().add(velocity.scale(predictTicks));

        double adaptiveOffset = Math.max(1.5, Math.min(6.0, distance * 1.5));
        Vec3 targetBack = predictedPos.subtract(target.getLookAngle().scale(adaptiveOffset));
        Vec3 toBack = targetBack.subtract(player.position()).normalize();

        double yawRad = Math.toRadians(player.getYRot());
        Vec3 right = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad));

        double dot = toBack.dot(right);
        float rawStrafe = (float) net.minecraft.util.Mth.clamp(dot * 2.0, -1.0, 1.0);

        return net.minecraft.util.Mth.lerp(0.25f, 0f, rawStrafe);
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
