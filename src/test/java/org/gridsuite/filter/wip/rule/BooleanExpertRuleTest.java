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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class BooleanExpertRuleTest {

    @Test
    void testGetDataTypeReturnsBoolean() {
        BooleanExpertRule rule = BooleanExpertRule.builder().field(FieldType.CONNECTED).operator(OperatorType.EQUALS).value(true).build();

        assertThat(rule.getDataType()).isEqualTo(DataType.BOOLEAN);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        BooleanExpertRule rule = BooleanExpertRule.builder().field(FieldType.CONNECTED).operator(OperatorType.EQUALS).value(true).build();

        assertThat(rule.getOperator()).isEqualTo(OperatorType.EQUALS);
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForBatteryTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForLinesTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForTwoWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForThreeWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForHvdcLinesTest",
    })
    void testFilterRoundTripSerializationDeserialization(OperatorType operator, FieldType field, Boolean value, Identifiable<?> equipment, boolean expected) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExpertRule rule = BooleanExpertRule.builder()
                .operator(operator)
                .field(field)
                .value(value)
                .build();

        String serializedRule = objectMapper.writeValueAsString(rule);
        ExpertRule deserializedRule = objectMapper.readValue(serializedRule, ExpertRule.class);

        assertThat(deserializedRule).isEqualTo(rule);
    }

    @ParameterizedTest
    @MethodSource("org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForTestWithException")
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class<Throwable> expectedException) {
        ExpertRule rule = BooleanExpertRule.builder()
                .operator(operator)
                .field(field)
                .build();

        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForBatteryTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForLinesTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForTwoWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForThreeWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.BooleanExpertRuleTest#provideArgumentsForHvdcLinesTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, Boolean value, Identifiable<?> equipment, boolean expected) {
        ExpertRule rule = BooleanExpertRule.builder()
                .operator(operator)
                .field(field)
                .value(value)
                .build();

        assertEquals(expected, rule.evaluateRule(equipment));
    }
}
