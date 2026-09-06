package com.iafenvoy.origins.data.action.builtin.item.meta;

import com.iafenvoy.origins.data.action.ItemAction;
import com.iafenvoy.origins.data.condition.ItemCondition;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record WhileAction(ItemCondition condition, ItemAction action) implements ItemAction {
    public static final MapCodec<WhileAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ItemCondition.CODEC.fieldOf("condition").forGetter(WhileAction::condition),
            ItemAction.CODEC.fieldOf("action").forGetter(WhileAction::action)
    ).apply(instance, WhileAction::new));

    @Override
    public @NotNull MapCodec<? extends ItemAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Level level, @NotNull Entity source, @NotNull SlotAccess access) {
        int repetitions = 0;
        int maxRepetitions = ResourceReference.maxActionIterations();
        while (this.condition.test(level, access.get())) {
            if (repetitions++ < maxRepetitions)
                this.action.execute(level, source, access);
            else
                throw new IllegalStateException("An \"origins:while\" loop was not terminated within " + maxRepetitions + " loops!");
        }
    }
}
