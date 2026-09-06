package com.iafenvoy.origins.util.math;

import net.minecraft.world.entity.Entity;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Expands origins-math resource variables in command strings.
 */
public final class VariableStringUtil {
    private static final Pattern INTERPOLATION = Pattern.compile("\\$:(?<simple>[A-Za-z_][A-Za-z0-9_]*)|\\$\\{(?<name>[A-Za-z_][A-Za-z0-9_]*)(?::%(?:d|\\.(?<precision>[0-9]{1,2})f)|:%(?<legacyPrecision>[0-9]{1,2})f)?}");

    private VariableStringUtil() {
    }

    public static String parse(String value, VariableSerializer variables, Entity entity) {
        Matcher matcher = INTERPOLATION.matcher(value);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String name = matcher.group("simple") != null ? matcher.group("simple") : matcher.group("name");
            if (!variables.hasVariable(name))
                throw new IllegalArgumentException("Non-existent variable: " + name + "! If this is not intended as a variable, prepend the '$' with '\\'.");
            double number = variables.value(name, entity);
            String precision = matcher.group("precision");
            if (precision == null) precision = matcher.group("legacyPrecision");
            if (precision != null && Integer.parseInt(precision) > 17)
                throw new IllegalArgumentException("The precision for a floating-point must be in the range of [0-17].");
            String replacement = precision == null ? Integer.toString((int) number) : String.format(Locale.ROOT, "%." + precision + "f", number);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);
        return result.toString();
    }
}
