package com.iafenvoy.origins.util.math;

import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Maps expression variable names to resource ids.
 */
public record VariableSerializer(Map<String, ResourceLocation> variables) {
    private static final Pattern VARIABLE_NAME = Pattern.compile("[A-Za-z_][A-Za-z0-9_]*");
    private static final Codec<Map<String, ResourceLocation>> RAW_CODEC = Codec.unboundedMap(Codec.STRING, ResourceLocation.CODEC);
    public static final Codec<VariableSerializer> CODEC = RAW_CODEC.comapFlatMap(VariableSerializer::decode, VariableSerializer::variables);
    public static final MapCodec<VariableSerializer> FIELD_CODEC = CODEC.optionalFieldOf("variables", new VariableSerializer(Map.of()));

    public VariableSerializer {
        variables = Map.copyOf(variables);
    }

    private static DataResult<VariableSerializer> decode(Map<String, ResourceLocation> values) {
        for (String name : values.keySet()) {
            if (!VARIABLE_NAME.matcher(name).matches())
                return DataResult.error(() -> "Invalid variable name: " + name);
        }
        return DataResult.success(new VariableSerializer(values));
    }

    public boolean hasVariable(String name) {
        return this.variables.containsKey(name);
    }

    public double value(String name, Entity entity) {
        ResourceLocation id = this.variables.get(name);
        return id == null ? 0 : ResourceValueHelper.value(entity, id);
    }
}
