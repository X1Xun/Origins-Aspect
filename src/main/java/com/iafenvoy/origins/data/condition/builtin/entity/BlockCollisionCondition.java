package com.iafenvoy.origins.data.condition.builtin.entity;

import com.iafenvoy.origins.data.condition.EntityCondition;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public record BlockCollisionCondition(ResourceReference offsetX, ResourceReference offsetY,
                                      ResourceReference offsetZ) implements EntityCondition {
    public static final MapCodec<BlockCollisionCondition> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceReference.FLOAT_CODEC.optionalFieldOf("offset_x", ResourceReference.number(0)).forGetter(BlockCollisionCondition::offsetX),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("offset_y", ResourceReference.number(0)).forGetter(BlockCollisionCondition::offsetY),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("offset_z", ResourceReference.number(0)).forGetter(BlockCollisionCondition::offsetZ)
    ).apply(i, BlockCollisionCondition::new));

    @Override
    public @NotNull MapCodec<? extends EntityCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull Entity entity) {
        Level level = entity.level();
        AABB bb = entity.getBoundingBox().move(this.offsetX.resolveFloat(entity), this.offsetY.resolveFloat(entity), this.offsetZ.resolveFloat(entity)).deflate(0.001);
        BlockPos min = BlockPos.containing(bb.minX, bb.minY, bb.minZ);
        BlockPos max = BlockPos.containing(bb.maxX, bb.maxY, bb.maxZ);
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
