package com.iafenvoy.origins.data.power.builtin.modify;

import com.iafenvoy.origins.data._common.helper.ModifierPowerHelper;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.util.codec.CombinedCodecs;
import com.iafenvoy.origins.util.codec.WildcardCodec;
import com.iafenvoy.origins.util.math.Modifier;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class ResourceModifyingPower extends Power implements ModifierPowerHelper {
    public static final MapCodec<ResourceModifyingPower.Settings> SETTINGS_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            WildcardCodec.INSTANCE.fieldOf("resource").forGetter(Settings::resource),
            Modifier.CODEC.optionalFieldOf("modifier").forGetter(Settings::modifier),
            CombinedCodecs.MODIFIER.optionalFieldOf("modifiers", List.of()).forGetter(Settings::modifiers)
    ).apply(i, (resource, modifier, modifiers) -> new Settings(resource, modifier, mergeModifiers(modifier, modifiers))));
    protected final ResourceLocation resource;
    protected final List<Modifier> modifiers;

    protected ResourceModifyingPower(BaseSettings settings, ResourceLocation resource, List<Modifier> modifiers) {
        super(settings);
        this.resource = resource;
        this.modifiers = modifiers;
    }

    public ResourceLocation getResource() {
        return this.resource;
    }

    @Override
    public List<Modifier> getModifier() {
        return this.modifiers;
    }

    public boolean appliesTo(ResourceLocation id) {
        return this.resource.equals(id);
    }

    @Override
    public @NotNull MapCodec<? extends Power> codec() {
        return this.codecImpl();
    }

    protected abstract MapCodec<? extends ResourceModifyingPower> codecImpl();

    public record Settings(ResourceLocation resource, Optional<Modifier> modifier, List<Modifier> modifiers) {
    }

    private static List<Modifier> mergeModifiers(Optional<Modifier> modifier, List<Modifier> modifiers) {
        List<Modifier> result = new ArrayList<>();
        modifier.ifPresent(result::add);
        result.addAll(modifiers);
        return List.copyOf(result);
    }
}
