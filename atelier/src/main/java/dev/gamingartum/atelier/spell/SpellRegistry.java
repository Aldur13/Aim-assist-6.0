package dev.gamingartum.atelier.spell;

import dev.gamingartum.atelier.spell.effects.FireboltEffect;
import dev.gamingartum.atelier.spell.effects.FlashEffect;
import dev.gamingartum.atelier.spell.effects.GustEffect;
import dev.gamingartum.atelier.spell.effects.HealingSpringEffect;
import dev.gamingartum.atelier.spell.effects.StoneBarrierEffect;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashMap;
import java.util.Map;

public final class SpellRegistry {

    private static final Map<Identifier, SpellEffectEntry> REGISTRY = new HashMap<>();

    /**
     * Mana cost base: actual cost = baseCost + baseCost * power.
     * A Masterful cast of an expensive spell costs ~2× the base.
     */
    public record SpellEffectEntry(SpellEffect effect, float baseCost) {}

    static {
        register(Identifier.fromNamespaceAndPath("atelier", "firebolt"),      FireboltEffect.INSTANCE,      20f);
        register(Identifier.fromNamespaceAndPath("atelier", "gust"),           GustEffect.INSTANCE,           15f);
        register(Identifier.fromNamespaceAndPath("atelier", "healing_spring"), HealingSpringEffect.INSTANCE, 25f);
        register(Identifier.fromNamespaceAndPath("atelier", "stone_barrier"),  StoneBarrierEffect.INSTANCE,  20f);
        register(Identifier.fromNamespaceAndPath("atelier", "flash"),          FlashEffect.INSTANCE,         18f);
    }

    private static void register(Identifier id, SpellEffect effect, float baseCost) {
        REGISTRY.put(id, new SpellEffectEntry(effect, baseCost));
    }

    /**
     * Attempt to cast from a SpellData. Deducts mana (base + base*power).
     * @return false if spell unknown or insufficient mana
     */
    public static boolean tryCast(ServerLevel level, ServerPlayer player, SpellData data) {
        SpellEffectEntry entry = REGISTRY.get(data.spellId());
        if (entry == null) return false;

        float cost = entry.baseCost() * (1.0f + data.power());
        if (!dev.gamingartum.atelier.mana.ManaAttachment.spend(player, cost)) return false;

        entry.effect().cast(level, player, data.power());
        return true;
    }
}
