package org.gridsuite.filter.globalfilter;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.assertj.core.api.*;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.IdentifierListFilter;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.gridsuite.filter.wip.rule.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/* Methods dependencies:
 * applyGlobalFilterOnNetwork
 *  ↳ applyGlobalFilterOnNetwork
 *     ↳ applyFilterOnNetwork
 *     ⇃  ↳ filterNetwork
 *     ↳ buildExpertFilter
 *        ↳ buildNominalVoltageRules
 *        ⇃  ↳ getNominalVoltageFieldType
 *        ↳ buildCountryCodeRules
 *        ⇃  ↳ getCountryCodeFieldType
 *        ↳ buildSubstationPropertyRules
 *           ↳ getSubstationPropertiesFieldTypes
 */
class GlobalFilterUtilsTest implements WithAssertions {
    private static <T extends ExpertRule> void testVariableOrCombinationRules(final Optional<ExpertRule> result, final int inputSize,
                                                                              final Class<T> ruleClass, final ThrowingConsumer<T>[] singleRuleAsserts,
                                                                              final Iterable<T> multiAssertElements) {
        final OptionalAssert<ExpertRule> assertion = Assertions.assertThat(result).as("result");
        if (inputSize <= 0) {
            assertion.isEmpty();
        } else {
            assertion.isPresent();
            if (inputSize == 1) {
                assertion.get(InstanceOfAssertFactories.type(ruleClass))
                    .as("expert rule")
                    .satisfies(singleRuleAsserts);
            } else {
                assertion.get(InstanceOfAssertFactories.type(CombinatorExpertRule.class))
                    .as("combinator expert rule")
                    .satisfies(cer -> Assertions.assertThat(cer.getCombinator()).as("combinator").isEqualTo(CombinatorType.OR))
                    .extracting(CombinatorExpertRule::getRules, InstanceOfAssertFactories.list(ExpertRule.class)).as("expert rules")
                    .containsExactlyInAnyOrderElementsOf(multiAssertElements);
            }
        }
    }

    @Nested
    @DisplayName("filterEquipmentTypes(...)")
    class FilterEquipmentTypes {

        @Test
        void testNoGenericFilters() {
            List<Filter> genericFilters = Collections.emptyList();

            // Expect all equipment types to be returned
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.LINE, genericFilters));
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.GENERATOR, genericFilters));
        }

        @Test
        void testSubstationAndVoltageLevelFilters() {
            // Mock the return values for your generic filters
            final Filter subStationFilter = mock(Filter.class);
            when(subStationFilter.getEquipmentType()).thenReturn(EquipmentType.SUBSTATION);
            final Filter voltageLevelfilter = mock(Filter.class);
            when(voltageLevelfilter.getEquipmentType()).thenReturn(EquipmentType.VOLTAGE_LEVEL);

            List<Filter> genericFilters = List.of(subStationFilter, voltageLevelfilter);

            // Expect all equipment types to be returned
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.LINE, genericFilters));
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.TWO_WINDINGS_TRANSFORMER, genericFilters));
        }

        @Test
        void testFiltersOnOtherEquipmentTypes() {
            // Mock the return values for your generic filters
            final Filter lineFilter = mock(Filter.class);
            when(lineFilter.getEquipmentType()).thenReturn(EquipmentType.LINE);

            final Filter voltageLevelFilter = mock(Filter.class);
            when(voltageLevelFilter.getEquipmentType()).thenReturn(EquipmentType.VOLTAGE_LEVEL);

            List<Filter> genericFilters = List.of(lineFilter, voltageLevelFilter);

            // Expect only TYPE_A to be returned
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.LINE, genericFilters));
            assertFalse(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.GENERATOR, genericFilters));
        }
    }

    /** Trick Java to create generic array */
    @SafeVarargs
    private static <T> ThrowingConsumer<T>[] createAssertArray(final ThrowingConsumer<T>... assertions) {
        return assertions;
    }

    @Nested
    @DisplayName("buildNominalVoltageRules(...)")
    class BuildNominalVoltageRules {
        @ParameterizedTest
        @MethodSource("expertRulesData")
        void shouldCreateExpertRules(final List<List<Integer>> voltageRanges) {
            testVariableOrCombinationRules(
                GlobalFilterUtils.buildNominalVoltageRules(voltageRanges, EquipmentType.VOLTAGE_LEVEL),
                voltageRanges.size(),
                NumberExpertRule.class,
                createAssertArray(
                    ner -> assertThat(ner.getValues()).as("values").containsExactlyInAnyOrderElementsOf(voltageRanges.getFirst().stream().map(Integer::doubleValue).toList()),
                    ner -> assertThat(ner.getField()).as("field").isEqualTo(FieldType.NOMINAL_VOLTAGE),
                    ner -> assertThat(ner.getOperator()).as("operator").isEqualTo(OperatorType.BETWEEN)
                ),
                voltageRanges.stream().map(vr -> NumberExpertRule.builder().values(vr.stream().map(Integer::doubleValue).toList())
                    .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.BETWEEN).build()).collect(Collectors.toUnmodifiableList()));
        }

        private static Stream<Arguments> expertRulesData() {
            return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of(List.of(225, 400))),
                Arguments.of(List.of(List.of(225, 400), List.of(300, 350)))
            );
        }
    }

    @Nested
    @DisplayName("buildCountryCodeRules(...)")
    class BuildCountryCodeRules {
        @ParameterizedTest
        @MethodSource("enumRulesData")
        void shouldCreateEnumRules(final List<Country> countries) {
            testVariableOrCombinationRules(
                GlobalFilterUtils.buildCountryCodeRules(countries, EquipmentType.VOLTAGE_LEVEL),
                countries.size(),
                EnumExpertRule.class,
                createAssertArray(
                    ner -> assertThat(ner.getValue()).as("value").hasToString(countries.getFirst().name()),
                    ner -> assertThat(ner.getField()).as("field").isEqualTo(FieldType.COUNTRY),
                    ner -> assertThat(ner.getOperator()).as("operator").isEqualTo(OperatorType.EQUALS)
                ),
                (List<EnumExpertRule>) countries.stream().map(c -> EnumExpertRule.builder().value(c.name())
                    .field(FieldType.COUNTRY).operator(OperatorType.EQUALS).build()).toList());
        }

        private static Stream<Arguments> enumRulesData() {
            return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of(Country.YT)),
                Arguments.of(List.of(Country.FR, Country.DE))
            );
        }
    }

    @Nested
    @DisplayName("buildSubstationPropertyRules(...)")
    class BuildSubstationPropertyRules {
        @ParameterizedTest
        @MethodSource("propertiesRulesData")
        void shouldCreateCorrectPropertiesRules(final Map<String, List<String>> properties) {
            testVariableOrCombinationRules(
                GlobalFilterUtils.buildSubstationPropertyRules(properties, EquipmentType.VOLTAGE_LEVEL),
                properties.size(),
                PropertiesExpertRule.class,
                createAssertArray(
                    per -> assertThat(per.getOperator()).as("operator").isEqualTo(OperatorType.IN),
                    per -> assertThat(per.getField()).as("field").isEqualTo(FieldType.SUBSTATION_PROPERTIES),
                    per -> assertThat(per.getPropertyName()).as("property name").isEqualTo("prop1"),
                    per -> assertThat(per.getPropertyValues()).as("property values").containsExactlyInAnyOrderElementsOf(properties.values().iterator().next())
                ),
                (List<PropertiesExpertRule>) properties.entrySet().stream().map(e -> PropertiesExpertRule.builder()
                    .operator(OperatorType.IN).field(FieldType.SUBSTATION_PROPERTIES).propertyName(e.getKey()).propertyValues(e.getValue()).build()).toList());
        }

        private static Stream<Arguments> propertiesRulesData() {
            return Stream.of(
                Arguments.of(Map.of()),
                Arguments.of(Map.of("prop1", List.of("value0"))),
                Arguments.of(Map.of("prop1", List.of("value1", "value2"), "prop2", List.of("value3")))
            );
        }
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nominalVoltageFieldTypeData")
    void shouldReturnCorrectNominalVoltageFieldTypes(final EquipmentType equipmentType, final List<FieldType> expectedFields) {
        assertThat(GlobalFilterUtils.getNominalVoltageFieldType(equipmentType))
            .as("result").containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    private static Stream<Arguments> nominalVoltageFieldTypeData() {
        return Stream.of(
            // Nominal voltage
            Arguments.of(EquipmentType.LINE, List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2)),
            Arguments.of(EquipmentType.TWO_WINDINGS_TRANSFORMER, List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2)),
            Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2, FieldType.NOMINAL_VOLTAGE_3)),
            Arguments.of(EquipmentType.VOLTAGE_LEVEL, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.GENERATOR, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.LOAD, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.SHUNT_COMPENSATOR, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.STATIC_VAR_COMPENSATOR, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.BATTERY, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.BUS, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.BUSBAR_SECTION, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.BOUNDARY_LINE, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.HVDC_LINE, List.of(FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2)),
            Arguments.of(EquipmentType.LCC_CONVERTER_STATION, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.VSC_CONVERTER_STATION, List.of(FieldType.NOMINAL_VOLTAGE))
            );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("countryCodeFieldTypeData")
    void shouldReturnCorrectCountryCodeFieldTypes(final EquipmentType equipmentType, final List<FieldType> expectedFields) {
        assertThat(GlobalFilterUtils.getCountryCodeFieldType(equipmentType))
            .as("result").containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    private static Stream<Arguments> countryCodeFieldTypeData() {
        return Stream.of(
            // Country code
            Arguments.of(EquipmentType.VOLTAGE_LEVEL, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.TWO_WINDINGS_TRANSFORMER, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.LINE, List.of(FieldType.COUNTRY_1, FieldType.COUNTRY_2)),
            Arguments.of(EquipmentType.GENERATOR, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.LOAD, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.SHUNT_COMPENSATOR, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.STATIC_VAR_COMPENSATOR, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.BATTERY, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.BUS, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.BUSBAR_SECTION, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.BOUNDARY_LINE, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.HVDC_LINE, List.of(FieldType.COUNTRY_1, FieldType.COUNTRY_2)),
            Arguments.of(EquipmentType.LCC_CONVERTER_STATION, List.of(FieldType.COUNTRY)),
            Arguments.of(EquipmentType.VSC_CONVERTER_STATION, List.of(FieldType.COUNTRY))
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("substationPropertyFieldTypeData")
    void shouldReturnCorrectSubstationPropertyFieldTypes(final EquipmentType equipmentType, final List<FieldType> expectedFields) {
        assertThat(GlobalFilterUtils.getSubstationPropertiesFieldTypes(equipmentType))
            .as("result").containsExactlyInAnyOrderElementsOf(expectedFields);
    }

    private static Stream<Arguments> substationPropertyFieldTypeData() {
        return Stream.of(
            // Substation properties
            Arguments.of(EquipmentType.LINE, List.of(FieldType.SUBSTATION_PROPERTIES_1, FieldType.SUBSTATION_PROPERTIES_2)),
            Arguments.of(EquipmentType.GENERATOR, List.of(FieldType.SUBSTATION_PROPERTIES)),
            Arguments.of(EquipmentType.SUBSTATION, List.of(FieldType.FREE_PROPERTIES)),
            Arguments.of(EquipmentType.BUS, List.of(FieldType.SUBSTATION_PROPERTIES)),
            Arguments.of(EquipmentType.HVDC_LINE, List.of(FieldType.SUBSTATION_PROPERTIES_1, FieldType.SUBSTATION_PROPERTIES_2)),
            Arguments.of(EquipmentType.VSC_CONVERTER_STATION, List.of(FieldType.SUBSTATION_PROPERTIES)),
            Arguments.of(EquipmentType.LCC_CONVERTER_STATION, List.of(FieldType.SUBSTATION_PROPERTIES))
        );
    }

    @Nested
    @DisplayName("buildExpertFilter(...)")
    class BuildExpertFilter {
        @Test
        void shouldReturnNullWhenNoExpertFiltersProvided() {
            FilterLoader filterLoader = mock(FilterLoader.class);
            final GlobalFilter globalFilter = new GlobalFilter(null, null, null, null, null);
            assertThat(GlobalFilterUtils.buildExpertFilter(globalFilter, EquipmentType.GENERATOR, filterLoader))
                    .as("result").isNull();
        }

        @Test
        void shouldReturnNullWhenNoRules() {
            FilterLoader filterLoader = mock(FilterLoader.class);
            final GlobalFilter globalFilter = new GlobalFilter(List.of(), List.of(), List.of(), List.of(), Map.of());
            assertTrue(globalFilter.isEmpty());
            assertThat(GlobalFilterUtils.buildExpertFilter(globalFilter, EquipmentType.GENERATOR, filterLoader))
                .as("result").isNull();
        }

        @Test
        void shouldReturnResult() {
            UUID filterUuid = UUID.randomUUID();
            Filter filter = new IdentifierListFilter(EquipmentType.GENERATOR, Set.of("GEN1", "GEN2"));
            FilterLoader filterLoader = mockFilterLoader(filterUuid, filter);
            final GlobalFilter globalFilter = new GlobalFilter(List.of(List.of(380, 400), List.of(225, 230)), List.of(Country.FR, Country.BE), List.of(filterUuid), List.of(), Map.of());
            assertThat(GlobalFilterUtils.buildExpertFilter(globalFilter, EquipmentType.GENERATOR, filterLoader))
                .as("result").isNotNull();
        }

        @Test
        void shouldReturnResultWithGenericFilterTypeDifferentFromEquipmentType() {
            UUID filterUuid = UUID.randomUUID();
            Filter filter = new IdentifierListFilter(EquipmentType.VOLTAGE_LEVEL, Set.of("GEN1", "GEN2"));
            FilterLoader filterLoader = mockFilterLoader(filterUuid, filter);
            final GlobalFilter globalFilter = new GlobalFilter(List.of(List.of(380, 400), List.of(225, 230)), List.of(Country.FR, Country.BE), List.of(filterUuid), List.of(), Map.of());
            assertThat(GlobalFilterUtils.buildExpertFilter(globalFilter, EquipmentType.GENERATOR, filterLoader))
                .as("result").isNotNull();
        }
    }

    private Identifiable<?> mockIdentifiable(String id) {
        Identifiable<?> identifiable = mock(Identifiable.class);
        when(identifiable.getId()).thenReturn(id);
        return identifiable;
    }

    private FilterLoader mockFilterLoader(UUID filterUuid, Filter filter) {
        FilterLoader filterLoader = mock(FilterLoader.class);
        AbstractFilter abstractFilter = mock(AbstractFilter.class);
        when(filterLoader.getFilters(List.of(filterUuid))).thenReturn(List.of(abstractFilter));
        when(abstractFilter.toFilter(filterLoader)).thenReturn(filter);
        return filterLoader;
    }

    @Nested
    @DisplayName("GlobalFilter.applyGlobalFilterOnNetwork(...)")
    class ApplyFilterOnNetwork {
        @Test
        void shouldReturnFilteredNetworkWhenSameEquipmentType() {
            final Network network = TestNetworkUtils.createTestNetwork();
            UUID filterUuid = UUID.randomUUID();
            Filter filter = mock(Filter.class);
            FilterLoader filterLoader = mockFilterLoader(filterUuid, filter);
            Identifiable<?> id1 = mockIdentifiable("GENERATOR_1");
            Identifiable<?> id2 = mockIdentifiable("GENERATOR_2");
            when(filter.getEquipmentType()).thenReturn(EquipmentType.GENERATOR);
            when(filter.evaluate(network)).thenReturn(List.of(id1, id2));
            GlobalFilter globalFilter = GlobalFilter.builder()
                    .genericFilter(List.of(filterUuid))
                    .build();

            List<String> evaluatedIdentifiables = globalFilter.evaluate(network, List.of(EquipmentType.GENERATOR), filterLoader).stream().map(Identifiable::getId).toList();
            assertThat(evaluatedIdentifiables).containsExactlyInAnyOrder("GENERATOR_1", "GENERATOR_2");
        }

        @Test
        void shouldReturnEmptyWhenDifferentEquipmentType() {
            final Network network = TestNetworkUtils.createTestNetwork();
            UUID filterUuid = UUID.randomUUID();
            Filter filter = mock(Filter.class);
            FilterLoader filterLoader = mockFilterLoader(filterUuid, filter);
            Identifiable<?> id1 = mockIdentifiable("GENERATOR_1");
            Identifiable<?> id2 = mockIdentifiable("GENERATOR_2");
            when(filter.getEquipmentType()).thenReturn(EquipmentType.GENERATOR);
            when(filter.evaluate(network)).thenReturn(List.of(id1, id2));
            GlobalFilter globalFilter = GlobalFilter.builder()
                    .genericFilter(List.of(filterUuid))
                    .build();

            List<String> evaluatedIdentifiables = globalFilter.evaluate(network, List.of(EquipmentType.LOAD), filterLoader).stream().map(Identifiable::getId).toList();
            assertThat(evaluatedIdentifiables).hasSize(0);
        }
    }

    @Nested
    @DisplayName("buildGenericFilterRule(...)")
    class BuildGenericFilterRuleTests {

        @Test
        void testSameEquipmentType() {
            Filter filter1 = mock(Filter.class);
            Filter filter2 = mock(Filter.class);
            when(filter1.getEquipmentType()).thenReturn(EquipmentType.LINE);
            when(filter2.getEquipmentType()).thenReturn(EquipmentType.LINE);

            List<Filter> filters = Arrays.asList(filter1, filter2);
            assertNotNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.LINE));
        }

        @Test
        void testSubstationAndVoltageLevelOnAnyEquipmentType() {
            Filter filter1 = mock(Filter.class);
            Filter filter2 = mock(Filter.class);
            when(filter1.getEquipmentType()).thenReturn(EquipmentType.VOLTAGE_LEVEL);
            when(filter2.getEquipmentType()).thenReturn(EquipmentType.SUBSTATION);

            List<Filter> filters = Arrays.asList(filter1, filter2);
            assertNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.LINE));
            assertNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.GENERATOR));
            assertNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.TWO_WINDINGS_TRANSFORMER));
            assertNotNull(GlobalFilterUtils.buildSubstationOrVoltageLevelFilterRule(filters, EquipmentType.LINE));
            assertNotNull(GlobalFilterUtils.buildSubstationOrVoltageLevelFilterRule(filters, EquipmentType.GENERATOR));
            assertNotNull(GlobalFilterUtils.buildSubstationOrVoltageLevelFilterRule(filters, EquipmentType.TWO_WINDINGS_TRANSFORMER));
        }

        @Test
        void testNotSameEquipmentType() {
            Filter filter1 = mock(Filter.class);
            Filter filter2 = mock(Filter.class);
            when(filter1.getEquipmentType()).thenReturn(EquipmentType.LINE);
            when(filter2.getEquipmentType()).thenReturn(EquipmentType.TWO_WINDINGS_TRANSFORMER);

            List<Filter> filters = Arrays.asList(filter1, filter2);
            assertNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.GENERATOR));
        }
    }
}
