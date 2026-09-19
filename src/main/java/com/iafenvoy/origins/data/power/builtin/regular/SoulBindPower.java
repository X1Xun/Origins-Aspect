package com.iafenvoy.origins.data.power.builtin.regular;

import com.google.common.collect.ImmutableSet;
import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.KeySettings;
import com.iafenvoy.origins.data.badge.Badge;
import com.iafenvoy.origins.data.badge.PresetBadges;
import com.iafenvoy.origins.data.power.HasCooldownPower;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.Toggleable;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SoulBindPower extends HasCooldownPower implements Toggleable {

    public static final MapCodec<SoulBindPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CooldownSettings.CODEC.forGetter(HasCooldownPower::getCooldown),
            KeySettings.CODEC.forGetter(SoulBindPower::getKey)
    ).apply(i, SoulBindPower::new));

    private final KeySettings key;
    public static final ConcurrentHashMap<UUID, UUID> ACTIVE_BINDS = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<UUID, Long> BIND_END_TIMES = new ConcurrentHashMap<>();

    public SoulBindPower(BaseSettings settings, CooldownSettings cooldown, KeySettings key) {
        super(settings, cooldown);
        this.key = key;
    }

    @Override
    public KeySettings getKey() { return this.key; }

    @Override
    public @NotNull MapCodec<? extends Power> codec() { return CODEC; }

    @Override
    public void collectBadges(ImmutableSet.Builder<Badge> builder) {
        super.collectBadges(builder);
        builder.add(PresetBadges.ACTIVE);
    }

    @Override
    public boolean isActive(OriginDataHolder holder) {
        return this.getCooldownComponent(holder).canUse();
    }

    @Override
    public void tick(OriginDataHolder holder) {
        super.tick(holder);

        Entity player = holder.getEntity();
        if (player != null && !player.level().isClientSide) {
            UUID playerUUID = player.getUUID();
            Long endTime = BIND_END_TIMES.get(playerUUID);
            if (endTime != null && player.level().getGameTime() >= endTime) {
                ACTIVE_BINDS.remove(playerUUID);
                BIND_END_TIMES.remove(playerUUID);
                player.sendSystemMessage(Component.literal("§6[Связка душ] §cДействие связи душ подошло к концу."));
            }
        }
    }

    @Override
    public void toggle(@NotNull OriginDataHolder holder, String key) {
        if (!this.key.match(key) || !this.isActive(holder)) return;

        this.getCooldownComponent(holder).useIfReady(() -> {
            Entity player = holder.getEntity();
            if (player == null || !(player.level() instanceof ServerLevel serverLevel)) return;

            double maxDistance = 16.0D;
            Vec3 lookVec = player.getLookAngle().scale(maxDistance);
            AABB searchBox = player.getBoundingBox().expandTowards(lookVec).inflate(1.0D);

            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(LivingEntity.class, searchBox,
                    entity -> entity != player && !entity.isSpectator() && entity.isPickable()
            );

            LivingEntity closestTarget = null;
            double closestDist = Double.MAX_VALUE;
            Vec3 eyePos = player.getEyePosition();

            for (LivingEntity entity : targets) {
                double dist = entity.getBoundingBox().distanceToSqr(eyePos);
                if (dist < closestDist) {
                    closestDist = dist;
                    closestTarget = entity;
                }
            }

            if (closestTarget != null) {
                this.getCooldownComponent(holder).startCooldown();
                this.getCooldownComponent(holder).setValue(this.getCooldown().cooldown());
                UUID playerUUID = player.getUUID();
                ACTIVE_BINDS.put(playerUUID, closestTarget.getUUID());
                BIND_END_TIMES.put(playerUUID, serverLevel.getGameTime() + 1200L); // 60 сек

                player.sendSystemMessage(Component.literal("§6[Связка душ] §aВы успешно связали свою душу с §e" + closestTarget.getDisplayName().getString() + "§a на 60 секунд!"));
            } else {
                player.sendSystemMessage(Component.literal("§cВы должны смотреть на живое существо, чтобы связать души!"));
            }
        });
    }
}
