package dev.anvilcraft.ping.util;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class HitUtil {
    public static @Nullable HitResult pick(Entity entity, double interactionRange, boolean allowEmpty, boolean allowPicked) {
        double d1 = Mth.square(interactionRange);
        Vec3 vec3 = entity.getEyePosition(1.0F);
        if (
            !allowPicked
            && Minecraft.getInstance().hitResult != null
            && Minecraft.getInstance().hitResult.getType() != HitResult.Type.MISS
        ) {
            return null;
        }
        HitResult hitresult = entity.pick(interactionRange, 1.0F, false);
        double d2 = hitresult.getLocation().distanceToSqr(vec3);
        if (hitresult.getType() != HitResult.Type.MISS) {
            d1 = d2;
        } else if (!allowEmpty) {
            hitresult = null;
        }

        Vec3 vec31 = entity.getViewVector(1.0F);
        Vec3 vec32 = vec3.add(vec31.x() * interactionRange, vec31.y() * interactionRange, vec31.z() * interactionRange);
        AABB aabb = entity.getBoundingBox().expandTowards(vec31.scale(interactionRange)).inflate(1.0, 1.0, 1.0);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(
            entity,
            vec3,
            vec32,
            aabb,
            entity1 -> (!entity1.isSpectator() && entity1.isPickable()) || entity1 instanceof ItemEntity,
            d1
        );
        return entityHitResult != null && entityHitResult.getLocation().distanceToSqr(vec3) < d2
               ? filterHitResult(entityHitResult, vec3, interactionRange, allowEmpty)
               : filterHitResult(hitresult, vec3, interactionRange, allowEmpty);
    }

    private static @Nullable HitResult filterHitResult(
        @Nullable HitResult hitResult,
        Vec3 pos,
        double blockInteractionRange,
        boolean allowEmpty
    ) {
        if (hitResult == null) return null;
        Vec3 vec3 = hitResult.getLocation();
        if (!vec3.closerThan(pos, blockInteractionRange)) {
            if (!allowEmpty) return null;
            BlockPos containing = BlockPos.containing(new Vec3(vec3.x() - pos.x(), vec3.y() - pos.y(), vec3.z() - pos.z()));
            Direction direction = Direction.getNearest(containing, Direction.UP);
            return BlockHitResult.miss(vec3, direction, BlockPos.containing(vec3));
        } else {
            return hitResult;
        }
    }
}
