/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import com.powsybl.iidm.network.Line;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.gridsuite.filter.wip.expert.rule.*;
import org.gridsuite.filter.wip.identifier.IdentifierListFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ExpertFilterTest {

    private final Network network = TestNetworkUtils.createTestNetwork();

    private static Stream<Arguments> provideFilterEvaluationArguments() {
        return Stream.of(
                //Boolean Expert Filter
                Arguments.of(buildBooleanExpertFilter(EquipmentType.TWO_WINDINGS_TRANSFORMER, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, OperatorType.EQUALS, true), Set.of("TWO_WINDINGS_TRANSFORMER_1", "TWO_WINDINGS_TRANSFORMER_2", "TWO_WINDINGS_TRANSFORMER_3")),
                Arguments.of(buildBooleanExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, OperatorType.EQUALS, true), Set.of("THREE_WINDINGS_TRANSFORMER_2")),
                Arguments.of(buildBooleanExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.HAS_RATIO_TAP_CHANGER_1, OperatorType.NOT_EQUALS, true), Set.of("THREE_WINDINGS_TRANSFORMER_1", "THREE_WINDINGS_TRANSFORMER_3")),
                Arguments.of(buildBooleanExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.HAS_RATIO_TAP_CHANGER_1, OperatorType.NOT_EQUALS, false), Set.of("THREE_WINDINGS_TRANSFORMER_2")),
                Arguments.of(buildBooleanExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, OperatorType.EXISTS, null), Set.of("THREE_WINDINGS_TRANSFORMER_2")),
                Arguments.of(buildBooleanExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_2, OperatorType.EXISTS, null), Set.of("THREE_WINDINGS_TRANSFORMER_1", "THREE_WINDINGS_TRANSFORMER_2", "THREE_WINDINGS_TRANSFORMER_3")),
                Arguments.of(buildBooleanExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, OperatorType.NOT_EXISTS, null), Set.of("THREE_WINDINGS_TRANSFORMER_1", "THREE_WINDINGS_TRANSFORMER_3")),
                Arguments.of(buildBooleanExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_2, OperatorType.NOT_EXISTS, null), Collections.emptySet()),
                //Enum Expert Filter
                Arguments.of(buildEnumExpertFilter(EquipmentType.LINE, FieldType.COUNTRY_1, OperatorType.EQUALS, "FR", null), Set.of("LINE_1", "LINE_2")),
                Arguments.of(buildEnumExpertFilter(EquipmentType.LINE, FieldType.COUNTRY_1, OperatorType.EQUALS, "DE", null), Set.of("LINE_3")),
                Arguments.of(buildEnumExpertFilter(EquipmentType.LINE, FieldType.COUNTRY_1, OperatorType.NOT_EQUALS, "ES", null), Set.of("LINE_1", "LINE_2", "LINE_3")),
                Arguments.of(buildEnumExpertFilter(EquipmentType.LINE, FieldType.COUNTRY_1, OperatorType.NOT_EQUALS, "FR", null), Set.of("LINE_3")),
                Arguments.of(buildEnumExpertFilter(EquipmentType.LINE, FieldType.COUNTRY_1, OperatorType.IN, null, Set.of("DE")), Set.of("LINE_3")),
                Arguments.of(buildEnumExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.RATIO_REGULATION_MODE_1, OperatorType.IN, null, Set.of("FIXED_RATIO")), Set.of("THREE_WINDINGS_TRANSFORMER_2")),
                Arguments.of(buildEnumExpertFilter(EquipmentType.LINE, FieldType.COUNTRY_2, OperatorType.NOT_IN, null, Set.of("FR")), Set.of("LINE_1", "LINE_2", "LINE_3")),
                Arguments.of(buildEnumExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.RATIO_REGULATION_MODE_1, OperatorType.NOT_IN, null, Set.of("FIXED_RATIO")), Collections.emptySet()),
                //Number Expert Filter
                Arguments.of(buildNumberExpertFilter(EquipmentType.BATTERY, FieldType.TARGET_P, OperatorType.EQUALS, 10D, null), Set.of("BATTERY_1")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.BATTERY, FieldType.TARGET_Q, OperatorType.EQUALS, 5D, null), Set.of("BATTERY_1", "BATTERY_2", "BATTERY_3")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.BATTERY, FieldType.TARGET_P, OperatorType.GREATER_OR_EQUALS, 11D, null), Set.of("BATTERY_2", "BATTERY_3")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.BATTERY, FieldType.TARGET_P, OperatorType.GREATER_OR_EQUALS, 21D, null), Set.of("BATTERY_3")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.BATTERY, FieldType.TARGET_P, OperatorType.GREATER, 10D, null), Set.of("BATTERY_2", "BATTERY_3")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.BATTERY, FieldType.TARGET_Q, OperatorType.GREATER, 5D, null), Collections.emptySet()),
                Arguments.of(buildNumberExpertFilter(EquipmentType.GENERATOR, FieldType.TARGET_P, OperatorType.LOWER_OR_EQUALS, 150D, null), Set.of("GENERATOR_1")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.GENERATOR, FieldType.TARGET_P, OperatorType.LOWER_OR_EQUALS, 200D, null), Set.of("GENERATOR_1", "GENERATOR_2")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.GENERATOR, FieldType.TARGET_P, OperatorType.LOWER, 150D, null), Set.of("GENERATOR_1")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.GENERATOR, FieldType.TARGET_P, OperatorType.LOWER, 200D, null), Set.of("GENERATOR_1")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.GENERATOR, FieldType.TARGET_P, OperatorType.BETWEEN, null, Set.of(100D, 200D)), Set.of("GENERATOR_1", "GENERATOR_2")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.GENERATOR, FieldType.TARGET_P, OperatorType.BETWEEN, null, Set.of(200D, 100D)), Set.of("GENERATOR_1", "GENERATOR_2")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.GENERATOR, FieldType.TARGET_Q, OperatorType.EXISTS, null, null), Set.of("GENERATOR_3")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.GENERATOR, FieldType.TARGET_P, OperatorType.EXISTS, null, null), Set.of("GENERATOR_1", "GENERATOR_2", "GENERATOR_3")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.RATIO_TARGET_V1, OperatorType.NOT_EXISTS, null, null), Set.of("THREE_WINDINGS_TRANSFORMER_1", "THREE_WINDINGS_TRANSFORMER_3")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.SERIE_RESISTANCE_2, OperatorType.NOT_EXISTS, null, null), Collections.emptySet()),
                Arguments.of(buildNumberExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.RATED_VOLTAGE_0, OperatorType.IN, null, Set.of(220D, 63D)), Collections.emptySet()),
                Arguments.of(buildNumberExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.RATED_VOLTAGE_0, OperatorType.IN, null, Set.of(400D)), Set.of("THREE_WINDINGS_TRANSFORMER_1", "THREE_WINDINGS_TRANSFORMER_2", "THREE_WINDINGS_TRANSFORMER_3")),
                Arguments.of(buildNumberExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.RATED_VOLTAGE_0, OperatorType.NOT_IN, null, Set.of(400D)), Collections.emptySet()),
                Arguments.of(buildNumberExpertFilter(EquipmentType.THREE_WINDINGS_TRANSFORMER, FieldType.RATED_VOLTAGE_0, OperatorType.NOT_IN, null, Set.of(220D, 63D)), Set.of("THREE_WINDINGS_TRANSFORMER_1", "THREE_WINDINGS_TRANSFORMER_2", "THREE_WINDINGS_TRANSFORMER_3")),
                //Properties Expert Filter
                Arguments.of(buildPropertiesExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Set.of("My custom value", "My other custom value")), Set.of("VOLTAGE_LEVEL_1")),
                Arguments.of(buildPropertiesExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.FREE_PROPERTIES, OperatorType.IN, "myCustomProperty", Set.of("blabla", "blablabla")), Collections.emptySet()),
                Arguments.of(buildPropertiesExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.SUBSTATION_PROPERTIES, OperatorType.NOT_IN, "myCustomProperty", Set.of("bla bla", "dfgjkhgfdsjkhfdsj")), Set.of("VL_S3")),
                Arguments.of(buildPropertiesExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.SUBSTATION_PROPERTIES, OperatorType.NOT_IN, "myCustomProperty", Set.of("My custom value", "My other custom value")), Collections.emptySet()),
                //String Expert Filter
                Arguments.of(buildStringExpertFilter(EquipmentType.LINE, FieldType.VOLTAGE_LEVEL_ID_1, OperatorType.IS, "VOLTAGE_LEVEL_1", null), Set.of("LINE_1", "LINE_2")),
                Arguments.of(buildStringExpertFilter(EquipmentType.LINE, FieldType.VOLTAGE_LEVEL_ID_1, OperatorType.IS, "VL_S2", null), Set.of("LINE_3")),
                Arguments.of(buildStringExpertFilter(EquipmentType.LINE, FieldType.VOLTAGE_LEVEL_ID_2, OperatorType.CONTAINS, "3", null), Set.of("LINE_2", "LINE_3")),
                Arguments.of(buildStringExpertFilter(EquipmentType.LINE, FieldType.VOLTAGE_LEVEL_ID_2, OperatorType.CONTAINS, "VL_S", null), Set.of("LINE_1", "LINE_2", "LINE_3")),
                Arguments.of(buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.BEGINS_WITH, "BEsT voLTaGe", null), Set.of("VOLTAGE_LEVEL_1")),
                Arguments.of(buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.BEGINS_WITH, "level", null), Collections.emptySet()),
                Arguments.of(buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.ENDS_WITH, ";)", null), Set.of("VOLTAGE_LEVEL_1")),
                Arguments.of(buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.ENDS_WITH, "best", null), Collections.emptySet()),
                Arguments.of(buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.EXISTS, null, null), Set.of("VOLTAGE_LEVEL_1", "VL_S3")),
                Arguments.of(buildStringExpertFilter(EquipmentType.LINE, FieldType.NAME, OperatorType.EXISTS, null, null), Collections.emptySet()),
                Arguments.of(buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.NOT_EXISTS, null, null), Set.of("VOLTAGE_LEVEL_2", "VOLTAGE_LEVEL_3", "VL_S2")),
                Arguments.of(buildStringExpertFilter(EquipmentType.LINE, FieldType.NAME, OperatorType.NOT_EXISTS, null, null), Set.of("LINE_1", "LINE_2", "LINE_3")),
                Arguments.of(buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.IN, null, Set.of("Best voltage level ;)", "other vl...")), Set.of("VOLTAGE_LEVEL_1", "VL_S3")),
                Arguments.of(buildStringExpertFilter(EquipmentType.LINE, FieldType.NAME, OperatorType.IN, null, Set.of("gdfsfds", "gfjkdhgfjdk")), Collections.emptySet()),
                Arguments.of(buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.NOT_IN, null, Set.of("Best voltage level ;)")), Set.of("VL_S3")),
                Arguments.of(buildStringExpertFilter(EquipmentType.LINE, FieldType.NAME, OperatorType.NOT_IN, null, Set.of("gdfsfds", "gfjkdhgfjdk")), Collections.emptySet()),
                //Combinator Expert Filter
                Arguments.of(buildCombinatorExpertFilter(EquipmentType.VOLTAGE_LEVEL, CombinatorType.OR,
                                Set.of(StringExpertRule.builder().fieldType(FieldType.NAME).operatorType(OperatorType.NOT_IN).referenceValues(Set.of("Best voltage level ;)")).build(),
                                        PropertiesExpertRule.builder().fieldType(FieldType.FREE_PROPERTIES).operatorType(OperatorType.IN).targetProperty("myCustomProperty").referenceValues(Set.of("My custom value", "My other custom value")).build(),
                                        NumberExpertRule.builder().fieldType(FieldType.NOMINAL_VOLTAGE).operatorType(OperatorType.EQUALS).referenceValue(90D).build())),
                        Set.of("VOLTAGE_LEVEL_1", "VOLTAGE_LEVEL_3", "VL_S3")),
                Arguments.of(buildCombinatorExpertFilter(EquipmentType.VOLTAGE_LEVEL, CombinatorType.AND,
                                Set.of(StringExpertRule.builder().fieldType(FieldType.NAME).operatorType(OperatorType.NOT_IN).referenceValues(Set.of("other vl...")).build(),
                                        PropertiesExpertRule.builder().fieldType(FieldType.FREE_PROPERTIES).operatorType(OperatorType.IN).targetProperty("myCustomProperty").referenceValues(Set.of("My custom value", "My other custom value")).build(),
                                        NumberExpertRule.builder().fieldType(FieldType.NOMINAL_VOLTAGE).operatorType(OperatorType.EQUALS).referenceValue(400D).build())),
                        Set.of("VOLTAGE_LEVEL_1")),
                //Filter Expert Filter
                Arguments.of(buildFilterExpertFilter(EquipmentType.VOLTAGE_LEVEL, OperatorType.IS_PART_OF,
                                Set.of(buildCombinatorExpertFilter(EquipmentType.VOLTAGE_LEVEL, CombinatorType.AND,
                                                Set.of(StringExpertRule.builder().fieldType(FieldType.NAME).operatorType(OperatorType.NOT_IN).referenceValues(Set.of("other vl...")).build(),
                                                        PropertiesExpertRule.builder().fieldType(FieldType.FREE_PROPERTIES).operatorType(OperatorType.IN).targetProperty("myCustomProperty").referenceValues(Set.of("My custom value", "My other custom value")).build(),
                                                        NumberExpertRule.builder().fieldType(FieldType.NOMINAL_VOLTAGE).operatorType(OperatorType.EQUALS).referenceValue(400D).build())),
                                        buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.EXISTS, null, null),
                                        new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VOLTAGE_LEVEL_3")))),
                        Set.of("VOLTAGE_LEVEL_1", "VOLTAGE_LEVEL_3", "VL_S3")
                ),
                Arguments.of(buildFilterExpertFilter(EquipmentType.VOLTAGE_LEVEL, OperatorType.IS_NOT_PART_OF,
                                Set.of(buildCombinatorExpertFilter(EquipmentType.VOLTAGE_LEVEL, CombinatorType.AND,
                                                Set.of(StringExpertRule.builder().fieldType(FieldType.NAME).operatorType(OperatorType.NOT_IN).referenceValues(Set.of("other vl...")).build(),
                                                        PropertiesExpertRule.builder().fieldType(FieldType.FREE_PROPERTIES).operatorType(OperatorType.IN).targetProperty("myCustomProperty").referenceValues(Set.of("My custom value", "My other custom value")).build(),
                                                        NumberExpertRule.builder().fieldType(FieldType.NOMINAL_VOLTAGE).operatorType(OperatorType.EQUALS).referenceValue(400D).build())),
                                        buildStringExpertFilter(EquipmentType.VOLTAGE_LEVEL, FieldType.NAME, OperatorType.EXISTS, null, null),
                                        new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("VOLTAGE_LEVEL_3")))),
                        Set.of("VOLTAGE_LEVEL_2", "VL_S2")
                )
        );
    }

    private static Filter buildBooleanExpertFilter(EquipmentType equipmentType, FieldType fieldType, OperatorType operatorType, Boolean referenceValue) {
        BooleanExpertRule rule = BooleanExpertRule.builder().fieldType(fieldType).operatorType(operatorType).referenceValue(referenceValue).build();
        return new ExpertFilter(equipmentType, rule);
    }

    private static Filter buildEnumExpertFilter(EquipmentType equipmentType, FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues) {
        EnumExpertRule rule = EnumExpertRule.builder().fieldType(fieldType).operatorType(operatorType).referenceValue(referenceValue).referenceValues(referenceValues).build();
        return new ExpertFilter(equipmentType, rule);
    }

    private static Filter buildNumberExpertFilter(EquipmentType equipmentType, FieldType fieldType, OperatorType operatorType, Double referenceValue, Set<Double> referenceValues) {
        NumberExpertRule rule = NumberExpertRule.builder().fieldType(fieldType).operatorType(operatorType).referenceValue(referenceValue).referenceValues(referenceValues).build();
        return new ExpertFilter(equipmentType, rule);
    }

    private static Filter buildPropertiesExpertFilter(EquipmentType equipmentType, FieldType fieldType, OperatorType operatorType, String targetProperty, Set<String> referenceValues) {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().fieldType(fieldType).operatorType(operatorType).targetProperty(targetProperty).referenceValues(referenceValues).build();
        return new ExpertFilter(equipmentType, rule);
    }

    private static Filter buildStringExpertFilter(EquipmentType equipmentType, FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues) {
        StringExpertRule rule = StringExpertRule.builder().fieldType(fieldType).operatorType(operatorType).referenceValue(referenceValue).referenceValues(referenceValues).build();
        return new ExpertFilter(equipmentType, rule);
    }

    private static Filter buildCombinatorExpertFilter(EquipmentType equipmentType, CombinatorType combinatorType, Set<ExpertRule> subRules) {
        CombinatorExpertRule rule = CombinatorExpertRule.builder().combinatorType(combinatorType).subRules(subRules).build();
        return new ExpertFilter(equipmentType, rule);
    }

    private static Filter buildFilterExpertFilter(EquipmentType equipmentType, OperatorType operatorType, Set<Filter> referenceFilters) {
        FilterExpertRule rule = FilterExpertRule.builder().operatorType(operatorType).referenceFilters(referenceFilters).build();
        return new ExpertFilter(equipmentType, rule);
    }

    @Test
    void testCreateFilterWithNullEquipmentTypeThrowsException() {
        assertThatThrownBy(() -> new ExpertFilter(null, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testCreateFilterWithNullEquipmentIdsThrowsException() {
        assertThatThrownBy(() -> new ExpertFilter(EquipmentType.LINE, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void testGetFilterTypeReturnsExpertFilter() {
        AbstractExpertRule mockRule = Mockito.mock(AbstractExpertRule.class);
        ExpertFilter filter = new ExpertFilter(EquipmentType.LINE, mockRule);

        assertThat(filter.getFilterType()).isEqualTo(FilterType.EXPERT);
    }

    @Test
    void testFilterEvaluationWithMockRuleDelegatesEvaluationToRuleForEachEquipment() {
        ExpertRule mockRule = Mockito.mock(ExpertRule.class);
        ExpertFilter filter = new ExpertFilter(EquipmentType.LINE, mockRule);

        filter.evaluate(network);

        ArgumentCaptor<Identifiable<Line>> argumentCaptor = ArgumentCaptor.forClass(Identifiable.class);
        verify(mockRule, times(3)).evaluateRule(argumentCaptor.capture());
        List<Identifiable<Line>> capturedLines = argumentCaptor.getAllValues();
        assertThat(capturedLines).hasSize(3)
                .allMatch(line -> IdentifiableType.LINE.equals(line.getType()))
                .allMatch(line -> line.getId().startsWith("LINE_"));
    }

    @Test
    void testFilterEvaluationWithMockRuleReturnsExpectedEquipment() {
        Line expectedLine = network.getLine("LINE_1");
        AbstractExpertRule mockRule = Mockito.mock(AbstractExpertRule.class);
        when(mockRule.evaluateRule(any())).thenReturn(false);
        when(mockRule.evaluateRule(expectedLine)).thenReturn(true);
        ExpertFilter filter = new ExpertFilter(EquipmentType.LINE, mockRule);

        List<Identifiable<?>> filteredIdentifiableList = filter.evaluate(network);

        assertThat(filteredIdentifiableList).hasSize(1);
        assertThat(filteredIdentifiableList.getFirst()).isEqualTo(expectedLine);
    }

    @Test
    void testMockFilterEvaluationReturnsExpectedEquipments() {
        ExpertRule mockRule = Mockito.mock(ExpertRule.class);
        Filter filter = new ExpertFilter(EquipmentType.LINE, mockRule);
        when(mockRule.evaluateRule(any())).thenReturn(false);
        when(mockRule.evaluateRule(any(Line.class))).thenReturn(true);
        Set<String> expectedEquipmentIds = Set.of("LINE_1", "LINE_2", "LINE_3");

        List<Identifiable<?>> filteredIdentifiableList = filter.evaluate(network);

        assertThat(filteredIdentifiableList).hasSize(expectedEquipmentIds.size());
        assertThat(filteredIdentifiableList.stream().map(Identifiable::getId)).containsAll(expectedEquipmentIds);
        verify(mockRule, times(expectedEquipmentIds.size())).evaluateRule(any(Line.class));
    }

    @ParameterizedTest
    @MethodSource("provideFilterEvaluationArguments")
    void testFilterEvaluationReturnsExpectedEquipments(Filter filter, Set<String> expectedEquipmentIds) {
        List<Identifiable<?>> filteredIdentifiableList = filter.evaluate(network);

        assertThat(filteredIdentifiableList).hasSize(expectedEquipmentIds.size());
        assertThat(filteredIdentifiableList.stream().map(Identifiable::getId)).containsAll(expectedEquipmentIds);
    }
}
