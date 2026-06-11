/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.rule;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.IdentifierListFilter;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.gridsuite.filter.wip.data.DataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class FilterExpertRuleTest {

    private static Stream<Arguments> provideMockRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(FieldType.ID, OperatorType.IS_PART_OF, List.of(mockFilter(EquipmentType.GENERATOR), mockFilter(EquipmentType.LOAD)), "GENERATOR_1", true),
                Arguments.of(FieldType.ID, OperatorType.IS_PART_OF, List.of(mockFilter(EquipmentType.BATTERY), mockFilter(EquipmentType.LOAD)), "GENERATOR_1", false),
                Arguments.of(FieldType.ID, OperatorType.IS_NOT_PART_OF, List.of(mockFilter(EquipmentType.BATTERY), mockFilter(EquipmentType.LOAD)), "GENERATOR_1", true),
                Arguments.of(FieldType.ID, OperatorType.IS_NOT_PART_OF, List.of(mockFilter(EquipmentType.GENERATOR), mockFilter(EquipmentType.LOAD)), "GENERATOR_1", false)
        );
    }

    private static Stream<Arguments> provideRealNetworkIdentifiableRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(FieldType.ID, OperatorType.IS_PART_OF,
                        List.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")),
                                new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2"))),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true),
                Arguments.of(FieldType.ID, OperatorType.IS_PART_OF,
                        List.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")),
                                new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2"))),
                        EquipmentType.GENERATOR,
                        "GENERATOR_3",
                        false),
                Arguments.of(FieldType.ID, OperatorType.IS_NOT_PART_OF,
                        List.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")),
                                new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2"))),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        false),
                Arguments.of(FieldType.ID, OperatorType.IS_NOT_PART_OF,
                        List.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")),
                                new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2"))),
                        EquipmentType.GENERATOR,
                        "GENERATOR_3",
                        true)
        );
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {
        Network network = Mockito.mock(Network.class);

        Generator gen1 = Mockito.mock(Generator.class);
        Mockito.when(gen1.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(gen1.getNetwork()).thenReturn(network);
        Generator gen2 = Mockito.mock(Generator.class);
        Mockito.when(gen2.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(gen2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(gen1.getId()).thenReturn("ID1");
        Mockito.when(gen2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Substation substation1 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Mockito.when(substation1.getId()).thenReturn("SUBST1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(gen1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Mockito.when(substation2.getId()).thenReturn("SUBST2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(gen2.getTerminal()).thenReturn(terminal2);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));

        Mockito.when(network.getGeneratorStream()).thenAnswer(invocation -> Stream.of(gen1, gen2));
        Mockito.when(network.getVoltageLevelStream()).thenAnswer(invocation -> Stream.of(voltageLevel1, voltageLevel2));
        Mockito.when(network.getSubstationStream()).thenAnswer(invocation -> Stream.of(substation1, substation2));

        return Stream.of(
                // --- IS_PART_OF --- //
                // Common fields
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("ID1"))), gen1, true),
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("ID2"))), gen2, true),
                // VoltageLevel fields
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), gen1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), gen2, true),
                // Substation fields
                Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST1"))), gen1, true),
                Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST2"))), gen2, true),

                // --- IS_NOT_PART_OF --- //
                // Common fields
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("ID1"))), gen2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("ID2"))), gen1, true),
                // VoltageLevel fields
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), gen1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), gen2, true),
                // Substation fields
                Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST2"))), gen1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST1"))), gen2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {
        Network network = Mockito.mock(Network.class);

        Load load1 = Mockito.mock(Load.class);
        Mockito.when(load1.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load1.getNetwork()).thenReturn(network);
        Load load2 = Mockito.mock(Load.class);
        Mockito.when(load2.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(load1.getId()).thenReturn("ID1");
        Mockito.when(load2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(load1.getTerminal()).thenReturn(terminal1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(load2.getTerminal()).thenReturn(terminal2);

        Mockito.when(network.getLoadStream()).thenAnswer(invocation -> Stream.of(load1, load2));
        Mockito.when(network.getVoltageLevelStream()).thenAnswer(invocation -> Stream.of(voltageLevel1, voltageLevel2));

        return Stream.of(
                // --- IS_PART_OF --- //
                // Common fields
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.LOAD, Set.of("ID1"))), load1, true),
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.LOAD, Set.of("ID2"))), load2, true),
                // VoltageLevel fields
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), load1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), load2, true),

                // --- IS_NOT_PART_OF --- //
                // Common fields
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.LOAD, Set.of("ID1"))), load2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.LOAD, Set.of("ID2"))), load1, true),
                // VoltageLevel fields
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), load1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), load2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {
        Network network = Mockito.mock(Network.class);

        Battery battery1 = Mockito.mock(Battery.class);
        Mockito.when(battery1.getType()).thenReturn(IdentifiableType.BATTERY);
        Mockito.when(battery1.getNetwork()).thenReturn(network);
        Battery battery2 = Mockito.mock(Battery.class);
        Mockito.when(battery2.getType()).thenReturn(IdentifiableType.BATTERY);
        Mockito.when(battery2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(battery1.getId()).thenReturn("ID1");
        Mockito.when(battery2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(battery1.getTerminal()).thenReturn(terminal1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(battery2.getTerminal()).thenReturn(terminal2);

        Mockito.when(network.getBatteryStream()).thenAnswer(invocation -> Stream.of(battery1, battery2));
        Mockito.when(network.getVoltageLevelStream()).thenAnswer(invocation -> Stream.of(voltageLevel1, voltageLevel2));

        return Stream.of(
                // --- IS_PART_OF --- //
                // Common fields
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.BATTERY, Set.of("ID1"))), battery1, true),
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.BATTERY, Set.of("ID2"))), battery2, true),
                // VoltageLevel fields
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), battery1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), battery2, true),

                // --- IS_NOT_PART_OF --- //
                // Common fields
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.BATTERY, Set.of("ID1"))), battery2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.BATTERY, Set.of("ID2"))), battery1, true),
                // VoltageLevel fields
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), battery1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), battery2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {
        Network network = Mockito.mock(Network.class);

        ShuntCompensator shuntCompensator1 = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator1.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        Mockito.when(shuntCompensator1.getNetwork()).thenReturn(network);
        ShuntCompensator shuntCompensator2 = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator2.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        Mockito.when(shuntCompensator2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(shuntCompensator1.getId()).thenReturn("ID1");
        Mockito.when(shuntCompensator2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(shuntCompensator1.getTerminal()).thenReturn(terminal1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(shuntCompensator2.getTerminal()).thenReturn(terminal2);

        Mockito.when(network.getShuntCompensatorStream()).thenAnswer(invocation -> Stream.of(shuntCompensator1, shuntCompensator2));
        Mockito.when(network.getVoltageLevelStream()).thenAnswer(invocation -> Stream.of(voltageLevel1, voltageLevel2));

        return Stream.of(
                // --- IS_PART_OF --- //
                // Common fields
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.SHUNT_COMPENSATOR, Set.of("ID1"))), shuntCompensator1, true),
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.SHUNT_COMPENSATOR, Set.of("ID2"))), shuntCompensator2, true),
                // VoltageLevel fields
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), shuntCompensator1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), shuntCompensator2, true),

                // --- IS_NOT_PART_OF --- //
                // Common fields
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.SHUNT_COMPENSATOR, Set.of("ID1"))), shuntCompensator2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.SHUNT_COMPENSATOR, Set.of("ID2"))), shuntCompensator1, true),
                // VoltageLevel fields
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), shuntCompensator1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), shuntCompensator2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBoundaryLinesTest() {
        Network network = Mockito.mock(Network.class);

        BoundaryLine boundaryLine1 = Mockito.mock(BoundaryLine.class);
        Mockito.when(boundaryLine1.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);
        Mockito.when(boundaryLine1.getNetwork()).thenReturn(network);
        BoundaryLine boundaryLine2 = Mockito.mock(BoundaryLine.class);
        Mockito.when(boundaryLine2.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);
        Mockito.when(boundaryLine2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(boundaryLine1.getId()).thenReturn("ID1");
        Mockito.when(boundaryLine2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(boundaryLine1.getTerminal()).thenReturn(terminal1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(boundaryLine2.getTerminal()).thenReturn(terminal2);

        Mockito.when(network.getBoundaryLineStream()).thenAnswer(invocation -> Stream.of(boundaryLine1, boundaryLine2));
        Mockito.when(network.getVoltageLevelStream()).thenAnswer(invocation -> Stream.of(voltageLevel1, voltageLevel2));

        return Stream.of(
                // --- IS_PART_OF --- //
                // Common fields
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.BOUNDARY_LINE, Set.of("ID1"))), boundaryLine1, true),
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.BOUNDARY_LINE, Set.of("ID2"))), boundaryLine2, true),
                // VoltageLevel fields
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), boundaryLine1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), boundaryLine2, true),

                // --- IS_NOT_PART_OF --- //
                // Common fields
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.BOUNDARY_LINE, Set.of("ID1"))), boundaryLine2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.BOUNDARY_LINE, Set.of("ID2"))), boundaryLine1, true),
                // VoltageLevel fields
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL2"))), boundaryLine1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL1"))), boundaryLine2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForLineTest() {
        Network network = Mockito.mock(Network.class);

        Line line1 = Mockito.mock(Line.class);
        Mockito.when(line1.getType()).thenReturn(IdentifiableType.LINE);
        Mockito.when(line1.getNetwork()).thenReturn(network);
        Line line2 = Mockito.mock(Line.class);
        Mockito.when(line2.getType()).thenReturn(IdentifiableType.LINE);
        Mockito.when(line2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(line1.getId()).thenReturn("ID1");
        Mockito.when(line2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1Line1 = Mockito.mock(VoltageLevel.class);
        Substation substation1Line1 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel1Line1.getId()).thenReturn("VL11");
        Mockito.when(substation1Line1.getId()).thenReturn("SUBST1");
        Terminal terminal1Line1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1Line1.getVoltageLevel()).thenReturn(voltageLevel1Line1);
        Mockito.when(line1.getTerminal(TwoSides.ONE)).thenReturn(terminal1Line1);
        Mockito.when(voltageLevel1Line1.getSubstation()).thenReturn(Optional.of(substation1Line1));

        VoltageLevel voltageLevel2Line1 = Mockito.mock(VoltageLevel.class);
        Substation substation2Line1 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel2Line1.getId()).thenReturn("VL21");
        Mockito.when(substation2Line1.getId()).thenReturn("SUBST2");
        Terminal terminal2Line1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2Line1.getVoltageLevel()).thenReturn(voltageLevel2Line1);
        Mockito.when(line1.getTerminal(TwoSides.TWO)).thenReturn(terminal2Line1);
        Mockito.when(voltageLevel2Line1.getSubstation()).thenReturn(Optional.of(substation2Line1));

        VoltageLevel voltageLevel1Line2 = Mockito.mock(VoltageLevel.class);
        Substation substation1Line2 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel1Line2.getId()).thenReturn("VL12");
        Mockito.when(substation1Line2.getId()).thenReturn("SUBST3");
        Terminal terminal1Line2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1Line2.getVoltageLevel()).thenReturn(voltageLevel1Line2);
        Mockito.when(line2.getTerminal(TwoSides.ONE)).thenReturn(terminal1Line2);
        Mockito.when(voltageLevel1Line2.getSubstation()).thenReturn(Optional.of(substation1Line2));

        VoltageLevel voltageLevel2Line2 = Mockito.mock(VoltageLevel.class);
        Substation substation2Line2 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel2Line2.getId()).thenReturn("VL22");
        Mockito.when(substation2Line2.getId()).thenReturn("SUBST4");
        Terminal terminal2Line2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2Line2.getVoltageLevel()).thenReturn(voltageLevel2Line2);
        Mockito.when(line2.getTerminal(TwoSides.TWO)).thenReturn(terminal2Line2);
        Mockito.when(voltageLevel2Line2.getSubstation()).thenReturn(Optional.of(substation2Line2));

        Mockito.when(network.getLineStream()).thenAnswer(invocation -> Stream.of(line1, line2));
        Mockito.when(network.getVoltageLevelStream()).thenAnswer(invocation -> Stream.of(voltageLevel1Line1, voltageLevel2Line1, voltageLevel1Line2, voltageLevel2Line2));
        Mockito.when(network.getSubstationStream()).thenAnswer(invocation -> Stream.of(substation1Line1, substation2Line1, substation1Line2, substation2Line2));

        return Stream.of(
                // --- IS_PART_OF --- //
                // Common fields
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.LINE, Set.of("ID1"))), line1, true),
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.LINE, Set.of("ID2"))), line2, true),
                // VoltageLevel fields
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL11"))), line1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL21"))), line1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL12"))), line2, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL22"))), line2, true),
                // Substation fields
                Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID_1, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST1"))), line1, true),
                Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID_2, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST2"))), line1, true),
                Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID_1, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST3"))), line2, true),
                Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID_2, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST4"))), line2, true),

                // --- IS_NOT_PART_OF --- //
                // Common fields
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.LINE, Set.of("ID1"))), line2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.LINE, Set.of("ID2"))), line1, true),
                // VoltageLevel fields
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL11"))), line2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL21"))), line2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL12"))), line1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL22"))), line1, true),
                // Substation fields
                Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID_1, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST1"))), line2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID_2, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST2"))), line2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID_1, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST3"))), line1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID_2, List.of(new IdentifierListFilter(EquipmentType.SUBSTATION, Set.of("SUBST4"))), line1, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForHvdcTest() {
        Network network = Mockito.mock(Network.class);

        HvdcLine hvdcLine1 = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine1.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        Mockito.when(hvdcLine1.getNetwork()).thenReturn(network);
        HvdcLine hvdcLine2 = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine2.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        Mockito.when(hvdcLine2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(hvdcLine1.getId()).thenReturn("ID1");
        Mockito.when(hvdcLine2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1Line1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1Line1.getId()).thenReturn("VL11");
        Terminal terminal1Line1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1Line1.getVoltageLevel()).thenReturn(voltageLevel1Line1);
        HvdcConverterStation converterStation1 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation1.getTerminal()).thenReturn(terminal1Line1);
        Mockito.when(hvdcLine1.getConverterStation1()).thenReturn(converterStation1);
        VoltageLevel voltageLevel2Line1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2Line1.getId()).thenReturn("VL21");
        Terminal terminal2Line1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2Line1.getVoltageLevel()).thenReturn(voltageLevel2Line1);
        HvdcConverterStation converterStation2 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation2.getTerminal()).thenReturn(terminal2Line1);
        Mockito.when(hvdcLine1.getConverterStation2()).thenReturn(converterStation2);

        VoltageLevel voltageLevel1Line2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1Line2.getId()).thenReturn("VL12");
        Terminal terminal1Line2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1Line2.getVoltageLevel()).thenReturn(voltageLevel1Line2);
        HvdcConverterStation converterStation3 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation3.getTerminal()).thenReturn(terminal1Line2);
        Mockito.when(hvdcLine2.getConverterStation1()).thenReturn(converterStation3);
        VoltageLevel voltageLevel2Line2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2Line2.getId()).thenReturn("VL22");
        Terminal terminal2Line2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2Line2.getVoltageLevel()).thenReturn(voltageLevel2Line2);
        HvdcConverterStation converterStation4 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation4.getTerminal()).thenReturn(terminal2Line2);
        Mockito.when(hvdcLine2.getConverterStation2()).thenReturn(converterStation4);

        Mockito.when(network.getHvdcLineStream()).thenAnswer(invocation -> Stream.of(hvdcLine1, hvdcLine2));
        Mockito.when(network.getVoltageLevelStream()).thenAnswer(invocation -> Stream.of(voltageLevel1Line1, voltageLevel2Line1, voltageLevel1Line2, voltageLevel2Line2));
        Mockito.when(network.getHvdcConverterStationStream()).thenAnswer(invocation -> Stream.of(converterStation1, converterStation2, converterStation3, converterStation4));

        return Stream.of(
                // --- IS_PART_OF --- //
                // Common fields
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.HVDC_LINE, Set.of("ID1"))), hvdcLine1, true),
                Arguments.of(IS_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.HVDC_LINE, Set.of("ID2"))), hvdcLine2, true),
                // VoltageLevel fields
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL11"))), hvdcLine1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL21"))), hvdcLine1, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL12"))), hvdcLine2, true),
                Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL22"))), hvdcLine2, true),

                // --- IS_NOT_PART_OF --- //
                // Common fields
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.HVDC_LINE, Set.of("ID1"))), hvdcLine2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.ID, List.of(new IdentifierListFilter(EquipmentType.HVDC_LINE, Set.of("ID2"))), hvdcLine1, true),
                // VoltageLevel fields
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL11"))), hvdcLine2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL21"))), hvdcLine2, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL12"))), hvdcLine1, true),
                Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, List.of(new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VL22"))), hvdcLine1, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForTestWithException() {
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(generator.getTerminal()).thenReturn(terminal);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);

        BoundaryLine boundaryLine = Mockito.mock(BoundaryLine.class);
        Mockito.when(boundaryLine.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);

        return Stream.of(
                // --- Test an unsupported field for each equipment --- //
                Arguments.of(IS, FieldType.P0, generator, PowsyblException.class),
                Arguments.of(IS, FieldType.RATED_S, load, PowsyblException.class),
                Arguments.of(IS, FieldType.MIN_P, shuntCompensator, PowsyblException.class),
                Arguments.of(IS, FieldType.HIGH_VOLTAGE_LIMIT, battery, PowsyblException.class),
                Arguments.of(IS, FieldType.MARGINAL_COST, line, PowsyblException.class),
                Arguments.of(IS, FieldType.MARGINAL_COST, boundaryLine, PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(EQUALS, FieldType.ID, generator, PowsyblException.class),
                Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, generator, PowsyblException.class)
        );
    }

    private static Filter mockFilter(EquipmentType equipmentType) {
        Network network = TestNetworkUtils.createTestNetwork();
        Filter filterMock = mock(Filter.class);

        if (EquipmentType.LOAD.equals(equipmentType)) {
            List<Identifiable<?>> loadList = network.getLoadStream().collect(Collectors.toList());
            when(filterMock.evaluate(any())).thenReturn(loadList);
        } else if (EquipmentType.GENERATOR.equals(equipmentType)) {
            List<Identifiable<?>> generatorList = network.getGeneratorStream().collect(Collectors.toList());
            when(filterMock.evaluate(any())).thenReturn(generatorList);
        } else if (EquipmentType.BATTERY.equals(equipmentType)) {
            List<Identifiable<?>> batteryList = network.getBatteryStream().collect(Collectors.toList());
            when(filterMock.evaluate(any())).thenReturn(batteryList);
        } else {
            when(filterMock.evaluate(any())).thenReturn(Collections.emptyList());
        }
        return filterMock;
    }

    @Test
    void testGetDataTypeReturnsFilter() {
        FilterExpertRule rule = FilterExpertRule.builder().field(FieldType.ID).operator(OperatorType.IS_PART_OF).filters(Collections.emptyList()).build();

        assertThat(rule.getDataType()).isEqualTo(DataType.FILTER);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        FilterExpertRule rule = FilterExpertRule.builder().field(FieldType.ID).operator(OperatorType.IS_PART_OF).filters(Collections.emptyList()).build();

        assertThat(rule.getOperator()).isEqualTo(OperatorType.IS_PART_OF);
    }

    @Test
    void testClearCacheClearsCachingSet() {
        String identifiableId = "identifiableId";
        Identifiable<?> identifiableMock = mock(Identifiable.class);
        when(identifiableMock.getId()).thenReturn(identifiableId);
        Filter filterMock = mock(Filter.class);
        when(filterMock.evaluate(any())).thenReturn(List.of(identifiableMock));
        FilterExpertRule rule = FilterExpertRule.builder().field(FieldType.ID).operator(OperatorType.IS_PART_OF).filters(List.of(filterMock)).build();
        rule.evaluateRule(mock(Identifiable.class));

        boolean isCachedFilterEvaluationBeforeClear = rule.isCachedFilterEvaluation();
        boolean isCacheEmptyBeforeClear = rule.getFilterEvaluationCache().isEmpty();
        boolean isIdPartOfCacheBeforeClear = rule.getFilterEvaluationCache().contains(identifiableId);
        rule.clearCache();
        boolean isCacheEmptyAfterClear = rule.getFilterEvaluationCache().isEmpty();
        boolean isCachedFilterEvaluationAfterClear = rule.isCachedFilterEvaluation();

        assertThat(isIdPartOfCacheBeforeClear).isTrue();
        assertThat(isCachedFilterEvaluationBeforeClear).isTrue();
        assertThat(isCacheEmptyBeforeClear).isFalse();
        assertThat(isCacheEmptyAfterClear).isTrue();
        assertThat(isCachedFilterEvaluationAfterClear).isFalse();
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForBoundaryLinesTest",
        "provideArgumentsForLineTest",
        "provideArgumentsForHvdcTest",
    })
    void testFilterRoundTripSerializationDeserialization(OperatorType operatorType, FieldType fieldType, List<Filter> filters,
                                                         Identifiable<?> equipment, boolean expected) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExpertRule rule = FilterExpertRule.builder()
                .operator(operatorType)
                .field(fieldType)
                .filters(filters)
                .build();

        String serializedRule = objectMapper.writeValueAsString(rule);
        ExpertRule deserializedRule = objectMapper.readValue(serializedRule, ExpertRule.class);

        assertThat(deserializedRule).isEqualTo(rule);
    }

    @ParameterizedTest
    @MethodSource("provideMockRuleEvaluationArguments")
    void testMockRuleEvaluationReturnsExpected(FieldType fieldType, OperatorType operatorType, List<Filter> filters, String equipmentId, boolean expectedResult) {
        Identifiable<?> identifiable = mock(Identifiable.class, RETURNS_DEEP_STUBS);
        when(identifiable.getId()).thenReturn(equipmentId);
        when(identifiable.getNetwork().getId()).thenReturn("testNetworkId");
        ExpertRule rule = FilterExpertRule.builder()
                .field(fieldType)
                .operator(operatorType)
                .filters(filters)
                .build();

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("provideRealNetworkIdentifiableRuleEvaluationArguments")
    void testRealRuleEvaluationReturnsExpected(FieldType fieldType, OperatorType operatorType, List<Filter> filters, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);
        ExpertRule rule = FilterExpertRule.builder()
                .field(fieldType)
                .operator(operatorType)
                .filters(filters)
                .build();

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("provideArgumentsForTestWithException")
    void testEvaluateRuleWithException(OperatorType operatorType, FieldType fieldType, Identifiable<?> equipment, Class<Throwable> expectedException) {
        ExpertRule rule = FilterExpertRule.builder()
                .field(fieldType)
                .operator(operatorType)
                .filters(Collections.emptyList())
                .build();

        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForBoundaryLinesTest",
        "provideArgumentsForLineTest",
        "provideArgumentsForHvdcTest",
    })
    void testEvaluateRule(OperatorType operatorType, FieldType fieldType, List<Filter> filters, Identifiable<?> equipment, boolean expected) {
        ExpertRule rule = FilterExpertRule.builder()
                .field(fieldType)
                .operator(operatorType)
                .filters(filters)
                .build();

        assertEquals(expected, rule.evaluateRule(equipment));
    }
}
