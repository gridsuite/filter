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

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class BooleanExpertRuleTest {

    private static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of(null, null, null),
                Arguments.of(FieldType.HAS_RATIO_TAP_CHANGER, null, null),
                Arguments.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.EQUALS, null),
                Arguments.of(FieldType.ID, OperatorType.EQUALS, false),
                Arguments.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.BEGINS_WITH, false),
                Arguments.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.EXISTS, false),
                Arguments.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.NOT_EXISTS, true)

        );
    }

    private static Stream<Arguments> provideRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(FieldType.CONNECTED_3, OperatorType.EQUALS, true, EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", true),
                Arguments.of(FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, OperatorType.EQUALS, true, EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", false),
                Arguments.of(FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, OperatorType.NOT_EXISTS, null, EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", true),
                Arguments.of(FieldType.LOAD_TAP_CHANGING_CAPABILITIES, OperatorType.NOT_EXISTS, null, EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", false),
                Arguments.of(FieldType.LOAD_TAP_CHANGING_CAPABILITIES, OperatorType.EXISTS, null, EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", true),
                Arguments.of(FieldType.HAS_PHASE_TAP_CHANGER, OperatorType.NOT_EQUALS, true, EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", true),
                Arguments.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.NOT_EQUALS, true, EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParameters")
    void testInvalidParameters(FieldType fieldType, OperatorType operatorType, Boolean referenceValue) {
        assertThatThrownBy(() -> BooleanExpertRule.of(fieldType, operatorType, referenceValue))
                .isInstanceOf(PowsyblException.class);
    }

    @Test
    void testValidParameters() {
        assertThatNoException().isThrownBy(() -> BooleanExpertRule.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.EQUALS, true));
        assertThatNoException().isThrownBy(() -> BooleanExpertRule.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.EXISTS, null));

    }

    @Test
    void testGetDataTypeReturnsBoolean() {
        BooleanExpertRule rule = BooleanExpertRule.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.EQUALS, true);

        assertThat(rule.getDataType()).isEqualTo(DataType.BOOLEAN);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        BooleanExpertRule rule = BooleanExpertRule.of(FieldType.HAS_RATIO_TAP_CHANGER, OperatorType.EQUALS, true);

        assertThat(rule.getOperatorType()).isEqualTo(OperatorType.EQUALS);
    }

    @ParameterizedTest
    @MethodSource("provideRuleEvaluationArguments")
    void testRuleEvaluationReturnsExpected(FieldType fieldType, OperatorType operatorType, Boolean referenceValue, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        ExpertRule rule = BooleanExpertRule.of(fieldType, operatorType, referenceValue);
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
