package com.iafenvoy.origins.data._common;

import com.iafenvoy.origins.Origins;
import com.iafenvoy.origins.data.condition.AlwaysTrueCondition;
import com.iafenvoy.origins.data.condition.EntityCondition;
import com.iafenvoy.origins.util.math.ResourceReference;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class HudRender {
    public static final ResourceLocation DEFAULT_SPRITE = ResourceLocation.fromNamespaceAndPath(Origins.MOD_ID, "textures/gui/resource_bar.png");

    private static final Codec<HudRender> SINGLE_CODEC = RecordCodecBuilder.create(i -> i.group(
            // Origins-math used `should_render` while Origins uses
            // `should_render_inactive`; accept both formats and prefer the
            // explicit Origins field when both are present.
            Codec.BOOL.optionalFieldOf("should_render_inactive").forGetter(render -> Optional.of(render.shouldRenderInActive)),
            Codec.BOOL.optionalFieldOf("should_render").forGetter(render -> Optional.empty()),
            ResourceLocation.CODEC.optionalFieldOf("sprite_location", DEFAULT_SPRITE).forGetter(HudRender::spriteLocation),
            ResourceReference.CODEC.optionalFieldOf("bar_index", ResourceReference.number(0)).forGetter(HudRender::barIndex),
            ResourceReference.CODEC.optionalFieldOf("icon_index", ResourceReference.number(0)).forGetter(HudRender::iconIndex),
            EntityCondition.optionalCodec("condition").forGetter(HudRender::condition),
            Codec.BOOL.optionalFieldOf("inverted", false).forGetter(HudRender::inverted),
            ResourceReference.CODEC.optionalFieldOf("order", ResourceReference.number(0)).forGetter(HudRender::order)
    ).apply(i, (shouldRenderInactive, originsMathShouldRender, spriteLocation, barIndex, iconIndex, condition, inverted, order) ->
            new HudRender(shouldRenderInactive.or(() -> originsMathShouldRender).orElse(true), spriteLocation,
                    barIndex, iconIndex, condition, inverted, order)));

    /**
     * Accepts the normal object form and origins-math's parent/children array form.
     */
    public static final Codec<HudRender> CODEC = Codec.either(SINGLE_CODEC.listOf(), SINGLE_CODEC).xmap(
            value -> value.map(HudRender::fromList, value2 -> value2),
            value -> value.children.isEmpty() ? Either.right(value) : Either.left(value.entries())
    );

    private final boolean shouldRenderInActive;
    private final ResourceLocation spriteLocation;
    private final ResourceReference barIndex;
    private final ResourceReference iconIndex;
    private final EntityCondition condition;
    private final boolean inverted;
    private ResourceReference order;
    private final List<HudRender> children = new ArrayList<>();

    public HudRender(boolean shouldRenderInActive, ResourceLocation spriteLocation, ResourceReference barIndex,
                     ResourceReference iconIndex, EntityCondition condition, boolean inverted, ResourceReference order) {
        this.shouldRenderInActive = shouldRenderInActive;
        this.spriteLocation = spriteLocation;
        this.barIndex = barIndex;
        this.iconIndex = iconIndex;
        this.condition = condition;
        this.inverted = inverted;
        this.order = order;
    }

    private static HudRender fromList(List<HudRender> entries) {
        if (entries.isEmpty())
            return new HudRender(false, DEFAULT_SPRITE, ResourceReference.number(0), ResourceReference.number(0),
                    AlwaysTrueCondition.INSTANCE, false, ResourceReference.number(0));
        HudRender parent = entries.getFirst();
        for (int index = 1; index < entries.size(); index++) parent.addChild(entries.get(index));
        return parent;
    }

    public boolean shouldRenderInActive() {
        return this.shouldRenderInActive;
    }

    public ResourceLocation spriteLocation() {
        return this.spriteLocation;
    }

    public ResourceReference barIndex() {
        return this.barIndex;
    }

    public ResourceReference iconIndex() {
        return this.iconIndex;
    }

    public EntityCondition condition() {
        return this.condition;
    }

    public boolean inverted() {
        return this.inverted;
    }

    public ResourceReference order() {
        return this.order;
    }

    public List<HudRender> children() {
        return Collections.unmodifiableList(this.children);
    }

    private List<HudRender> entries() {
        List<HudRender> entries = new ArrayList<>(1 + this.children.size());
        entries.add(this);
        entries.addAll(this.children);
        return entries;
    }

    public void addChild(HudRender child) {
        if (child == this) return;
        if (child.order.value().left().filter(number -> number == 0D).isPresent()) child.order = this.order;
        this.children.add(child);
    }

    /**
     * Returns this entry when active, otherwise the first active child.
     */
    public HudRender getActive(Entity viewer, boolean powerActive) {
        if (this.condition.test(viewer) && (this.shouldRenderInActive || powerActive)) return this;
        for (HudRender child : this.children) {
            HudRender active = child.getActive(viewer, powerActive);
            if (active != null) return active;
        }
        return null;
    }

    public HudRender getActive(Entity viewer) {
        return this.getActive(viewer, true);
    }

    public boolean shouldRender(Entity viewer) {
        return this.shouldRenderInActive && this.condition.test(viewer);
    }
}
