package com.iafenvoy.origins.data.power.component.builtin;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.builtin.regular.DamageOverTimePower;
import com.iafenvoy.origins.data.power.component.PowerComponent;
import com.iafenvoy.origins.data.power.reference.PowerHolder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

/**
 * Persists the active and inactive durations used by {@link DamageOverTimePower}.
 */
public final class DamageOverTimeComponent extends PowerComponent {
    public static final MapCodec<DamageOverTimeComponent> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.INT.optionalFieldOf("in_damage_ticks", 0).forGetter(DamageOverTimeComponent::inDamageTicks),
            Codec.INT.optionalFieldOf("out_damage_ticks", 0).forGetter(DamageOverTimeComponent::outDamageTicks)
    ).apply(instance, DamageOverTimeComponent::new));

    private int inDamageTicks;
    private int outDamageTicks;

    public DamageOverTimeComponent() {
        this(0, 0);
    }

    public DamageOverTimeComponent(int inDamageTicks, int outDamageTicks) {
        this.inDamageTicks = inDamageTicks;
        this.outDamageTicks = outDamageTicks;
    }

    public int inDamageTicks() {
        return this.inDamageTicks;
    }

    public int outDamageTicks() {
        return this.outDamageTicks;
    }

    public void reset() {
        if (this.inDamageTicks == 0 && this.outDamageTicks == 0) return;
        this.inDamageTicks = 0;
        this.outDamageTicks = 0;
        this.markDirty();
    }

    @Override
    public void tick(OriginDataHolder holder, PowerHolder parent) {
        if (!(parent.power() instanceof DamageOverTimePower power)) return;

        boolean persist = false;
        if (power.isActive(holder)) {
            if (this.outDamageTicks != 0) {
                this.outDamageTicks = 0;
                persist = true;
            }
            int damageBegin = power.getDamageBegin(holder);
            // Match Apoli's post-increment ordering: the onset check uses the
            // previous tick count while interval alignment uses the incremented
            // count.
            int damageTicks = this.inDamageTicks++;
            int elapsedTicks = this.inDamageTicks - damageBegin;
            if (!holder.getEntity().level().isClientSide()
                    && damageTicks - damageBegin >= 0
                    && (power.getInterval() <= 0 || elapsedTicks % power.getInterval() == 0))
                power.damage(holder);
            persist |= this.inDamageTicks % 20 == 0;
        } else if (this.outDamageTicks >= 20) {
            if (this.inDamageTicks != 0) {
                this.inDamageTicks = 0;
                persist = true;
            }
        } else {
            this.outDamageTicks++;
            persist = this.outDamageTicks == 20;
        }
        if (persist) this.markDirty();
    }

    @Override
    public @NotNull MapCodec<? extends PowerComponent> codec() {
        return CODEC;
    }
}
