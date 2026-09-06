package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.Origins;
import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.component.ComponentCollector;
import com.iafenvoy.origins.data.power.component.PowerComponent;
import com.iafenvoy.origins.data.power.component.builtin.DamageOverTimeComponent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class DamageOverTimePower extends Power {
    /**
     * The default damage type used by origins-math when the field is omitted.
     */
    public static final ResourceKey<DamageType> GENERIC_DAMAGE =
            ResourceKey.create(Registries.DAMAGE_TYPE,
                    ResourceLocation.fromNamespaceAndPath(Origins.MOD_ID, "damage_over_time"));

    public static final MapCodec<DamageOverTimePower> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Codec.INT.optionalFieldOf("interval", 20).forGetter(DamageOverTimePower::getInterval),
            Codec.INT.optionalFieldOf("onset_delay").forGetter(DamageOverTimePower::getOnsetDelay),
            Codec.FLOAT.fieldOf("damage").forGetter(DamageOverTimePower::getDamage),
            Codec.FLOAT.optionalFieldOf("damage_easy").forGetter(DamageOverTimePower::getDamageEasy),
            // Origins-math defaults this field to its generic damage type.
            // Keep it optional and resolve the registry entry at damage time so
            // datapack-provided damage types remain level-aware.
            DamageType.CODEC.optionalFieldOf("damage_type").forGetter(DamageOverTimePower::getDamageType),
            Enchantment.CODEC.optionalFieldOf("protection_enchantment").forGetter(DamageOverTimePower::getProtectionEnchantment),
            Codec.FLOAT.optionalFieldOf("protection_effectiveness", 1.0F).forGetter(DamageOverTimePower::getProtectionEffectiveness)
    ).apply(i, DamageOverTimePower::new));

    private final int interval;
    private final int onsetDelay;
    private final float damage, damageEasy;
    private final Optional<Holder<DamageType>> damageType;
    private final Optional<Holder<Enchantment>> protectionEnchantment;
    private final float protectionEffectiveness;

    public DamageOverTimePower(BaseSettings settings, int interval, Optional<Integer> onsetDelay, float damage, Optional<Float> damageEasy, Optional<Holder<DamageType>> damageType, Optional<Holder<Enchantment>> protectionEnchantment, float protectionEffectiveness) {
        super(settings);
        this.interval = interval;
        this.onsetDelay = onsetDelay.orElse(this.interval);
        this.damage = damage;
        this.damageEasy = damageEasy.orElse(this.damage);
        this.damageType = damageType;
        this.protectionEnchantment = protectionEnchantment;
        this.protectionEffectiveness = protectionEffectiveness;
    }

    public int getInterval() {
        return this.interval;
    }

    public Optional<Integer> getOnsetDelay() {
        return Optional.of(this.onsetDelay);
    }

    public float getDamage() {
        return this.damage;
    }

    public Optional<Float> getDamageEasy() {
        return Optional.of(this.damageEasy);
    }

    public Optional<Holder<DamageType>> getDamageType() {
        return this.damageType;
    }

    public Optional<Holder<Enchantment>> getProtectionEnchantment() {
        return this.protectionEnchantment;
    }

    public float getProtectionEffectiveness() {
        return this.protectionEffectiveness;
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return CODEC;
    }

    @Override
    public int tickInterval() {
        return 1;
    }

    @Override
    public void createComponents(ComponentCollector collector) {
        super.createComponents(collector);
        collector.add(new DamageOverTimeComponent());
    }

    @Override
    public void tick(@NotNull OriginDataHolder holder) {
        ResourceLocation id = this.getId(holder.getAccess());
        Map<Class<? extends PowerComponent>, PowerComponent> components = holder.getData().getComponents().get(id);
        if (components == null || !components.containsKey(DamageOverTimeComponent.class)) {
            Map<Class<? extends PowerComponent>, PowerComponent> updated = new LinkedHashMap<>();
            if (components != null) updated.putAll(components);
            ComponentCollector defaults = ComponentCollector.create();
            this.createComponents(defaults);
            defaults.build().forEach(updated::putIfAbsent);
            holder.getData().getComponents().put(id, updated);
            if (!holder.getEntity().level().isClientSide()) holder.sync();
        }
        super.tick(holder);
    }

    @Override
    public void respawn(OriginDataHolder holder, boolean backFromEnd) {
        holder.getComponentFor(this, DamageOverTimeComponent.class).ifPresent(DamageOverTimeComponent::reset);
    }

    public int getDamageBegin(OriginDataHolder holder) {
        int protection = this.getProtection(holder.getEntity());
        // Apoli truncates the protection factor before converting it to ticks.
        int delay = (int) (Math.pow(protection * 2, 1.3) * this.protectionEffectiveness);
        return this.onsetDelay + delay * 20;
    }

    public void damage(OriginDataHolder holder) {
        Entity entity = holder.getEntity();
        Holder<DamageType> type = this.damageType.orElseGet(() -> entity.level().registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(GENERIC_DAMAGE));
        entity.hurt(new DamageSource(type), entity.level().getDifficulty() == Difficulty.EASY ? this.damageEasy : this.damage);
    }

    private int getProtection(Entity entity) {
        if (!(entity instanceof LivingEntity living) || this.protectionEnchantment.isEmpty()) return 0;
        int accumulated = 0;
        int items = 0;
        // Respect the slots declared by the protection enchantment. The built-in
        // water protection enchantment declares the four standard armor slots.
        // A few older datapacks omit the slot declaration; retain the pre-1.21
        // armor behaviour as a compatibility fallback in that case.
        Map<EquipmentSlot, ItemStack> potentialItems = this.protectionEnchantment.get().value().getSlotItems(living);
        boolean hasSlotDeclaration = Arrays.stream(EquipmentSlot.values())
                .anyMatch(this.protectionEnchantment.get().value()::matchingSlot);
        // getSlotItems returns only non-empty stacks in slots declared by the
        // enchantment. Older datapacks may have no slot declaration at all;
        // in that case inspect the standard armor slots without mutating the
        // map returned by vanilla (its mutability is not part of the API).
        Iterable<ItemStack> armorItems = !hasSlotDeclaration
                ? Stream.of(EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET)
                .map(living::getItemBySlot)
                .filter(stack -> !stack.isEmpty())
                .toList()
                : potentialItems.values();
        for (ItemStack stack : armorItems) {
            int level = EnchantmentHelper.getItemEnchantmentLevel(this.protectionEnchantment.get(), stack);
            accumulated += level;
            if (level > 0) items++;
        }
        return accumulated + items;
    }
}
