package com.iafenvoy.origins.mixin;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.power.builtin.prevent.PreventBlockTriggerPower;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(VibrationSystem.User.class)
public interface SculkIgnoreMixin {
    @Inject(method = "isValidVibration", at = @At("HEAD"), cancellable = true)
    default void ignoreRangerVibrations(Holder<GameEvent> gameEvent, GameEvent.Context context, CallbackInfoReturnable<Boolean> cir) {
        Entity entity = context.sourceEntity();
        if (entity == null) return;

        boolean isWalkingOrFalling = gameEvent.is(GameEvent.STEP.unwrapKey().get())
                || gameEvent.is(GameEvent.HIT_GROUND.unwrapKey().get())
                || gameEvent.is(GameEvent.SPLASH.unwrapKey().get());

        if (isWalkingOrFalling) {
            var list = PowerHelper.get(entity).listActive(PreventBlockTriggerPower.class);
            if (!list.isEmpty()) {
                list.forEach(p -> p.getEntityAction().execute(entity));
                cir.setReturnValue(false);
            }
        }
    }
}
