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

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.IN;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.NOT_IN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class PropertiesExpertRuleTest {

    @Test
    void testGetDataTypeReturnsProperties() {
        PropertiesExpertRule rule = PropertiesExpertRule.builder()
                .field(FieldType.VOLTAGE_LEVEL_PROPERTIES)
                .operator(OperatorType.IN)
                .propertyName("")
                .propertyValues(Collections.emptyList())
                .build();

        assertThat(rule.getDataType()).isEqualTo(DataType.PROPERTIES);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        PropertiesExpertRule rule = PropertiesExpertRule.builder()
                .field(FieldType.VOLTAGE_LEVEL_PROPERTIES)
                .operator(OperatorType.IN)
                .propertyName("")
                .propertyValues(Collections.emptyList())
                .build();

        assertThat(rule.getOperator()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForSubstationTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForTwoWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForLineTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForThreeWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForHvdcLineTest",
    })
    void testFilterRoundTripSerializationDeserialization(OperatorType operatorType, FieldType fieldType, String propertyName, List<String> propertyValues,
                                                         Identifiable<?> equipment, boolean expected) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExpertRule rule = PropertiesExpertRule.builder()
                .operator(operatorType)
                .field(fieldType)
                .propertyName(propertyName)
                .propertyValues(propertyValues)
                .build();

        String serializedRule = objectMapper.writeValueAsString(rule);
        ExpertRule deserializedRule = objectMapper.readValue(serializedRule, ExpertRule.class);

        assertThat(deserializedRule).isEqualTo(rule);
    }

    @Test
    void testPropertiesValue() {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operator(IN).field(FieldType.FREE_PROPERTIES).propertyName("property")
                .propertyValues(List.of("value1")).build();
        assertEquals(List.of("value1"), rule.getPropertyValues());
        assertEquals("property", rule.getPropertyName());
        assertEquals(FieldType.FREE_PROPERTIES, rule.getField());
        assertEquals(IN, rule.getOperator());
        rule = PropertiesExpertRule.builder().operator(NOT_IN).field(FieldType.FREE_PROPERTIES).propertyName("property2")
                .propertyValues(List.of("value2")).build();
        assertEquals(List.of("value2"), rule.getPropertyValues());
        assertEquals("property2", rule.getPropertyName());
        assertEquals(FieldType.FREE_PROPERTIES, rule.getField());
        assertEquals(NOT_IN, rule.getOperator());
    }

    @ParameterizedTest
    @MethodSource("org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForTestWithException")
    void testEvaluateRuleWithException(OperatorType operatorType, FieldType fieldType, Identifiable<?> equipment,
                                       String propertyName, List<String> propertyValues, Class<Throwable> expectedException) {
        ExpertRule rule = PropertiesExpertRule.builder()
                .operator(operatorType)
                .field(fieldType)
                .propertyName(propertyName)
                .propertyValues(propertyValues)
                .build();

        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    @ParameterizedTest
    @MethodSource({
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForSubstationTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForGeneratorTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForTwoWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForLoadTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForShuntCompensatorTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForLineTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForStaticVarCompensatorTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForBoundaryLineTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForThreeWindingTransformerTest",
        "org.gridsuite.filter.expertfilter.PropertiesExpertRuleTest#provideArgumentsForHvdcLineTest",
    })
    void testEvaluateRule(OperatorType operatorType, FieldType fieldType, String propertyName, List<String> propertyValues, Identifiable<?> equipment, boolean expected) {
        ExpertRule rule = PropertiesExpertRule.builder()
                .operator(operatorType)
                .field(fieldType)
                .propertyName(propertyName)
                .propertyValues(propertyValues)
                .build();

        assertEquals(expected, rule.evaluateRule(equipment));
    }
}
