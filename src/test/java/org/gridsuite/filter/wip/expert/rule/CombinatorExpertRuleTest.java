/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rule;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.gridsuite.filter.wip.expert.data.DataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CombinatorExpertRuleTest {

    private static Stream<Arguments> provideArgumentsForTest() {
        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        Mockito.when(gen.getId()).thenReturn("GEN");
        Mockito.when(gen.getMinP()).thenReturn(-500.0);
        Mockito.when(gen.isVoltageRegulatorOn()).thenReturn(true);

        return Stream.of(
                // --- Single rule AND --- //
                Arguments.of(CombinatorType.AND, Set.of(
                        EnumExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.ENERGY_SOURCE).referenceValue(EnergySource.HYDRO.name()).build()
                ), gen, true),
                Arguments.of(CombinatorType.AND, Set.of(
                        EnumExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.ENERGY_SOURCE).referenceValue(EnergySource.THERMAL.name()).build()
                ), gen, false),
                // --- Rule tree AND --- //
                Arguments.of(CombinatorType.AND, Set.of(
                        EnumExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.ENERGY_SOURCE).referenceValue(EnergySource.HYDRO.name()).build(),
                        CombinatorExpertRule.builder().combinatorType(CombinatorType.AND).subRules(Set.of(
                                        StringExpertRule.builder().operatorType(OperatorType.IS).fieldType(FieldType.ID).referenceValue("GEN").build(),
                                        NumberExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.MIN_P).referenceValue(-500.0).build()
                                )
                        ).build()
                ), gen, true),
                Arguments.of(CombinatorType.AND, Set.of(
                        EnumExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.ENERGY_SOURCE).referenceValue(EnergySource.HYDRO.name()).build(),
                        CombinatorExpertRule.builder().combinatorType(CombinatorType.AND).subRules(Set.of(
                                        StringExpertRule.builder().operatorType(OperatorType.IS).fieldType(FieldType.ID).referenceValue("GEN").build(),
                                        NumberExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.MIN_P).referenceValue(-400.0).build()
                                )
                        ).build()
                ), gen, false),
                // --- Single rule OR --- //
                Arguments.of(CombinatorType.OR, Set.of(
                       EnumExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.ENERGY_SOURCE).referenceValue(EnergySource.HYDRO.name()).build()
                ), gen, true),
                Arguments.of(CombinatorType.OR, Set.of(
                        EnumExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.ENERGY_SOURCE).referenceValue(EnergySource.THERMAL.name()).build()
                ), gen, false),
                // --- Rule tree OR --- //
                Arguments.of(CombinatorType.OR, Set.of(
                        EnumExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.ENERGY_SOURCE).referenceValue(EnergySource.THERMAL.name()).build(),
                        CombinatorExpertRule.builder().combinatorType(CombinatorType.OR).subRules(Set.of(
                                        StringExpertRule.builder().operatorType(OperatorType.IS).fieldType(FieldType.ID).referenceValue("GEN").build(),
                                        BooleanExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.VOLTAGE_REGULATOR_ON).referenceValue(false).build()
                                )
                        ).build()
                ), gen, true),
                Arguments.of(CombinatorType.OR, Set.of(
                        EnumExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.ENERGY_SOURCE).referenceValue(EnergySource.THERMAL.name()).build(),
                        CombinatorExpertRule.builder().combinatorType(CombinatorType.OR).subRules(Set.of(
                                        StringExpertRule.builder().operatorType(OperatorType.IS).fieldType(FieldType.ID).referenceValue("GEN_2").build(),
                                        BooleanExpertRule.builder().operatorType(OperatorType.EQUALS).fieldType(FieldType.VOLTAGE_REGULATOR_ON).referenceValue(false).build()
                                )
                        ).build()
                ), gen, false)
        );
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
                        Set.of(BooleanExpertRule.builder().fieldType(FieldType.CONNECTED).operatorType(OperatorType.EQUALS).referenceValue(true).build(),
                                EnumExpertRule.builder().fieldType(FieldType.COUNTRY).operatorType(OperatorType.EQUALS).referenceValue("FR").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.OR,
                        Set.of(BooleanExpertRule.builder().fieldType(FieldType.CONNECTED).operatorType(OperatorType.EQUALS).referenceValue(false).build(),
                                EnumExpertRule.builder().fieldType(FieldType.COUNTRY).operatorType(OperatorType.EQUALS).referenceValue("FR").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.OR,
                        Set.of(BooleanExpertRule.builder().fieldType(FieldType.CONNECTED).operatorType(OperatorType.EQUALS).referenceValue(false).build(),
                                EnumExpertRule.builder().fieldType(FieldType.COUNTRY).operatorType(OperatorType.EQUALS).referenceValue("ES").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        false
                ),
                Arguments.of(CombinatorType.AND,
                        Set.of(BooleanExpertRule.builder().fieldType(FieldType.CONNECTED).operatorType(OperatorType.EQUALS).referenceValue(true).build(),
                                EnumExpertRule.builder().fieldType(FieldType.COUNTRY).operatorType(OperatorType.EQUALS).referenceValue("FR").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.AND,
                        Set.of(BooleanExpertRule.builder().fieldType(FieldType.CONNECTED).operatorType(OperatorType.EQUALS).referenceValue(false).build(),
                                EnumExpertRule.builder().fieldType(FieldType.COUNTRY).operatorType(OperatorType.EQUALS).referenceValue("FR").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        false
                ),
                Arguments.of(CombinatorType.AND,
                        Set.of(BooleanExpertRule.builder().fieldType(FieldType.CONNECTED).operatorType(OperatorType.EQUALS).referenceValue(true).build(),
                                EnumExpertRule.builder().fieldType(FieldType.COUNTRY).operatorType(OperatorType.EQUALS).referenceValue("ES").build()),
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

    @Test
    void testGetDataTypeReturnsCombinator() {
        CombinatorExpertRule rule = CombinatorExpertRule.builder().combinatorType(CombinatorType.OR).build();

        assertThat(rule.getDataType()).isEqualTo(DataType.COMBINATOR);
    }

    @Test
    void testGetOperatorTypeThrowsUnsupportedOperationException() {
        CombinatorExpertRule rule = CombinatorExpertRule.builder().combinatorType(CombinatorType.OR).build();

        assertThatThrownBy(rule::getOperatorType).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testClearCacheCombinatorRuleClearsCacheForExpectedSubRules() {
        AbstractExpertRule nonCachingRule = mock(AbstractExpertRule.class);
        AbstractCachingExpertRule cachingRule = mock(AbstractCachingExpertRule.class);
        AbstractCachingExpertRule nestedCachingRule = mock(AbstractCachingExpertRule.class);
        CombinatorExpertRule nestedCombinatorRule = CombinatorExpertRule.builder().combinatorType(CombinatorType.AND).subRules(Set.of(nonCachingRule, nestedCachingRule)).build();
        CombinatorExpertRule rule = CombinatorExpertRule.builder().combinatorType(CombinatorType.OR).subRules(Set.of(nonCachingRule, cachingRule, nestedCombinatorRule)).build();

        rule.clearCache();

        verifyNoInteractions(nonCachingRule);
        verify(cachingRule).clearCache();
        verify(nestedCachingRule).clearCache();
    }

    @ParameterizedTest
    @MethodSource({"provideArgumentsForTest"})
    void testEvaluateRule(CombinatorType combinatorType, Set<ExpertRule> rules, Identifiable<?> equipment, boolean expected) {
        CombinatorExpertRule rule = CombinatorExpertRule.builder().combinatorType(combinatorType).subRules(rules).build();

        assertEquals(expected, rule.evaluateRule(equipment));
    }

    @ParameterizedTest
    @MethodSource("provideMockRuleEvaluationArguments")
    void testMockRuleEvaluationReturnsExpected(CombinatorType combinatorType, Set<ExpertRule> subRules, boolean expectedResult) {
        ExpertRule rule = CombinatorExpertRule.builder().combinatorType(combinatorType).subRules(subRules).build();
        Identifiable<?> identifiable = mock(Identifiable.class);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("provideRealRuleEvaluationArguments")
    void testRealRuleEvaluationReturnsExpected(CombinatorType combinatorType, Set<ExpertRule> subRules, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        ExpertRule rule = CombinatorExpertRule.builder().combinatorType(combinatorType).subRules(subRules).build();
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
