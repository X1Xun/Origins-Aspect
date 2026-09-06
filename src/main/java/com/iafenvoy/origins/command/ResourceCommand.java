package com.iafenvoy.origins.command;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data._common.helper.ResourceHelper;
import com.iafenvoy.origins.data._common.helper.ResourceValueHelper;
import com.iafenvoy.origins.data._common.helper.ResourceValueHelper.ResourceValue;
import com.iafenvoy.origins.data.power.reference.PowerHolder;
import com.iafenvoy.origins.data.power.reference.PowerReference;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ObjectiveArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.Objective;
import net.minecraft.world.scores.ScoreHolder;
import net.minecraft.world.scores.Scoreboard;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

public final class ResourceCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> registerCommand() {
        return literal("resource")
                .requires(source -> source.hasPermission(2))
                .then(literal("has")
                        .then(argument("target", EntityArgument.entity())
                                .then(argument("power", ResourceLocationArgument.id())
                                        .suggests(ResourceCommand::suggestAllResource)
                                        .executes(ResourceCommand::has))))
                .then(literal("get")
                        .then(literal("absolute")
                                .then(argument("target", EntityArgument.entity())
                                        .then(argument("power", ResourceLocationArgument.id())
                                                .suggests(ResourceCommand::suggestResource)
                                                .executes(ResourceCommand::absoluteGet))))
                        .then(argument("target", EntityArgument.entity())
                                .then(argument("power", ResourceLocationArgument.id())
                                        .suggests(ResourceCommand::suggestResource)
                                        .executes(ResourceCommand::get))))
                .then(literal("change")
                        .then(literal("absolute")
                                .then(argument("target", EntityArgument.entity())
                                        .then(argument("power", ResourceLocationArgument.id())
                                                .suggests(ResourceCommand::suggestResource)
                                                .then(argument("value", DoubleArgumentType.doubleArg())
                                                        .executes(ResourceCommand::absoluteChange)))))
                        .then(argument("target", EntityArgument.entity())
                                .then(argument("power", ResourceLocationArgument.id())
                                        .suggests(ResourceCommand::suggestIntegerResource)
                                        .then(argument("value", DoubleArgumentType.doubleArg())
                                                .executes(ResourceCommand::change)))))
                .then(literal("operation")
                        .then(argument("target", EntityArgument.entity())
                                .then(argument("power", ResourceLocationArgument.id())
                                        .suggests(ResourceCommand::suggestResource)
                                        .then(operationBranch(Operation.MOD))
                                        .then(operationBranch(Operation.MUL))
                                        .then(operationBranch(Operation.ADD))
                                        .then(operationBranch(Operation.SUB))
                                        .then(operationBranch(Operation.DIV))
                                        .then(operationBranch(Operation.MIN))
                                        .then(operationBranch(Operation.SET))
                                        .then(operationBranch(Operation.MAX))
                                        .then(operationBranch(Operation.SWAP))
                                )));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> operationBranch(Operation operation) {
        return literal(operation.symbol)
                .then(argument("sourceEntity", EntityArgument.entity())
                        .then(argument("sourceObjective", ObjectiveArgument.objective())
                                .executes(ctx -> operation(ctx, operation))));
    }

    private static CompletableFuture<Suggestions> suggestAllResource(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) {
        return SharedSuggestionProvider.suggestResource(PowerReference.listAllPowers(context.getSource().registryAccess()).filter(ResourceCommand::isResource).flatMap(PowerHolder::stream).map(PowerHolder::id), builder);
    }

    private static CompletableFuture<Suggestions> suggestResource(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        OriginDataHolder holder = OriginDataHolder.get(target);
        if (holder == null) return CompletableFuture.completedFuture(builder.build());
        return SharedSuggestionProvider.suggestResource(holder.getAllPowers().stream().filter(ResourceCommand::isResource).flatMap(PowerHolder::stream).map(PowerHolder::id), builder);
    }

    private static int has(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        ResourceLocation power = ResourceLocationArgument.getId(context, "power");
        CommandSourceStack source = context.getSource();
        boolean has = ResourceValueHelper.hasResource(target, power);
        if (has)
            source.sendSuccess(() -> Component.translatable("commands.origins.resource.has.success", target.getName(), power.toString()), false);
        return has ? 1 : 0;
    }

    private static int get(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        ResourceLocation power = ResourceLocationArgument.getId(context, "power");
        if (!ResourceValueHelper.hasResource(target, power)) {
            context.getSource().sendFailure(Component.translatable("commands.origins.resource.missing_power", target.getName(), power.toString()));
            return 0;
        }
        context.getSource().sendSuccess(() -> Component.translatable("commands.origins.resource.get.result", target.getName(), power.toString(), ResourceValueHelper.value(target, power)), false);
        return 1;
    }

    private static int absoluteGet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        ResourceLocation power = ResourceLocationArgument.getId(context, "power");
        if (!ResourceValueHelper.hasResource(target, power)) {
            context.getSource().sendFailure(Component.translatable("commands.origins.resource.missing_power", target.getName(), power.toString()));
            return 0;
        }
        double value = ResourceValueHelper.value(target, power);
        context.getSource().sendSuccess(() -> Component.translatable("commands.origins.resource.get.result", target.getName(), power.toString(), value), false);
        return (int) value;
    }

    private static int change(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        ResourceLocation power = ResourceLocationArgument.getId(context, "power");
        if (!ResourceValueHelper.hasResource(target, power)) {
            context.getSource().sendFailure(Component.translatable("commands.origins.resource.missing_power", target.getName(), power.toString()));
            return 0;
        }
        double value = DoubleArgumentType.getDouble(context, "value");
        ResourceValueHelper.add(target, power, value);
        OriginDataHolder.get(target).sync();
        context.getSource().sendSuccess(() -> Component.translatable("commands.origins.resource.change.success", target.getName(), power.toString(), value, ResourceValueHelper.value(target, power)), true);
        return 1;
    }

    private static int absoluteChange(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        ResourceLocation power = ResourceLocationArgument.getId(context, "power");
        if (!ResourceValueHelper.hasResource(target, power)) {
            context.getSource().sendFailure(Component.translatable("commands.origins.resource.missing_power", target.getName(), power.toString()));
            return 0;
        }
        if (!ResourceValueHelper.isMutableResource(target, power)) {
            context.getSource().sendFailure(Component.literal("Power " + power + " is not a modifiable resource."));
            return 0;
        }
        double value = DoubleArgumentType.getDouble(context, "value");
        ResourceValueHelper.addOrThrow(target, power, value);
        OriginDataHolder.get(target).sync();
        context.getSource().sendSuccess(() -> Component.translatable("commands.origins.resource.change.success", target.getName(), power.toString(), value, ResourceValueHelper.value(target, power)), true);
        return 1;
    }

    private static int operation(CommandContext<CommandSourceStack> context, Operation operation) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        ResourceLocation power = ResourceLocationArgument.getId(context, "power");
        OriginDataHolder holder = OriginDataHolder.get(target);
        if (holder == null) {
            context.getSource().sendFailure(Component.translatable("commands.origins.resource.missing_power", target.getName(), power.toString()));
            return 0;
        }
        ResourceHelper resource = getResourceComponent(holder, power, context);
        if (resource == null) return 0;

        Entity sourceEntity = EntityArgument.getEntity(context, "sourceEntity");
        Objective objective = ObjectiveArgument.getObjective(context, "sourceObjective");
        Scoreboard scoreboard = sourceEntity.level().getScoreboard();
        ScoreHolder score = ScoreHolder.forNameOnly(sourceEntity.getScoreboardName());
        int sourceValue = scoreboard.getOrCreatePlayerScore(score, objective).get();
        int targetValue = resource.getValue(holder);
        int newValue = operation.apply(targetValue, sourceValue);
        resource.setValue(holder, newValue);
        holder.sync();

        if (operation == Operation.SWAP)
            scoreboard.getOrCreatePlayerScore(score, objective).set(targetValue);
        else
            scoreboard.getOrCreatePlayerScore(score, objective).set(sourceValue);
        context.getSource().sendSuccess(() -> Component.translatable("commands.origins.resource.operation.success", target.getName(), power.toString(), operation.symbol, sourceEntity.getName(), objective.getName(), newValue), true);
        return 1;
    }

    private static ResourceHelper getResourceComponent(OriginDataHolder holder, ResourceLocation power, CommandContext<CommandSourceStack> context) {
        ResourceHelper resource = holder.getAllPowers().stream().filter(x -> Objects.equals(x.id(), power)).findAny().map(PowerHolder::power).filter(ResourceHelper.class::isInstance).map(ResourceHelper.class::cast).orElse(null);
        if (resource != null) return resource;
        context.getSource().sendFailure(Component.translatable("commands.origins.resource.missing_power", holder.getEntity().getName(), power.toString()));
        return null;
    }

    private static CompletableFuture<Suggestions> suggestIntegerResource(final CommandContext<CommandSourceStack> context, final SuggestionsBuilder builder) throws CommandSyntaxException {
        Entity target = EntityArgument.getEntity(context, "target");
        OriginDataHolder holder = OriginDataHolder.get(target);
        if (holder == null) return CompletableFuture.completedFuture(builder.build());
        return SharedSuggestionProvider.suggestResource(holder.getAllPowers().stream()
                .filter(power -> power.power() instanceof ResourceHelper)
                .flatMap(PowerHolder::stream)
                .map(PowerHolder::id), builder);
    }

    private static boolean isResource(PowerHolder power) {
        return power.power() instanceof ResourceHelper || power.power() instanceof ResourceValue;
    }

    private enum Operation {
        MOD("%="),
        MUL("*="),
        ADD("+="),
        SUB("-="),
        DIV("/="),
        MIN("<"),
        SET("="),
        MAX(">"),
        SWAP("><");

        private final String symbol;

        Operation(String symbol) {
            this.symbol = symbol;
        }

        private int apply(int targetValue, int sourceValue) {
            return switch (this) {
                case MOD -> sourceValue == 0 ? 0 : targetValue % sourceValue;
                case MUL -> targetValue * sourceValue;
                case ADD -> targetValue + sourceValue;
                case SUB -> targetValue - sourceValue;
                case DIV -> sourceValue == 0 ? 0 : targetValue / sourceValue;
                case MIN -> Math.min(targetValue, sourceValue);
                case SET, SWAP -> sourceValue;
                case MAX -> Math.max(targetValue, sourceValue);
            };
        }
    }
}
