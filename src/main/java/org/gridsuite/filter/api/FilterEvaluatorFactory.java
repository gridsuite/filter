package org.gridsuite.filter.api;

import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.internal.impl.DefaultFilterEvaluator;

import java.util.Objects;

public final class FilterEvaluatorFactory {
    private FilterEvaluatorFactory() {
        throw new IllegalStateException("Should not initialize an utility class");
    }

    public static FilterEvaluator create(FilterLoader filterLoader) {
        Objects.requireNonNull(filterLoader, "Filter loader is not provided while creating filter evaluator");
        return new DefaultFilterEvaluator(filterLoader);
    }
}
