package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.data.condition.BiEntityCondition;
import com.iafenvoy.origins.util.codec.MiscCodecs;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public record SpawnParticlesAction(ParticleOptions particle, BiEntityCondition biEntityCondition,
                                   ResourceReference count,
                                   ResourceReference speed, boolean force, Vec3 spread, ResourceReference offsetX,
                                   ResourceReference offsetY,
                                   ResourceReference offsetZ) implements EntityAction {
    public static final MapCodec<SpawnParticlesAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            MiscCodecs.PARTICLE_OPTION_OR_SINGLE.fieldOf("particle").forGetter(SpawnParticlesAction::particle),
            BiEntityCondition.optionalCodec("bientity_condition").forGetter(SpawnParticlesAction::biEntityCondition),
            ResourceReference.INT_CODEC.fieldOf("count").forGetter(SpawnParticlesAction::count),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("speed", ResourceReference.number(0)).forGetter(SpawnParticlesAction::speed),
            Codec.BOOL.optionalFieldOf("force", false).forGetter(SpawnParticlesAction::force),
            Vec3.CODEC.optionalFieldOf("spread", new Vec3(0.5, 0.5, 0.5)).forGetter(SpawnParticlesAction::spread),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("offset_x", ResourceReference.number(0)).forGetter(SpawnParticlesAction::offsetX),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("offset_y", ResourceReference.number(0.5)).forGetter(SpawnParticlesAction::offsetY),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("offset_z", ResourceReference.number(0)).forGetter(SpawnParticlesAction::offsetZ)
    ).apply(i, SpawnParticlesAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        if (source.level() instanceof ServerLevel serverLevel) {
            Vec3 delta = this.spread.multiply(source.getBbWidth(), source.getEyeHeight(source.getPose()), source.getBbWidth());
            Vec3 pos = source.position().add(this.offsetX.resolveFloat(source), this.offsetY.resolveFloat(source), this.offsetZ.resolveFloat(source));
            int count = Math.max(0, this.count.resolveInt(source));
            float speed = this.speed.resolveFloat(source);
            // SimpleParticleType implements both ParticleType and ParticleOptions.
            // Complex particle types (dust, block, item) do NOT implement ParticleOptions directly.
            for (ServerPlayer player : serverLevel.players())
                if (this.biEntityCondition.test(source, player))
                    serverLevel.sendParticles(player, this.particle, this.force, pos.x, pos.y, pos.z, count, delta.x, delta.y, delta.z, speed);
        }
    }
}
