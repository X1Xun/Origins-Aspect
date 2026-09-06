package com.iafenvoy.origins.data.power.builtin.regular;

import com.iafenvoy.origins.attachment.OriginDataHolder;
import com.iafenvoy.origins.data.power.Power;
import com.iafenvoy.origins.util.math.MathExpression;
import com.iafenvoy.origins.util.math.VariableSerializer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Map;

/**
 * A read-only resource calculated from an exp4j expression.
 */
public final class MathResourcePower extends LinkedResourcePower {
    public static final MapCodec<MathResourcePower> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            BaseSettings.CODEC.forGetter(Power::getSettings),
            Codec.STRING.fieldOf("expression").forGetter(MathResourcePower::expression),
            VariableSerializer.CODEC.optionalFieldOf("variables", new VariableSerializer(Map.of())).forGetter(MathResourcePower::variables)
    ).apply(instance, MathResourcePower::new));

    private final String expression;
    private final VariableSerializer variables;
    private final MathExpression parsedExpression;

    public MathResourcePower(BaseSettings settings, String expression, VariableSerializer variables) {
        super(settings);
        this.expression = expression;
        this.variables = variables;
        this.parsedExpression = new MathExpression(expression, variables.variables().keySet());
    }

    public String expression() {
        return this.expression;
    }

    public VariableSerializer variables() {
        return this.variables;
    }

    @Override
    protected double supply(OriginDataHolder holder) {
        return this.parsedExpression.evaluate(holder.getEntity(), this.variables);
    }

    @Override
    protected MapCodec<? extends LinkedResourcePower> codecImpl() {
        return CODEC;
    }
}
