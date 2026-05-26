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
class StringExpertRuleTest {

    private static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(FieldType.NAME, null, null, null),
                Arguments.of(FieldType.NAME, OperatorType.IS, null, null),
                Arguments.of(FieldType.SUBSTATION_PROPERTIES, OperatorType.IS, null, null),
                Arguments.of(FieldType.NAME, OperatorType.IS_NOT_PART_OF, null, null),
                Arguments.of(FieldType.NAME, OperatorType.EXISTS, "lol", null),
                Arguments.of(FieldType.NAME, OperatorType.NOT_EXISTS, null, Set.of("lol")),
                Arguments.of(FieldType.NAME, OperatorType.IS, null, Set.of("funny name", "not so funny name")),
                Arguments.of(FieldType.NAME, OperatorType.IS, "", null),
                Arguments.of(FieldType.NAME, OperatorType.IN, "another funny name", null),
                Arguments.of(FieldType.NAME, OperatorType.NOT_IN, null, Collections.emptySet())
        );
    }

    private static Stream<Arguments> provideRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(FieldType.NAME, OperatorType.IS, "Best voltage level ;)", null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true),
                Arguments.of(FieldType.NAME, OperatorType.IS, "Worst voltage level", null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", false),
                Arguments.of(FieldType.NAME, OperatorType.CONTAINS, "voltage level ;)", null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true),
                Arguments.of(FieldType.NAME, OperatorType.CONTAINS, "voltage ;)", null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", false),
                Arguments.of(FieldType.NAME, OperatorType.BEGINS_WITH, "Best", null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true),
                Arguments.of(FieldType.NAME, OperatorType.BEGINS_WITH, ";)", null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", false),
                Arguments.of(FieldType.NAME, OperatorType.ENDS_WITH, "level ;)", null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true),
                Arguments.of(FieldType.NAME, OperatorType.ENDS_WITH, "voltage ;)", null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", false),
                Arguments.of(FieldType.NAME, OperatorType.EXISTS, null, null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true),
                Arguments.of(FieldType.NAME, OperatorType.EXISTS, null, null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_2", false),
                Arguments.of(FieldType.NAME, OperatorType.EXISTS, null, null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_3", false),
                Arguments.of(FieldType.NAME, OperatorType.NOT_EXISTS, null, null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", false),
                Arguments.of(FieldType.NAME, OperatorType.NOT_EXISTS, null, null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_2", true),
                Arguments.of(FieldType.NAME, OperatorType.NOT_EXISTS, null, null, EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_3", true),
                Arguments.of(FieldType.NAME, OperatorType.IN, null, Set.of("Best voltage level ;)", "Worst voltage level"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true),
                Arguments.of(FieldType.NAME, OperatorType.IN, null, Set.of("Best voltage level ;)", "Worst voltage level"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_2", false),
                Arguments.of(FieldType.NAME, OperatorType.NOT_IN, null, Set.of("Best voltage level ;)", "Worst voltage level"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", false),
                Arguments.of(FieldType.NAME, OperatorType.NOT_IN, null, Set.of("Worst voltage level"), EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1", true)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParameters")
    void testInvalidParameters(FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues) {
        assertThatThrownBy(() -> StringExpertRule.of(fieldType, operatorType, referenceValue, referenceValues))
                .isInstanceOf(PowsyblException.class);
    }

    @Test
    void testValidParameters() {
        assertThatNoException().isThrownBy(() -> StringExpertRule.of(FieldType.NAME, OperatorType.IN, null, Set.of("funny name", "not so funny name")));
        assertThatNoException().isThrownBy(() -> StringExpertRule.of(FieldType.NAME, OperatorType.IS, "funny name", null));
    }

    @Test
    void testGetDataTypeReturnsString() {
        StringExpertRule rule = StringExpertRule.of(FieldType.NAME, OperatorType.IN, null, Set.of("funny name", "not so funny name"));

        assertThat(rule.getDataType()).isEqualTo(DataType.STRING);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        StringExpertRule rule = StringExpertRule.of(FieldType.NAME, OperatorType.IN, null, Set.of("funny name", "not so funny name"));

        assertThat(rule.getOperatorType()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource("provideRuleEvaluationArguments")
    void testRuleEvaluationReturnsExpected(FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        ExpertRule rule = StringExpertRule.of(fieldType, operatorType, referenceValue, referenceValues);
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
