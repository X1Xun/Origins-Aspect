package com.iafenvoy.origins.data.action.builtin.bientity;

import com.iafenvoy.origins.data._common.helper.CommandHelper;
import com.iafenvoy.origins.data.action.BiEntityAction;
import com.iafenvoy.origins.util.math.VariableSerializer;
import com.iafenvoy.origins.util.math.VariableStringUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record VariableExecuteCommandAction(String command, String actorSelector, String targetSelector,
                                           VariableSerializer variables) implements BiEntityAction, CommandHelper {
    public static final MapCodec<VariableExecuteCommandAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("command").forGetter(VariableExecuteCommandAction::command),
            Codec.STRING.optionalFieldOf("actor_selector", "%a").forGetter(VariableExecuteCommandAction::actorSelector),
            Codec.STRING.optionalFieldOf("target_selector", "%t").forGetter(VariableExecuteCommandAction::targetSelector),
            VariableSerializer.CODEC.optionalFieldOf("variables", new VariableSerializer(Map.of())).forGetter(VariableExecuteCommandAction::variables)
    ).apply(instance, VariableExecuteCommandAction::new));

    @Override
    public @NotNull MapCodec<? extends BiEntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source, @NotNull Entity target) {
        String parsed = VariableStringUtil.parse(this.command, this.variables, source)
                .replace(this.actorSelector, source.getUUID().toString())
                .replace(this.targetSelector, target.getUUID().toString());
        this.executeCommand(source, parsed);
    }
}
