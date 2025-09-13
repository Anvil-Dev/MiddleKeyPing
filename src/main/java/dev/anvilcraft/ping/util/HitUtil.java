package dev.anvilcraft.ping.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class HitUtil {
    public static HitResult pick(Entity entity, double interactionRange) {
        double d1 = Mth.square(interactionRange);
        Vec3 vec3 = entity.getEyePosition(1.0F);
        HitResult hitresult = entity.pick(interactionRange, 1.0F, false);
        double d2 = hitresult.getLocation().distanceToSqr(vec3);
        if (hitresult.getType() != HitResult.Type.MISS) {
            d1 = d2;
            interactionRange = Math.sqrt(d2);
        }

        Vec3 vec31 = entity.getViewVector(1.0F);
        Vec3 vec32 = vec3.add(vec31.x * interactionRange, vec31.y * interactionRange, vec31.z * interactionRange);
        AABB aabb = entity.getBoundingBox().expandTowards(vec31.scale(interactionRange)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityhitresult = ProjectileUtil.getEntityHitResult(
            entity,
            vec3,
            vec32,
            aabb,
            p_234237_ -> !p_234237_.isSpectator() && p_234237_.isPickable(),
            d1
        );
        return entityhitresult != null && entityhitresult.getLocation().distanceToSqr(vec3) < d2
               ? filterHitResult(entityhitresult, vec3, interactionRange)
               : filterHitResult(hitresult, vec3, interactionRange);
    }

    private static HitResult filterHitResult(HitResult hitResult, Vec3 pos, double blockInteractionRange) {
        Vec3 vec3 = hitResult.getLocation();
        if (!vec3.closerThan(pos, blockInteractionRange)) {
            Vec3 vec31 = hitResult.getLocation();
            Direction direction = Direction.getNearest(vec31.x - pos.x, vec31.y - pos.y, vec31.z - pos.z);
            return BlockHitResult.miss(vec31, direction, BlockPos.containing(vec31));
        } else {
            return hitResult;
        }
    }
}
