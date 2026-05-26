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
class EnumExpertRuleTest {

    private static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(
                Arguments.of(null, null, null, null),
                Arguments.of(FieldType.COUNTRY, null, null, null),
                Arguments.of(FieldType.COUNTRY, OperatorType.EXISTS, null, null),
                Arguments.of(FieldType.PAIRED, OperatorType.EQUALS, null, null),
                Arguments.of(FieldType.COUNTRY, OperatorType.IN, null, null),
                Arguments.of(FieldType.COUNTRY, OperatorType.IN, "FR", null),
                Arguments.of(FieldType.COUNTRY, OperatorType.IN, null, Collections.emptySet()),
                Arguments.of(FieldType.COUNTRY, OperatorType.EQUALS, null, null),
                Arguments.of(FieldType.COUNTRY, OperatorType.EQUALS, null, Set.of("FR")),
                Arguments.of(FieldType.COUNTRY, OperatorType.EQUALS, "", null)
        );
    }

    private static Stream<Arguments> provideRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(FieldType.ENERGY_SOURCE, OperatorType.EQUALS, "NUCLEAR", Collections.emptySet(), EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.ENERGY_SOURCE, OperatorType.NOT_EQUALS, "NUCLEAR", Collections.emptySet(), EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.ENERGY_SOURCE, OperatorType.NOT_EQUALS, "THERMAL", Collections.emptySet(), EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.ENERGY_SOURCE, OperatorType.IN, null, Set.of("HYDRO", "THERMAL"), EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.ENERGY_SOURCE, OperatorType.NOT_IN, null, Set.of("HYDRO", "THERMAL"), EquipmentType.GENERATOR, "GENERATOR_1", true),
                Arguments.of(FieldType.ENERGY_SOURCE, OperatorType.NOT_IN, null, Set.of("NUCLEAR"), EquipmentType.GENERATOR, "GENERATOR_1", false),
                Arguments.of(FieldType.PHASE_REGULATION_MODE_1, OperatorType.EQUALS, "CURRENT_LIMITER", Collections.emptySet(), EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", false)
        );
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParameters")
    void testInvalidParameters(FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues) {
        assertThatThrownBy(() -> EnumExpertRule.of(fieldType, operatorType, referenceValue, referenceValues))
                .isInstanceOf(PowsyblException.class);
    }

    @Test
    void testValidParameters() {
        assertThatNoException().isThrownBy(() -> EnumExpertRule.of(FieldType.COUNTRY, OperatorType.IN, null, Set.of("FR", "BE")));
        assertThatNoException().isThrownBy(() -> EnumExpertRule.of(FieldType.COUNTRY, OperatorType.EQUALS, "FR", null));
    }

    @Test
    void testGetDataTypeReturnsEnum() {
        EnumExpertRule rule = EnumExpertRule.of(FieldType.COUNTRY, OperatorType.IN, null, Set.of("FR", "BE"));

        assertThat(rule.getDataType()).isEqualTo(DataType.ENUM);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        EnumExpertRule rule = EnumExpertRule.of(FieldType.COUNTRY, OperatorType.IN, null, Set.of("FR", "BE"));

        assertThat(rule.getOperatorType()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource("provideRuleEvaluationArguments")
    void testRuleEvaluationReturnsExpected(FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        ExpertRule rule = EnumExpertRule.of(fieldType, operatorType, referenceValue, referenceValues);
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
