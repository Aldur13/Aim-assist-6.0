package dev.gamingartum.aimassist.client.mixin;

import dev.gamingartum.aimassist.client.AimAssistState;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels mouse-driven rotation for the local player while aim assist has a
 * locked target. AimAssistFeature sets the rotation directly each tick instead.
 */
@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "turn", at = @At("HEAD"), cancellable = true)
    private void aimAssist_cancelMouseInput(double yaw, double pitch, CallbackInfo ci) {
        if (!((Object) this instanceof LocalPlayer)) return;
        AimAssistState state = AimAssistState.getInstance();
        if (state.isEnabled() && state.getCurrentTarget() != null) {
            ci.cancel();
        }
    }
}
