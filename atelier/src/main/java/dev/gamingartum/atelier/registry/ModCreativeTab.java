package dev.gamingartum.atelier.registry;

import dev.gamingartum.atelier.Atelier;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public final class ModCreativeTab {

    public static final ResourceKey<CreativeModeTab> ATELIER_TAB_KEY = ResourceKey.create(
        BuiltInRegistries.CREATIVE_MODE_TAB.key(),
        Identifier.fromNamespaceAndPath(Atelier.MOD_ID, "atelier")
    );

    public static final CreativeModeTab ATELIER_TAB = FabricCreativeModeTab.builder()
        .icon(() -> new ItemStack(ModItems.MAGIC_PAPER))
        .title(Component.translatable("creativeTab.atelier.atelier"))
        .displayItems((params, output) -> {
            output.accept(ModItems.MAGIC_PAPER);
            output.accept(ModItems.CONJURERS_INK);
            output.accept(ModItems.WITCHS_HANDBOOK);
            output.accept(ModItems.INSCRIBED_SCROLL);
        })
        .build();

    public static void initialize() {
        Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, ATELIER_TAB_KEY, ATELIER_TAB);
    }
}
