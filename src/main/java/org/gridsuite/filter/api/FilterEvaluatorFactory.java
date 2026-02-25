package org.gridsuite.filter.api;

import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.internal.DefaultFilterEvaluator;

import java.util.Objects;

/**
 * Factory for creating {@link FilterEvaluator} instances.
 * <p>
 * This is a simple construction utility that wires the default implementation
 * ({@link DefaultFilterEvaluator}) with the required dependencies.
 * </p>
 */
public final class FilterEvaluatorFactory {
    private FilterEvaluatorFactory() {
        throw new IllegalStateException("Should not initialize an utility class");
    }

    /**
     * Creates a {@link FilterEvaluator} using the provided {@link FilterLoader}.
     *
     * @param filterLoader loader used to resolve and load filters during evaluation
     * @return a {@link FilterEvaluator} instance
     * @throws NullPointerException if {@code filterLoader} is {@code null}
     */
    public static FilterEvaluator create(FilterLoader filterLoader) {
        Objects.requireNonNull(filterLoader, "Filter loader is not provided while creating filter evaluator");
        return new DefaultFilterEvaluator(filterLoader);
    }
}
