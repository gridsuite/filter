/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rules;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.gridsuite.filter.wip.expert.data.DataType;
import org.gridsuite.filter.wip.expert.data.FieldType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class PropertiesExpertRuleTest {

    private static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(FieldType.FREE_PROPERTIES, null, null, null),
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.IN, null, null),
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", null),
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Collections.emptySet()),
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.EQUALS, "myCustomProperty", Set.of("My custom value", "My other custom value")),
                Arguments.of(FieldType.ID, OperatorType.EQUALS, "myCustomProperty", Set.of("My custom value", "My other custom value"))
        );
    }

    private static Stream<Arguments> provideRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Set.of("My custom value", "My other custom value"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true),
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Set.of("My other custom value"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", false),
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Set.of("My custom value", "My other custom value"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_2", false),
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.NOT_IN, "myCustomProperty", Set.of("My custom value", "My other custom value"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", false),
                Arguments.of(FieldType.FREE_PROPERTIES, OperatorType.NOT_IN, "myCustomProperty", Set.of("My other custom value"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParameters")
    void testInvalidParameters(FieldType fieldType, OperatorType operatorType, String targetProperty, Set<String> referenceValues) {
        assertThatThrownBy(() -> PropertiesExpertRule.of(fieldType, operatorType, targetProperty, referenceValues))
                .isInstanceOf(PowsyblException.class);
    }

    @Test
    void testValidParameters() {
        assertThatNoException().isThrownBy(() -> PropertiesExpertRule.of(FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Set.of("My custom value", "My other custom value")));
    }

    @Test
    void testGetDataTypeReturnsProperties() {
        PropertiesExpertRule rule = PropertiesExpertRule.of(FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Set.of("My custom value", "My other custom value"));

        assertThat(rule.getDataType()).isEqualTo(DataType.PROPERTIES);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        PropertiesExpertRule rule = PropertiesExpertRule.of(FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Set.of("My custom value", "My other custom value"));

        assertThat(rule.getOperatorType()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource("provideRuleEvaluationArguments")
    void testRuleEvaluationReturnsExpected(FieldType fieldType, OperatorType operatorType, String targetProperty, Set<String> referenceValues, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        ExpertRule rule = PropertiesExpertRule.of(fieldType, operatorType, targetProperty, referenceValues);
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
