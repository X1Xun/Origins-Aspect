package com.iafenvoy.origins.util.math;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public record Modifier(double value, ModifierOperation operation, Optional<ResourceLocation> resource,
                       List<Modifier> modifier) {
    // origins-math calls the numeric field "amount", while older Origins
    // data used "value". Read both fields in one map codec: using two
    // alternatives with optional fields is ambiguous because the first
    // alternative would accept a legacy object with its default amount of
    // zero. Encoding is canonicalized to the amount field below.
    public static final Codec<Modifier> CODEC = Codec.recursive(Modifier.class.getSimpleName(), Modifier::modifierCodec);

    private static Codec<Modifier> modifierCodec(Codec<Modifier> nested) {
        return RecordCodecBuilder.create(i -> i.group(
                Codec.DOUBLE.optionalFieldOf("amount").forGetter(modifier -> Optional.of(modifier.value())),
                Codec.DOUBLE.optionalFieldOf("value").forGetter(modifier -> Optional.empty()),
                ModifierOperation.CODEC.optionalFieldOf("operation", ModifierOperation.ADD_BASE_EARLY).forGetter(Modifier::operation),
                ResourceLocation.CODEC.optionalFieldOf("resource").forGetter(Modifier::resource),
                Codec.either(nested, nested.listOf()).xmap(value -> value.map(List::of, list -> list), value -> value.size() == 1 ? Either.left(value.getFirst()) : Either.right(value))
                        .optionalFieldOf("modifier", List.of()).forGetter(Modifier::modifier)
        ).apply(i, (amount, legacyValue, operation, resource, modifier) ->
                new Modifier(amount.or(() -> legacyValue).orElse(0D), operation, resource, modifier)));
    }

    public double getValue(OriginDataHolder holder) {
        return this.resource.filter(id -> ResourceValueHelper.hasResource(holder.getEntity(), id)).map(id -> ResourceValueHelper.value(holder.getEntity(), id))
                .map(x -> this.modifier.isEmpty() ? x : applyModifiers(holder, this.modifier, x))
                .orElse(this.value);
    }

    //Event only invoke when call with ModifierPowerHelper
    public static int applyModifiers(OriginDataHolder holder, List<Modifier> modifiers, int value) {
        return (int) applyModifiers(holder, modifiers, (double) value);
    }

    public static float applyModifiers(OriginDataHolder holder, List<Modifier> modifiers, float value) {
        return (float) applyModifiers(holder, modifiers, (double) value);
    }

    public static double applyModifiers(OriginDataHolder holder, List<Modifier> modifiers, double value) {
        Map<ModifierOperation, DoubleList> modifierMap = new EnumMap<>(ModifierOperation.class);
        modifiers.forEach(m -> modifierMap.computeIfAbsent(m.operation(), op -> new DoubleArrayList()).add(m.getValue(holder)));
        double base = value;
        ModifierOperation.Phase phase = ModifierOperation.Phase.BASE;
        for (ModifierOperation operation : ModifierOperation.ORDERED) {
            DoubleList values = modifierMap.get(operation);
            if (values == null) continue;
            if (operation.phase != phase) {
                phase = operation.phase;
                base = value;
            }
            value = operation.apply(values, base, value);
        }
        return value;
    }

    public static Modifier fromAttributeModifier(AttributeModifier attributeModifier) {
        return new Modifier(attributeModifier.amount(), switch (attributeModifier.operation()) {
            case ADD_VALUE -> ModifierOperation.ADD_BASE_EARLY;
            case ADD_MULTIPLIED_BASE -> ModifierOperation.MULTIPLY_BASE_MULTIPLICATIVE;
            case ADD_MULTIPLIED_TOTAL -> ModifierOperation.MULTIPLY_TOTAL_MULTIPLICATIVE;
        }, Optional.empty(), List.of());
    }

    public enum ModifierOperation implements StringRepresentable {
        ADD_BASE_EARLY(Phase.BASE, 0),
        STANDARD_MULTIPLY_BASE(Phase.BASE, 33),
        STANDARD_DIVIDE_BASE(Phase.BASE, 66),
        MULTIPLY_BASE_ADDITIVE(Phase.BASE, 100),
        MULTIPLY_BASE_MULTIPLICATIVE(Phase.BASE, 200),
        ADD_BASE_LATE(Phase.BASE, 300),
        MIN_BASE(Phase.BASE, 400),
        MAX_BASE(Phase.BASE, 500),
        SET_BASE(Phase.BASE, 600),
        ADD_TOTAL_EARLY(Phase.TOTAL, 0),
        STANDARD_MULTIPLY_TOTAL(Phase.TOTAL, 33),
        STANDARD_DIVIDE_TOTAL(Phase.TOTAL, 66),
        MULTIPLY_TOTAL_ADDITIVE(Phase.TOTAL, 100),
        MULTIPLY_TOTAL_MULTIPLICATIVE(Phase.TOTAL, 200),
        ADD_TOTAL_LATE(Phase.TOTAL, 300),
        MIN_TOTAL(Phase.TOTAL, 400),
        MAX_TOTAL(Phase.TOTAL, 500),
        SET_TOTAL(Phase.TOTAL, 600);
        /**
         * Accept both vanilla-style names and namespaced origins operation ids.
         */
        public static final Codec<ModifierOperation> CODEC = Codec.STRING.comapFlatMap(ModifierOperation::decode, ModifierOperation::getSerializedName);

        private static DataResult<ModifierOperation> decode(String serialized) {
            String name = serialized;
            int separator = serialized.indexOf(':');
            if (separator >= 0) {
                String namespace = serialized.substring(0, separator);
                if (!namespace.equals("origins") && !namespace.equals("origins-math") && !namespace.equals("apoli-math"))
                    return DataResult.error(() -> "Unknown modifier operation namespace: " + namespace);
                name = serialized.substring(separator + 1);
            }
            for (ModifierOperation operation : values())
                if (operation.getSerializedName().equals(name)) return DataResult.success(operation);
            return DataResult.error(() -> "Unknown modifier operation: " + serialized);
        }

        private static final ModifierOperation[] ORDERED = Arrays.stream(values())
                .sorted(Comparator.comparing((ModifierOperation operation) -> operation.phase).thenComparingInt(operation -> operation.order))
                .toArray(ModifierOperation[]::new);
        private final Phase phase;
        private final int order;

        ModifierOperation(Phase phase, int order) {
            this.phase = phase;
            this.order = order;
        }

        private double apply(DoubleList values, double base, double current) {
            double sum = values.doubleStream().sum();
            return switch (this) {
                case ADD_BASE_EARLY, ADD_BASE_LATE, ADD_TOTAL_EARLY, ADD_TOTAL_LATE -> current + sum;
                case MULTIPLY_BASE_ADDITIVE, MULTIPLY_TOTAL_ADDITIVE -> current + base * sum;
                case MULTIPLY_BASE_MULTIPLICATIVE, MULTIPLY_TOTAL_MULTIPLICATIVE -> current * (1.0 + sum);
                case STANDARD_MULTIPLY_BASE, STANDARD_MULTIPLY_TOTAL -> current * sum;
                case STANDARD_DIVIDE_BASE, STANDARD_DIVIDE_TOTAL -> current / sum;
                case MIN_BASE, MIN_TOTAL -> values.doubleStream().reduce(current, Math::max);
                case MAX_BASE, MAX_TOTAL -> values.doubleStream().reduce(current, Math::min);
                case SET_BASE, SET_TOTAL -> values.doubleStream().reduce(current, (a, b) -> b);
            };
        }

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        private enum Phase {
            BASE,
            TOTAL
        }
    }
}
