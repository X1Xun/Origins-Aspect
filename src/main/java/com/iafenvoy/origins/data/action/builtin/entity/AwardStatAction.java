package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data._common.StatReference;
import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.codec.CombinedCodecs;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AwardStatAction(List<StatReference> stats, ResourceReference amount) implements EntityAction {
    public static final MapCodec<AwardStatAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            CombinedCodecs.STAT_REFERENCE.fieldOf("stat").forGetter(AwardStatAction::stats),
            ResourceReference.INT_CODEC.optionalFieldOf("amount", ResourceReference.number(1)).forGetter(AwardStatAction::amount)
    ).apply(i, AwardStatAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        if (!(source instanceof ServerPlayer player)) return;
        for (StatReference ref : this.stats) {
            Stat<?> resolved = ref.resolve();
            if (resolved != null) player.awardStat(resolved, this.amount.resolveInt(source));
        }
    }
}
