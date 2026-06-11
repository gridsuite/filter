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
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.data.DataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class NumberExpertRuleTest {

    @Test
    void testGetDataTypeReturnsNumber() {
        NumberExpertRule rule = NumberExpertRule.builder()
                .field(FieldType.NOMINAL_VOLTAGE)
                .operator(OperatorType.IN)
                .build();

        assertThat(rule.getDataType()).isEqualTo(DataType.NUMBER);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        NumberExpertRule rule = NumberExpertRule.builder()
                .field(FieldType.NOMINAL_VOLTAGE)
                .operator(OperatorType.IN)
                .build();

        assertThat(rule.getOperator()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForBusTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForBusBarSectionTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForBatteryTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForVoltageLevelTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForLinesTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForTwoWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForThreeWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForHvdcLinesTest",
    })
    void testFilterRoundTripSerializationDeserialization(OperatorType operator, FieldType field, Double value, Set<Double> values,
                                                         Identifiable<?> equipment, boolean expected) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<Double> valuesList = values != null ? List.copyOf(values) : null;
        ExpertRule rule = NumberExpertRule.builder()
                .operator(operator)
                .field(field)
                .value(value)
                .values(valuesList)
                .build();

        String serializedRule = objectMapper.writeValueAsString(rule);
        ExpertRule deserializedRule = objectMapper.readValue(serializedRule, ExpertRule.class);

        assertThat(deserializedRule).isEqualTo(rule);
    }

    @ParameterizedTest
    @MethodSource("org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForTestWithException")
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class<Throwable> expectedException) {
        ExpertRule rule = NumberExpertRule.builder()
                .operator(operator)
                .field(field)
                .build();

        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForBusTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForBusBarSectionTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForBatteryTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForVoltageLevelTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForLinesTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForTwoWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForThreeWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.NumberExpertRuleTest#provideArgumentsForHvdcLinesTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, Double value, Set<Double> values, Identifiable<?> equipment, boolean expected) {
        List<Double> valuesList = values != null ? List.copyOf(values) : null;
        ExpertRule rule = NumberExpertRule.builder()
                .operator(operator)
                .field(field)
                .value(value)
                .values(valuesList)
                .build();

        assertEquals(expected, rule.evaluateRule(equipment));
    }
}
