package com.iafenvoy.origins.data.action.builtin.entity.meta;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record ChanceAction(EntityAction action, ResourceReference chance,
                           EntityAction failAction) implements EntityAction {
    public static final MapCodec<ChanceAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            EntityAction.CODEC.fieldOf("action").forGetter(ChanceAction::action),
            ResourceReference.FLOAT_CODEC.fieldOf("chance").forGetter(ChanceAction::chance),
            EntityAction.optionalCodec("fail_action").forGetter(ChanceAction::failAction)
    ).apply(i, ChanceAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        if (Math.random() < this.chance.resolveFloat(source)) this.action.execute(source);
        else this.failAction.execute(source);
    }
}
