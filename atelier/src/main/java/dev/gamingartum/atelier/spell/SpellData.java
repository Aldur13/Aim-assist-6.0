package dev.gamingartum.atelier.spell;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

public record SpellData(Identifier spellId, float power) {

    public static final Codec<SpellData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Identifier.CODEC.fieldOf("spell_id").forGetter(SpellData::spellId),
        Codec.FLOAT.fieldOf("power").forGetter(SpellData::power)
    ).apply(inst, SpellData::new));

    public static final StreamCodec<ByteBuf, SpellData> STREAM_CODEC = StreamCodec.composite(
        Identifier.STREAM_CODEC, SpellData::spellId,
        ByteBufCodecs.FLOAT, SpellData::power,
        SpellData::new
    );

    public GlyphScorer.PowerTier tier() {
        return GlyphScorer.PowerTier.forPower(power);
    }
}
