package com.iafenvoy.origins.data.power.builtin.regular;

import com.google.common.collect.ImmutableSet;
import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.KeySettings;
import com.iafenvoy.origins.data.badge.Badge;
import com.iafenvoy.origins.data.badge.PresetBadges;
import com.iafenvoy.origins.data.power.HasCooldownPower;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.Toggleable;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class WarlockMainPower extends HasCooldownPower implements Toggleable {

    public enum warlock_powers {
        INFERNAL, EARTH, ANCIENT, UNDEAD, PILLAGER, DRAGON, WIND;

        public static final Codec<warlock_powers> CODEC = Codec.stringResolver(warlock_powers::name, warlock_powers::valueOf);
    }

    public static final MapCodec<WarlockMainPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CooldownSettings.CODEC.forGetter(HasCooldownPower::getCooldown),
            KeySettings.CODEC.forGetter(WarlockMainPower::getKey),
            warlock_powers.CODEC.optionalFieldOf("default_power", warlock_powers.UNDEAD).forGetter(WarlockMainPower::getDefaultPower)
    ).apply(i, WarlockMainPower::new));

    private final KeySettings key;
    private final warlock_powers defaultPower;

    public WarlockMainPower(BaseSettings settings, CooldownSettings cooldown, KeySettings key, warlock_powers defaultPower) {
        super(settings, cooldown);
        this.key = key;
        this.defaultPower = defaultPower;
    }

    public warlock_powers getDefaultPower() {
        return this.defaultPower;
    }

    public void setCurrentPower(OriginDataHolder holder, warlock_powers power) {
        Entity entity = holder.getEntity();
        if (entity != null) {
            // тут в NBT тэг записывается
            entity.getPersistentData().putString("WarlockCurrentPower", power.name());

            try {
                holder.sync();
            } catch (Exception ignored) {}
        }
    }

    // этот метод обрабатывает клавишу
    @Override
    public KeySettings getKey() {
        return this.key;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

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
    public java.util.Optional<com.iafenvoy.origins.data._common.HudRender> getHudRenderData() {
        if (net.neoforged.fml.loading.FMLEnvironment.dist.isDedicatedServer()) {
            return super.getHudRenderData();
        }

        return super.getHudRenderData().map(baseHud -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            warlock_powers playerPower = this.defaultPower;

            if (mc.player != null) {
                playerPower = CLIENT_POWER_CACHE.getOrDefault(mc.player.getUUID(), this.defaultPower);
            }

            int targetBarIndex = switch (playerPower) {
                case EARTH -> 4;
                case UNDEAD -> 3;
                case INFERNAL -> 2;
                case DRAGON -> 1;
                case ANCIENT -> 0;
                case PILLAGER -> 5;
                case WIND -> 6;
            };


            return new com.iafenvoy.origins.data._common.HudRender(
                    baseHud.shouldRenderInActive(),
                    baseHud.spriteLocation(),
                    targetBarIndex,
                    baseHud.iconIndex(),
                    baseHud.condition(),
                    baseHud.inverted(),
                    baseHud.order()
            );
        });
    }

    private static final java.util.Map<java.util.UUID, warlock_powers> CLIENT_POWER_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public warlock_powers getCurrentPower(OriginDataHolder holder) {
        Entity entity = holder.getEntity();
        if (entity != null) {
            if (entity.level().isClientSide()) {
                return CLIENT_POWER_CACHE.getOrDefault(entity.getUUID(), this.defaultPower);
            }

            net.minecraft.nbt.CompoundTag tag = entity.getPersistentData();
            if (tag.contains("WarlockCurrentPower")) {
                try {
                    warlock_powers power = warlock_powers.valueOf(tag.getString("WarlockCurrentPower"));
                    CLIENT_POWER_CACHE.put(entity.getUUID(), power);
                    return power;
                } catch (IllegalArgumentException e) {
                    return this.defaultPower;
                }
            }
        }
        return this.defaultPower;
    }

    private static final java.util.Map<java.util.UUID, Long> SCROLL_COOLDOWN_CACHE = new java.util.concurrent.ConcurrentHashMap<>();

    public static int getScrollCooldownRemaining(net.minecraft.world.entity.player.Player player) {
        long currentTime = player.level().getGameTime();
        long lastUsedTime = SCROLL_COOLDOWN_CACHE.getOrDefault(player.getUUID(), 0L);

        long ticksPassed = currentTime - lastUsedTime;
        if (lastUsedTime != 0 && ticksPassed < 1200) {
            return (int) ((1200 - ticksPassed) / 20);
        }
        return 0;
    }

    public static void changePowerForPlayer(net.minecraft.world.entity.player.Player player, warlock_powers newPower) {
        player.getPersistentData().putString("WarlockCurrentPower", newPower.name());
        CLIENT_POWER_CACHE.put(player.getUUID(), newPower);


        if (!player.level().isClientSide()) {
            SCROLL_COOLDOWN_CACHE.put(player.getUUID(), player.level().getGameTime());
        }
    }

    @Override
    public void toggle(@NotNull OriginDataHolder holder, String key) {
        if (!this.key.match(key) || !this.isActive(holder)) return;

        Entity entity = holder.getEntity();
        if (entity != null && entity.level() instanceof ServerLevel serverLevel) {

            var cooldownComponent = this.getCooldownComponent(holder);
            if (cooldownComponent.getValue() <= 0) {

                int currentCooldownTicks = 20; //заглушка. на всякий случай.
                warlock_powers activePower = this.getCurrentPower(holder);
                switch (activePower) {
                    //стихия земли
                    case EARTH -> {
                        if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                            living.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                    net.minecraft.world.effect.MobEffects.REGENERATION, 600, 2, false, true));
                        }

                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
                                entity.getX(), entity.getY() + 1.0D, entity.getZ(), 30, 0.5D, 0.5D, 0.5D, 0.1D);

                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.BONE_MEAL_USE, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        currentCooldownTicks = 1200;
                    }
                    //драконья. не сделана. вроде её вырезать надо.
                    case DRAGON -> {
                        currentCooldownTicks = 600;
                    }
                    //Свиток мертвеца - призыв 4-х зомби, минута
                    case UNDEAD -> {
                        net.minecraft.world.scores.Scoreboard scoreboard = serverLevel.getScoreboard();
                        String teamName = entity.getName().getString() + "_minions";
                        net.minecraft.world.scores.PlayerTeam warlockTeam = scoreboard.getPlayerTeam(teamName);

                        if (warlockTeam == null) {
                            warlockTeam = scoreboard.addPlayerTeam(teamName);
                            warlockTeam.setSeeFriendlyInvisibles(true);
                            warlockTeam.setAllowFriendlyFire(false);
                        }

                        scoreboard.addPlayerToTeam(entity.getScoreboardName(), warlockTeam);

                        for (int i = 0; i < 4; i++) {
                            net.minecraft.world.entity.monster.Zombie zombie =
                                    net.minecraft.world.entity.EntityType.ZOMBIE.create(serverLevel);

                            if (zombie != null) {
                                double angle = i * (Math.PI / 2.0D);
                                double spawnX = entity.getX() + Math.cos(angle) * 2.0D;
                                double spawnZ = entity.getZ() + Math.sin(angle) * 2.0D;
                                double spawnY = entity.getY() + 0.5D;

                                zombie.setPos(spawnX, spawnY, spawnZ);

                                net.minecraft.core.BlockPos zombiePos = zombie.blockPosition();
                                zombie.finalizeSpawn(serverLevel, serverLevel.getCurrentDifficultyAt(zombiePos),
                                        net.minecraft.world.entity.MobSpawnType.MOB_SUMMONED, null);

                                zombie.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                                        net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 6000, 0, false, false));

                                zombie.addTag("warlock_minion");
                                zombie.addTag("owner_" + entity.getUUID().toString());

                                scoreboard.addPlayerToTeam(zombie.getScoreboardName(), warlockTeam);
                                serverLevel.addFreshEntity(zombie);

                                serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL,
                                        zombie.getX(), zombie.getY() + 1.0D, zombie.getZ(), 10, 0.3D, 0.5D, 0.3D, 0.05D);
                            }
                        }

                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.ZOMBIE_VILLAGER_CONVERTED, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.8F);

                        currentCooldownTicks = 1200;
                    }
                    case ANCIENT -> {
                        net.minecraft.world.phys.HitResult hit = net.minecraft.world.entity.projectile.ProjectileUtil.getHitResultOnViewVector(
                                entity, e -> !e.isSpectator() && e.isPickable(), 150.0D);

                        Vec3 eyePos = entity.getEyePosition();
                        Vec3 lookDir = entity.getLookAngle();

                        Vec3 endPoint = hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS ? hit.getLocation() : eyePos.add(lookDir.scale(15.0D));

                        double currentDistance = eyePos.distanceTo(endPoint);

                        for (int step = 1; step <= (int) currentDistance; step++) {
                            double pX = eyePos.x + (lookDir.x * step);
                            double pY = eyePos.y + (lookDir.y * step);
                            double pZ = eyePos.z + (lookDir.z * step);

                            serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.SONIC_BOOM,
                                    pX, pY, pZ,
                                    1, 0.0D, 0.0D, 0.0D, 0.0D);
                        }

                        if (hit instanceof net.minecraft.world.phys.EntityHitResult entityHit) {
                            net.minecraft.world.entity.Entity target = entityHit.getEntity();

                            target.hurt(serverLevel.damageSources().sonicBoom(entity), 20.0F);

                            target.setDeltaMovement(target.getDeltaMovement().add(lookDir.x * 1.5D, 0.5D, lookDir.z * 1.5D));
                            target.hurtMarked = true;
                        }

                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.WARDEN_SONIC_BOOM, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        currentCooldownTicks = 600;
                    }
                    //это вроде ваще не добавляли никогда, но мне как-то похуй, удалять лень.
                    case INFERNAL -> {
                        Vec3 lookDir = entity.getLookAngle();
                        Vec3 launchPos = entity.getEyePosition().add(lookDir.scale(1.5D));

                        for (double i = -1; i <= 1; i+=0.5) {
                            double spreadX = lookDir.x + (i * 0.15D);
                            double spreadZ = lookDir.z + (i * 0.15D);

                            net.minecraft.world.entity.projectile.SmallFireball fireball =
                                    new net.minecraft.world.entity.projectile.SmallFireball(net.minecraft.world.entity.EntityType.SMALL_FIREBALL, serverLevel);
                            fireball.setOwner(entity);
                            fireball.setPos(launchPos.x + (i * 0.4D), launchPos.y, launchPos.z);
                            fireball.setDeltaMovement(spreadX, lookDir.y, spreadZ);
                            serverLevel.addFreshEntity(fireball);
                        }

                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.FLAME,
                                entity.getX(), entity.getY() + 1.0D, entity.getZ(),
                                25, 0.5D, 0.5D, 0.5D, 0.1D);

                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.LAVA,
                                entity.getX(), entity.getY() + 1.0D, entity.getZ(),
                                5, 0.3D, 0.3D, 0.3D, 0.0D);
                        
                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.BLAZE_SHOOT, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        currentCooldownTicks = 100;
                    }
                    //челюсти хихих
                    case PILLAGER -> {
                        Vec3 look = entity.getLookAngle();
                        double startX = entity.getX();
                        double startY = entity.getY();
                        double startZ = entity.getZ();

                        for (int i = 1; i <= 28; i++) {
                            double spawnX = startX + look.x * (i * 0.75D);
                            double spawnZ = startZ + look.z * (i * 0.75D);

                            net.minecraft.core.BlockPos pos = net.minecraft.core.BlockPos.containing(spawnX, startY, spawnZ);
                            while (serverLevel.getBlockState(pos).isAir() && pos.getY() > serverLevel.getMinBuildHeight()) {
                                pos = pos.below();
                            }
                            double spawnY = pos.getY() + 1.0D;
                            net.minecraft.world.entity.projectile.EvokerFangs fangs =
                                    new net.minecraft.world.entity.projectile.EvokerFangs(serverLevel, spawnX, spawnY, spawnZ,
                                            entity.getYRot(), i, (net.minecraft.world.entity.LivingEntity) entity);

                            serverLevel.addFreshEntity(fangs);
                        }

                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.EVOKER_FANGS_ATTACK, net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 1.0F);

                        currentCooldownTicks = 600;
                    }
                    //ветрянной
                    case WIND -> {
                        Vec3 lookDir = entity.getLookAngle();

                        entity.setDeltaMovement(lookDir.x * 1.8D, lookDir.y, lookDir.z * 1.8D);
                        entity.hurtMarked = true;

                        serverLevel.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,
                                entity.getX(), entity.getY() + 0.5D, entity.getZ(), 40, 0.5D, 0.5D, 0.5D, 0.2D);
                        serverLevel.sendParticles(ParticleTypes.SOUL,
                                entity.getX(), entity.getY() + 1.0D, entity.getZ(), 3, 0.2D, 0.2D, 0.2D, 0.0D);

                        serverLevel.playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                                net.minecraft.sounds.SoundEvents.BREEZE_SHOOT, net.minecraft.sounds.SoundSource.PLAYERS, 1.5F, 1.0F);

                        java.util.List<Entity> nearby = serverLevel.getEntities(entity, entity.getBoundingBox().inflate(4.0D),
                                e -> e instanceof net.minecraft.world.entity.LivingEntity && !e.isAlliedTo(entity));

                        for (Entity target : nearby) {
                            if (target instanceof net.minecraft.world.entity.LivingEntity livingTarget) {
                                Vec3 knockbackDir = target.position().subtract(entity.position()).normalize();
                                livingTarget.setDeltaMovement(knockbackDir.x * 1.2D, 0.6D, knockbackDir.z * 1.2D);
                                livingTarget.hurtMarked = true;

                                livingTarget.hurt(serverLevel.damageSources().fall(), 4.0F);
                            }
                        }

                        currentCooldownTicks = 100;
                    }

                }

                cooldownComponent.setValue(currentCooldownTicks);
            }
        }
    }
}
