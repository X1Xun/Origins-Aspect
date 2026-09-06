package com.iafenvoy.origins.command;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.helper.ModifierPowerHelper;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.data.power.reference.PowerHolder;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.text.DecimalFormat;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ModifierCommand {
    private ModifierCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> registerCommand() {
        return literal("modifier")
                .requires(source -> source.hasPermission(2))
                .then(literal("apply")
                        .then(argument("target", EntityArgument.entity())
                                .then(argument("power", ResourceLocationArgument.id())
                                        .suggests(ModifierCommand::suggestModifiers)
                                        .then(argument("base", DoubleArgumentType.doubleArg())
                                                .executes(ModifierCommand::apply)))));
    }

    private static CompletableFuture<Suggestions> suggestModifiers(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        return SharedSuggestionProvider.suggestResource(OriginDataHolder.get(target).getAllPowers().stream()
                .filter(power -> power.power() instanceof ModifierPowerHelper)
                .map(PowerHolder::id), builder);
    }

    private static int apply(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        ResourceLocation id = ResourceLocationArgument.getId(context, "power");
        OriginDataHolder holder = OriginDataHolder.get(target);
        Power power = holder.getAllPowers().stream().filter(entry -> entry.id().equals(id)).map(PowerHolder::power).findFirst().orElse(null);
        if (!(power instanceof ModifierPowerHelper modifier)) {
            context.getSource().sendFailure(Component.literal("Power " + id + " is not a modifying power."));
            return 0;
        }

        double base = DoubleArgumentType.getDouble(context, "base");
        double result = modifier.modify(holder, base);
        DecimalFormat format = new DecimalFormat("#.#####");
        context.getSource().sendSuccess(() -> Component.literal("Applied modifier: " + id
                + "\nBase value: " + format.format(base)
                + "\nModifier result: " + format.format(result)), false);
        return 1;
    }
}
