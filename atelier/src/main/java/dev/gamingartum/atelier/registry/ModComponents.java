package dev.gamingartum.atelier.registry;

import dev.gamingartum.atelier.Atelier;
import dev.gamingartum.atelier.spell.SpellData;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

public final class ModComponents {

    public static final DataComponentType<SpellData> SPELL_DATA = DataComponentType.<SpellData>builder()
        .persistent(SpellData.CODEC)
        .networkSynchronized(SpellData.STREAM_CODEC)
        .build();

    public static void initialize() {
        Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath(Atelier.MOD_ID, "spell_data"),
            SPELL_DATA
        );
    }
}
