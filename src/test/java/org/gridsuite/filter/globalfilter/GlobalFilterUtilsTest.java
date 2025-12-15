package org.gridsuite.filter.globalfilter;

import com.powsybl.iidm.network.Country;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.assertj.core.api.*;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.expertfilter.expertrule.*;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilterEquipmentAttributes;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FiltersUtils;
import org.gridsuite.filter.utils.UuidUtils;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

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
    private static <T extends AbstractExpertRule> void testVariableOrCombinationRules(final Optional<AbstractExpertRule> result, final int inputSize,
                                                                                      final Class<T> ruleClass, final ThrowingConsumer<T>[] singleRuleAsserts,
                                                                                      final Iterable<T> multiAssertElements) {
        final OptionalAssert<AbstractExpertRule> assertion = Assertions.assertThat(result).as("result");
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
                    .extracting(CombinatorExpertRule::getRules, InstanceOfAssertFactories.list(AbstractExpertRule.class)).as("expert rules")
                    .containsExactlyInAnyOrderElementsOf(multiAssertElements);
            }
        }
    }

    @Nested
    @DisplayName("filterEquipmentTypes(...)")
    class FilterEquipmentTypes {

        @Test
        void testNoGenericFilters() {
            List<AbstractFilter> genericFilters = Collections.emptyList();

            // Expect all equipment types to be returned
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.LINE, genericFilters));
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.GENERATOR, genericFilters));
        }

        @Test
        void testSubstationAndVoltageLevelFilters() {
            final FilterLoader loader = Mockito.mock(FilterLoader.class);
            List<UUID> genericFiltersUuids = List.of(UUID.randomUUID(), UUID.randomUUID());

            // Mock the return values for your generic filters
            final AbstractFilter subStationFilter = Mockito.mock(AbstractFilter.class);
            Mockito.when(subStationFilter.getEquipmentType()).thenReturn(EquipmentType.SUBSTATION);
            final AbstractFilter voltageLevelfilter = Mockito.mock(AbstractFilter.class);
            Mockito.when(voltageLevelfilter.getEquipmentType()).thenReturn(EquipmentType.VOLTAGE_LEVEL);

            Mockito.when(loader.getFilters(genericFiltersUuids)).thenReturn(List.of(
                subStationFilter, voltageLevelfilter));

            List<AbstractFilter> genericFilters = loader.getFilters(genericFiltersUuids);

            // Expect all equipment types to be returned
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.LINE, genericFilters));
            assertTrue(GlobalFilterUtils.shouldProcessEquipmentType(EquipmentType.TWO_WINDINGS_TRANSFORMER, genericFilters));
        }

        @Test
        void testFiltersOnOtherEquipmentTypes() {
            final FilterLoader loader = Mockito.mock(FilterLoader.class);
            List<UUID> genericFiltersUuids = List.of(UUID.randomUUID(), UUID.randomUUID());

            // Mock the return values for your generic filters
            final AbstractFilter lineFilter = Mockito.mock(AbstractFilter.class);
            Mockito.when(lineFilter.getEquipmentType()).thenReturn(EquipmentType.LINE);

            final AbstractFilter voltageLevelFilter = Mockito.mock(AbstractFilter.class);
            Mockito.when(voltageLevelFilter.getEquipmentType()).thenReturn(EquipmentType.VOLTAGE_LEVEL);

            Mockito.when(loader.getFilters(genericFiltersUuids)).thenReturn(List.of(
                lineFilter, voltageLevelFilter));

            List<AbstractFilter> genericFilters = loader.getFilters(genericFiltersUuids);

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
        void shouldCreateExpertRules(final List<String> nominalVoltages) {
            testVariableOrCombinationRules(
                GlobalFilterUtils.buildNominalVoltageRules(nominalVoltages, EquipmentType.VOLTAGE_LEVEL),
                nominalVoltages.size(),
                NumberExpertRule.class,
                createAssertArray(
                    ner -> assertThat(ner.getValue()).as("value").hasToString(nominalVoltages.getFirst()),
                    ner -> assertThat(ner.getField()).as("field").isEqualTo(FieldType.NOMINAL_VOLTAGE),
                    ner -> assertThat(ner.getOperator()).as("operator").isEqualTo(OperatorType.EQUALS)
                ),
                nominalVoltages.stream().map(nv -> NumberExpertRule.builder().value(Double.valueOf(nv))
                    .field(FieldType.NOMINAL_VOLTAGE).operator(OperatorType.EQUALS).build()).collect(Collectors.toUnmodifiableList()));
        }

        private static Stream<Arguments> expertRulesData() {
            return Stream.of(
                Arguments.of(List.of()),
                Arguments.of(List.of("300.0")),
                Arguments.of(List.of("400.0", "225.0"))
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
                    per -> assertThat(per.getCombinator()).as("combinator").isEqualTo(CombinatorType.OR),
                    per -> assertThat(per.getOperator()).as("operator").isEqualTo(OperatorType.IN),
                    per -> assertThat(per.getField()).as("field").isEqualTo(FieldType.SUBSTATION_PROPERTIES),
                    per -> assertThat(per.getPropertyName()).as("property name").isEqualTo("prop1"),
                    per -> assertThat(per.getPropertyValues()).as("property values").containsExactlyInAnyOrderElementsOf(properties.values().iterator().next())
                ),
                (List<PropertiesExpertRule>) properties.entrySet().stream().map(e -> PropertiesExpertRule.builder().combinator(CombinatorType.OR)
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
            Arguments.of(EquipmentType.DANGLING_LINE, List.of(FieldType.NOMINAL_VOLTAGE)),
            Arguments.of(EquipmentType.HVDC_LINE, List.of(FieldType.NOMINAL_VOLTAGE_1, FieldType.NOMINAL_VOLTAGE_2)),
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
            Arguments.of(EquipmentType.DANGLING_LINE, List.of(FieldType.COUNTRY)),
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
            Arguments.of(EquipmentType.GENERATOR, List.of(FieldType.SUBSTATION_PROPERTIES))
        );
    }

    @Nested
    @DisplayName("buildExpertFilter(...)")
    class BuildExpertFilter {
        @Test
        void shouldReturnNullWhenNoExpertFiltersProvided() {
            final GlobalFilter globalFilter = new GlobalFilter(null, null, null, null);
            assertThat(GlobalFilterUtils.buildExpertFilter(globalFilter, EquipmentType.GENERATOR, List.of()))
                    .as("result").isNull();
        }

        @Test
        void shouldReturnNullWhenNoRules() {
            final GlobalFilter globalFilter = new GlobalFilter(List.of(), List.of(), List.of(), Map.of());
            assertTrue(globalFilter.isEmpty());
            assertThat(GlobalFilterUtils.buildExpertFilter(globalFilter, EquipmentType.GENERATOR, List.of()))
                .as("result").isNull();
        }

        @Test
        void shouldReturnResult() {
            List<UUID> filterUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<AbstractFilter> filters = List.of(
                new IdentifierListFilter(UUID.randomUUID(), new Date(), EquipmentType.GENERATOR,
                    List.of(new IdentifierListFilterEquipmentAttributes("GEN1", 50.),
                        new IdentifierListFilterEquipmentAttributes("GEN2", 50.)
                )));
            final GlobalFilter globalFilter = new GlobalFilter(List.of("380", "225"), List.of(Country.FR, Country.BE), filterUuids, Map.of());
            assertThat(GlobalFilterUtils.buildExpertFilter(globalFilter, EquipmentType.GENERATOR, filters))
                .as("result").isNotNull();
        }

        @Test
        void shouldReturnResultWithGenericFilterTypeDifferentFromEquipmentType() {
            List<UUID> filterUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
            List<AbstractFilter> filters = List.of(
                new IdentifierListFilter(UUID.randomUUID(), new Date(), EquipmentType.VOLTAGE_LEVEL,
                    List.of(new IdentifierListFilterEquipmentAttributes("GEN1", 50.),
                        new IdentifierListFilterEquipmentAttributes("GEN2", 50.)
                    )));
            final GlobalFilter globalFilter = new GlobalFilter(List.of("380", "225"), List.of(Country.FR, Country.BE), filterUuids, Map.of());
            assertThat(GlobalFilterUtils.buildExpertFilter(globalFilter, EquipmentType.GENERATOR, filters))
                .as("result").isNotNull();
        }
    }

    @Nested
    @DisplayName("filterNetwork(...)")
    class FilterNetwork {
        @Test
        void shouldReturnIdsFromFilteredNetwork() {
            final Network network = Mockito.mock(Network.class);
            final FilterLoader loader = Mockito.mock(FilterLoader.class);
            final AbstractFilter filter = Mockito.mock(AbstractFilter.class);
            when(filter.getEquipmentType()).thenReturn(EquipmentType.GENERATOR);
            try (final MockedStatic<FiltersUtils> mockedFU = Mockito.mockStatic(FiltersUtils.class, Mockito.CALLS_REAL_METHODS)) {
                final Identifiable<?> i1 = Mockito.mock(Identifiable.class);
                when(i1.getId()).thenReturn("id1");
                final Identifiable<?> i2 = Mockito.mock(Identifiable.class);
                when(i2.getId()).thenReturn("id2");
                final List<Identifiable<?>> attributes = List.of(i1, i2);
                mockedFU.when(() -> FiltersUtils.getIdentifiables(filter, network, loader)).thenReturn(attributes);
                mockedFU.clearInvocations(); //important because stubbing static method counts as call
                assertThat(GlobalFilterUtils.filterNetwork(filter, network, loader)).as("result")
                    .containsExactlyInAnyOrder("id1", "id2");
                Mockito.verify(i1, Mockito.atLeastOnce()).getId();
                Mockito.verify(i2, Mockito.atLeastOnce()).getId();
                Mockito.verify(filter, Mockito.atLeastOnce()).getEquipmentType();
                Mockito.verifyNoMoreInteractions(filter, network, loader, i1, i2);
                mockedFU.verify(() -> FiltersUtils.getIdentifiables(eq(filter), eq(network), eq(loader)), Mockito.times(1));
                mockedFU.verifyNoMoreInteractions(); //check if forget to mock a method
            }
        }
    }

    @Nested
    @DisplayName("applyFilterOnNetwork(...)")
    class ApplyFilterOnNetwork {
        @Test
        void shouldReturnFilteredNetworkWhenSameEquipmentType() {
            final Network network = Mockito.mock(Network.class);
            final FilterLoader loader = Mockito.mock(FilterLoader.class);
            final AbstractFilter filter = Mockito.mock(AbstractFilter.class);
            when(filter.getEquipmentType()).thenReturn(EquipmentType.GENERATOR);
            try (final MockedStatic<FiltersUtils> mockedFU = Mockito.mockStatic(FiltersUtils.class, Mockito.CALLS_REAL_METHODS)) {
                final Identifiable<?> gen1 = Mockito.mock(Identifiable.class);
                when(gen1.getId()).thenReturn("gen1");
                final Identifiable<?> gen2 = Mockito.mock(Identifiable.class);
                when(gen2.getId()).thenReturn("gen2");
                final List<Identifiable<?>> attributes = List.of(gen1, gen2);
                mockedFU.when(() -> FiltersUtils.getIdentifiables(filter, network, loader)).thenReturn(attributes);
                mockedFU.clearInvocations(); //important because stubbing static method counts as call
                assertThat(GlobalFilterUtils.applyFilterOnNetwork(filter, EquipmentType.GENERATOR, network, loader))
                    .as("result").containsExactlyInAnyOrder("gen1", "gen2");
                Mockito.verify(filter, Mockito.atLeastOnce()).getEquipmentType();
                Mockito.verify(gen1, Mockito.atLeastOnce()).getId();
                Mockito.verify(gen2, Mockito.atLeastOnce()).getId();
                Mockito.verifyNoMoreInteractions(filter, network, loader, gen1, gen2);
                mockedFU.verify(() -> FiltersUtils.getIdentifiables(eq(filter), eq(network), eq(loader)), Mockito.atLeastOnce());
                mockedFU.verifyNoMoreInteractions(); //check if forget to mock a method
            }
        }

        @Test
        void shouldBuildVoltageLevelFilterWhenVoltageLevelType() {
            final Network network = Mockito.mock(Network.class);
            final FilterLoader loader = Mockito.mock(FilterLoader.class);
            final AbstractFilter filter = Mockito.mock(AbstractFilter.class);
            when(filter.getEquipmentType()).thenReturn(EquipmentType.VOLTAGE_LEVEL);
            final UUID filterUuid = UuidUtils.createUUID(0);
            when(filter.getId()).thenReturn(filterUuid);
            try (final MockedStatic<FiltersUtils> mockedFU = Mockito.mockStatic(FiltersUtils.class, Mockito.CALLS_REAL_METHODS)) {
                final Identifiable<?> line1 = Mockito.mock(Identifiable.class);
                when(line1.getId()).thenReturn("line1");
                final Identifiable<?> line2 = Mockito.mock(Identifiable.class);
                when(line2.getId()).thenReturn("line2");
                final List<Identifiable<?>> attributes = List.of(line1, line2);
                mockedFU.when(() -> FiltersUtils.getIdentifiables(any(ExpertFilter.class), eq(network), eq(loader))).thenReturn(attributes);
                mockedFU.clearInvocations(); //important because stubbing static method counts as call
                assertThat(GlobalFilterUtils.applyFilterOnNetwork(filter, EquipmentType.LINE, network, loader))
                    .as("result").containsExactlyInAnyOrder("line1", "line2");
                Mockito.verify(filter, Mockito.atLeastOnce()).getEquipmentType();
                Mockito.verify(filter, Mockito.atLeastOnce()).getId();
                Mockito.verify(line1, Mockito.atLeastOnce()).getId();
                Mockito.verify(line2, Mockito.atLeastOnce()).getId();
                Mockito.verifyNoMoreInteractions(filter, network, loader, line1, line2);
                mockedFU.verify(() -> FiltersUtils.getIdentifiables(any(ExpertFilter.class), eq(network), eq(loader)), Mockito.atLeastOnce());
                mockedFU.verifyNoMoreInteractions(); //check if forget to mock a method
            }
        }

        @Test
        void shouldReturnEmptyWhenDifferentEquipmentType() {
            final FilterLoader loader = Mockito.mock(FilterLoader.class);
            final Network network = Mockito.mock(Network.class);
            final AbstractFilter filter = Mockito.mock(AbstractFilter.class);
            when(filter.getEquipmentType()).thenReturn(EquipmentType.LOAD);
            assertThat(GlobalFilterUtils.applyFilterOnNetwork(filter, EquipmentType.GENERATOR, network, loader))
                .as("result").isEmpty();
            Mockito.verify(filter, Mockito.atLeastOnce()).getEquipmentType();
            Mockito.verifyNoMoreInteractions(loader, network, filter);
        }
    }

    @Nested
    @DisplayName("applyGlobalFilterOnNetworkWithSingleEquipmentType(...)")
    class ApplyGlobalFilterOnNetworkWithSingleEquipmentType {
        @Test
        void shouldReturnFilteredNetworkWhenSameEquipmentType() {
            final Network network = Mockito.mock(Network.class);
            final FilterLoader loader = Mockito.mock(FilterLoader.class);

            final AbstractFilter genericFilter = Mockito.mock(AbstractFilter.class);
            when(genericFilter.getEquipmentType()).thenReturn(EquipmentType.GENERATOR);
            final UUID filterUuid = UuidUtils.createUUID(0);
            when(genericFilter.getId()).thenReturn(filterUuid);

            final GlobalFilter globalFilter = Mockito.mock(GlobalFilter.class);
            List<UUID> genericFilterUuids = List.of(filterUuid);
            when(globalFilter.getGenericFilter()).thenReturn(genericFilterUuids);

            when(loader.getFilters(genericFilterUuids)).thenReturn(List.of(genericFilter));

            try (final MockedStatic<FiltersUtils> mockedFU = Mockito.mockStatic(FiltersUtils.class, Mockito.CALLS_REAL_METHODS)) {
                final Identifiable<?> gen1 = Mockito.mock(Identifiable.class);
                when(gen1.getId()).thenReturn("gen1");
                final Identifiable<?> gen2 = Mockito.mock(Identifiable.class);
                when(gen2.getId()).thenReturn("gen2");
                final List<Identifiable<?>> attributes = List.of(gen1, gen2);
                mockedFU.when(() -> FiltersUtils.getIdentifiables(any(ExpertFilter.class), eq(network), eq(loader))).thenReturn(attributes);
                mockedFU.clearInvocations(); //important because stubbing static method counts as call

                // call test method and check result
                assertThat(GlobalFilterUtils.applyGlobalFilterOnNetwork(network, globalFilter, List.of(EquipmentType.GENERATOR), loader))
                    .as("result").containsExactlyInAnyOrderEntriesOf(Map.of(EquipmentType.GENERATOR, List.of("gen1", "gen2")));

                // check some interactions
                Mockito.verify(genericFilter, Mockito.atLeastOnce()).getEquipmentType();
                Mockito.verify(genericFilter, Mockito.atLeastOnce()).getId();
                Mockito.verify(globalFilter, Mockito.atLeastOnce()).getGenericFilter();
                Mockito.verify(gen1, Mockito.atLeastOnce()).getId();
                Mockito.verify(gen2, Mockito.atLeastOnce()).getId();
                Mockito.verifyNoMoreInteractions(genericFilter, network, gen1, gen2);
                mockedFU.verify(() -> FiltersUtils.getIdentifiables(any(ExpertFilter.class), eq(network), eq(loader)), Mockito.atLeastOnce());
            }
        }

        @Test
        void shouldBuildVoltageLevelFilterWhenVoltageLevelType() {
            final Network network = Mockito.mock(Network.class);
            final FilterLoader loader = Mockito.mock(FilterLoader.class);
            final AbstractFilter filter = Mockito.mock(AbstractFilter.class);
            final GlobalFilter globalFilter = Mockito.mock(GlobalFilter.class);
            when(filter.getEquipmentType()).thenReturn(EquipmentType.VOLTAGE_LEVEL);
            final UUID filterUuid = UuidUtils.createUUID(0);
            when(filter.getId()).thenReturn(filterUuid);
            try (final MockedStatic<FiltersUtils> mockedFU = Mockito.mockStatic(FiltersUtils.class, Mockito.CALLS_REAL_METHODS)) {
                final Identifiable<?> line1 = Mockito.mock(Identifiable.class);
                when(line1.getId()).thenReturn("line1");
                final Identifiable<?> line2 = Mockito.mock(Identifiable.class);
                when(line2.getId()).thenReturn("line2");
                final List<Identifiable<?>> attributes = List.of(line1, line2);
                mockedFU.when(() -> FiltersUtils.getIdentifiables(any(ExpertFilter.class), eq(network), eq(loader))).thenReturn(attributes);
                mockedFU.clearInvocations(); //important because stubbing static method counts as call
                assertThat(GlobalFilterUtils.applyGlobalFilterOnNetwork(network, globalFilter, EquipmentType.LINE, List.of(filter), loader))
                    .as("result").containsExactlyInAnyOrder("line1", "line2");
                Mockito.verify(filter, Mockito.atLeastOnce()).getEquipmentType();
                Mockito.verify(filter, Mockito.atLeastOnce()).getId();
                Mockito.verify(line1, Mockito.atLeastOnce()).getId();
                Mockito.verify(line2, Mockito.atLeastOnce()).getId();
                Mockito.verifyNoMoreInteractions(filter, network, line1, line2);
                mockedFU.verify(() -> FiltersUtils.getIdentifiables(any(ExpertFilter.class), eq(network), eq(loader)), Mockito.atLeastOnce());
            }
        }

        @Test
        void shouldReturnEmptyWhenDifferentEquipmentType() {
            final FilterLoader loader = Mockito.mock(FilterLoader.class);
            final Network network = Mockito.mock(Network.class);
            final AbstractFilter filter = Mockito.mock(AbstractFilter.class);
            final GlobalFilter globalFilter = Mockito.mock(GlobalFilter.class);
            when(filter.getEquipmentType()).thenReturn(EquipmentType.LOAD);
            assertThat(GlobalFilterUtils.applyGlobalFilterOnNetwork(network, globalFilter, EquipmentType.GENERATOR, List.of(filter), loader))
                .as("result").isEmpty();
            Mockito.verify(filter, Mockito.atLeastOnce()).getEquipmentType();
            Mockito.verifyNoMoreInteractions(network, filter);
        }
    }

    @Nested
    @DisplayName("applyGlobalFilterOnNetworkWithMultipleEquipmentType(...)")
    class ApplyGlobalFilterOnNetworkWithMultipleEquipmentType {
        @Test
        void shouldReturnFilteredNetwork() {
            final Network network = Mockito.mock(Network.class);
            final FilterLoader loader = Mockito.mock(FilterLoader.class);

            final AbstractFilter filterLine = Mockito.mock(AbstractFilter.class);
            when(filterLine.getEquipmentType()).thenReturn(EquipmentType.LINE);
            final UUID filterLineUuid = UuidUtils.createUUID(0);
            when(filterLine.getId()).thenReturn(filterLineUuid);

            final AbstractFilter filterTrans = Mockito.mock(AbstractFilter.class);
            when(filterTrans.getEquipmentType()).thenReturn(EquipmentType.TWO_WINDINGS_TRANSFORMER);
            final UUID filterTransUuid = UuidUtils.createUUID(1);
            when(filterTrans.getId()).thenReturn(filterTransUuid);

            final GlobalFilter globalFilter = Mockito.mock(GlobalFilter.class);
            List<UUID> genericFilterUuids = List.of(filterLineUuid, filterTransUuid);
            when(globalFilter.getGenericFilter()).thenReturn(genericFilterUuids);

            when(loader.getFilters(genericFilterUuids)).thenReturn(List.of(filterLine, filterTrans));

            try (final MockedStatic<FiltersUtils> mockedFU = Mockito.mockStatic(FiltersUtils.class, Mockito.CALLS_REAL_METHODS)) {
                final Identifiable<?> line1 = Mockito.mock(Identifiable.class);
                when(line1.getId()).thenReturn("line1");
                final List<Identifiable<?>> lineAttributes = List.of(line1);
                final Identifiable<?> trf1 = Mockito.mock(Identifiable.class);
                when(trf1.getId()).thenReturn("trf1");
                final List<Identifiable<?>> transAttributes = List.of(trf1);
                mockedFU.when(() -> FiltersUtils.getIdentifiables(argThat((ExpertFilter isPartOfFilter) ->
                        isPartOfFilter != null &&
                        isPartOfFilter.getEquipmentType().equals(EquipmentType.LINE)
                ), eq(network), eq(loader))).thenReturn(lineAttributes);
                mockedFU.when(() -> FiltersUtils.getIdentifiables(argThat((ExpertFilter isPartOfFilter) ->
                        isPartOfFilter != null &&
                        isPartOfFilter.getEquipmentType().equals(EquipmentType.TWO_WINDINGS_TRANSFORMER)
                ), eq(network), eq(loader))).thenReturn(transAttributes);
                mockedFU.clearInvocations(); //important because stubbing static method counts as call

                // call test method and check result
                assertThat(GlobalFilterUtils.applyGlobalFilterOnNetwork(network, globalFilter,
                    List.of(EquipmentType.LINE, EquipmentType.TWO_WINDINGS_TRANSFORMER, EquipmentType.GENERATOR), loader))
                    .as("result").containsExactlyInAnyOrderEntriesOf(Map.of(EquipmentType.LINE, List.of("line1"), EquipmentType.TWO_WINDINGS_TRANSFORMER, List.of("trf1")));

                // check some interactions
                Mockito.verify(filterLine, Mockito.atLeastOnce()).getEquipmentType();
                Mockito.verify(filterLine, Mockito.atLeastOnce()).getId();
                Mockito.verify(filterTrans, Mockito.atLeastOnce()).getEquipmentType();
                Mockito.verify(filterTrans, Mockito.atLeastOnce()).getId();
                Mockito.verify(line1, Mockito.atLeastOnce()).getId();
                Mockito.verify(trf1, Mockito.atLeastOnce()).getId();
                Mockito.verifyNoMoreInteractions(filterLine, filterTrans, network, line1, trf1);
                mockedFU.verify(() -> FiltersUtils.getIdentifiables(any(ExpertFilter.class), eq(network), eq(loader)), Mockito.atLeastOnce());
            }
        }
    }

    @Nested
    @DisplayName("buildGenericFilterRule(...)")
    class BuildGenericFilterRuleTests {

        @Test
        void testSameEquipmentType() {
            List<UUID> filterUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
            AbstractFilter filter1 = Mockito.mock(AbstractFilter.class);
            AbstractFilter filter2 = Mockito.mock(AbstractFilter.class);
            when(filter1.getEquipmentType()).thenReturn(EquipmentType.LINE);
            when(filter2.getEquipmentType()).thenReturn(EquipmentType.LINE);
            when(filter1.getId()).thenReturn(filterUuids.get(0));
            when(filter2.getId()).thenReturn(filterUuids.get(1));

            List<AbstractFilter> filters = Arrays.asList(filter1, filter2);
            assertNotNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.LINE));
        }

        @Test
        void testSubstationAndVoltageLevelOnAnyEquipmentType() {
            List<UUID> filterUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
            AbstractFilter filter1 = Mockito.mock(AbstractFilter.class);
            AbstractFilter filter2 = Mockito.mock(AbstractFilter.class);
            when(filter1.getEquipmentType()).thenReturn(EquipmentType.VOLTAGE_LEVEL);
            when(filter2.getEquipmentType()).thenReturn(EquipmentType.SUBSTATION);
            when(filter1.getId()).thenReturn(filterUuids.get(0));
            when(filter2.getId()).thenReturn(filterUuids.get(1));

            List<AbstractFilter> filters = Arrays.asList(filter1, filter2);
            assertNotNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.LINE));
            assertNotNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.GENERATOR));
            assertNotNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.TWO_WINDINGS_TRANSFORMER));
        }

        @Test
        void testNotSameEquipmentType() {
            List<UUID> filterUuids = List.of(UUID.randomUUID(), UUID.randomUUID());
            AbstractFilter filter1 = Mockito.mock(AbstractFilter.class);
            AbstractFilter filter2 = Mockito.mock(AbstractFilter.class);
            when(filter1.getEquipmentType()).thenReturn(EquipmentType.LINE);
            when(filter2.getEquipmentType()).thenReturn(EquipmentType.TWO_WINDINGS_TRANSFORMER);
            when(filter1.getId()).thenReturn(filterUuids.get(0));
            when(filter2.getId()).thenReturn(filterUuids.get(1));

            List<AbstractFilter> filters = Arrays.asList(filter1, filter2);
            assertNull(GlobalFilterUtils.buildGenericFilterRule(filters, EquipmentType.GENERATOR));
        }
    }
}
