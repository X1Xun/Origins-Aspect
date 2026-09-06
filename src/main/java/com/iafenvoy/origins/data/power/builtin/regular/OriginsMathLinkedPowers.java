package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.mixin.accessor.BiomeAccessor;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ReadOnlyScoreInfo;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;

/**
 * Implementations of origins-math's game-state linked resources.
 */
public final class OriginsMathLinkedPowers {
    private OriginsMathLinkedPowers() {
    }

    public static class PlayerLinkedResourcePower extends LinkedResourcePower {
        public static final MapCodec<PlayerLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BaseSettings.CODEC.forGetter(Power::getSettings),
                PlayerProperty.CODEC.fieldOf("property").forGetter(x -> x.property)
        ).apply(i, PlayerLinkedResourcePower::new));
        private final PlayerProperty property;

        private PlayerLinkedResourcePower(BaseSettings settings, PlayerProperty property) {
            super(settings);
            this.property = property;
        }

        @Override
        protected double supply(OriginDataHolder h) {
            Entity e = h.getEntity();
            if (!(e instanceof Player p)) return 0;
            return switch (this.property) {
                case FOOD_LEVEL -> p.getFoodData().getFoodLevel();
                case SATURATION -> p.getFoodData().getSaturationLevel();
                case HEALTH -> p.getHealth();
                case RELATIVE_HEALTH -> p.getMaxHealth() == 0 ? 0 : p.getHealth() / p.getMaxHealth();
                case ABSORPTION -> p.getAbsorptionAmount();
                case BREATHING -> p.getAirSupply();
                case FIRE_TICKS -> p.getRemainingFireTicks();
                case FROZEN_TICKS -> p.getTicksFrozen();
                case FREEZING_SCALE -> p.getPercentFrozen();
                case EXP_LEVEL -> p.experienceLevel;
                case EXP_POINTS -> p.totalExperience;
                case EXP_SCORE -> p.getScore();
                case SLEEP_TIMER -> p.getSleepTimer();
                case STUCK_ARROWS -> p.getArrowCount();
                case FALL_DISTANCE -> p.fallDistance;
                case TIME_OF_DAY -> p.level().getDayTime() % 24000L;
                case TIME -> p.level().getGameTime();
                case LUNAR_TIME -> p.level().getDayTime();
                case LIGHT_LEVEL -> p.level().getMaxLocalRawBrightness(p.blockPosition());
                case BLOCK_LIGHT_LEVEL -> p.level().getBrightness(LightLayer.BLOCK, p.blockPosition());
                case SKY_LIGHT_LEVEL -> p.level().getBrightness(LightLayer.SKY, p.blockPosition());
                case AGE -> p.tickCount;
                case X -> p.getX();
                case Y -> p.getY();
                case Z -> p.getZ();
                case VELOCITY_X -> p.getDeltaMovement().x;
                case VELOCITY_Y -> p.getDeltaMovement().y;
                case VELOCITY_Z -> p.getDeltaMovement().z;
                // Entity roll was removed from the 1.21.1 API. Keep the documented property
                // decodable for data-pack compatibility with its only safe value on this version.
                case PITCH -> p.getXRot();
                case YAW -> p.getYRot();
                case ROLL -> 0;
            };
        }

        @Override
        protected MapCodec<? extends LinkedResourcePower> codecImpl() {
            return CODEC;
        }

        private enum PlayerProperty implements StringRepresentable {
            FOOD_LEVEL, SATURATION, HEALTH, RELATIVE_HEALTH, ABSORPTION, BREATHING, FIRE_TICKS, FROZEN_TICKS,
            FREEZING_SCALE, EXP_LEVEL, EXP_POINTS, EXP_SCORE, SLEEP_TIMER, STUCK_ARROWS, FALL_DISTANCE,
            TIME_OF_DAY, TIME, LUNAR_TIME, LIGHT_LEVEL, BLOCK_LIGHT_LEVEL, SKY_LIGHT_LEVEL, AGE, X, Y, Z,
            VELOCITY_X, VELOCITY_Y, VELOCITY_Z, PITCH, YAW, ROLL;
            private static final Codec<PlayerProperty> CODEC = StringRepresentable.fromEnum(PlayerProperty::values);

            @Override
            public @NotNull String getSerializedName() {
                return this.name().toLowerCase(Locale.ROOT);
            }
        }
    }

    public static class LivingEntityLinkedResourcePower extends LinkedResourcePower {
        public static final MapCodec<LivingEntityLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BaseSettings.CODEC.forGetter(Power::getSettings),
                LivingEntityProperty.CODEC.fieldOf("property").forGetter(x -> x.property)
        ).apply(i, LivingEntityLinkedResourcePower::new));
        private final LivingEntityProperty property;

        private LivingEntityLinkedResourcePower(BaseSettings settings, LivingEntityProperty property) {
            super(settings);
            this.property = property;
        }

        @Override
        protected double supply(OriginDataHolder h) {
            if (!(h.getEntity() instanceof LivingEntity e)) return 0;
            return switch (this.property) {
                case HEALTH -> e.getHealth();
                case RELATIVE_HEALTH -> e.getMaxHealth() == 0 ? 0 : e.getHealth() / e.getMaxHealth();
                case ABSORPTION -> e.getAbsorptionAmount();
                case BREATHING -> e.getAirSupply();
                case FIRE_TICKS -> e.getRemainingFireTicks();
                case FROZEN_TICKS -> e.getTicksFrozen();
                case FREEZING_SCALE -> e.getPercentFrozen();
                case STUCK_ARROWS -> e.getArrowCount();
                case FALL_DISTANCE -> e.fallDistance;
                case TIME_OF_DAY -> e.level().getDayTime() % 24000L;
                case TIME -> e.level().getGameTime();
                case LUNAR_TIME -> e.level().getDayTime();
                case LIGHT_LEVEL -> e.level().getMaxLocalRawBrightness(e.blockPosition());
                case BLOCK_LIGHT_LEVEL -> e.level().getBrightness(LightLayer.BLOCK, e.blockPosition());
                case SKY_LIGHT_LEVEL -> e.level().getBrightness(LightLayer.SKY, e.blockPosition());
                case AGE -> e.tickCount;
                case X -> e.getX();
                case Y -> e.getY();
                case Z -> e.getZ();
                case VELOCITY_X -> e.getDeltaMovement().x;
                case VELOCITY_Y -> e.getDeltaMovement().y;
                case VELOCITY_Z -> e.getDeltaMovement().z;
                case PITCH -> e.getXRot();
                case YAW -> e.getYRot();
                case ROLL -> 0;
            };
        }

        @Override
        protected MapCodec<? extends LinkedResourcePower> codecImpl() {
            return CODEC;
        }

        private enum LivingEntityProperty implements StringRepresentable {
            HEALTH, RELATIVE_HEALTH, ABSORPTION, BREATHING, FIRE_TICKS, FROZEN_TICKS, FREEZING_SCALE,
            STUCK_ARROWS, FALL_DISTANCE, TIME_OF_DAY, TIME, LUNAR_TIME, LIGHT_LEVEL, BLOCK_LIGHT_LEVEL,
            SKY_LIGHT_LEVEL, AGE, X, Y, Z, VELOCITY_X, VELOCITY_Y, VELOCITY_Z, PITCH, YAW, ROLL;
            private static final Codec<LivingEntityProperty> CODEC = StringRepresentable.fromEnum(LivingEntityProperty::values);

            @Override
            public @NotNull String getSerializedName() {
                return this.name().toLowerCase(Locale.ROOT);
            }
        }
    }

    public static class AttributeLinkedResourcePower extends LinkedResourcePower {
        public static final MapCodec<AttributeLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BaseSettings.CODEC.forGetter(Power::getSettings),
                Attribute.CODEC.fieldOf("attribute").forGetter(x -> x.attribute),
                AttributeValue.CODEC.optionalFieldOf("value", AttributeValue.TOTAL).forGetter(x -> x.value)
        ).apply(i, AttributeLinkedResourcePower::new));
        private final Holder<Attribute> attribute;
        private final AttributeValue value;

        private AttributeLinkedResourcePower(BaseSettings settings, Holder<Attribute> attribute, AttributeValue value) {
            super(settings);
            this.attribute = attribute;
            this.value = value;
        }

        @Override
        protected double supply(OriginDataHolder h) {
            return h.getEntity() instanceof LivingEntity e && e.getAttributes().hasAttribute(this.attribute) ? (this.value == AttributeValue.BASE ? e.getAttributeBaseValue(this.attribute) : e.getAttributeValue(this.attribute)) : 0;
        }

        @Override
        protected MapCodec<? extends LinkedResourcePower> codecImpl() {
            return CODEC;
        }

        private enum AttributeValue implements StringRepresentable {
            BASE, TOTAL;
            private static final Codec<AttributeValue> CODEC = StringRepresentable.fromEnum(AttributeValue::values);

            @Override
            public @NotNull String getSerializedName() {
                return this.name().toLowerCase(Locale.ROOT);
            }
        }
    }

    public static class CurrentBiomeLinkedResourcePower extends LinkedResourcePower {
        public static final MapCodec<CurrentBiomeLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BaseSettings.CODEC.forGetter(Power::getSettings),
                BiomeProperty.CODEC.fieldOf("property").forGetter(x -> x.property)
        ).apply(i, CurrentBiomeLinkedResourcePower::new));
        private final BiomeProperty property;

        private CurrentBiomeLinkedResourcePower(BaseSettings settings, BiomeProperty property) {
            super(settings);
            this.property = property;
        }

        @Override
        protected double supply(OriginDataHolder h) {
            Biome biome = h.getEntity().level().getBiome(h.getEntity().blockPosition()).value();
            return this.property == BiomeProperty.HUMIDITY
                    ? ((BiomeAccessor) (Object) biome).origins$getClimateSettings().downfall()
                    : biome.getBaseTemperature();
        }

        @Override
        protected MapCodec<? extends LinkedResourcePower> codecImpl() {
            return CODEC;
        }

        private enum BiomeProperty implements StringRepresentable {
            TEMPERATURE, HUMIDITY;
            private static final Codec<BiomeProperty> CODEC = StringRepresentable.fromEnum(BiomeProperty::values);

            @Override
            public @NotNull String getSerializedName() {
                return this.name().toLowerCase(Locale.ROOT);
            }
        }
    }

    public static class StatusEffectLinkedResourcePower extends LinkedResourcePower {
        public static final MapCodec<StatusEffectLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BaseSettings.CODEC.forGetter(Power::getSettings),
                MobEffect.CODEC.fieldOf("effect").forGetter(x -> x.effect),
                StatusEffectProperty.CODEC.fieldOf("property").forGetter(x -> x.property)
        ).apply(i, StatusEffectLinkedResourcePower::new));
        private final Holder<MobEffect> effect;
        private final StatusEffectProperty property;

        private StatusEffectLinkedResourcePower(BaseSettings settings, Holder<MobEffect> effect, StatusEffectProperty property) {
            super(settings);
            this.effect = effect;
            this.property = property;
        }

        @Override
        protected double supply(OriginDataHolder h) {
            if (!(h.getEntity() instanceof LivingEntity e)) return 0;
            MobEffectInstance instance = e.getEffect(this.effect);
            return this.property == StatusEffectProperty.AMPLIFIER ? instance == null ? -1 : instance.getAmplifier() : instance == null ? 0 : instance.getDuration();
        }

        @Override
        protected MapCodec<? extends LinkedResourcePower> codecImpl() {
            return CODEC;
        }

        private enum StatusEffectProperty implements StringRepresentable {
            AMPLIFIER, DURATION;
            private static final Codec<StatusEffectProperty> CODEC = StringRepresentable.fromEnum(StatusEffectProperty::values);

            @Override
            public @NotNull String getSerializedName() {
                return this.name().toLowerCase(Locale.ROOT);
            }
        }
    }

    public static class ScoreboardLinkedResourcePower extends LinkedResourcePower {
        public static final MapCodec<ScoreboardLinkedResourcePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                BaseSettings.CODEC.forGetter(Power::getSettings), Codec.STRING.fieldOf("objective").forGetter(x -> x.objective)
        ).apply(i, ScoreboardLinkedResourcePower::new));
        private final String objective;

        public ScoreboardLinkedResourcePower(BaseSettings settings, String objective) {
            super(settings);
            this.objective = objective;
        }

        @Override
        protected double supply(OriginDataHolder h) {
            Scoreboard scoreboard = h.getEntity().level().getScoreboard();
            Objective objective = scoreboard.getObjective(this.objective);
            if (objective == null) return 0;
            String holderName = h.getEntity() instanceof Player player
                    ? player.getGameProfile().getName()
                    : h.getEntity().getUUID().toString();
            ReadOnlyScoreInfo score = scoreboard.getPlayerScoreInfo(ScoreHolder.forNameOnly(holderName), objective);
            return score == null ? 0 : score.value();
        }

        @Override
        protected MapCodec<? extends LinkedResourcePower> codecImpl() {
            return CODEC;
        }
    }
}
