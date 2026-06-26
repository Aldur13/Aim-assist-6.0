package dev.gamingartum.aimassist.client.mixin;

import dev.gamingartum.aimassist.client.AimAssistState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * LocalPlayer.getViewXRot reads its own xRotLast field rather than Entity.xRot,
 * so setXRot calls from AimAssistFeature never reach the camera.
 *
 * This mixin intercepts getViewXRot and returns a value interpolated between
 * prevPitch (saved just before the tick update) and the new xRot using partialTick.
 * This gives smooth sub-tick camera motion instead of per-tick snapping.
 */
@Mixin(LocalPlayer.class)
public class LocalPlayerMixin {

    @Inject(method = "getViewXRot", at = @At("HEAD"), cancellable = true)
    private void aimAssist_overrideViewPitch(float partialTick,
                                             CallbackInfoReturnable<Float> cir) {
        AimAssistState state = AimAssistState.getInstance();
        if (!state.isEnabled() || state.getCurrentTarget() == null) return;

        LocalPlayer self = (LocalPlayer) (Object) this;
        float prev    = state.getPrevPitch();
        float current = self.getXRot();
        float result  = Mth.lerp(partialTick, prev, current);

        cir.setReturnValue(Mth.clamp(result, -90f, 90f));
    }
}
