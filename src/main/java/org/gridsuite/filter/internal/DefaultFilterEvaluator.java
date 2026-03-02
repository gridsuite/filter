package org.gridsuite.filter.internal;

import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.api.FilterEvaluator;
import org.gridsuite.filter.api.dto.FiltersWithEquipmentTypes;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;
import org.gridsuite.filter.utils.FilterServiceUtils;

/**
 * Default {@link FilterEvaluator} implementation.
 * <p>
 * Delegates the evaluation to {@link FilterServiceUtils} and uses the provided {@link FilterLoader}
 * to resolve and load the filters required by the evaluation request.
 * </p>
 */
public class DefaultFilterEvaluator implements FilterEvaluator {

    private final FilterLoader filterLoader;

    /**
     * Creates an evaluator using the given filter loader.
     *
     * @param filterLoader loader used to resolve and load filters during evaluation
     */
    public DefaultFilterEvaluator(FilterLoader filterLoader) {
        this.filterLoader = filterLoader;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FilteredIdentifiables evaluateFilters(FiltersWithEquipmentTypes filtersWithEquipmentTypes, Network network) {
        return FilterServiceUtils.evaluateFiltersWithEquipmentTypes(filtersWithEquipmentTypes, network, filterLoader);
    }
}
