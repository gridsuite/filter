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

        FilterEquipments filterEquipments = filter.toFilterEquipments(identifiables);
        if (filterEquipments.getNotFoundEquipments() != null) {
            filterEquipments.getNotFoundEquipments().forEach(id -> notFound.put(
                    id,
                    new IdentifiableAttributes(id, EquipmentTypeMapper.map(filter.getEquipmentType()), null)));
        }

        if (filterEquipmentType != EquipmentType.SUBSTATION && filterEquipmentType != EquipmentType.VOLTAGE_LEVEL) {
            filterEquipments.getIdentifiableAttributes().forEach(id -> result.put(id.getId(), id));
            return;
        }

        Set<IdentifiableType> selectedEquipmentTypes = getSelectedEquipmentTypes(filtersWithEquipmentTypes, filter.getId());

        // This list is the result of the original filter and so necessarily contains a list of IDs of substations or voltage levels.
        List<String> filteredEquipmentIds = identifiables.stream().map(Identifiable::getId).toList();

        List<IdentifiableAttributes> identifiableAttributes = getIdentifiableAttributesWithEquipmentTypes(
            filterEquipmentType, filteredEquipmentIds, selectedEquipmentTypes, network);
        identifiableAttributes.forEach(element -> result.put(element.getId(), element));
    }

    private static List<IdentifiableAttributes> getIdentifiableAttributesWithEquipmentTypes(EquipmentType filterEquipmentType,
                                                                                             List<String> filteredEquipmentIds,
                                                                                             Set<IdentifiableType> selectedEquipmentTypes,
                                                                                             Network network) {
        List<IdentifiableAttributes> result = new ArrayList<>();

        selectedEquipmentTypes.forEach(selectedEquipmentType -> {
            if (selectedEquipmentType != IdentifiableType.SWITCH) {
                ExpertFilter filter = FilterWithEquipmentTypesUtils.createFilterForSubEquipment(filterEquipmentType, filteredEquipmentIds, selectedEquipmentType);
                List<IdentifiableAttributes> attributes = filter.evaluate(network).stream().map(FilterServiceUtils::map).toList();
                result.addAll(attributes);
            } else {
                // for switch selected equipment type, we cannot create and evaluate an expert filter (it is not implemented yet) to get all
                // the non fictitious breaker or load break switches in coupler cells of the voltageLevels/substations
                result.addAll(getNonFictitiousSwitchesInCouplerCells(filterEquipmentType, filteredEquipmentIds, network));
            }
        });

        return result;
    }

    private static List<IdentifiableAttributes> getNonFictitiousSwitchesInCouplerCells(EquipmentType filterEquipmentType,
                                                                                        List<String> filteredEquipmentIds,
                                                                                        Network network) {
        List<IdentifiableAttributes> switches = new ArrayList<>();

        List<String> voltageLevelIds = new ArrayList<>();
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

    public static IdentifiableAttributes map(Identifiable<?> identifiable) {
        return new IdentifiableAttributes(identifiable.getId(), identifiable.getType(), null);
    }

    public static FilteredIdentifiables map(List<Identifiable<?>> identifiables) {
        return new FilteredIdentifiables(identifiables.stream().map(FilterServiceUtils::map).toList(), List.of());
    }
}
