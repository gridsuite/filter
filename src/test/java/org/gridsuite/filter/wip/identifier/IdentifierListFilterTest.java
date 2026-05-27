/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.identifier;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class IdentifierListFilterTest {

    private Network network;

    @BeforeEach
    void setUp() {
        network = TestNetworkUtils.createTestNetwork();
    }

    private static Stream<Arguments> provideFilterArguments() {
        return Stream.of(// For each equipment type, we test that the filter is able to return multiple equipments, one equipment and no equipment when it's missing
                // Lines
                Arguments.of(new IdentifierListFilter(EquipmentType.LINE, Set.of("LINE_1", "LINE_2")), Set.of("LINE_1", "LINE_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.LINE, Set.of("LINE_3")), Set.of("LINE_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.LINE, Set.of("MISSING_LINE")), Collections.emptySet()),
                // Boundary Lines
                Arguments.of(new IdentifierListFilter(EquipmentType.BOUNDARY_LINE, Set.of("BOUNDARY_LINE_1", "BOUNDARY_LINE_2")), Set.of("BOUNDARY_LINE_1", "BOUNDARY_LINE_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.BOUNDARY_LINE, Set.of("BOUNDARY_LINE_3")), Set.of("BOUNDARY_LINE_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.BOUNDARY_LINE, Set.of("MISSING_BOUNDARY_LINE")), Collections.emptySet()),
                // HVDC Lines
                Arguments.of(new IdentifierListFilter(EquipmentType.HVDC_LINE, Set.of("HVDC_LINE_1", "HVDC_LINE_2")), Set.of("HVDC_LINE_1", "HVDC_LINE_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.HVDC_LINE, Set.of("HVDC_LINE_3")), Set.of("HVDC_LINE_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.HVDC_LINE, Set.of("MISSING_HVDC_LINE")), Collections.emptySet()),
                // Substations
                Arguments.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBSTATION_1", "SUBSTATION_2")), Set.of("SUBSTATION_1", "SUBSTATION_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBSTATION_3")), Set.of("SUBSTATION_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("MISSING_SUBSTATION")), Collections.emptySet()),
                // Voltage Leves
                Arguments.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VOLTAGE_LEVEL_1", "VOLTAGE_LEVEL_2")), Set.of("VOLTAGE_LEVEL_1", "VOLTAGE_LEVEL_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VOLTAGE_LEVEL_3")), Set.of("VOLTAGE_LEVEL_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("MISSING_VOLTAGE_LEVEL")), Collections.emptySet()),
                // Busbar Sections
                Arguments.of(new IdentifierListFilter(EquipmentType.BUSBAR_SECTION, Set.of("BUSBAR_SECTION_1", "BUSBAR_SECTION_2")), Set.of("BUSBAR_SECTION_1", "BUSBAR_SECTION_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.BUSBAR_SECTION, Set.of("BUSBAR_SECTION_3")), Set.of("BUSBAR_SECTION_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.BUSBAR_SECTION, Set.of("MISSING_BUSBAR_SECTION")), Collections.emptySet()),
                // Buses
                Arguments.of(new IdentifierListFilter(EquipmentType.BUS, Set.of("BUS_S2", "BUS_S3")), Set.of("BUS_S2", "BUS_S3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.BUS, Set.of("BUS_S3")), Set.of("BUS_S3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.BUS, Set.of("MISSING_BUS")), Collections.emptySet()),
                // Generators
                Arguments.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")), Set.of("GENERATOR_1", "GENERATOR_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_3")), Set.of("GENERATOR_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("MISSING_GENERATOR")), Collections.emptySet()),
                // Batteries
                Arguments.of(new IdentifierListFilter(EquipmentType.BATTERY, Set.of("BATTERY_1", "BATTERY_2")), Set.of("BATTERY_1", "BATTERY_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.BATTERY, Set.of("BATTERY_3")), Set.of("BATTERY_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.BATTERY, Set.of("MISSING_BATTERY")), Collections.emptySet()),
                // Loads
                Arguments.of(new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2")), Set.of("LOAD_1", "LOAD_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_3")), Set.of("LOAD_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.LOAD, Set.of("MISSING_LOAD")), Collections.emptySet()),
                // Shunt Compensators
                Arguments.of(new IdentifierListFilter(EquipmentType.SHUNT_COMPENSATOR, Set.of("SHUNT_COMPENSATOR_1", "SHUNT_COMPENSATOR_2")), Set.of("SHUNT_COMPENSATOR_1", "SHUNT_COMPENSATOR_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.SHUNT_COMPENSATOR, Set.of("SHUNT_COMPENSATOR_3")), Set.of("SHUNT_COMPENSATOR_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.SHUNT_COMPENSATOR, Set.of("MISSING_SHUNT_COMPENSATOR")), Collections.emptySet()),
                // Static Var Compensators
                Arguments.of(new IdentifierListFilter(EquipmentType.STATIC_VAR_COMPENSATOR, Set.of("STATIC_VAR_COMPENSATOR_1", "STATIC_VAR_COMPENSATOR_2")), Set.of("STATIC_VAR_COMPENSATOR_1", "STATIC_VAR_COMPENSATOR_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.STATIC_VAR_COMPENSATOR, Set.of("STATIC_VAR_COMPENSATOR_3")), Set.of("STATIC_VAR_COMPENSATOR_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.STATIC_VAR_COMPENSATOR, Set.of("MISSING_STATIC_VAR_COMPENSATOR")), Collections.emptySet()),
                // Two Windings Transformers
                Arguments.of(new IdentifierListFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, Set.of("TWO_WINDINGS_TRANSFORMER_1", "TWO_WINDINGS_TRANSFORMER_2")), Set.of("TWO_WINDINGS_TRANSFORMER_1", "TWO_WINDINGS_TRANSFORMER_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, Set.of("TWO_WINDINGS_TRANSFORMER_3")), Set.of("TWO_WINDINGS_TRANSFORMER_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, Set.of("MISSING_TWO_WINDINGS_TRANSFORMER")), Collections.emptySet()),
                // Three Windings Transformers
                Arguments.of(new IdentifierListFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, Set.of("THREE_WINDINGS_TRANSFORMER_1", "THREE_WINDINGS_TRANSFORMER_2")), Set.of("THREE_WINDINGS_TRANSFORMER_1", "THREE_WINDINGS_TRANSFORMER_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, Set.of("THREE_WINDINGS_TRANSFORMER_3")), Set.of("THREE_WINDINGS_TRANSFORMER_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, Set.of("MISSING_THREE_WINDINGS_TRANSFORMER")), Collections.emptySet()),
                // Lcc Converter Stations
                Arguments.of(new IdentifierListFilter(EquipmentType.LCC_CONVERTER_STATION, Set.of("LCC_CONVERTER_STATION_1", "LCC_CONVERTER_STATION_2")), Set.of("LCC_CONVERTER_STATION_1", "LCC_CONVERTER_STATION_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.LCC_CONVERTER_STATION, Set.of("LCC_CONVERTER_STATION_3")), Set.of("LCC_CONVERTER_STATION_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.LCC_CONVERTER_STATION, Set.of("MISSING_LCC_CONVERTER_STATION")), Collections.emptySet()),
                // Vsc Converter Stations
                Arguments.of(new IdentifierListFilter(EquipmentType.VSC_CONVERTER_STATION, Set.of("VSC_CONVERTER_STATION_1", "VSC_CONVERTER_STATION_2")), Set.of("VSC_CONVERTER_STATION_1", "VSC_CONVERTER_STATION_2")),
                Arguments.of(new IdentifierListFilter(EquipmentType.VSC_CONVERTER_STATION, Set.of("VSC_CONVERTER_STATION_3")), Set.of("VSC_CONVERTER_STATION_3")),
                Arguments.of(new IdentifierListFilter(EquipmentType.VSC_CONVERTER_STATION, Set.of("MISSING_VSC_CONVERTER_STATION")), Collections.emptySet())
        );
    }

    private static Stream<Arguments> provideTopologyDependedFilterArguments() {
        return Stream.of(
                // Buses - BUS_BREAKER
                Arguments.of(TopologyKind.BUS_BREAKER, new IdentifierListFilter(EquipmentType.BUS, Set.of("BUS_S2", "BUS_S3")), Set.of("BUS_S2", "BUS_S3")),
                Arguments.of(TopologyKind.BUS_BREAKER, new IdentifierListFilter(EquipmentType.BUS, Set.of("BUS_S3")), Set.of("BUS_S3")),
                Arguments.of(TopologyKind.BUS_BREAKER, new IdentifierListFilter(EquipmentType.BUS, Set.of("MISSING_BUS")), Collections.emptySet()),
                // Buses - NODE_BREAKER
                Arguments.of(TopologyKind.NODE_BREAKER, new IdentifierListFilter(EquipmentType.BUS, Set.of("BUS_S2", "BUS_S3")), Collections.emptySet()),
                Arguments.of(TopologyKind.NODE_BREAKER, new IdentifierListFilter(EquipmentType.BUS, Set.of("VOLTAGE_LEVEL_1_0", "VOLTAGE_LEVEL_2_0")), Set.of("VOLTAGE_LEVEL_1_0", "VOLTAGE_LEVEL_2_0")),
                Arguments.of(TopologyKind.NODE_BREAKER, new IdentifierListFilter(EquipmentType.BUS, Set.of("VOLTAGE_LEVEL_3_0")), Set.of("VOLTAGE_LEVEL_3_0")),
                Arguments.of(TopologyKind.NODE_BREAKER, new IdentifierListFilter(EquipmentType.BUS, Set.of("MISSING_BUS")), Collections.emptySet()),
                // Buses - No Topology provided
                Arguments.of(null, new IdentifierListFilter(EquipmentType.BUS, Set.of("BUS_S2", "BUS_S3")), Set.of("BUS_S2", "BUS_S3")),
                Arguments.of(null, new IdentifierListFilter(EquipmentType.BUS, Set.of("BUS_S2", "BUS_S3", "VOLTAGE_LEVEL_1_0")), Set.of("BUS_S2", "BUS_S3", "VOLTAGE_LEVEL_1_0")),
                Arguments.of(null, new IdentifierListFilter(EquipmentType.BUS, Set.of("MISSING_BUS")), Collections.emptySet())
        );
    }

    @Test
    void testCreateFilterWithNullEquipmentTypeThrowsException() {
        Set<String> emptySet = Collections.emptySet();
        assertThatThrownBy(() -> new IdentifierListFilter(null, emptySet))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testCreateFilterWithNullEquipmentIdsThrowsException() {
        assertThatThrownBy(() -> new IdentifierListFilter(EquipmentType.LINE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testGetFilterTypeReturnsIdentifierList() {
        Filter filter = new IdentifierListFilter(EquipmentType.LINE, Collections.emptySet());

        assertThat(filter.getFilterType()).isEqualTo(FilterType.IDENTIFIER_LIST);
    }

    @ParameterizedTest
    @MethodSource("provideFilterArguments")
    void testFilterEvaluationReturnsExpectedEquipments(Filter filter, Set<String> expectedEquipmentIds) {
        List<Identifiable<?>> filteredIdentifiableList = filter.evaluate(network);

        assertThat(filteredIdentifiableList).hasSize(expectedEquipmentIds.size());
        assertThat(filteredIdentifiableList.stream().map(Identifiable::getId)).containsAll(expectedEquipmentIds);
    }

    @ParameterizedTest
    @MethodSource("provideTopologyDependedFilterArguments")
    void testFilterEvaluationWithTopologyKindReturnsExpectedEquipments(TopologyKind topologyKind, Filter filter, Set<String> expectedEquipmentIds) {
        List<Identifiable<?>> filteredIdentifiableList = filter.evaluate(network, topologyKind);

        assertThat(filteredIdentifiableList).hasSize(expectedEquipmentIds.size());
        assertThat(filteredIdentifiableList.stream().map(Identifiable::getId)).containsAll(expectedEquipmentIds);
    }
}
