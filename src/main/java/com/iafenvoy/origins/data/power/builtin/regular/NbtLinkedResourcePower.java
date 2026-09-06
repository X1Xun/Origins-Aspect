package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.commands.arguments.NbtPathArgument;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NumericTag;

/**
 * Reads a numeric value from the holder's serialized entity NBT.
 */
public final class NbtLinkedResourcePower extends LinkedResourcePower {
    public static final MapCodec<NbtLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            NbtPathArgument.NbtPath.CODEC.fieldOf("path").forGetter(NbtLinkedResourcePower::path)
    ).apply(instance, NbtLinkedResourcePower::new));

    private final NbtPathArgument.NbtPath path;

    public NbtLinkedResourcePower(BaseSettings settings, NbtPathArgument.NbtPath path) {
        super(settings);
        this.path = path;
    }

    public NbtPathArgument.NbtPath path() {
        return this.path;
    }

    @Override
    protected double supply(OriginDataHolder holder) {
        try {
            CompoundTag tag = holder.getEntity().saveWithoutId(new CompoundTag());
            return this.path.get(tag).stream().filter(NumericTag.class::isInstance).map(NumericTag.class::cast).findFirst().map(NumericTag::getAsDouble).orElse(0D);
        } catch (Exception exception) {
            return 0D;
        }
    }

    @Override
    protected MapCodec<? extends LinkedResourcePower> codecImpl() {
        return CODEC;
    }
}
