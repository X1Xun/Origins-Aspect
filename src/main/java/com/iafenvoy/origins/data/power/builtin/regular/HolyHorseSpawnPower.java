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
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.Horse;
import net.minecraft.world.entity.animal.horse.Variant;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class HolyHorseSpawnPower extends HasCooldownPower implements Toggleable {
    public static int timer = 2000;
    public static final MapCodec<HolyHorseSpawnPower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            CooldownSettings.CODEC.forGetter(HasCooldownPower::getCooldown),
            Codec.DOUBLE.fieldOf("speed").orElse(15.0D).forGetter(HolyHorseSpawnPower::getSpeed),
            Codec.DOUBLE.fieldOf("time").orElse(4.0D).forGetter(HolyHorseSpawnPower::getTime),
            KeySettings.CODEC.forGetter(HolyHorseSpawnPower::getKey)
    ).apply(i, HolyHorseSpawnPower::new));

    private final double speed;
    private final double time;
    private final KeySettings key;

    public HolyHorseSpawnPower(BaseSettings settings, CooldownSettings cooldown, double time, double speed, KeySettings key) {
        super(settings, cooldown);
        this.time = time;
        this.speed = speed;
        this.key = key;
    }

    public double getTime() { return this.time; }
    public double getSpeed() { return this.speed; }

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
    public void toggle(@NotNull OriginDataHolder holder, String key) {
        if (!this.isActive(holder) || !this.key.match(key)) return;

        timer = 2000;
        Entity owner = holder.getEntity();
        if (owner != null && !owner.level().isClientSide() && owner.level() instanceof ServerLevel serverLevel) {
            if (owner.isPassenger()) {
                owner.stopRiding();
            }
            Horse divineSteed = EntityType.HORSE.create(serverLevel);
            if (divineSteed != null) {
                divineSteed.setVariant(Variant.WHITE);
                divineSteed.moveTo(owner.getX(), owner.getY(), owner.getZ(), owner.getYRot(), owner.getXRot());
                divineSteed.setTamed(true);
                divineSteed.getInventory().setItem(0, new ItemStack(Items.SADDLE));
                divineSteed.addTag("paladin_steed");
                int ticksToLive = (int) (this.getTime() * 20);
                divineSteed.getPersistentData().putInt("despawn_timer", ticksToLive);
                serverLevel.addFreshEntity(divineSteed);
                owner.startRiding(divineSteed, true);
                this.getCooldownComponent(holder).startCooldown();
            }
        }
    }
}
