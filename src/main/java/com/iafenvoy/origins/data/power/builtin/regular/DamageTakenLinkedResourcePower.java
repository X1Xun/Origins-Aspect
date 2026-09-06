package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.PowerHelper;
import com.iafenvoy.origins.data._common.helper.ModifierPowerHelper;
import com.iafenvoy.origins.data.action.BiEntityAction;
import com.iafenvoy.origins.data.condition.DamageCondition;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.util.codec.CombinedCodecs;
import com.iafenvoy.origins.util.math.Modifier;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@EventBusSubscriber
public final class DamageTakenLinkedResourcePower extends TimedLinkedResourcePower implements ModifierPowerHelper {
    public static final MapCodec<DamageTakenLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.forGetter(Power::getSettings), Codec.INT.optionalFieldOf("duration", Integer.MAX_VALUE).forGetter(DamageTakenLinkedResourcePower::duration),
            DamageCondition.optionalCodec("damage_condition").forGetter(DamageTakenLinkedResourcePower::damageCondition),
            BiEntityAction.optionalCodec("bientity_action").forGetter(DamageTakenLinkedResourcePower::action),
            ModifierSettings.CODEC.forGetter(DamageTakenLinkedResourcePower::modifierSettings)
    ).apply(instance, (settings, duration, condition, action, modifiers) -> new DamageTakenLinkedResourcePower(settings, duration, condition, action, modifiers.toList())));
    private final DamageCondition damageCondition;
    private final BiEntityAction action;
    private final List<Modifier> modifiers;

    public DamageTakenLinkedResourcePower(BaseSettings settings, int duration, DamageCondition damageCondition, BiEntityAction action, List<Modifier> modifiers) {
        super(settings, duration);
        this.damageCondition = damageCondition;
        this.action = action;
        this.modifiers = modifiers;
    }

    public DamageCondition damageCondition() {
        return this.damageCondition;
    }

    public BiEntityAction action() {
        return this.action;
    }

    @Override
    public List<Modifier> getModifier() {
        return this.modifiers;
    }

    private ModifierSettings modifierSettings() {
        return new ModifierSettings(Optional.empty(), this.modifiers);
    }

    @Override
    protected MapCodec<? extends LinkedResourcePower> codecImpl() {
        return CODEC;
    }

    private record ModifierSettings(Optional<Modifier> modifier, List<Modifier> modifiers) {
        private static final MapCodec<ModifierSettings> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Modifier.CODEC.optionalFieldOf("modifier").forGetter(ModifierSettings::modifier),
                CombinedCodecs.MODIFIER.optionalFieldOf("modifiers", List.of()).forGetter(ModifierSettings::modifiers)
        ).apply(instance, ModifierSettings::new));

        private List<Modifier> toList() {
            // Keep the source order: singular modifier, then modifiers list.
            List<Modifier> result = new ArrayList<>();
            this.modifier.ifPresent(result::add);
            result.addAll(this.modifiers);
            return List.copyOf(result);
        }
    }

    @SubscribeEvent
    public static void recordDamage(LivingDamageEvent.Post event) {
        Entity target = event.getEntity();
        PowerHelper.get(target).execute(DamageTakenLinkedResourcePower.class,
                power -> power.damageCondition == null || power.damageCondition.test(event.getSource(), event.getNewDamage()),
                (holder, power) -> {
                    power.setTemporaryValue(holder, event.getNewDamage());
                    Entity source = event.getSource().getEntity();
                    if (source != null && power.action != null) power.action.execute(target, source);
                });
    }

    @SubscribeEvent(priority = EventPriority.NORMAL)
    public static void modifyDamage(LivingDamageEvent.Pre event) {
        Entity target = event.getEntity();
        PowerHelper helper = PowerHelper.get(target);
        List<Modifier> modifiers = helper.listActive(DamageTakenLinkedResourcePower.class).stream()
                .flatMap(power -> power.modifiers.stream())
                .toList();
        event.setNewDamage(helper.applyModifiers(modifiers, event.getNewDamage()));
    }
}
