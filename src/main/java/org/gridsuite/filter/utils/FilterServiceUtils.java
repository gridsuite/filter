/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.*;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.FilteredIdentifiables;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class FilterServiceUtils {
    private FilterServiceUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static List<IdentifiableAttributes> getIdentifiableAttributes(AbstractFilter filter, Network network, FilterLoader filterLoader) {
        if (filter instanceof IdentifierListFilter identifierListFilter &&
            (filter.getEquipmentType() == EquipmentType.GENERATOR ||
                filter.getEquipmentType() == EquipmentType.LOAD)) {
            return FiltersUtils.getIdentifiables(filter, network, filterLoader)
                .stream()
                .map(identifiable -> new IdentifiableAttributes(identifiable.getId(),
                    identifiable.getType(),
                    identifierListFilter.getDistributionKey(identifiable.getId())))
                .toList();
        } else {
            return FiltersUtils.getIdentifiables(filter, network, filterLoader).stream()
                .map(identifiable -> new IdentifiableAttributes(identifiable.getId(), identifiable.getType(), null))
                .toList();
        }
    }

    public static List<FilterEquipments> getFilterEquipmentsFromUuid(Network network, List<UUID> uuids, FilterLoader filterLoader) {
        return getFilterEquipmentsFromUuid(network, uuids, filterLoader, Set.of());
    }

    public static List<FilterEquipments> getFilterEquipmentsFromUuid(Network network, List<UUID> uuids, FilterLoader filterLoader, Set<FilterType> filterTypesToExclude) {
        List<AbstractFilter> filters = filterLoader.getFilters(uuids);
        return filters.stream()
            .filter(filter -> filter != null && !filterTypesToExclude.contains(filter.getType()))
            .map(filter -> filter.toFilterEquipments(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader)))
            .toList();
    }

    public static List<FilterEquipments> getFilterEquipmentsFromUuid(Network network, UUID uuid, FilterLoader filterLoader) {
        List<AbstractFilter> filters = filterLoader.getFilters(List.of(uuid));
        return filters.stream()
            .map(filter -> filter.toFilterEquipments(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader)))
            .toList();
    }

    public static FilteredIdentifiables evaluateFiltersWithEquipmentTypes(FiltersWithEquipmentTypes filtersWithEquipmentTypes, Network network, FilterLoader filterLoader) {
        Map<String, IdentifiableAttributes> result = new TreeMap<>();
        Map<String, IdentifiableAttributes> notFound = new TreeMap<>();

        filtersWithEquipmentTypes.filters().forEach((IFilterAttributes filterAttributes) -> {
                UUID filterUuid = filterAttributes.getId();
                Optional<AbstractFilter> filterList = filterLoader.getFilter(filterUuid);
                if (filterList.isEmpty()) {
                    return;
                }
                AbstractFilter filter = filterList.get();
                Objects.requireNonNull(filter);
                EquipmentType filterEquipmentType = filter.getEquipmentType();
                FilteredIdentifiables filteredIdentifiables = filter.toFilteredIdentifiables(FilterServiceUtils.getIdentifiableAttributes(filter, network, filterLoader));

                // unduplicate equipments and merge in common lists
                if (filteredIdentifiables.notFoundIds() != null) {
                    filteredIdentifiables.notFoundIds().forEach(element -> notFound.put(element.getId(), element));
                }

                if (filteredIdentifiables.equipmentIds() != null) {
                    if (filterEquipmentType != EquipmentType.SUBSTATION && filterEquipmentType != EquipmentType.VOLTAGE_LEVEL) {
                        filteredIdentifiables.equipmentIds().forEach(element -> result.put(element.getId(), element));
                    } else {
                        Set<IdentifiableType> selectedEquipmentTypes = filtersWithEquipmentTypes.selectedEquipmentTypesByFilter()
                            .stream()
                            .filter(equipmentTypesByFilterId -> equipmentTypesByFilterId.filterId().equals(filterUuid))
                            .findFirst()
                            .map(EquipmentTypesByFilterId::equipmentTypes)
                            .orElseThrow(
                                () -> new IllegalStateException("No selected equipment types for filter " + filterUuid
                                    + " : substation and voltage level filters should contain an equipment types list")
                            );

                        // This list is the result of the original filter and so necessarily contais a list of IDs of substations or voltage levels
                        Set<String> filteredEquipmentIds = filteredIdentifiables.equipmentIds().stream().map(IdentifiableAttributes::getId).collect(Collectors.toSet());
                        List<ExpertFilter> filters = FilterWithEquipmentTypesUtils.createFiltersForSubEquipments(filterEquipmentType,
                            filteredEquipmentIds,
                            selectedEquipmentTypes);
                        filters.stream().flatMap(expertFilter -> getIdentifiableAttributes(expertFilter, network, filterLoader).stream())
                            .forEach(element -> result.put(element.getId(), element));
                    }
                }
            }
        );
        return new FilteredIdentifiables(
            result.values().stream().sorted(Comparator.comparing(e -> e.getType().ordinal())).toList(),
            notFound.values().stream().sorted(Comparator.comparing(e -> e.getType().ordinal())).toList());
    }
}
