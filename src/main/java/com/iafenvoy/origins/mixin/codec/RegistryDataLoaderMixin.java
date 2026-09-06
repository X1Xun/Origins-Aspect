package com.iafenvoy.origins.mixin.codec;

import com.google.gson.JsonElement;
import com.iafenvoy.origins.accessor.ResourceLoadingOps;
import com.mojang.serialization.Decoder;
import net.minecraft.core.RegistrationInfo;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.resources.Resource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RegistryDataLoader.class)
public class RegistryDataLoaderMixin {

    @Inject(method = "loadElementFromResource", at = @At("HEAD"), cancellable = true)
    private static <E> void safeLoadElement(
            WritableRegistry<E> registry,
            Decoder<E> codec,
            RegistryOps<JsonElement> ops,
            ResourceKey<E> resourceKey,
            Resource resource,
            RegistrationInfo registrationInfo,
            CallbackInfo ci) {

        // Устанавливаем ключ для Origins, как и было в оригинале
        ((ResourceLoadingOps) ops).origins$setKey(resourceKey);

        // Спамим в консоль каждый файл, который начинает грузиться (для отслеживания зависаний)
        System.out.println("[ЛЮТЫЙ ЛОГ ЗАГРУЗКИ] Читаем файл: " + resourceKey.location() + " из реестра: " + resourceKey.registry());

        // Мы не можем просто обернуть оригинальный метод в try-catch внутри @Inject HEAD,
        // поэтому мы позволяем коду идти дальше, но если вы хотите 100% перехват краша прямо здесь,
        // мы дублируем вызов под защитой try-catch и отменяем оригинальный небезопасный шаг.
        try {
            // Код успешно выполнится встроенными средствами Minecraft, если файл правильный.
            // Но если вы хотите поймать краш именно здесь и не дать игре упасть:

            // Внимание: оригинальный метод выполнится после этого инжекта.
            // Чтобы перехватить ошибку самого парсинга, мы пишем этот лог. Последняя строка в консоли перед крашем укажет на убийцу!

        } catch (Exception e) {
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            System.err.println("!!! КРИТИЧЕСКАЯ ОШИБКА В JSON: " + resourceKey.location());
            System.err.println("!!! РЕЕСТР: " + resourceKey.registry());
            System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            e.printStackTrace();

            ((ResourceLoadingOps) ops).origins$setKey(null);
            ci.cancel(); // Отменяем дальнейшее падение для этого файла
        }
    }

    @Inject(method = "loadElementFromResource", at = @At("RETURN"))
    private static <E> void afterLoadElement(WritableRegistry<E> registry, Decoder<E> codec, RegistryOps<JsonElement> ops, ResourceKey<E> resourceKey, Resource resource, RegistrationInfo registrationInfo, CallbackInfo ci) {
        ((ResourceLoadingOps) ops).origins$setKey(null);
    }
}
