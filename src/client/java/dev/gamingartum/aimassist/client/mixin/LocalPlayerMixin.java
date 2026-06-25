package dev.gamingartum.aimassist.client.mixin;

import dev.gamingartum.aimassist.client.AimAssistState;
import dev.gamingartum.aimassist.client.feature.AimAssistFeature;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * LocalPlayer overrides getViewXRot(float) and reads its own xRotLast field —
 * not Entity.xRot — so setXRot() calls from AimAssistFeature never reach the
 * camera. This mixin intercepts getViewXRot at the point the renderer calls it
 * and returns the correct target pitch directly, bypassing xRotLast entirely.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "getViewXRot", at = @At("HEAD"), cancellable = true)
    private void aimAssist_overrideViewPitch(float partialTick,
                                             CallbackInfoReturnable<Float> cir) {
        AimAssistState state = AimAssistState.getInstance();
        if (!state.isEnabled()) return;

        Player target = state.getCurrentTarget();
        if (target == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        Vec3 from = mc.player.getEyePosition();
        Vec3 to   = target.getEyePosition();
        float[] angles = AimAssistFeature.calcAngles(from, to);
        float targetPitch = Mth.clamp(angles[1], -90f, 90f);

        // Apply smoothness the same way the tick does, but use per-frame data
        float currentPitch = mc.player.getXRot();
        float smoothness = state.getConfig().aimSmoothness;
        float result = currentPitch + Mth.wrapDegrees(targetPitch - currentPitch) * smoothness;

        cir.setReturnValue(Mth.clamp(result, -90f, 90f));
    }
}
