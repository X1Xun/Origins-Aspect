package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.codec.CombinedCodecs;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record SpawnEffectCloudAction(ResourceReference radius, ResourceReference radiusOnUse,
                                     ResourceReference waitTime,
                                     List<MobEffectInstance> effect) implements EntityAction {
    public static final MapCodec<SpawnEffectCloudAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceReference.FLOAT_CODEC.optionalFieldOf("radius", ResourceReference.number(3)).forGetter(SpawnEffectCloudAction::radius),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("radius_on_use", ResourceReference.number(-0.5)).forGetter(SpawnEffectCloudAction::radiusOnUse),
            ResourceReference.INT_CODEC.optionalFieldOf("wait_time", ResourceReference.number(10)).forGetter(SpawnEffectCloudAction::waitTime),
            CombinedCodecs.MOB_EFFECT_INSTANCE.optionalFieldOf("effect", List.of()).forGetter(SpawnEffectCloudAction::effect)
    ).apply(i, SpawnEffectCloudAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        if (source.level() instanceof ServerLevel serverLevel)
            EntityType.AREA_EFFECT_CLOUD.spawn(serverLevel, c -> {
                c.setRadius(this.radius.resolveFloat(source));
                c.setRadiusOnUse(this.radiusOnUse.resolveFloat(source));
                c.setWaitTime(this.waitTime.resolveInt(source));
                this.effect.stream().map(MobEffectInstance::new).forEach(c::addEffect);
            }, source.blockPosition(), MobSpawnType.MOB_SUMMONED, false, false);
    }
}
