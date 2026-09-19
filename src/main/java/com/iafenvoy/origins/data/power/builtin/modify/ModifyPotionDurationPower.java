package com.iafenvoy.origins.data.power.builtin.modify;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@EventBusSubscriber
public class ModifyPotionDurationPower extends Power {
    public static final MapCodec<ModifyPotionDurationPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Codec.DOUBLE.fieldOf("multiplier").orElse(1.0).forGetter(ModifyPotionDurationPower::getMultiplier)
    ).apply(i, ModifyPotionDurationPower::new));

    private final double multiplier;

    public ModifyPotionDurationPower(BaseSettings settings, double multiplier) {
        super(settings);
        this.multiplier = multiplier;
    }

    public double getMultiplier() {
        return this.multiplier;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @SubscribeEvent
    public static void onItemUseFinish(LivingEntityUseItemEvent.Finish event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide()) return;

        ItemStack itemStack = event.getItem();
        PotionContents potionContents = itemStack.getOrDefault(net.minecraft.core.component.DataComponents.POTION_CONTENTS, PotionContents.EMPTY);
        var list = PowerHelper.get(entity).listActive(ModifyPotionDurationPower.class);
        if (potionContents.hasEffects() && !list.isEmpty()) {
            for (MobEffectInstance baseEffect : potionContents.getAllEffects()) {
                if (!baseEffect.getEffect().value().isInstantenous() && baseEffect.getDuration() > 0) {

                    int newDuration = baseEffect.getDuration();

                    newDuration = (int) (newDuration * 3);
                    if (newDuration != baseEffect.getDuration()) {
                        entity.removeEffect(baseEffect.getEffect());

                        MobEffectInstance extendedEffect = new MobEffectInstance(
                                baseEffect.getEffect(),
                                newDuration,
                                baseEffect.getAmplifier(),
                                baseEffect.isAmbient(),
                                baseEffect.isVisible(),
                                baseEffect.showIcon()
                        );
                        entity.addEffect(extendedEffect);
                        entity.setAbsorptionAmount(2.0F);
                    }
                }
            }
        }
    }
}