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
class StringExpertRuleTest {

    @Test
    void testGetDataTypeReturnsString() {
        StringExpertRule rule = StringExpertRule.builder()
                .field(FieldType.NAME)
                .operator(OperatorType.IN)
                .build();

        assertThat(rule.getDataType()).isEqualTo(DataType.STRING);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        StringExpertRule rule = StringExpertRule.builder()
                .field(FieldType.NAME)
                .operator(OperatorType.IN)
                .build();

        assertThat(rule.getOperator()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForBusTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForBusBarSectionTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForBatteryTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForLinesTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForTwoWindingsTransformerTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForThreeWindingsTransformerTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForHvdcLineTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForHvdcConverterStationTest",
    })
    void testFilterRoundTripSerializationDeserialization(OperatorType operatorType, FieldType fieldType, String value, Set<String> values,
                                                         Identifiable<?> equipment, boolean expected) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> valuesList = values != null ? List.copyOf(values) : null;
        ExpertRule rule = StringExpertRule.builder()
                .operator(operatorType)
                .field(fieldType)
                .value(value)
                .values(valuesList)
                .build();

        String serializedRule = objectMapper.writeValueAsString(rule);
        ExpertRule deserializedRule = objectMapper.readValue(serializedRule, ExpertRule.class);

        assertThat(deserializedRule).isEqualTo(rule);
    }

    @ParameterizedTest
    @MethodSource("org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForTestWithException")
    void testEvaluateRuleWithException(OperatorType operatorType, FieldType fieldType, Identifiable<?> equipment, Class<Throwable> expectedException) {
        ExpertRule rule = StringExpertRule.builder()
                .operator(operatorType)
                .field(fieldType)
                .build();

        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForBusTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForBusBarSectionTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForBatteryTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForLinesTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForTwoWindingsTransformerTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForThreeWindingsTransformerTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForHvdcLineTest",
        "org.gridsuite.filter.expertfilter.StringExpertRuleTest#provideArgumentsForHvdcConverterStationTest",
    })
    void testEvaluateRule(OperatorType operatorType, FieldType fieldType, String value, Set<String> values, Identifiable<?> equipment, boolean expected) {
        List<String> valuesList = values != null ? List.copyOf(values) : null;
        ExpertRule rule = StringExpertRule.builder()
                .operator(operatorType)
                .field(fieldType)
                .value(value)
                .values(valuesList)
                .build();

        assertEquals(expected, rule.evaluateRule(equipment));
    }
}
