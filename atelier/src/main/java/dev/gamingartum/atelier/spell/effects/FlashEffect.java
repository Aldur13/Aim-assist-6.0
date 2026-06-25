package dev.gamingartum.atelier.spell.effects;

import dev.gamingartum.atelier.spell.SpellEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.phys.AABB;

public final class FlashEffect implements SpellEffect {

    public static final FlashEffect INSTANCE = new FlashEffect();

    @Override
    public void cast(ServerLevel level, ServerPlayer caster, float power) {
        double radius = 4.0 + power * 8.0;
        int blindDuration = (int)(40 + power * 80); // 40-120 ticks (2-6 seconds)

        AABB area = AABB.ofSize(caster.position(), radius * 2, radius * 2, radius * 2);
        level.getEntitiesOfClass(Monster.class, area, e -> true)
            .forEach(mob -> {
                mob.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, blindDuration, 0));
                // Higher power also applies Glowing so the player can see them through walls
                if (power > 0.6f) {
                    mob.addEffect(new MobEffectInstance(MobEffects.GLOWING, blindDuration, 0));
                }
            });
    }
}
