package com.iafenvoy.origins.content;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, "origins");
    public static final DeferredHolder<Potion, Potion> STRENGTH_3 = POTIONS.register("stronger_strength",
            () -> new Potion("stronger_strength", new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1800, 2)));
    public static final DeferredHolder<Potion, Potion> REGENERATION_3 = POTIONS.register("stronger_regeneration",
            () -> new Potion("stronger_regeneration", new MobEffectInstance(MobEffects.REGENERATION, 900, 2)));
    public static final DeferredHolder<Potion, Potion> LEAPING_3 = POTIONS.register("stronger_leaping",
            () -> new Potion("stronger_leaping", new MobEffectInstance(MobEffects.JUMP, 1800, 2)));
    public static final DeferredHolder<Potion, Potion> SWIFTNESS_3 = POTIONS.register("stronger_swiftness",
            () -> new Potion("stronger_swiftness", new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1800, 2)));
    public static final DeferredHolder<Potion, Potion> HIGH_GRAVITY = POTIONS.register("high_gravity",
            () -> new Potion("high_gravity", new MobEffectInstance(ModEffects.HIGH_GRAVITY, 3600, 0)));
    public static final DeferredHolder<Potion, Potion> LOW_GRAVITY = POTIONS.register("low_gravity",
            () -> new Potion("low_gravity", new MobEffectInstance(ModEffects.LOW_GRAVITY, 3600, 0)));
    public static final DeferredHolder<Potion, Potion> NO_GRAVITY = POTIONS.register("no_gravity",
            () -> new Potion("no_gravity", new MobEffectInstance(ModEffects.LOW_GRAVITY, 440, 1)));
    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}

