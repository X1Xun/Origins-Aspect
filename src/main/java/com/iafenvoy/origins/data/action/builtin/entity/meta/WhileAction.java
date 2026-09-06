package com.iafenvoy.origins.data.action.builtin.entity.meta;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.data.condition.EntityCondition;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record WhileAction(EntityCondition condition, EntityAction action) implements EntityAction {
    public static final MapCodec<WhileAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            EntityCondition.CODEC.fieldOf("condition").forGetter(WhileAction::condition),
            EntityAction.CODEC.fieldOf("action").forGetter(WhileAction::action)
    ).apply(instance, WhileAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        int repetitions = 0;
        int maxRepetitions = ResourceReference.maxActionIterations();
        while (this.condition.test(source)) {
            if (repetitions++ < maxRepetitions)
                this.action.execute(source);
            else
                throw new IllegalStateException("An \"origins:while\" loop was not terminated within " + maxRepetitions + " loops!");
        }
    }
}
