/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.Substation;
import com.powsybl.iidm.network.Switch;
import com.powsybl.iidm.network.SwitchKind;
import com.powsybl.iidm.network.VoltageLevel;
import com.powsybl.sld.builders.NetworkGraphBuilder;
import com.powsybl.sld.layout.ImplicitCellDetector;
import com.powsybl.sld.layout.LayoutParameters;
import com.powsybl.sld.model.graphs.VoltageLevelGraph;
import com.powsybl.sld.model.nodes.Node;
import org.gridsuite.filter.*;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.identifierlistfilter.*;

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
                filter.getEquipmentType() == EquipmentType.LOAD || filter.getEquipmentType() == EquipmentType.BATTERY)) {
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
        FilteredIdentifiables filteredIdentifiables = filter.toFilteredIdentifiables(getIdentifiableAttributes(filter, network, filterLoader));

        if (filteredIdentifiables.notFoundIds() != null) {
            filteredIdentifiables.notFoundIds().forEach(element -> notFound.put(element.getId(), element));
        }

        if (filteredIdentifiables.equipmentIds() == null) {
            return;
        }

        if (filterEquipmentType != EquipmentType.SUBSTATION && filterEquipmentType != EquipmentType.VOLTAGE_LEVEL) {
            filteredIdentifiables.equipmentIds().forEach(element -> result.put(element.getId(), element));
            return;
        }

        Set<IdentifiableType> selectedEquipmentTypes = getSelectedEquipmentTypes(filtersWithEquipmentTypes, filter.getId());

        // This list is the result of the original filter and so necessarily contains a list of IDs of substations or voltage levels.
        Set<String> filteredEquipmentIds = filteredIdentifiables.equipmentIds().stream().map(IdentifiableAttributes::getId).collect(Collectors.toSet());

        List<IdentifiableAttributes> identifiableAttributes = getIdentifiableAttributesWithEquipmentTypes(
            filterEquipmentType, filteredEquipmentIds, selectedEquipmentTypes, network, filterLoader);
        identifiableAttributes.forEach(element -> result.put(element.getId(), element));
    }

    private static List<IdentifiableAttributes> getIdentifiableAttributesWithEquipmentTypes(EquipmentType filterEquipmentType,
                                                                                            Set<String> filteredEquipmentIds,
                                                                                            Set<IdentifiableType> selectedEquipmentTypes,
                                                                                            Network network,
                                                                                            FilterLoader filterLoader) {
        List<IdentifiableAttributes> result = new ArrayList<>();

        selectedEquipmentTypes.forEach(selectedEquipmentType -> {
            if (selectedEquipmentType != IdentifiableType.SWITCH) {
                ExpertFilter filter = FilterWithEquipmentTypesUtils.createFilterForSubEquipment(filterEquipmentType, filteredEquipmentIds, selectedEquipmentType);
                result.addAll(getIdentifiableAttributes(filter, network, filterLoader));
            } else {
                // for switch selected equipment type, we cannot create and evaluate an expert filter (it is not implemented yet) to get all
                // the non fictitious breaker or load break switches in coupler cells of the voltageLevels/substations
                result.addAll(getNonFictitiousSwitchesInCouplerCells(filterEquipmentType, filteredEquipmentIds, network));
            }
        });

        return result;
    }

    private static List<IdentifiableAttributes> getNonFictitiousSwitchesInCouplerCells(EquipmentType filterEquipmentType,
                                                                                       Set<String> filteredEquipmentIds,
                                                                                       Network network) {
        List<IdentifiableAttributes> switches = new ArrayList<>();

        Set<String> voltageLevelIds = new HashSet<>();
        if (filterEquipmentType == EquipmentType.SUBSTATION) {
            // get all voltage levels ids of the substations in the list of filtered substations
            network.getSubstationStream()
                    .filter(substation -> filteredEquipmentIds.contains(substation.getId()))
                    .flatMap(Substation::getVoltageLevelStream)
                    .map(VoltageLevel::getId)
                    .forEach(voltageLevelIds::add);
        } else {
            voltageLevelIds = filteredEquipmentIds;
        }

        voltageLevelIds.forEach(vlId -> {
            VoltageLevel vl = network.getVoltageLevel(vlId);
            if (vl != null) {
                // TODO later : use powsybl api when coupling switches detection will be implemented in powsybl-core
                // create voltage level graph (with default layout parameters)
                VoltageLevelGraph voltageLevelGraph = new NetworkGraphBuilder(network, new LayoutParameters()).buildVoltageLevelGraph(vlId);

                // detect cells on this graph
                new ImplicitCellDetector().detectCells(voltageLevelGraph);

                // get all non fictitious breaker or load breaker switches in the graph intern cells
                Set<String> switchIds = getSwitchesFromVoltageLevelCells(voltageLevelGraph, network);
                switchIds.forEach(switchId -> switches.add(new IdentifiableAttributes(switchId, IdentifiableType.SWITCH, null)));
            }
        });

        return switches;
    }

    private static Set<String> getSwitchesFromVoltageLevelCells(VoltageLevelGraph voltageLevelGraph, Network network) {
        Set<String> switchIds = new HashSet<>();
        voltageLevelGraph.getInternCellStream().forEach(cell -> cell.getNodes().forEach(node -> {
            if (node.getType() == Node.NodeType.SWITCH) {
                Switch switchEquipment = network.getSwitch(node.getId());
                if (switchEquipment != null &&
                    !switchEquipment.isFictitious() &&
                    (switchEquipment.getKind() == SwitchKind.BREAKER || switchEquipment.getKind() == SwitchKind.LOAD_BREAK_SWITCH)) {
                    switchIds.add(node.getId());
                }
            }
        }));

        return switchIds;
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
}
