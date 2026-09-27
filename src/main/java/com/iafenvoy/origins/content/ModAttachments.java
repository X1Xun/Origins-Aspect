package com.iafenvoy.origins.content;

import com.iafenvoy.origins.Origins;
import com.mojang.serialization.Codec;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModAttachments {
    public static final DeferredRegister<AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, Origins.MOD_ID);

    // Регистрируем наш числовой тег. Используем Codec.INT для сериализации в NBT.
    public static final Supplier<AttachmentType<Integer>> BLOODY =
            ATTACHMENT_TYPES.register("bloody", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

    public static final Supplier<AttachmentType<Integer>> BLOOD_TIMER =
            ATTACHMENT_TYPES.register("blood_timer", () -> AttachmentType.builder(() -> 0).serialize(Codec.INT).build());

    public static void register(IEventBus eventBus) {
        ATTACHMENT_TYPES.register(eventBus);
    }
}
