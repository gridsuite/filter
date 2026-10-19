/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.*;
import org.gridsuite.filter.identifierlistfilter.*;
import org.gridsuite.filter.wip.ExpertFilter;

import java.util.*;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class FilterServiceUtils {
    private FilterServiceUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static FilteredIdentifiables evaluateFiltersWithEquipmentTypes(FiltersWithEquipmentTypes filtersWithEquipmentTypes, Network network, FilterLoader filterLoader) {
        Map<String, IdentifiableAttributes> result = new TreeMap<>();
        Map<String, IdentifiableAttributes> notFound = new TreeMap<>();

        List<AbstractFilter> abstractFilters = filterLoader.getFilters(filtersWithEquipmentTypes.filters().stream().map(FilterAttributes::getId).toList());

        abstractFilters.forEach(filter -> mergeFilteredIdentifiables(filtersWithEquipmentTypes, network, filterLoader, result, notFound, filter));
        return new FilteredIdentifiables(
            result.values().stream().sorted(Comparator.comparing(e -> e.getType().ordinal())).toList(),
            notFound.values().stream().sorted(Comparator.comparing(e -> e.getType().ordinal())).toList());
    }

    private static void mergeFilteredIdentifiables(FiltersWithEquipmentTypes filtersWithEquipmentTypes, Network network, FilterLoader filterLoader,
                                                   Map<String, IdentifiableAttributes> result, Map<String, IdentifiableAttributes> notFound,
                                                   AbstractFilter filter) {
        Objects.requireNonNull(filter);
        EquipmentType filterEquipmentType = filter.getEquipmentType();
        List<Identifiable<?>> identifiables = filter.toFilter(filterLoader).evaluate(network);

        if (filterEquipmentType != EquipmentType.SUBSTATION && filterEquipmentType != EquipmentType.VOLTAGE_LEVEL) {
            FilterEquipments filterEquipments = filter.toFilterEquipments(identifiables);
            if (filterEquipments.getNotFoundEquipments() != null) {
                filterEquipments.getNotFoundEquipments().forEach(id -> notFound.put(
                        id,
                        new IdentifiableAttributes(id, EquipmentTypeMapper.map(filter.getEquipmentType()), null)));
            }
            filterEquipments.getIdentifiableAttributes().forEach(id -> result.put(id.getId(), id));
            return;
        }

        Set<IdentifiableType> selectedEquipmentTypes = getSelectedEquipmentTypes(filtersWithEquipmentTypes, filter.getId());

        // This list is the result of the original filter and so necessarily contains a list of IDs of substations or voltage levels.
        List<String> filteredEquipmentIds = identifiables.stream().map(Identifiable::getId).toList();
        List<ExpertFilter> filters = FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(filterEquipmentType, filteredEquipmentIds, selectedEquipmentTypes);
        filters.stream()
                .flatMap(expertFilter -> expertFilter.evaluate(network).stream()).map(FilterServiceUtils::map)
                .forEach(element -> result.put(element.getId(), element));
    }

    private static Set<IdentifiableType> getSelectedEquipmentTypes(FiltersWithEquipmentTypes filtersWithEquipmentTypes, UUID filterUuid) {
        return filtersWithEquipmentTypes.selectedEquipmentTypesByFilter()
            .stream()
            .filter(equipmentTypesByFilterId -> equipmentTypesByFilterId.filterId().equals(filterUuid))
            .findFirst()
            .map(EquipmentTypesByFilterId::equipmentTypes)
            .orElseThrow(
                () -> new IllegalStateException("No selected equipment types for filter " + filterUuid
                    + " : substation and voltage level filters should contain an equipment types list")
            );
    }

    public static IdentifiableAttributes map(Identifiable<?> identifiable) {
        return new IdentifiableAttributes(identifiable.getId(), identifiable.getType(), null);
    }

    public static FilteredIdentifiables map(List<Identifiable<?>> identifiables) {
        return new FilteredIdentifiables(identifiables.stream().map(FilterServiceUtils::map).toList(), List.of());
    }
}
