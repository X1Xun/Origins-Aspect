package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.math.Space;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import java.util.function.Consumer;

public record AddVelocityAction(ResourceReference x, ResourceReference y, ResourceReference z, Space space,
                                boolean set) implements EntityAction {
    public static final MapCodec<AddVelocityAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceReference.FLOAT_CODEC.optionalFieldOf("x", ResourceReference.number(0)).forGetter(AddVelocityAction::x),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("y", ResourceReference.number(0)).forGetter(AddVelocityAction::y),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("z", ResourceReference.number(0)).forGetter(AddVelocityAction::z),
            Space.CODEC.optionalFieldOf("space", Space.WORLD).forGetter(AddVelocityAction::space),
            Codec.BOOL.optionalFieldOf("set", false).forGetter(AddVelocityAction::set)
    ).apply(i, AddVelocityAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        Vector3f velocity = new Vector3f(this.x.resolveFloat(source), this.y.resolveFloat(source), this.z.resolveFloat(source));
        Consumer<Vec3> method = this.set ? source::setDeltaMovement : source::addDeltaMovement;
        this.space.toGlobal(velocity, source);
        method.accept(new Vec3(velocity));
        source.hurtMarked = true;
    }
}
