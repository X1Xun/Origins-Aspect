package com.iafenvoy.origins.data.condition.builtin.item;

import com.iafenvoy.origins.data.condition.ItemCondition;
import com.iafenvoy.origins.accessor.EntityLinkedItemStack;
import com.iafenvoy.origins.util.math.Comparison;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public record BaseEnchantmentCondition(Holder<Enchantment> enchantment, Comparison comparison,
                                       ResourceReference compareTo) implements ItemCondition {
    public static final MapCodec<BaseEnchantmentCondition> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Enchantment.CODEC.fieldOf("enchantment").forGetter(BaseEnchantmentCondition::enchantment),
            Comparison.CompareOperation.CODEC.optionalFieldOf("comparison", Comparison.CompareOperation.GREATER_THAN_OR_EQUAL).forGetter(condition -> condition.comparison().comparison()),
            ResourceReference.INT_CODEC.fieldOf("compare_to").forGetter(BaseEnchantmentCondition::compareTo)
    ).apply(instance, (enchantment, operation, compareTo) -> new BaseEnchantmentCondition(enchantment, new Comparison(operation, 0), compareTo)));

    @Override
    public @NotNull MapCodec<? extends ItemCondition> codec() {
        return CODEC;
    }

    @Override
    public boolean test(@NotNull Level level, @NotNull ItemStack stack) {
        Entity holder = EntityLinkedItemStack.getEntity(stack);
        return this.comparison.comparison().compare(stack.getTagEnchantments().getLevel(this.enchantment), this.compareTo.resolveInt(holder));
    }
}
