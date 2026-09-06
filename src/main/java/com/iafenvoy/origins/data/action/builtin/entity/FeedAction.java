package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public record FeedAction(ResourceReference food, ResourceReference saturation) implements EntityAction {
    public static final MapCodec<FeedAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceReference.INT_CODEC.fieldOf("food").forGetter(FeedAction::food),
            ResourceReference.FLOAT_CODEC.fieldOf("saturation").forGetter(FeedAction::saturation)
    ).apply(i, FeedAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        if (source instanceof Player player)
            player.getFoodData().eat(this.food.resolveInt(source), this.saturation.resolveFloat(source));
    }
}
