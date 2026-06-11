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
import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.gridsuite.filter.wip.data.DataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class CombinatorExpertRuleTest {

    private static Stream<Arguments> provideLegacyTestArgumentsForTest() {
        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        Mockito.when(gen.getId()).thenReturn("GEN");
        Mockito.when(gen.getMinP()).thenReturn(-500.0);
        Mockito.when(gen.isVoltageRegulatorOn()).thenReturn(true);

        return Stream.of(
                // --- Single rule AND --- //
                Arguments.of(CombinatorType.AND, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build()
                ), gen, true),
                Arguments.of(CombinatorType.AND, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build()
                ), gen, false),
                // --- Rule tree AND --- //
                Arguments.of(CombinatorType.AND, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build(),
                        CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(
                                        StringExpertRule.builder().operator(OperatorType.IS).field(FieldType.ID).value("GEN").build(),
                                        NumberExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.MIN_P).value(-500.0).build()
                                )
                        ).build()
                ), gen, true),
                Arguments.of(CombinatorType.AND, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build(),
                        CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(
                                        StringExpertRule.builder().operator(OperatorType.IS).field(FieldType.ID).value("GEN").build(),
                                        NumberExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.MIN_P).value(-400.0).build()
                                )
                        ).build()
                ), gen, false),
                // --- Single rule OR --- //
                Arguments.of(CombinatorType.OR, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build()
                ), gen, true),
                Arguments.of(CombinatorType.OR, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build()
                ), gen, false),
                // --- Rule tree OR --- //
                Arguments.of(CombinatorType.OR, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build(),
                        CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(
                                        StringExpertRule.builder().operator(OperatorType.IS).field(FieldType.ID).value("GEN").build(),
                                        BooleanExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.VOLTAGE_REGULATOR_ON).value(false).build()
                                )
                        ).build()
                ), gen, true),
                Arguments.of(CombinatorType.OR, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build(),
                        CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(
                                        StringExpertRule.builder().operator(OperatorType.IS).field(FieldType.ID).value("GEN_2").build(),
                                        BooleanExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.VOLTAGE_REGULATOR_ON).value(false).build()
                                )
                        ).build()
                ), gen, false)
        );
    }

    private static Stream<Arguments> provideMockRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(CombinatorType.OR, List.of(mockExpertRule(true), mockExpertRule(false)), true),
                Arguments.of(CombinatorType.OR, List.of(mockExpertRule(false), mockExpertRule(false)), false),
                Arguments.of(CombinatorType.AND, List.of(mockExpertRule(true), mockExpertRule(true)), true),
                Arguments.of(CombinatorType.AND, List.of(mockExpertRule(true), mockExpertRule(false)), false),
                Arguments.of(CombinatorType.AND, List.of(mockExpertRule(false), mockExpertRule(false)), false)
        );
    }

    private static Stream<Arguments> provideRealNetworkIdentifiableRuleEvaluationArguments() {
        return Stream.of(
                Arguments.of(CombinatorType.OR,
                        List.of(BooleanExpertRule.builder().field(FieldType.CONNECTED).operator(OperatorType.EQUALS).value(true).build(),
                                EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.EQUALS).value("FR").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.OR,
                        List.of(BooleanExpertRule.builder().field(FieldType.CONNECTED).operator(OperatorType.EQUALS).value(false).build(),
                                EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.EQUALS).value("FR").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.OR,
                        List.of(BooleanExpertRule.builder().field(FieldType.CONNECTED).operator(OperatorType.EQUALS).value(false).build(),
                                EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.EQUALS).value("ES").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        false
                ),
                Arguments.of(CombinatorType.AND,
                        List.of(BooleanExpertRule.builder().field(FieldType.CONNECTED).operator(OperatorType.EQUALS).value(true).build(),
                                EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.EQUALS).value("FR").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        true
                ),
                Arguments.of(CombinatorType.AND,
                        List.of(BooleanExpertRule.builder().field(FieldType.CONNECTED).operator(OperatorType.EQUALS).value(false).build(),
                                EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.EQUALS).value("FR").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        false
                ),
                Arguments.of(CombinatorType.AND,
                        List.of(BooleanExpertRule.builder().field(FieldType.CONNECTED).operator(OperatorType.EQUALS).value(true).build(),
                                EnumExpertRule.builder().field(FieldType.COUNTRY).operator(OperatorType.EQUALS).value("ES").build()),
                        EquipmentType.GENERATOR,
                        "GENERATOR_1",
                        false
                )
        );
    }

    private static ExpertRule mockExpertRule(boolean ruleEvaluationOutput) {
        ExpertRule rule = mock(ExpertRule.class);
        when(rule.evaluateRule(any())).thenReturn(ruleEvaluationOutput);
        return rule;
    }

    @Test
    void testGetDataTypeReturnsCombinator() {
        CombinatorExpertRule rule = CombinatorExpertRule.builder()
                .combinator(CombinatorType.OR)
                .rules(Collections.emptyList())
                .build();

        assertThat(rule.getDataType()).isEqualTo(DataType.COMBINATOR);
    }

    @Test
    void testGetOperatorTypeThrowsUnsupportedOperationException() {
        CombinatorExpertRule rule = CombinatorExpertRule.builder()
                .combinator(CombinatorType.OR)
                .rules(Collections.emptyList())
                .build();

        assertThatThrownBy(rule::getOperator).isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void testClearCacheCombinatorRuleClearsCacheForSubRules() {
        ExpertRule cachingRuleOne = mock(ExpertRule.class);
        ExpertRule cachingRuleTwo = mock(ExpertRule.class);
        ExpertRule nestedCachingRule = mock(ExpertRule.class);
        CombinatorExpertRule nestedCombinatorRule = CombinatorExpertRule.builder()
                .combinator(CombinatorType.AND)
                .rules(List.of(nestedCachingRule))
                .build();
        CombinatorExpertRule rule = CombinatorExpertRule.builder()
                .combinator(CombinatorType.OR)
                .rules(List.of(cachingRuleOne, cachingRuleTwo, nestedCombinatorRule))
                .build();

        rule.clearCache();

        verify(cachingRuleOne).clearCache();
        verify(cachingRuleTwo).clearCache();
        verify(nestedCachingRule).clearCache();
    }

    @ParameterizedTest
    @MethodSource("provideLegacyTestArgumentsForTest")
    void testFilterRoundTripSerializationDeserialization(CombinatorType combinatorType, List<ExpertRule> rules, Identifiable<?> equipment, boolean expected) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        ExpertRule rule = CombinatorExpertRule.builder()
                .combinator(combinatorType)
                .rules(rules)
                .build();

        String serializedRule = objectMapper.writeValueAsString(rule);
        ExpertRule deserializedRule = objectMapper.readValue(serializedRule, ExpertRule.class);

        assertThat(deserializedRule).isEqualTo(rule);
    }

    @ParameterizedTest
    @MethodSource("provideLegacyTestArgumentsForTest")
    void testEvaluateRule(CombinatorType combinatorType, List<ExpertRule> rules, Identifiable<?> equipment, boolean expected) {
        ExpertRule rule = CombinatorExpertRule.builder()
                .combinator(combinatorType)
                .rules(rules)
                .build();

        assertEquals(expected, rule.evaluateRule(equipment));
    }

    @ParameterizedTest
    @MethodSource("provideMockRuleEvaluationArguments")
    void testMockRuleEvaluationReturnsExpected(CombinatorType combinatorType, List<ExpertRule> rules, boolean expectedResult) {
        Identifiable<?> identifiable = mock(Identifiable.class);
        ExpertRule rule = CombinatorExpertRule.builder()
                .combinator(combinatorType)
                .rules(rules)
                .build();

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }

    @ParameterizedTest
    @MethodSource("provideRealNetworkIdentifiableRuleEvaluationArguments")
    void testRealRuleEvaluationReturnsExpected(CombinatorType combinatorType, List<ExpertRule> rules, EquipmentType equipmentType, String equipmentId, boolean expectedResult) {
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);
        ExpertRule rule = CombinatorExpertRule.builder()
                .combinator(combinatorType)
                .rules(rules)
                .build();

        assertThat(rule.evaluateRule(identifiable)).isEqualTo(expectedResult);
    }
}
