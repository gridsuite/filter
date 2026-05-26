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
class NumberExpertRuleTest {

    private static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(FieldType.NOMINAL_VOLTAGE, null, null, null),
                Arguments.of(FieldType.NOMINAL_VOLTAGE, OperatorType.IS_PART_OF, null, null),
                Arguments.of(FieldType.NOMINAL_VOLTAGE, OperatorType.NOT_IN, null, null),
                Arguments.of(FieldType.ID, OperatorType.NOT_IN, null, null),
                Arguments.of(FieldType.NOMINAL_VOLTAGE, OperatorType.NOT_IN, 15D, null),
                Arguments.of(FieldType.NOMINAL_VOLTAGE, OperatorType.BETWEEN, null, Collections.emptySet()),
                Arguments.of(FieldType.NOMINAL_VOLTAGE, OperatorType.EQUALS, null, Set.of(15D)),
                Arguments.of(FieldType.NOMINAL_VOLTAGE, OperatorType.EXISTS, null, Set.of(15D)),
                Arguments.of(FieldType.NOMINAL_VOLTAGE, OperatorType.EXISTS, 15D, null)
        );
    }

    private static Stream<Arguments> provideRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(FieldType.TARGET_P, OperatorType.EQUALS, 100D, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.EQUALS, 100D, null, EquipmentType.GENERATOR, "GENERATOR_2", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.LOWER, 101D, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.LOWER, 100D, null, EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.LOWER, 99D, null, EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.GREATER, 100D, null, EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.GREATER, 99D, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.GREATER, 101D, null, EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.LOWER_OR_EQUALS, 101D, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.LOWER_OR_EQUALS, 100D, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.LOWER_OR_EQUALS, 99D, null, EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.GREATER_OR_EQUALS, 99D, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.GREATER_OR_EQUALS, 100D, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.GREATER_OR_EQUALS, 101D, null, EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.NOT_EXISTS, null, null, EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.TARGET_Q, OperatorType.NOT_EXISTS, null, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.RATIO_TARGET_V1, OperatorType.NOT_EXISTS, null, null, EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.EXISTS, null, null, EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.BETWEEN, null, Set.of(99D, 101D), EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.BETWEEN, null, Set.of(99D, 101D), EquipmentType.GENERATOR, "GENERATOR_2", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.IN, null, Set.of(99D, 100D, 101D), EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.TARGET_P, OperatorType.IN, null, Set.of(99D, 100D, 101D), EquipmentType.GENERATOR, "GENERATOR_2", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.NOT_IN, null, Set.of(99D, 100D, 101D), EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.TARGET_P, OperatorType.NOT_IN, null, Set.of(99D, 100D, 101D), EquipmentType.GENERATOR, "GENERATOR_2", true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParameters")
    void testInvalidParameters(FieldType fieldType, OperatorType operatorType, Double referenceValue, Set<Double> referenceValues) {
        assertThatThrownBy(() -> NumberExpertRule.of(fieldType, operatorType, referenceValue, referenceValues))
                .isInstanceOf(PowsyblException.class);
    }

    @Test
    void testValidParameters() {
        assertThatNoException().isThrownBy(() -> NumberExpertRule.of(FieldType.NOMINAL_VOLTAGE, OperatorType.IN, null, Set.of(15D, 16D)));
        assertThatNoException().isThrownBy(() -> NumberExpertRule.of(FieldType.NOMINAL_VOLTAGE, OperatorType.EQUALS, 15D, null));
        assertThatNoException().isThrownBy(() -> NumberExpertRule.of(FieldType.NOMINAL_VOLTAGE, OperatorType.EXISTS, null, null));
    }

    @Test
    void testGetDataTypeReturnsNumber() {
        NumberExpertRule rule = NumberExpertRule.of(FieldType.NOMINAL_VOLTAGE, OperatorType.IN, null, Set.of(15D, 16D));

        assertThat(rule.getDataType()).isEqualTo(DataType.NUMBER);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        NumberExpertRule rule = NumberExpertRule.of(FieldType.NOMINAL_VOLTAGE, OperatorType.IN, null, Set.of(15D, 16D));

        assertThat(rule.getOperatorType()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource("provideRuleEvaluationArguments")
    void testRuleEvaluationReturnsExpected(FieldType fieldType, OperatorType operatorType, Double referenceValue, Set<Double> referenceValues, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        ExpertRule rule = NumberExpertRule.of(fieldType, operatorType, referenceValue, referenceValues);
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
