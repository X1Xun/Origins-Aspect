package com.iafenvoy.origins.mixin;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MobEffect.class)
public abstract class InstantHealthMixin {

    @Inject(method = "applyInstantenousEffect", at = @At("HEAD"), cancellable = true)
    private void origins$healToDamage(
            @org.jetbrains.annotations.Nullable Entity source,
            @org.jetbrains.annotations.Nullable Entity indirectSource,
            LivingEntity target,
            int amplifier,
            double effectiveness,
            CallbackInfo ci) {

        // 1. Приводим текущий экземпляр миксина к MobEffect
        MobEffect currentEffect = (MobEffect) (Object) this;

        // 2. Безопасно проверяем через встроенный метод встроенных маппингов 1.21.1,
        // является ли обрабатываемый эффект Мгновенным Исцелением
        if (!currentEffect.equals(MobEffects.HEAL.value())) {
            return;
        }

        // 3. Проверяем, что цель — игрок на стороне сервера
        if (target instanceof Player && !target.level().isClientSide()) {

            // Считаем урон: базовый 4.0F на уровень эффекта (усилок 0 = 4 урона, усилок 1 = 8 урона)
            float baseDamage = 4.0F * (amplifier + 1);

            // Учитываем эффективность взрыва (для обычного питья она всегда 1.0)
            float finalDamage = (float) (baseDamage * effectiveness);

            // Наносим магический урон
            target.hurt(target.damageSources().magic(), finalDamage);

            // Полностью отменяем ванильное восстановление здоровья
            ci.cancel();
        }
    }
}
