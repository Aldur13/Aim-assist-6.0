package dev.gamingartum.atelier.network;

import dev.gamingartum.atelier.Atelier;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

public record ScribeSpellPayload(Identifier spellId, float power) implements CustomPacketPayload {

    public static final CustomPacketPayload.Type<ScribeSpellPayload> TYPE =
        new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath(Atelier.MOD_ID, "scribe_spell"));

    public static final StreamCodec<ByteBuf, ScribeSpellPayload> CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, ScribeSpellPayload::spellId,
        ByteBufCodecs.FLOAT, ScribeSpellPayload::power,
        ScribeSpellPayload::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
