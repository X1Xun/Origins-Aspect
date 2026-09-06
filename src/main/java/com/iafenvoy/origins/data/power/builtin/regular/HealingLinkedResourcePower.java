package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;

@EventBusSubscriber
public final class HealingLinkedResourcePower extends TimedLinkedResourcePower {
    public static final MapCodec<HealingLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.forGetter(Power::getSettings), Codec.INT.optionalFieldOf("duration", Integer.MAX_VALUE).forGetter(HealingLinkedResourcePower::duration),
            EntityAction.optionalCodec("entity_action").forGetter(HealingLinkedResourcePower::action)
    ).apply(instance, HealingLinkedResourcePower::new));
    private final EntityAction action;

    public HealingLinkedResourcePower(BaseSettings settings, int duration, EntityAction action) {
        super(settings, duration);
        this.action = action;
    }

    public EntityAction action() {
        return this.action;
    }

    @Override
    protected MapCodec<? extends LinkedResourcePower> codecImpl() {
        return CODEC;
    }

    @SubscribeEvent
    public static void recordHealing(LivingHealEvent event) {
        if (event.isCanceled()) return;
        PowerHelper.get(event.getEntity()).execute(HealingLinkedResourcePower.class,
                (holder, power) -> {
                    power.setTemporaryValue(holder, event.getAmount());
                    if (power.action != null) power.action.execute(event.getEntity());
                });
    }
}
