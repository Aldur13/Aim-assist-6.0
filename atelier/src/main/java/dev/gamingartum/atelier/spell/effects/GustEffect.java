package dev.gamingartum.atelier.spell.effects;

import dev.gamingartum.atelier.spell.SpellEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class GustEffect implements SpellEffect {

    public static final GustEffect INSTANCE = new GustEffect();

    @Override
    public void cast(ServerLevel level, ServerPlayer caster, float power) {
        Vec3 look = caster.getLookAngle();
        double radius = 3.0 + power * 4.0;
        double force = 0.8 + power * 1.5;
        Vec3 center = caster.position().add(look.scale(radius * 0.5));
        AABB area = AABB.ofSize(center, radius * 2, radius * 2, radius * 2);

        level.getEntitiesOfClass(LivingEntity.class, area, e -> e != caster)
            .forEach(entity -> {
                Vec3 kick = look.scale(force).add(0, 0.4 + power * 0.4, 0);
                entity.setDeltaMovement(kick);
                entity.hurtMarked = true;
            });
    }
}
