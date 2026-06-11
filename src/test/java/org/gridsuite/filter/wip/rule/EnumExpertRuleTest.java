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
class EnumExpertRuleTest {

    @Test
    void testGetDataTypeReturnsEnum() {
        EnumExpertRule rule = EnumExpertRule.builder()
                .field(FieldType.COUNTRY)
                .operator(OperatorType.IN)
                .build();

        assertThat(rule.getDataType()).isEqualTo(DataType.ENUM);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        EnumExpertRule rule = EnumExpertRule.builder()
                .field(FieldType.COUNTRY)
                .operator(OperatorType.IN)
                .build();

        assertThat(rule.getOperator()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForBusTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForBusBarSectionTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForBatteryTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForVoltageLevelTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForSubstationTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForLinesTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForTwoWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForThreeWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForHvdcLineTest",
    })
    void testFilterRoundTripSerializationDeserialization(OperatorType operator, FieldType field, String value, Set<String> values,
                                                         Identifiable<?> equipment, boolean expected) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        List<String> valuesList = values != null ? List.copyOf(values) : null;
        ExpertRule rule = EnumExpertRule.builder()
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
    @MethodSource({
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, String value, Set<String> values, Class<Throwable> expectedException) {
        List<String> valuesList = values != null ? List.copyOf(values) : null;
        ExpertRule rule = EnumExpertRule.builder()
                .operator(operator)
                .field(field)
                .value(value)
                .values(valuesList)
                .build();

        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForBusTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForBusBarSectionTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForBatteryTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForVoltageLevelTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForSubstationTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForLinesTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForTwoWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForThreeWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.EnumExpertRuleTest#provideArgumentsForHvdcLineTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, String value, Set<String> values, Identifiable<?> equipment, boolean expected) {
        List<String> valuesList = values != null ? List.copyOf(values) : null;
        ExpertRule rule = EnumExpertRule.builder()
                .operator(operator)
                .field(field)
                .value(value)
                .values(valuesList)
                .build();

        assertEquals(expected, rule.evaluateRule(equipment));
    }
}
