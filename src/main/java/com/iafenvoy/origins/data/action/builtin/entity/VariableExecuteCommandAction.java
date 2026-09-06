package com.iafenvoy.origins.data.action.builtin.entity;

import com.iafenvoy.origins.data._common.helper.CommandHelper;
import com.iafenvoy.origins.data.action.EntityAction;
import com.iafenvoy.origins.util.math.VariableSerializer;
import com.iafenvoy.origins.util.math.VariableStringUtil;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record VariableExecuteCommandAction(String command,
                                           VariableSerializer variables) implements EntityAction, CommandHelper {
    public static final MapCodec<VariableExecuteCommandAction> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.fieldOf("command").forGetter(VariableExecuteCommandAction::command),
            VariableSerializer.CODEC.optionalFieldOf("variables", new VariableSerializer(Map.of())).forGetter(VariableExecuteCommandAction::variables)
    ).apply(instance, VariableExecuteCommandAction::new));

    @Override
    public @NotNull MapCodec<? extends EntityAction> codec() {
        return CODEC;
    }

    @Override
    public void execute(@NotNull Entity source) {
        this.executeCommand(source, VariableStringUtil.parse(this.command, this.variables, source));
    }
}
