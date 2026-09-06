package com.iafenvoy.origins.attachment;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.iafenvoy.origins.data.layer.Layer;
import com.iafenvoy.origins.data.layer.LayerRegistries;
import com.iafenvoy.origins.data.origin.Origin;
import com.iafenvoy.origins.data.origin.OriginRegistries;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.PowerRegistries;
import com.iafenvoy.origins.data.power.component.PowerComponent;
import com.iafenvoy.origins.util.codec.CollectionCodecs;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class EntityOriginAttachment {
    public static final Codec<EntityOriginAttachment> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC).fieldOf("origins").forGetter(EntityOriginAttachment::getSerializedOrigins),
            CollectionCodecs.multiMapCodec(ResourceLocation.CODEC, ResourceLocation.CODEC).fieldOf("powers").forGetter(EntityOriginAttachment::getSerializedPowers),
            CollectionCodecs.ofAutoIgnore(ResourceLocation.CODEC, CollectionCodecs.classMapCodec(PowerComponent.CODEC)).fieldOf("components").forGetter(EntityOriginAttachment::getComponents)
    ).apply(i, EntityOriginAttachment::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, EntityOriginAttachment> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);
    private final Map<Holder<Layer>, Holder<Origin>> origins = new LinkedHashMap<>();
    private final Multimap<ResourceLocation, Holder<Power>> powers = HashMultimap.create();
    private final Map<ResourceLocation, Map<Class<? extends PowerComponent>, PowerComponent>> components = new LinkedHashMap<>();
    private final Map<ResourceLocation, ResourceLocation> unresolvedOrigins = new LinkedHashMap<>();
    private final Multimap<ResourceLocation, ResourceLocation> unresolvedPowers = HashMultimap.create();
    private boolean selecting = false;

    public EntityOriginAttachment() {
    }

    private EntityOriginAttachment(Map<ResourceLocation, ResourceLocation> origins, Multimap<ResourceLocation, ResourceLocation> powers, Map<ResourceLocation, Map<Class<? extends PowerComponent>, PowerComponent>> components) {
        this.unresolvedOrigins.putAll(origins);
        this.unresolvedPowers.putAll(powers);
        this.components.putAll(components);
    }

    public Map<Holder<Layer>, Holder<Origin>> getOrigins() {
        return this.origins;
    }

    public Multimap<ResourceLocation, Holder<Power>> getPowers() {
        return this.powers;
    }

    private Map<ResourceLocation, ResourceLocation> getSerializedOrigins() {
        Map<ResourceLocation, ResourceLocation> result = new LinkedHashMap<>(this.unresolvedOrigins);
        this.origins.forEach((layer, origin) -> result.put(layer.unwrapKey().orElseThrow().location(), origin.unwrapKey().orElseThrow().location()));
        return result;
    }

    private Multimap<ResourceLocation, ResourceLocation> getSerializedPowers() {
        Multimap<ResourceLocation, ResourceLocation> result = HashMultimap.create(this.unresolvedPowers);
        this.powers.forEach((source, power) -> result.put(source, power.unwrapKey().orElseThrow().location()));
        return result;
    }

    public boolean resolvePendingData(RegistryAccess access) {
        Registry<Layer> layerRegistry = access.registryOrThrow(LayerRegistries.LAYER_KEY);
        Registry<Origin> originRegistry = access.registryOrThrow(OriginRegistries.ORIGIN_KEY);
        Registry<Power> powerRegistry = access.registryOrThrow(PowerRegistries.POWER_KEY);
        boolean resolved = false;

        for (Iterator<Map.Entry<ResourceLocation, ResourceLocation>> iterator = this.unresolvedOrigins.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<ResourceLocation, ResourceLocation> entry = iterator.next();
            Optional<Holder.Reference<Layer>> layer = getHolder(layerRegistry, LayerRegistries.LAYER_KEY, entry.getKey());
            Optional<Holder.Reference<Origin>> origin = getHolder(originRegistry, OriginRegistries.ORIGIN_KEY, entry.getValue());
            if (layer.isPresent() && origin.isPresent()) {
                this.origins.put(layer.get(), origin.get());
                iterator.remove();
                resolved = true;
            }
        }

        for (Iterator<Map.Entry<ResourceLocation, ResourceLocation>> iterator = this.unresolvedPowers.entries().iterator(); iterator.hasNext(); ) {
            Map.Entry<ResourceLocation, ResourceLocation> entry = iterator.next();
            Optional<Holder.Reference<Power>> power = getHolder(powerRegistry, PowerRegistries.POWER_KEY, entry.getValue());
            if (power.isPresent()) {
                this.powers.put(entry.getKey(), power.get());
                iterator.remove();
                resolved = true;
            }
        }
        return resolved;
    }

    private static <T> Optional<Holder.Reference<T>> getHolder(Registry<T> registry, ResourceKey<Registry<T>> registryKey, ResourceLocation id) {
        return registry.getHolder(ResourceKey.create(registryKey, id));
    }

    public Map<ResourceLocation, Map<Class<? extends PowerComponent>, PowerComponent>> getComponents() {
        return this.components;
    }

    public boolean isSelecting() {
        return this.selecting;
    }

    public void setSelecting(boolean selecting) {
        this.selecting = selecting;
    }
}
