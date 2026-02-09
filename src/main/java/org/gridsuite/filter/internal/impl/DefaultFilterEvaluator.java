package org.gridsuite.filter.internal.impl;

import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.api.FilterEvaluator;
import org.gridsuite.filter.api.dto.FiltersWithEquipmentTypes;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;
import org.gridsuite.filter.utils.FilterServiceUtils;

public class DefaultFilterEvaluator implements FilterEvaluator {

    private final FilterLoader filterLoader;

    public DefaultFilterEvaluator(FilterLoader filterLoader) {
        this.filterLoader = filterLoader;
    }

    @Override
    public FilteredIdentifiables evaluateFilters(FiltersWithEquipmentTypes filtersWithEquipmentTypes, Network network) {
        return FilterServiceUtils.evaluateFiltersWithEquipmentTypes(filtersWithEquipmentTypes, network, filterLoader);
    }
}
