package com.iafenvoy.origins.util.math;

import net.minecraft.world.entity.Entity;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

import java.util.Set;

/**
 * A compiled exp4j expression whose mutable evaluation state is isolated per
 * thread. Resource variables are supplied from the owning entity on demand.
 */
public final class MathExpression {
    private final String source;
    private final Set<String> variableNames;
    private final ThreadLocal<Expression> compiled;

    public MathExpression(String source, Set<String> variableNames) {
        this.source = source.trim();
        if (this.source.isEmpty()) throw new IllegalArgumentException("Expression must not be empty");
        this.variableNames = Set.copyOf(variableNames);
        this.build(); // Validate datapack expressions while their power or action is decoded.
        this.compiled = ThreadLocal.withInitial(this::build);
    }

    public double evaluate(Entity entity, VariableSerializer variables) {
        Expression expression = this.compiled.get();
        for (String name : this.variableNames)
            expression.setVariable(name, variables.value(name, entity));
        double result = expression.evaluate();
        return Double.isFinite(result) ? result : 0D;
    }

    private Expression build() {
        return new ExpressionBuilder(this.source)
                .variables(this.variableNames)
                .build();
    }
}
