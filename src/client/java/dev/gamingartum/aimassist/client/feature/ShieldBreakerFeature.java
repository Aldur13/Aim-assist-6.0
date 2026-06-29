package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import dev.gamingartum.aimassist.client.util.HumanizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.ClipContext;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ShieldBreakerFeature {

    private static final int    AXE_COOLDOWN      = 25;  // ticks between axe swings
    private static final int    FOLLOW_UP_WINDOW   = 120; // ticks of fast follow-ups (~6s)
    private static final double ATTACK_RANGE       = 3.5;
    private static final double SNEAK_RANGE        = 3.2;  // max distance to maintain strafe
    private static final float  MIN_AXE_CHARGE     = 0.85f; // axe swing threshold
    private static final float  MIN_FOLLOW_UP_CHARGE = 0.90f; // follow-up attack threshold

    // Strafe smoothing for natural movement
    private static final float  STRAFE_LERP_SPEED  = 0.25f;
    private static final float  STRAFE_DEADZONE    = 0.15f;

    private static int   axeCooldown    = 0;
    private static int   followUpWindow = 0;
    private static int   savedSlot      = -1;
    private static float pendingStrafe  = 0f;
    private static float strafeLerpTarget = 0f;
    private static int   lastShieldDurability = -1;
    private static int   equipmentDelay = 0;
    private static int   equipmentTarget = -1;

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
            strafeLerpTarget = 0f;
            lastShieldDurability = -1;
            return;
        }
        if (minecraft.player == null || minecraft.level == null) {
            pendingStrafe = 0f;
            strafeLerpTarget = 0f;
            lastShieldDurability = -1;
            return;
        }

        Player player = minecraft.player;
        Player target = state.getCurrentTarget();
        if (target == null) {
            restoreSlot(player);
            lastShieldDurability = -1;
            return;
        }

        if (target.isBlocking()) {
            followUpWindow = 0;

            if (savedSlot < 0) {
                savedSlot = player.getInventory().getSelectedSlot();
            }

            // Track shield durability for predictive cooldown
            ItemStack shield = target.getOffhandItem();
            if (shield.is(Items.SHIELD)) {
                int currentDurability = shield.getMaxDamage() - shield.getDamageValue();
                if (lastShieldDurability > 0 && currentDurability < lastShieldDurability - 5) {
                    axeCooldown = Math.max(0, axeCooldown - 5);
                }
                lastShieldDurability = currentDurability;
            }

            if (axeCooldown <= 0) {
                tryAxeAttack(minecraft, player, target);
            }

            if (state.getConfig().sneakBehind) {
                float desiredStrafe = calcStrafeDir(player, target);
                strafeLerpTarget = desiredStrafe;
                pendingStrafe = Mth.lerp(STRAFE_LERP_SPEED, pendingStrafe, strafeLerpTarget);
                if (Math.abs(pendingStrafe) < STRAFE_DEADZONE) {
                    pendingStrafe = 0f;
                }
            }
        } else {
            pendingStrafe = 0f;
            strafeLerpTarget = 0f;
            lastShieldDurability = -1;

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
            if (charge >= MIN_AXE_CHARGE && canRaycastHit(player, target)) {
                minecraft.gameMode.attack(player, target);
                player.swing(InteractionHand.MAIN_HAND);
                axeCooldown = HumanizationUtils.getVariableCooldown(AXE_COOLDOWN, 0.3f);
            }
        }
    }

    private static boolean canRaycastHit(Player player, Player target) {
        if (player.level() == null) return false;

        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = target.getEyePosition();
        Vec3 direction = targetPos.subtract(eyePos);
        double distance = direction.length();

        if (distance <= 0) return false;
        direction = direction.normalize();

        Vec3 rayEnd = eyePos.add(direction.scale(distance + 0.5));
        HitResult result = player.level().clip(new ClipContext(
            eyePos, rayEnd,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.NONE,
            player
        ));

        if (result.getType() == HitResult.Type.MISS) {
            return true;
        }

        if (result instanceof EntityHitResult) {
            return ((EntityHitResult) result).getEntity() == target;
        }

        return false;
    }

    // ─── rapid follow-up after shield is disabled ─────────────────────────────

    private static void followUpAttack(Minecraft minecraft, Player player, Player target) {
        if (player.distanceTo(target) > ATTACK_RANGE) return;
        if (player.getAttackStrengthScale(0.5f) >= MIN_FOLLOW_UP_CHARGE) {
            minecraft.gameMode.attack(player, target);
            player.swing(InteractionHand.MAIN_HAND);
        }
    }

    // ─── helpers ──────────────────────────────────────────────────────────────

    private static int findBestAxeSlot(Player player) {
        int current = player.getInventory().getSelectedSlot();
        ItemStack currentItem = player.getInventory().getItem(current);

        if (currentItem.is(ItemTags.AXES) && !currentItem.isDamaged()) {
            return current;
        }

        int bestSlot = -1;
        int bestDurability = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item.is(ItemTags.AXES)) {
                int durability = item.getMaxDamage() - item.getDamageValue();
                if (durability > bestDurability) {
                    bestDurability = durability;
                    bestSlot = i;
                }
            }
        }

        return bestSlot;
    }

    private static void restoreSlot(Player player) {
        if (savedSlot >= 0 && player != null) {
            player.getInventory().setSelectedSlot(savedSlot);
        }
        savedSlot = -1;
        pendingStrafe = 0f;
        strafeLerpTarget = 0f;
        followUpWindow = 0;
        lastShieldDurability = -1;
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
