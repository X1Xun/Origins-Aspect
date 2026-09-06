package com.iafenvoy.origins.data.action.builtin.bientity.meta;

import com.iafenvoy.origins.data.action.BiEntityAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public record ChanceAction(BiEntityAction action, ResourceReference chance,
                           BiEntityAction failAction) implements BiEntityAction {
    public static final MapCodec<ChanceAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BiEntityAction.CODEC.fieldOf("action").forGetter(ChanceAction::action),
            ResourceReference.FLOAT_CODEC.fieldOf("chance").forGetter(ChanceAction::chance),
            BiEntityAction.optionalCodec("fail_action").forGetter(ChanceAction::failAction)
    ).apply(i, ChanceAction::new));

    @Override
    public @NotNull MapCodec<? extends BiEntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source, @NotNull Entity target) {
        if (Math.random() < this.chance.resolveFloat(source)) this.action.execute(source, target);
        else this.failAction.execute(source, target);
    }
}
