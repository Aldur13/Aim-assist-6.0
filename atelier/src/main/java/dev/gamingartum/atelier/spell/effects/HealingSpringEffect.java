package dev.gamingartum.atelier.spell.effects;

import dev.gamingartum.atelier.spell.SpellEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class HealingSpringEffect implements SpellEffect {

    public static final HealingSpringEffect INSTANCE = new HealingSpringEffect();

    @Override
    public void cast(ServerLevel level, ServerPlayer caster, float power) {
        // Immediate heal scaled by power
        float healAmount = 2.0f + power * 14.0f;
        caster.heal(healAmount);

        // Regeneration buff for stronger casts (power > 0.5)
        if (power > 0.5f) {
            int regenDuration = (int)((power - 0.5f) * 400); // up to 200 ticks at power=1
            caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenDuration, 0));
        }
    }
}
