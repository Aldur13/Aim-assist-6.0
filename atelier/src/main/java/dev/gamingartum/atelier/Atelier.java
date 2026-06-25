package dev.gamingartum.atelier;

import dev.gamingartum.atelier.mana.ManaAttachment;
import dev.gamingartum.atelier.network.ModPayloads;
import dev.gamingartum.atelier.registry.ModComponents;
import dev.gamingartum.atelier.registry.ModCreativeTab;
import dev.gamingartum.atelier.registry.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Atelier implements ModInitializer {
    public static final String MOD_ID = "atelier";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        ModItems.initialize();
        ModComponents.initialize();
        ModCreativeTab.initialize();
        ModPayloads.initialize();

        ServerTickEvents.END_SERVER_TICK.register(server ->
            server.getPlayerList().getPlayers().forEach(ManaAttachment::tickRegen)
        );

        LOGGER.info("Atelier initialized.");
    }
}
