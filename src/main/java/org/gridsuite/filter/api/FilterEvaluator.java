package org.gridsuite.filter.api;

import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.api.dto.FiltersWithEquipmentTypes;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;

public interface FilterEvaluator {
    FilteredIdentifiables evaluateFilters(FiltersWithEquipmentTypes filtersWithEquipmentTypes, Network network);
}
