package com.iafenvoy.origins.data.action.builtin.item.meta;

import com.iafenvoy.origins.data.action.ItemAction;
import com.iafenvoy.origins.util.Timeout;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record DelayAction(ItemAction action, ResourceReference ticks) implements ItemAction {
    public static final MapCodec<DelayAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ItemAction.CODEC.fieldOf("action").forGetter(DelayAction::action),
            ResourceReference.INT_CODEC.fieldOf("ticks").forGetter(DelayAction::ticks)
    ).apply(i, DelayAction::new));

    @Override
    public @NotNull MapCodec<? extends ItemAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull Entity source, @NotNull SlotAccess access) {
        Timeout.create(this.ticks.resolveInt(source), () -> this.action.execute(level, source, access));
    }
}
