package com.iafenvoy.origins.mixin;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.power.builtin.prevent.PreventBlockTriggerPower;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.BasePressurePlateBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import java.util.List;

@Mixin(BasePressurePlateBlock.class)
public class PressurePlateBlockMixin {
    @Inject(method = "getEntityCount", at = @At("RETURN"), cancellable = true)
    private static void removeRangerFromPlateCount(Level level, AABB bounds, Class<?> entityClass, CallbackInfoReturnable<Integer> cir) {
        if (cir.getReturnValue() > 0) {
            List<Entity> entities = level.getEntitiesOfClass(Entity.class, bounds);
            int rangersCount = 0;

            for (Entity entity : entities) {
                var list = PowerHelper.get(entity).listActive(PreventBlockTriggerPower.class);
                if (!list.isEmpty()) {
                    list.forEach(p -> p.getEntityAction().execute(entity));
                    rangersCount++;
                }
            }
            int finalCount = cir.getReturnValue() - rangersCount;
            cir.setReturnValue(Math.max(0, finalCount));
        }
    }
}
