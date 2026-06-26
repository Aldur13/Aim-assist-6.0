package dev.gamingartum.aimassist.client.mixin;

import dev.gamingartum.aimassist.client.feature.ShieldBreakerFeature;
import net.minecraft.client.player.KeyboardInput;
import net.minecraft.world.phys.Vec2;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardInput.class)
public class KeyboardInputMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        if (ShieldBreakerFeature.isSneakBehindActive()) {
            float strafe = ShieldBreakerFeature.getPendingStrafe();
            // Access moveVector via the ClientInputAccessor mixin on the parent class.
            // KeyboardInput extends ClientInput, so this cast is valid at runtime.
            ClientInputAccessor accessor = (ClientInputAccessor)(Object)this;
            Vec2 cur = accessor.getMoveVector();
            // x = left/right strafe (positive = left, negative = right)
            accessor.setMoveVector(new Vec2(strafe, cur.y));
        }
    }
}
