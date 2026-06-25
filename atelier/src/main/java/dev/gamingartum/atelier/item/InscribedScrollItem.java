package dev.gamingartum.atelier.item;

import dev.gamingartum.atelier.registry.ModComponents;
import dev.gamingartum.atelier.spell.SpellData;
import dev.gamingartum.atelier.spell.SpellRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

import java.util.function.Consumer;

public final class InscribedScrollItem extends Item {

    public InscribedScrollItem(Properties props) {
        super(props);
    }

    @Override
    public InteractionResult use(Level level, Player user, InteractionHand hand) {
        ItemStack stack = user.getItemInHand(hand);
        SpellData data = stack.get(ModComponents.SPELL_DATA);
        if (data == null) return InteractionResult.PASS;

        if (level instanceof ServerLevel serverLevel && user instanceof ServerPlayer serverPlayer) {
            boolean cast = SpellRegistry.tryCast(serverLevel, serverPlayer, data);
            if (cast) {
                stack.shrink(1);
                return InteractionResult.SUCCESS;
            } else {
                serverPlayer.sendSystemMessage(
                    Component.literal("Not enough mana!").withStyle(style -> style.withColor(0xFF4444))
                );
                return InteractionResult.FAIL;
            }
        }
        return InteractionResult.SUCCESS; // client acknowledges
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> lines, TooltipFlag flag) {
        SpellData data = stack.get(ModComponents.SPELL_DATA);
        if (data != null) {
            lines.accept(Component.translatable("tooltip.atelier.spell", data.spellId().getPath())
                .withStyle(style -> style.withColor(0xAAAAFF)));
            lines.accept(Component.translatable("tooltip.atelier.power", data.tier().name())
                .withStyle(style -> style.withColor(0x88AAFF)));
        }
    }
}
