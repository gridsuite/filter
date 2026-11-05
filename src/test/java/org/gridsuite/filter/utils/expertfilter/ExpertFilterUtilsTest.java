/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.utils.expertfilter;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.OptionalAssert;
import org.assertj.core.api.WithAssertions;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.rule.CombinatorExpertRule;
import org.gridsuite.filter.wip.rule.ExpertRule;
import org.gridsuite.filter.wip.rule.FilterExpertRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class ExpertFilterUtilsTest implements WithAssertions {
    @ParameterizedTest
    @MethodSource("orCombinationData")
    void shouldCreateOrCombination(final List<ExpertRule> rules, final boolean expectEmpty, final boolean expectSingle) {
        final OptionalAssert<ExpertRule> assertOpt = assertThat(ExpertFilterUtils.buildOrCombination(rules)).as("result");
        if (expectEmpty) {
            assertOpt.isEmpty();
        } else {
            assertOpt.isPresent();
            final var assertRule = assertOpt.get().as("rule");
            if (expectSingle) {
                assertRule.isEqualTo(rules.getFirst());
            } else {
                assertRule.asInstanceOf(InstanceOfAssertFactories.type(CombinatorExpertRule.class)).satisfies(
                    cer -> assertThat(cer.getCombinator()).as("combinator").isEqualTo(CombinatorType.OR),
                    cer -> assertThat(cer.getRules()).as("rules").containsExactlyInAnyOrderElementsOf(rules)
                );
            }
        }
    }

    private static Stream<Arguments> orCombinationData() {
        return Stream.of(
            Arguments.of(List.of(), true, false),
            Arguments.of(List.of(mock(ExpertRule.class)), false, true),
            Arguments.of(List.of(mock(ExpertRule.class), mock(ExpertRule.class)), false, false)
        );
    }

    @Test
    void shouldCreateExpertFilterWithVoltageLevelIdsCriteria() {
        final Filter filter = mock(Filter.class);
        final EquipmentType equipmentType = EquipmentType.LINE;
        assertThat(ExpertFilterUtils.buildExpertFilterWithVoltageLevelIdsCriteria(filter, equipmentType)).as("result").isNotNull().satisfies(
            ef -> assertThat(ef.getEquipmentType()).as("equipmentType").isEqualTo(equipmentType),
            ef -> assertThat(ef.getRule()).as("expert rules").isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.type(CombinatorExpertRule.class))
                .satisfies(
                    cer -> assertThat(cer.getCombinator()).as("combinator").isEqualTo(CombinatorType.OR),
                    cer -> assertThat(cer.getRules()).as("combinator rules").hasSize(2)
                        .allSatisfy(er -> assertThat(er).as("expert rule")
                                .asInstanceOf(InstanceOfAssertFactories.type(FilterExpertRule.class))
                                .satisfies(fuer -> assertThat(fuer.getOperator()).as("operator").isEqualTo(OperatorType.IS_PART_OF))
                                .extracting(FilterExpertRule::getFilters, InstanceOfAssertFactories.list(Filter.class)).as("filters")
                                .containsExactly(filter))
                        .map(FilterExpertRule.class::cast)
                        .extracting(FilterExpertRule::getField).as("fields")
                        .containsExactly(FieldType.VOLTAGE_LEVEL_ID_1, FieldType.VOLTAGE_LEVEL_ID_2)
                )
        );
    }
}
