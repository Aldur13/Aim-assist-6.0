package dev.gamingartum.atelier.registry;

import dev.gamingartum.atelier.Atelier;
import dev.gamingartum.atelier.item.InscribedScrollItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;

import java.util.function.Function;

public final class ModItems {

    public static final Item MAGIC_PAPER = register("magic_paper", Item::new, new Item.Properties().stacksTo(16));
    public static final Item CONJURERS_INK = register("conjurers_ink", Item::new, new Item.Properties().stacksTo(16));
    public static final Item WITCHS_HANDBOOK = register("witchs_handbook", Item::new, new Item.Properties().stacksTo(1));
    public static final Item INSCRIBED_SCROLL = register("inscribed_scroll", InscribedScrollItem::new, new Item.Properties().stacksTo(1));

    private static <T extends Item> T register(String name, Function<Item.Properties, T> factory, Item.Properties props) {
        ResourceKey<Item> key = ResourceKey.create(
            BuiltInRegistries.ITEM.key(),
            Identifier.fromNamespaceAndPath(Atelier.MOD_ID, name)
        );
        T item = factory.apply(props.setId(key));
        Registry.register(BuiltInRegistries.ITEM, key, item);
        return item;
    }

    public static void initialize() {}
}
