package com.iafenvoy.origins.content;

import com.iafenvoy.origins.Origins;
import com.iafenvoy.origins.content.HighGravityEffect;
import com.iafenvoy.origins.content.LowGravityEffect;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(Registries.MOB_EFFECT, Origins.MOD_ID);
    public static final DeferredHolder<MobEffect, MobEffect> LOW_GRAVITY = MOB_EFFECTS.register("low_gravity",
            () -> new LowGravityEffect().addAttributeModifier(
                    Attributes.GRAVITY,
                    ResourceLocation.fromNamespaceAndPath(Origins.MOD_ID, "low_gravity"),
                    -0.5D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
    public static final DeferredHolder<MobEffect, MobEffect> HIGH_GRAVITY = MOB_EFFECTS.register("high_gravity",
            () -> new HighGravityEffect().addAttributeModifier(
                    Attributes.GRAVITY,
                    ResourceLocation.fromNamespaceAndPath(Origins.MOD_ID, "high_gravity"),
                    1.0D,
                    AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL
            ));
    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
