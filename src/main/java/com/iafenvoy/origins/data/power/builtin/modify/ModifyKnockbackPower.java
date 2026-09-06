package com.iafenvoy.origins.data.power.builtin.modify;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.util.codec.CombinedCodecs;
import com.iafenvoy.origins.util.math.Modifier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public final class ModifyKnockbackPower extends Power {
    public static final MapCodec<ModifyKnockbackPower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CombinedCodecs.MODIFIER.optionalFieldOf("x", List.of()).forGetter(ModifyKnockbackPower::getXModifiers),
            CombinedCodecs.MODIFIER.optionalFieldOf("y", List.of()).forGetter(ModifyKnockbackPower::getYModifiers),
            CombinedCodecs.MODIFIER.optionalFieldOf("z", List.of()).forGetter(ModifyKnockbackPower::getZModifiers)
    ).apply(instance, ModifyKnockbackPower::new));

    private final List<Modifier> xModifiers;
    private final List<Modifier> yModifiers;
    private final List<Modifier> zModifiers;

    public ModifyKnockbackPower(BaseSettings settings, List<Modifier> xModifiers, List<Modifier> yModifiers, List<Modifier> zModifiers) {
        super(settings);
        this.xModifiers = List.copyOf(xModifiers);
        this.yModifiers = List.copyOf(yModifiers);
        this.zModifiers = List.copyOf(zModifiers);
    }

    public List<Modifier> getXModifiers() {
        return this.xModifiers;
    }

    public List<Modifier> getYModifiers() {
        return this.yModifiers;
    }

    public List<Modifier> getZModifiers() {
        return this.zModifiers;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    public double modify(OriginDataHolder holder, Direction.Axis axis, double value) {
        return Modifier.applyModifiers(holder, this.getModifiers(axis), value);
    }

    public static double modify(Entity entity, Direction.Axis axis, double value) {
        OriginDataHolder holder = OriginDataHolder.get(entity);
        if (holder == null) return value;
        List<Modifier> modifiers = holder.getHelper().listActive(ModifyKnockbackPower.class).stream()
                .flatMap(power -> power.getModifiers(axis).stream())
                .toList();
        return Modifier.applyModifiers(holder, modifiers, value);
    }

    private List<Modifier> getModifiers(Direction.Axis axis) {
        return switch (axis) {
            case X -> this.xModifiers;
            case Y -> this.yModifiers;
            case Z -> this.zModifiers;
        };
    }
}
