package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

public class MaceHitFeature {

    /**
     * Horizontal distance within which the smash attack can connect.
     * Matches vanilla melee reach.
     */
    private static final double ATTACK_RANGE_H = 3.0;

    /**
     * The player must be at least this many blocks above the target when the
     * attack triggers so the smash bonus actually applies.
     */
    private static final double MIN_HEIGHT_ABOVE = 0.5;

    /**
     * Minimum fall speed (blocks/tick downward) before we consider the player
     * to be in a real fall rather than just gravity ticking.
     */
    private static final double MIN_FALL_SPEED = 0.15;

    /**
     * Minimum accumulated fall distance required for Minecraft's smash mechanic
     * to grant bonus damage.
     */
    private static final float MIN_FALL_DISTANCE = 1.5f;

    /** Cooldown in ticks between auto-hits to prevent spam. */
    private static final int HIT_COOLDOWN_TICKS = 10;
    private static int cooldown = 0;

    /**
     * Called every client tick. Triggers a mace smash when all conditions are met:
     * aim assist on, mace held, player falling, target in range.
     */
    public static void tick(MinecraftClient client) {
        if (cooldown > 0) {
            cooldown--;
        }

        AimAssistState state = AimAssistState.getInstance();
        if (!state.isEnabled() || !state.getConfig().maceMode) return;
        if (client.player == null || client.world == null) return;
        if (cooldown > 0) return;

        PlayerEntity player = client.player;
        PlayerEntity target = state.getCurrentTarget();
        if (target == null) return;

        boolean holdingMace    = player.getMainHandStack().isOf(Items.MACE);
        boolean isFalling      = player.getVelocity().y < -MIN_FALL_SPEED;
        boolean enoughFallDist = player.fallDistance >= MIN_FALL_DISTANCE;

        if (!holdingMace || !isFalling || !enoughFallDist) return;

        if (isInStrikeZone(player, target)) {
            client.interactionManager.attackEntity(player, target);
            player.swingHand(Hand.MAIN_HAND);
            cooldown = HIT_COOLDOWN_TICKS;
        }
    }

    /**
     * Returns true when the player is horizontally close enough to the target
     * and is above them — the exact geometry Minecraft needs for a smash hit.
     */
    private static boolean isInStrikeZone(PlayerEntity player, PlayerEntity target) {
        Vec3d pPos = player.getPos();
        Vec3d tPos = target.getPos();

        double dx = tPos.x - pPos.x;
        double dz = tPos.z - pPos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double heightAbove = pPos.y - tPos.y;

        return hDist <= ATTACK_RANGE_H
                && heightAbove >= MIN_HEIGHT_ABOVE
                && player.distanceTo(target) <= ATTACK_RANGE_H + 1.0;
    }
}
