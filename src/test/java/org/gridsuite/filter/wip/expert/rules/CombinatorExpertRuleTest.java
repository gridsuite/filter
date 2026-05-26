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
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class CombinatorExpertRuleTest {

    private static Stream<Arguments> provideInvalidParameters() {
        return Stream.of(Arguments.of(null, null), Arguments.of(CombinatorType.OR, null), Arguments.of(CombinatorType.OR, Collections.emptySet()));
    }

    private static Stream<Arguments> provideMockRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(CombinatorType.OR, Set.of(mockExpertRule(true), mockExpertRule(false)), true),
                Arguments.of(CombinatorType.OR, Set.of(mockExpertRule(false), mockExpertRule(false)), false),
                Arguments.of(CombinatorType.AND, Set.of(mockExpertRule(true), mockExpertRule(true)), true),
                Arguments.of(CombinatorType.AND, Set.of(mockExpertRule(true), mockExpertRule(false)), false),
                Arguments.of(CombinatorType.AND, Set.of(mockExpertRule(false), mockExpertRule(false)), false)
        );
    }

    private static Stream<Arguments> provideRealRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(CombinatorType.OR,
                        Set.of(BooleanExpertRule.of(FieldType.CONNECTED, OperatorType.EQUALS, true),
                                EnumExpertRule.of(FieldType.COUNTRY, OperatorType.EQUALS, "FR", null)),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.OR,
                        Set.of(BooleanExpertRule.of(FieldType.CONNECTED, OperatorType.EQUALS, false),
                                EnumExpertRule.of(FieldType.COUNTRY, OperatorType.EQUALS, "FR", null)),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.OR,
                        Set.of(BooleanExpertRule.of(FieldType.CONNECTED, OperatorType.EQUALS, true),
                                EnumExpertRule.of(FieldType.COUNTRY, OperatorType.EQUALS, "PL", null)),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.AND,
                        Set.of(BooleanExpertRule.of(FieldType.CONNECTED, OperatorType.EQUALS, true),
                                EnumExpertRule.of(FieldType.COUNTRY, OperatorType.EQUALS, "FR", null)),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.AND,
                        Set.of(BooleanExpertRule.of(FieldType.CONNECTED, OperatorType.EQUALS, true),
                                EnumExpertRule.of(FieldType.COUNTRY, OperatorType.EQUALS, "PL", null)),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        false
                )
        );
    }

    private static AbstractExpertRule mockExpertRule(boolean ruleEvaluationOutput) {
        AbstractExpertRule rule = mock(AbstractExpertRule.class);
        when(rule.evaluateRule(any())).thenReturn(ruleEvaluationOutput);
        return rule;
    }

    @ParameterizedTest
    @MethodSource("provideInvalidParameters")
    void testInvalidParameters(CombinatorType combinatorType, Set<ExpertRule> subRules) {
        assertThatThrownBy(() -> CombinatorExpertRule.of(combinatorType, subRules)).isInstanceOf(PowsyblException.class);
    }

    @Test
    void testValidParameters() {
        assertThatNoException().isThrownBy(() -> CombinatorExpertRule.of(CombinatorType.OR, Collections.singleton(mock(AbstractExpertRule.class))));
    }

    @Test
    void testGetDataTypeReturnsCombinator() {
        CombinatorExpertRule rule = CombinatorExpertRule.of(CombinatorType.OR, Collections.singleton(mock(AbstractExpertRule.class)));

        assertThat(rule.getDataType()).isEqualTo(DataType.COMBINATOR);
    }

    @Test
    void testGetOperatorTypeThrowsUnsupportedOperationException() {
        CombinatorExpertRule rule = CombinatorExpertRule.of(CombinatorType.OR, Collections.singleton(mock(AbstractExpertRule.class)));

        assertThatThrownBy(rule::getOperatorType).isInstanceOf(UnsupportedOperationException.class);
    }

    @ParameterizedTest
    @MethodSource("provideMockRuleEvaluationArguments")
    void testMockRuleEvaluationReturnsExpected(CombinatorType combinatorType, Set<ExpertRule> subRules, boolean expectedResult) {
        ExpertRule rule = CombinatorExpertRule.of(combinatorType, subRules);
        Identifiable<?> identifiable = mock(Identifiable.class);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("provideRealRuleEvaluationArguments")
    void testRealRuleEvaluationReturnsExpected(CombinatorType combinatorType, Set<ExpertRule> subRules, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        ExpertRule rule = CombinatorExpertRule.of(combinatorType, subRules);
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
