package dev.gamingartum.atelier.client;

import dev.gamingartum.atelier.client.hud.ManaHud;
import dev.gamingartum.atelier.client.screen.DrawingScreen;
import dev.gamingartum.atelier.client.screen.HandbookScreen;
import dev.gamingartum.atelier.registry.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;

public class AtelierClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ManaHud.register();
        registerItemUseEvents();
    }

    private static void registerItemUseEvents() {
        UseItemCallback.EVENT.register((player, level, hand) -> {
            if (!level.isClientSide()) {
                return InteractionResult.PASS;
            }
            // Paper + ink → drawing screen
            if (hand == InteractionHand.MAIN_HAND
                    && player.getMainHandItem().is(ModItems.MAGIC_PAPER)
                    && player.getOffhandItem().is(ModItems.CONJURERS_INK)) {
                Minecraft.getInstance().gui.setScreen(new DrawingScreen());
                return InteractionResult.SUCCESS;
            }
            // Handbook → handbook screen
            if (hand == InteractionHand.MAIN_HAND
                    && player.getMainHandItem().is(ModItems.WITCHS_HANDBOOK)) {
                Minecraft.getInstance().gui.setScreen(new HandbookScreen());
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        });
    }
}

