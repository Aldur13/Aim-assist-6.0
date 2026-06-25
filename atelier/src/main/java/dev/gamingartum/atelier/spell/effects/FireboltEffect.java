package dev.gamingartum.atelier.spell.effects;

import dev.gamingartum.atelier.spell.SpellEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.phys.Vec3;

public final class FireboltEffect implements SpellEffect {

    public static final FireboltEffect INSTANCE = new FireboltEffect();

    @Override
    public void cast(ServerLevel level, ServerPlayer caster, float power) {
        Vec3 look = caster.getLookAngle();
        double speed = 1.0 + power * 2.0;
        Vec3 origin = caster.getEyePosition().add(look.scale(1.2));
        SmallFireball fireball = new SmallFireball(level, caster, look.scale(speed));
        fireball.setPos(origin.x, origin.y, origin.z);
        level.addFreshEntity(fireball);
    }
}
