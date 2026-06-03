/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rule;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.AbstractFilter;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.gridsuite.filter.wip.expert.data.DataType;
import org.gridsuite.filter.wip.identifier.IdentifierListFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class FilterExpertRuleTest {

    private static Stream<Arguments> provideMockRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(OperatorType.IS_PART_OF, Set.of(mockFilter(EquipmentType.GENERATOR), mockFilter(EquipmentType.LOAD)), "GENERATOR_1", true),
                Arguments.of(OperatorType.IS_PART_OF, Set.of(mockFilter(EquipmentType.BATTERY), mockFilter(EquipmentType.LOAD)), "GENERATOR_1", false),
                Arguments.of(OperatorType.IS_NOT_PART_OF, Set.of(mockFilter(EquipmentType.BATTERY), mockFilter(EquipmentType.LOAD)), "GENERATOR_1", true),
                Arguments.of(OperatorType.IS_NOT_PART_OF, Set.of(mockFilter(EquipmentType.GENERATOR), mockFilter(EquipmentType.LOAD)), "GENERATOR_1", false)
        );
    }

    private static Stream<Arguments> provideRealRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(OperatorType.IS_PART_OF,
                        Set.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")),
                                new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2"))),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true),
                Arguments.of(OperatorType.IS_PART_OF,
                        Set.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")),
                                new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2"))),
                        EquipmentType.GENERATOR,
                        "GENERATOR_3",
                        false),
                Arguments.of(OperatorType.IS_NOT_PART_OF,
                        Set.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")),
                                new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2"))),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        false),
                Arguments.of(OperatorType.IS_NOT_PART_OF,
                        Set.of(new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GENERATOR_1", "GENERATOR_2")),
                                new IdentifierListFilter(EquipmentType.LOAD, Set.of("LOAD_1", "LOAD_2"))),
                        EquipmentType.GENERATOR,
                        "GENERATOR_3",
                        true)
        );
    }

    private static AbstractFilter mockFilter(EquipmentType equipmentType) {
        Network network = TestNetworkUtils.createTestNetwork();
        AbstractFilter abstractFilter = mock(AbstractFilter.class);

        if (EquipmentType.LOAD.equals(equipmentType)) {
            List<Identifiable<?>> loadList = network.getLoadStream().collect(Collectors.toList());
            when(abstractFilter.evaluate(any())).thenReturn(loadList);
        } else if (EquipmentType.GENERATOR.equals(equipmentType)) {
            List<Identifiable<?>> generatorList = network.getGeneratorStream().collect(Collectors.toList());
            when(abstractFilter.evaluate(any())).thenReturn(generatorList);
        } else if (EquipmentType.BATTERY.equals(equipmentType)) {
            List<Identifiable<?>> batteryList = network.getBatteryStream().collect(Collectors.toList());
            when(abstractFilter.evaluate(any())).thenReturn(batteryList);
        } else {
            when(abstractFilter.evaluate(any())).thenReturn(Collections.emptyList());
        }
        return abstractFilter;
    }

    @Test
    void testGetDataTypeReturnsFilter() {
        FilterExpertRule rule = FilterExpertRule.builder().build();

        assertThat(rule.getDataType()).isEqualTo(DataType.FILTER);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        FilterExpertRule rule = FilterExpertRule.builder().operatorType(OperatorType.IS_PART_OF).build();

        assertThat(rule.getOperatorType()).isEqualTo(OperatorType.IS_PART_OF);
    }

    @ParameterizedTest
    @MethodSource("provideMockRuleEvaluationArguments")
    void testMockRuleEvaluationReturnsExpected(OperatorType operatorType, Set<Filter> referenceFilters, String equipmentId, boolean expectedResult) {
        ExpertRule rule = FilterExpertRule.builder().operatorType(operatorType).referenceFilters(referenceFilters).build();
        Identifiable<?> identifiable = mock(Identifiable.class, RETURNS_DEEP_STUBS);
        when(identifiable.getId()).thenReturn(equipmentId);
        when(identifiable.getNetwork().getId()).thenReturn("testNetworkId");

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("provideRealRuleEvaluationArguments")
    void testRealRuleEvaluationReturnsExpected(OperatorType operatorType, Set<Filter> referenceFilters, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        ExpertRule rule = FilterExpertRule.builder().operatorType(operatorType).referenceFilters(referenceFilters).build();
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
