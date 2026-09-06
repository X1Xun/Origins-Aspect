package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.data.condition.BlockCondition;
import com.iafenvoy.origins.data.condition.EntityCondition;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

//FIXME::Optimize
public record RandomTeleportAction(ResourceReference areaWidth, ResourceReference areaHeight,
                                   Optional<Heightmap.Types> heightmap,
                                   Optional<ResourceReference> attempts, Optional<BlockCondition> landingBlockCondition,
                                   Optional<EntityCondition> landingCondition, Vec3 landingOffset,
                                   boolean loadedChunksOnly, EntityAction successAction,
                                   EntityAction failAction) implements EntityAction {
    public static final MapCodec<RandomTeleportAction> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            ResourceReference.FLOAT_CODEC.optionalFieldOf("area_width", ResourceReference.number(8)).forGetter(RandomTeleportAction::areaWidth),
            ResourceReference.FLOAT_CODEC.optionalFieldOf("area_height", ResourceReference.number(8)).forGetter(RandomTeleportAction::areaHeight),
            Heightmap.Types.CODEC.optionalFieldOf("heightmap").forGetter(RandomTeleportAction::heightmap),
            ResourceReference.INT_CODEC.optionalFieldOf("attempts").forGetter(RandomTeleportAction::attempts),
            BlockCondition.CODEC.optionalFieldOf("landing_block_condition").forGetter(RandomTeleportAction::landingBlockCondition),
            EntityCondition.CODEC.optionalFieldOf("landing_condition").forGetter(RandomTeleportAction::landingCondition),
            Vec3.CODEC.optionalFieldOf("landing_offset", Vec3.ZERO).forGetter(RandomTeleportAction::landingOffset),
            Codec.BOOL.optionalFieldOf("loaded_chunks_only", true).forGetter(RandomTeleportAction::loadedChunksOnly),
            EntityAction.optionalCodec("success_action").forGetter(RandomTeleportAction::successAction),
            EntityAction.optionalCodec("fail_action").forGetter(RandomTeleportAction::failAction)
    ).apply(i, RandomTeleportAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        if (!(source.level() instanceof ServerLevel level)) return;

        RandomSource random = RandomSource.create();
        boolean succeeded = false;
        double x, y, z;
        float areaWidth = this.areaWidth.resolveFloat(source);
        float areaHeight = this.areaHeight.resolveFloat(source);
        int attempts = this.attempts.map(value -> value.resolveInt(source)).orElseGet(() -> (int) (areaWidth * 2 + areaHeight * 2));
        for (int i = 0; i < attempts; i++) {
            x = source.getX() + (random.nextDouble() - 0.5) * areaWidth;
            y = Mth.clamp(source.getY() + (random.nextInt(Math.max((int) areaHeight, 1)) - (areaHeight / 2)), level.getMinBuildHeight(), level.getMinBuildHeight() + (level.getLogicalHeight() - 1));
            z = source.getZ() + (random.nextDouble() - 0.5) * areaWidth;

            if (this.attemptToTeleport(source, level, x, y, z)) {
                this.successAction.execute(source);
                source.resetFallDistance();

                succeeded = true;
                break;
            }
        }
        if (!succeeded) this.failAction.execute(source);
    }

    private boolean attemptToTeleport(Entity entity, ServerLevel serverWorld, double destX, double destY, double destZ) {
        BlockPos.MutableBlockPos destBlockPos = BlockPos.containing(destX, destY, destZ).mutable();
        boolean foundSurface = false;

        if (this.heightmap.isPresent()) {
            destBlockPos.set(serverWorld.getHeightmapPos(this.heightmap.get(), destBlockPos).below());
            foundSurface = this.shouldLandOnBlock(serverWorld, destBlockPos);
            if (foundSurface) destBlockPos.set(destBlockPos.above());
        }

        double areaHeight = this.areaHeight.resolveFloat(entity);
        for (double decrements = 0; !foundSurface && decrements < areaHeight / 2; ++decrements) {
            destBlockPos.set(destBlockPos.below());
            foundSurface = this.shouldLandOnBlock(serverWorld, destBlockPos);
            if (foundSurface) destBlockPos.set(destBlockPos.above());
        }

        if (!foundSurface) return false;

        destX = this.landingOffset.x() == 0 ? destX : Mth.floor(destX) + this.landingOffset.x();
        destY = destBlockPos.getY() + this.landingOffset.y();
        destZ = this.landingOffset.z() == 0 ? destZ : Mth.floor(destZ) + this.landingOffset.z();

        destBlockPos.set(destX, destY, destZ);

        double prevX = entity.getX();
        double prevY = entity.getY();
        double prevZ = entity.getZ();

        ChunkPos destChunkPos = new ChunkPos(destBlockPos);
        if (!this.loadedChunksOnly && !serverWorld.hasChunk(destChunkPos.x, destChunkPos.z)) {
            serverWorld.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, destChunkPos, 0, entity.getId());
            serverWorld.getChunk(destChunkPos.x, destChunkPos.z);
        }

        entity.teleportTo(destX, destY, destZ);

        if (!this.shouldLand(entity)) {
            entity.teleportTo(prevX, prevY, prevZ);
            return false;
        }

        if (entity instanceof PathfinderMob mob) mob.getNavigation().stop();
        return true;
    }

    private boolean shouldLandOnBlock(Level world, BlockPos pos) {
        return this.landingBlockCondition.map(x -> x.test(world, pos)).orElseGet(() -> world.getBlockState(pos).blocksMotion());
    }

    private boolean shouldLand(Entity entity) {
        return this.landingCondition.map(condition -> condition.test(entity)).orElseGet(() -> entity.level().noCollision(entity) && !entity.level().containsAnyLiquid(entity.getBoundingBox()));
    }
}
