package org.gridsuite.filter.api;

import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.api.dto.FiltersWithEquipmentTypes;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;

/**
 * Evaluates one or more filters against a given {@link Network}.
 * <p>
 * Implementations typically resolve the referenced filters (e.g., via a {@code FilterLoader})
 * and return the resulting set of matching identifiables.
 * </p>
 */
public interface FilterEvaluator {

    /**
     * Evaluates the provided filters for the given network.
     *
     * @param filtersWithEquipmentTypes the filters to evaluate along with their associated equipment types
     * @param network the IIDM network used as the evaluation context
     * @return the evaluation result containing the filtered identifiables and not found identifiables
     * @throws NullPointerException if {@code filtersWithEquipmentTypes} or {@code network} is {@code null}
     */
    FilteredIdentifiables evaluateFilters(FiltersWithEquipmentTypes filtersWithEquipmentTypes, Network network);
}
