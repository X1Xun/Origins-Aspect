package com.iafenvoy.origins.registry;

import com.iafenvoy.origins.Origins;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class OriginsSounds {
    // Создаем регистратор для звуковых событий
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, Origins.MOD_ID);

    // Регистрируем конкретный звук. Имя в коде: example_sound
    public static final DeferredHolder<SoundEvent, SoundEvent> BARRIER_AMBIENT = SOUND_EVENTS.register("barrier_ambient",
            () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(Origins.MOD_ID, "barrier_ambient")));

    // Метод для подключения регистратора к шине мода
    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }
}
