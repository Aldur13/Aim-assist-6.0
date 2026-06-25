package dev.gamingartum.aimassist.client.mixin;

import dev.gamingartum.aimassist.client.AimAssistState;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancels mouse-driven look input for the local player while aim assist has a
 * locked target. AimAssistFeature sets the rotation directly each tick instead.
 *
 * Targets Entity (where changeLookDirection is defined) and guards with an
 * instanceof check so only the local ClientPlayerEntity is affected.
 */
@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "changeLookDirection", at = @At("HEAD"), cancellable = true)
    private void aimAssist_cancelMouseInput(double cursorDeltaX, double cursorDeltaY,
                                            CallbackInfo ci) {
        if (!((Object) this instanceof ClientPlayerEntity)) return;

        AimAssistState state = AimAssistState.getInstance();
        if (state.isEnabled() && state.getCurrentTarget() != null) {
            ci.cancel();
        }
    }
}
