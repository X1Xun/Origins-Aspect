package com.iafenvoy.origins.mixin.accessor;

import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/**
 * Exposes the unmodified climate settings used by origins-math humidity values.
 */
@Mixin(Biome.class)
public interface BiomeAccessor {
    @Accessor("climateSettings")
    Biome.ClimateSettings origins$getClimateSettings();
}
