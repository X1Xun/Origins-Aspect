package com.iafenvoy.origins.data._common.helper;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.builtin.regular.AttributeLikeResourcePower;
import com.iafenvoy.origins.data.power.reference.PowerHolder;
import com.iafenvoy.origins.data.power.builtin.modify.ModifyAttributeLikeResourcePower;
import com.iafenvoy.origins.util.math.Modifier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Resolves an entity's power resource without exposing the attachment internals to codecs.
 */
public final class ResourceValueHelper {
    private ResourceValueHelper() {
    }

    public static Optional<Power> findPower(Entity entity, ResourceLocation id) {
        OriginDataHolder holder = OriginDataHolder.get(entity);
        if (holder == null) return Optional.empty();
        return powerFor(holder, id);
    }

    public static double value(Entity entity, ResourceLocation id) {
        try {
            return valueOrThrow(entity, id);
        } catch (IllegalArgumentException ignored) {
            return 0D;
        }
    }

    public static double min(Entity entity, ResourceLocation id) {
        try {
            return minOrThrow(entity, id);
        } catch (IllegalArgumentException ignored) {
            return 0D;
        }
    }

    public static double max(Entity entity, ResourceLocation id) {
        try {
            return maxOrThrow(entity, id);
        } catch (IllegalArgumentException ignored) {
            return 0D;
        }
    }

    public static boolean set(Entity entity, ResourceLocation id, double value) {
        try {
            setOrThrow(entity, id, value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean add(Entity entity, ResourceLocation id, double value) {
        try {
            addOrThrow(entity, id, value);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean hasResource(Entity entity, ResourceLocation id) {
        try {
            requireResource(entity, id);
            return true;
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static boolean isMutableResource(Entity entity, ResourceLocation id) {
        try {
            ResourceAccess access = requireResource(entity, id);
            return !(access.power instanceof ResourceValue resource) || resource.isMutable();
        } catch (IllegalArgumentException ignored) {
            return false;
        }
    }

    public static double valueOrThrow(Entity entity, ResourceLocation id) {
        ResourceAccess access = requireResource(entity, id);
        if (access.power instanceof ResourceValue resource) return resource.getDoubleValue(access.holder);
        return ((ResourceHelper) access.power).getValue(access.holder);
    }

    public static double minOrThrow(Entity entity, ResourceLocation id) {
        ResourceAccess access = requireResource(entity, id);
        if (access.power instanceof ResourceValue resource) return resource.getDoubleMin(access.holder);
        return ((ResourceHelper) access.power).getMinValue();
    }

    public static double maxOrThrow(Entity entity, ResourceLocation id) {
        ResourceAccess access = requireResource(entity, id);
        if (access.power instanceof ResourceValue resource) return resource.getDoubleMax(access.holder);
        return ((ResourceHelper) access.power).getMaxValue();
    }

    public static void setOrThrow(Entity entity, ResourceLocation id, double value) {
        ResourceAccess access = requireResource(entity, id);
        if (access.power instanceof ResourceValue resource) resource.setDoubleValue(access.holder, value);
        else ((ResourceHelper) access.power).setValue(access.holder, (int) value);
    }

    public static void addOrThrow(Entity entity, ResourceLocation id, double value) {
        ResourceAccess access = requireResource(entity, id);
        double change = value;
        if (access.power instanceof AttributeLikeResourcePower)
            change = access.holder.getHelper().modify(ModifyAttributeLikeResourcePower.class, power -> power.appliesTo(id), value);
        if (access.power instanceof ResourceValue resource) {
            resource.setDoubleValue(access.holder, resource.getDoubleValue(access.holder) + change);
        } else {
            // Integer resources use origins-math's VariableIntPower semantics:
            // truncate the delta before adding it to the current value.
            ResourceHelper resource = (ResourceHelper) access.power;
            resource.setValue(access.holder, resource.getValue(access.holder) + (int) change);
        }
    }

    private static Optional<Power> powerFor(OriginDataHolder holder, ResourceLocation id) {
        return holder.getAllPowers().stream()
                .filter(power -> power.id().equals(id)
                        || Objects.equals(power.power().getId(holder.getAccess()), id))
                .map(PowerHolder::power)
                .findFirst();
    }

    private static ResourceAccess requireResource(Entity entity, ResourceLocation id) {
        OriginDataHolder holder = OriginDataHolder.get(entity);
        if (holder == null)
            throw new IllegalArgumentException("No Origins data is attached to entity " + entity);
        Power power = powerFor(holder, id).orElseThrow(() -> new IllegalArgumentException("Resource power '" + id + "' does not exist for entity " + entity));
        if (!(power instanceof ResourceValue) && !(power instanceof ResourceHelper))
            throw new IllegalArgumentException("Power '" + id + "' is not a resource power");
        return new ResourceAccess(holder, power);
    }

    private record ResourceAccess(OriginDataHolder holder, Power power) {
    }

    /**
     * Applies all matching power modifiers as one ordered modifier set.
     */
    public static <T extends Power & ModifierPowerHelper> double applyModifiers(OriginDataHolder holder, Class<T> type,
                                                                                Predicate<T> filter, double base) {
        return Modifier.applyModifiers(holder, holder.getHelper().listActive(type, filter).stream()
                .flatMap(power -> power.getModifier().stream())
                .toList(), base);
    }

    /**
     * Optional double-precision resource contract used by origins-math powers.
     */
    public interface ResourceValue {
        double getDoubleValue(OriginDataHolder holder);

        double getDoubleMin(OriginDataHolder holder);

        double getDoubleMax(OriginDataHolder holder);

        default boolean isMutable() {
            return true;
        }

        void setDoubleValue(OriginDataHolder holder, double value);
    }
}
