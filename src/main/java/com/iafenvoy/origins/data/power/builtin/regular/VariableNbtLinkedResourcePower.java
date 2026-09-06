package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.util.math.VariableSerializer;
import com.iafenvoy.origins.util.math.VariableStringUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;

import java.util.Map;

/**
 * Reads a numeric NBT path after expanding resource variables in the path string.
 */
public final class VariableNbtLinkedResourcePower extends LinkedResourcePower {
    public static final MapCodec<VariableNbtLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Codec.STRING.fieldOf("path").forGetter(VariableNbtLinkedResourcePower::path),
            VariableSerializer.CODEC.optionalFieldOf("variables", new VariableSerializer(Map.of())).forGetter(VariableNbtLinkedResourcePower::variables)
    ).apply(instance, VariableNbtLinkedResourcePower::new));

    private final String path;
    private final VariableSerializer variables;

    public VariableNbtLinkedResourcePower(BaseSettings settings, String path, VariableSerializer variables) {
        super(settings);
        this.path = path;
        this.variables = variables;
    }

    public String path() {
        return this.path;
    }

    public VariableSerializer variables() {
        return this.variables;
    }

    @Override
    protected double supply(OriginDataHolder holder) {
        try {
            NbtPathArgument.NbtPath nbtPath = NbtPathArgument.NbtPath.of(VariableStringUtil.parse(this.path, this.variables, holder.getEntity()));
            CompoundTag tag = holder.getEntity().saveWithoutId(new CompoundTag());
            return nbtPath.get(tag).stream().filter(NumericTag.class::isInstance).map(NumericTag.class::cast).findFirst().map(NumericTag::getAsDouble).orElse(0D);
        } catch (Exception exception) {
            return 0D;
        }
    }

    @Override
    protected MapCodec<? extends LinkedResourcePower> codecImpl() {
        return CODEC;
    }
}
