package com.iafenvoy.origins.data.action.builtin.bientity.meta;

import com.iafenvoy.origins.data.action.BiEntityAction;
import com.iafenvoy.origins.util.Timeout;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record DelayAction(BiEntityAction action, ResourceReference ticks) implements BiEntityAction {
    public static final MapCodec<DelayAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiEntityAction.CODEC.fieldOf("action").forGetter(DelayAction::action),
            ResourceReference.INT_CODEC.fieldOf("ticks").forGetter(DelayAction::ticks)
    ).apply(i, DelayAction::new));

    @Override
    public @NotNull MapCodec<? extends BiEntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source, @NotNull Entity target) {
        Timeout.create(this.ticks.resolveInt(source), () -> this.action.execute(source, target));
    }
}
