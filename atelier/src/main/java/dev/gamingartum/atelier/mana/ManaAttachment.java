package dev.gamingartum.atelier.mana;

import com.mojang.serialization.Codec;
import dev.gamingartum.atelier.Atelier;
import io.netty.buffer.ByteBuf;
import net.fabricmc.fabric.api.attachment.v1.AttachmentRegistry;
import net.fabricmc.fabric.api.attachment.v1.AttachmentSyncPredicate;
import net.fabricmc.fabric.api.attachment.v1.AttachmentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public final class ManaAttachment {

    public static final float MAX_MANA = 100.0f;
    private static final float REGEN_PER_TICK = 0.05f; // fills fully in ~33 seconds

    public static final AttachmentType<Float> MANA = AttachmentRegistry.create(
        Atelier.id("mana"),
        builder -> builder
            .persistent(Codec.FLOAT)
            .initializer(() -> MAX_MANA)
            .copyOnDeath()
            .syncWith(ByteBufCodecs.FLOAT, AttachmentSyncPredicate.targetOnly())
    );

    /** Called each server tick per player to regenerate mana. */
    public static void tickRegen(net.minecraft.server.level.ServerPlayer player) {
        float current = player.getAttachedOrCreate(MANA);
        if (current < MAX_MANA) {
            player.setAttached(MANA, Math.min(MAX_MANA, current + REGEN_PER_TICK));
        }
    }

    public static float get(net.minecraft.world.entity.player.Player player) {
        return player.getAttachedOrCreate(MANA);
    }

    /** Returns true if the mana was spent, false if insufficient. */
    public static boolean spend(net.minecraft.world.entity.player.Player player, float amount) {
        float current = player.getAttachedOrCreate(MANA);
        if (current < amount) return false;
        player.setAttached(MANA, current - amount);
        return true;
    }
}
