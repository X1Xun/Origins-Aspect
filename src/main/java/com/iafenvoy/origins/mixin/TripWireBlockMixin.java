package com.iafenvoy.origins.mixin;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.power.builtin.prevent.PreventBlockTriggerPower;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.TripWireBlock;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import java.util.List;

@Mixin(TripWireBlock.class)
public class TripWireBlockMixin {

    @Inject(method = "checkPressed", at = @At("HEAD"), cancellable = true)
    private void hideRangerFromWires(Level level, BlockPos pos, CallbackInfo ci) {
        AABB bounds = level.getBlockState(pos).getShape(level, pos).bounds().move(pos);
        List<Entity> entities = level.getEntitiesOfClass(Entity.class, bounds);

        boolean onlyRangers = false;

        for (Entity entity : entities) {
            var list = PowerHelper.get(entity).listActive(PreventBlockTriggerPower.class);
            if (!list.isEmpty()) {
                list.forEach(p -> p.getEntityAction().execute(entity));
                onlyRangers = true;
            } else {
                onlyRangers = false;
                break;
            }
        }
        if (onlyRangers && !entities.isEmpty()) {
            ci.cancel();
        }
    }
}
