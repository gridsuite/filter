package org.gridsuite.filter.utils.expertfilter;

import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.api.OptionalAssert;
import org.assertj.core.api.WithAssertions;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.CombinatorExpertRule;
import org.gridsuite.filter.expertfilter.expertrule.FilterUuidExpertRule;
import org.gridsuite.filter.utils.EquipmentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.Mockito.mock;

class ExpertFilterUtilsTest implements WithAssertions {
    @ParameterizedTest
    @MethodSource("orCombinationData")
    void shouldCreateOrCombination(final List<AbstractExpertRule> rules, final boolean expectEmpty, final boolean expectSingle) {
        final OptionalAssert<AbstractExpertRule> assertOpt = assertThat(ExpertFilterUtils.buildOrCombination(rules)).as("result");
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
            Arguments.of(List.of(mock(AbstractExpertRule.class)), false, true),
            Arguments.of(List.of(mock(AbstractExpertRule.class), mock(AbstractExpertRule.class)), false, false)
        );
    }

    @Test
    void shouldCreateExpertFilterWithVoltageLevelIdsCriteria() {
        final UUID filterUuid = UUID.randomUUID();
        final EquipmentType equipmentType = EquipmentType.LINE;
        assertThat(ExpertFilterUtils.buildExpertFilterWithVoltageLevelIdsCriteria(filterUuid, equipmentType)).as("result").isNotNull().satisfies(
            ef -> assertThat(ef.getEquipmentType()).as("equipmentType").isEqualTo(equipmentType),
            ef -> assertThat(ef.getRules()).as("expert rules").isNotNull()
                .asInstanceOf(InstanceOfAssertFactories.type(CombinatorExpertRule.class))
                .satisfies(
                    cer -> assertThat(cer.getCombinator()).as("combinator").isEqualTo(CombinatorType.OR),
                    cer -> assertThat(cer.getRules()).as("combinator rules").hasSize(2)
                        .allSatisfy(er -> assertThat(er).as("expert rule")
                            .asInstanceOf(InstanceOfAssertFactories.type(FilterUuidExpertRule.class))
                            .satisfies(fuer -> assertThat(fuer.getOperator()).as("operator").isEqualTo(OperatorType.IS_PART_OF))
                            .extracting(FilterUuidExpertRule::getValues, InstanceOfAssertFactories.set(String.class)).as("UUIDs")
                            .containsExactly(filterUuid.toString()))
                        .map(AbstractExpertRule::getField).as("fields")
                        .containsExactly(FieldType.VOLTAGE_LEVEL_ID_1, FieldType.VOLTAGE_LEVEL_ID_2)
                )
        );
    }
}
