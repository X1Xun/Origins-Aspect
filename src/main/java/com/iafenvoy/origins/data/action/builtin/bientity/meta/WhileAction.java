package com.iafenvoy.origins.data.action.builtin.bientity.meta;

import com.iafenvoy.origins.data.action.BiEntityAction;
import com.iafenvoy.origins.data.condition.BiEntityCondition;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record WhileAction(BiEntityCondition condition, BiEntityAction action) implements BiEntityAction {
    public static final MapCodec<WhileAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BiEntityCondition.CODEC.fieldOf("condition").forGetter(WhileAction::condition),
            BiEntityAction.CODEC.fieldOf("action").forGetter(WhileAction::action)
    ).apply(instance, WhileAction::new));

    @Override
    public @NotNull MapCodec<? extends BiEntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source, @NotNull Entity target) {
        int repetitions = 0;
        int maxRepetitions = ResourceReference.maxActionIterations();
        while (this.condition.test(source, target)) {
            if (repetitions++ < maxRepetitions)
                this.action.execute(source, target);
            else
                throw new IllegalStateException("An \"origins:while\" loop was not terminated within " + maxRepetitions + " loops!");
        }
    }
}
