package com.iafenvoy.origins.data.action.builtin.item.meta;

import com.iafenvoy.origins.data.action.ItemAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record ForRangeAction(ResourceReference range, ItemAction action) implements ItemAction {
    public static final MapCodec<ForRangeAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ResourceReference.CODEC.fieldOf("range").forGetter(ForRangeAction::range),
            ItemAction.CODEC.fieldOf("action").forGetter(ForRangeAction::action)
    ).apply(instance, ForRangeAction::new));

    @Override
    public @NotNull MapCodec<? extends ItemAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull Entity source, @NotNull SlotAccess access) {
        int count = this.range.resolveIterations(source);
        for (int i = 0; i < count; i++) this.action.execute(level, source, access);
    }
}
