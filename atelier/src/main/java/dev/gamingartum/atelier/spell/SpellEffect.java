package dev.gamingartum.atelier.spell;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

@FunctionalInterface
public interface SpellEffect {
    /**
     * @param level  the server level
     * @param caster the casting player
     * @param power  [0,1] — scales magnitude/range/duration
     */
    void cast(ServerLevel level, ServerPlayer caster, float power);
}
