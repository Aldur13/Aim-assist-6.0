package dev.gamingartum.aimassist.client.mixin;

import dev.gamingartum.aimassist.client.feature.ShieldBreakerFeature;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    // moveVector is a protected Vec2 field declared in ClientInput (parent of KeyboardInput)
    @Shadow
    protected Vec2 moveVector;

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (ShieldBreakerFeature.isSneakBehindActive()) {
            float strafe = ShieldBreakerFeature.getPendingStrafe();
            // x = left/right strafe impulse (positive = left, negative = right)
            this.moveVector = new Vec2(strafe, this.moveVector.y);
        }
    }
}
