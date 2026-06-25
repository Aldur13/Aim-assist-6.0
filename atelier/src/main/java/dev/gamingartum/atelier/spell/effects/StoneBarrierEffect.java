package dev.gamingartum.atelier.spell.effects;

import dev.gamingartum.atelier.spell.SpellEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public final class StoneBarrierEffect implements SpellEffect {

    public static final StoneBarrierEffect INSTANCE = new StoneBarrierEffect();

    @Override
    public void cast(ServerLevel level, ServerPlayer caster, float power) {
        Vec3 look = caster.getLookAngle().normalize();
        int height = 2 + (int)(power * 2); // 2-4 blocks tall
        int width = 1 + (int)(power * 2);  // 1-3 blocks wide
        double distance = 2.0;

        // Barrier face perpendicular to look direction (use right vector)
        Vec3 right = new Vec3(-look.z, 0, look.x).normalize();
        Vec3 basePos = caster.position().add(look.scale(distance));

        for (int w = -width / 2; w <= width / 2; w++) {
            for (int h = 0; h < height; h++) {
                Vec3 pos = basePos.add(right.scale(w)).add(0, h, 0);
                BlockPos bp = BlockPos.containing(pos.x, pos.y, pos.z);
                if (level.isEmptyBlock(bp)) {
                    level.setBlock(bp, Blocks.STONE.defaultBlockState(), 3);
                }
            }
        }
    }
}
