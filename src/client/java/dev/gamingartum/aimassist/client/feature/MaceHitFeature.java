package dev.gamingartum.aimassist.client.feature;

import dev.gamingartum.aimassist.client.AimAssistState;
import dev.gamingartum.aimassist.client.util.HumanizationUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class MaceHitFeature {

    private static final double ATTACK_RANGE_H  = 3.0;
    private static final double MIN_HEIGHT_ABOVE = 0.5;
    private static final double MIN_FALL_SPEED   = 0.15;
    private static final float  MIN_FALL_DIST    = 1.5f;
    private static final int    HIT_COOLDOWN     = 10;

    private static int cooldown = 0;

    public static void tick(Minecraft minecraft) {
        if (cooldown > 0) cooldown--;

        AimAssistState state = AimAssistState.getInstance();
        if (!state.isEnabled() || !state.getConfig().maceMode) return;
        if (minecraft.player == null || minecraft.level == null) return;
        if (cooldown > 0) return;

        Player player = minecraft.player;
        Player target = state.getCurrentTarget();
        if (target == null) return;

        boolean holdingMace    = player.getMainHandItem().is(Items.MACE);
        boolean isFalling      = player.getDeltaMovement().y < -MIN_FALL_SPEED;
        boolean enoughFallDist = player.fallDistance >= MIN_FALL_DIST;

        if (!holdingMace || !isFalling || !enoughFallDist) return;

        if (isInStrikeZone(player, target)) {
            double distance = player.distanceTo(target);
            float missChance = (float) Math.min(0.15, distance * 0.05);

            if (!HumanizationUtils.shouldMiss(missChance)) {
                minecraft.gameMode.attack(player, target);
                player.swing(InteractionHand.MAIN_HAND);
            }
            cooldown = HumanizationUtils.getVariableCooldown(HIT_COOLDOWN, 0.3f);
        }
    }

    private static boolean isInStrikeZone(Player player, Player target) {
        Vec3 pPos = player.position();
        Vec3 tPos = target.position();
        double dx = tPos.x - pPos.x;
        double dz = tPos.z - pPos.z;
        double hDist = Math.sqrt(dx * dx + dz * dz);
        double heightAbove = pPos.y - tPos.y;
        return hDist <= ATTACK_RANGE_H
                && heightAbove >= MIN_HEIGHT_ABOVE
                && player.distanceTo(target) <= ATTACK_RANGE_H + 1.0;
    }
}
