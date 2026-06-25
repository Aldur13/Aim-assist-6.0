package dev.gamingartum.atelier.network;

import dev.gamingartum.atelier.registry.ModComponents;
import dev.gamingartum.atelier.registry.ModItems;
import dev.gamingartum.atelier.spell.SpellData;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class ModPayloads {

    public static void initialize() {
        // Register C2S payload type on both sides (required by Fabric networking)
        PayloadTypeRegistry.serverboundPlay().register(ScribeSpellPayload.TYPE, ScribeSpellPayload.CODEC);

        // Server-side handler: validate held items, consume them, produce Inscribed Scroll
        ServerPlayNetworking.registerGlobalReceiver(ScribeSpellPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();

            // Guard: both items must still be held (player can't cheat by swapping)
            if (!player.getMainHandItem().is(ModItems.MAGIC_PAPER)) return;
            if (!player.getOffhandItem().is(ModItems.CONJURERS_INK)) return;
            // Guard: power must be in valid range
            if (payload.power() <= 0 || payload.power() > 1) return;

            // Consume paper (main hand) and one ink (off-hand)
            player.getMainHandItem().shrink(1);
            player.getOffhandItem().shrink(1);

            // Create and give the Inscribed Scroll
            ItemStack scroll = new ItemStack(ModItems.INSCRIBED_SCROLL);
            scroll.set(ModComponents.SPELL_DATA, new SpellData(payload.spellId(), payload.power()));
            if (!player.addItem(scroll)) {
                player.drop(scroll, false);
            }
        });
    }
}
