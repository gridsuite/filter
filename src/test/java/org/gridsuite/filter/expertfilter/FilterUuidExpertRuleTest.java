package org.gridsuite.filter.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.expertrule.FilterUuidExpertRule;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class FilterUuidExpertRuleTest {
    private static final UUID FILTER_GENERATOR_1_UUID = UUID.fromString("7928181c-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_GENERATOR_2_UUID = UUID.fromString("7928181d-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_GENERATOR_1_UUID = UUID.fromString("7928181e-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_GENERATOR_2_UUID = UUID.fromString("7928181f-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_SUBSTATION_GENERATOR_1_UUID = UUID.fromString("7222181e-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_SUBSTATION_GENERATOR_2_UUID = UUID.fromString("7111181f-7977-4592-ba19-88027e4254e4");

    private static final UUID FILTER_LOAD_1_UUID = UUID.fromString("1928181c-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_LOAD_2_UUID = UUID.fromString("1928181d-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_LOAD_1_UUID = UUID.fromString("1928181e-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_LOAD_2_UUID = UUID.fromString("1928181f-7977-4592-ba19-88027e4254e4");

    private static final UUID FILTER_BATTERY_1_UUID = UUID.fromString("2928181c-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_BATTERY_2_UUID = UUID.fromString("2928181d-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_BATTERY_1_UUID = UUID.fromString("2928181e-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_BATTERY_2_UUID = UUID.fromString("2928181f-7977-4592-ba19-88027e4254e4");

    private static final UUID FILTER_SHUNT_COMPENSATOR_1_UUID = UUID.fromString("3928181c-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_SHUNT_COMPENSATOR_2_UUID = UUID.fromString("3928181d-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_SHUNT_COMPENSATOR_1_UUID = UUID.fromString("3928181e-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_SHUNT_COMPENSATOR_2_UUID = UUID.fromString("3928181f-7977-4592-ba19-88027e4254e4");

    private static final UUID FILTER_LINE_1_UUID = UUID.fromString("49281810-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_LINE_2_UUID = UUID.fromString("49281811-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_1_LINE_1_UUID = UUID.fromString("49281812-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_2_LINE_1_UUID = UUID.fromString("49281813-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_1_LINE_2_UUID = UUID.fromString("49281814-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_2_LINE_2_UUID = UUID.fromString("49281815-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_SUBSTATION_1_LINE_1_UUID = UUID.fromString("49281111-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_SUBSTATION_2_LINE_1_UUID = UUID.fromString("49281222-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_SUBSTATION_1_LINE_2_UUID = UUID.fromString("49281333-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_SUBSTATION_2_LINE_2_UUID = UUID.fromString("49281444-7977-4592-ba19-88027e4254e4");

    private static final UUID FILTER_DANGLING_LINE_1_UUID = UUID.fromString("18273121-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_DANGLING_LINE_2_UUID = UUID.fromString("18273122-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_DANGLING_LINE_1_UUID = UUID.fromString("18273123-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_DANGLING_LINE_2_UUID = UUID.fromString("18273124-7977-4592-ba19-88027e4254e4");

    private static final UUID FILTER_HVDC_LINE_1_UUID = UUID.fromString("65432936-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_HVDC_LINE_2_UUID = UUID.fromString("65432937-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_1_HVDC_LINE_1_UUID = UUID.fromString("65432938-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_2_HVDC_LINE_1_UUID = UUID.fromString("65432939-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_1_HVDC_LINE_2_UUID = UUID.fromString("65432940-7977-4592-ba19-88027e4254e4");
    private static final UUID FILTER_VOLTAGE_LEVEL_2_HVDC_LINE_2_UUID = UUID.fromString("65432941-7977-4592-ba19-88027e4254e4");

    private FilterLoader filterLoader;

    @BeforeEach
    void setUp() {
        filterLoader = new FilterLoader() {
            @Override
            public List<AbstractFilter> getFilters(List <UUID> uuids) {
                return List.of();
            }

            @Override
            public Optional<AbstractFilter> getFilter(UUID uuid) {
                return Optional.empty();
            }
        };
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class<Throwable> expectedException) {
        FilterUuidExpertRule rule = FilterUuidExpertRule.builder().operator(operator).field(field).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForTestWithException() {
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(generator.getTerminal()).thenReturn(terminal);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);

        DanglingLine danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getType()).thenReturn(IdentifiableType.DANGLING_LINE);

        return Stream.of(
            // --- Test an unsupported field for each equipment --- //
            Arguments.of(IS, FieldType.P0, generator, PowsyblException.class),
            Arguments.of(IS, FieldType.RATED_S, load, PowsyblException.class),
            Arguments.of(IS, FieldType.MIN_P, shuntCompensator, PowsyblException.class),
            Arguments.of(IS, FieldType.HIGH_VOLTAGE_LIMIT, battery, PowsyblException.class),
            Arguments.of(IS, FieldType.MARGINAL_COST, line, PowsyblException.class),
            Arguments.of(IS, FieldType.MARGINAL_COST, danglingLine, PowsyblException.class),

            // --- Test an unsupported operator for this rule type --- //
            Arguments.of(EQUALS, FieldType.ID, generator, PowsyblException.class),
            Arguments.of(BEGINS_WITH, FieldType.VOLTAGE_LEVEL_ID, generator, PowsyblException.class)
        );
    }

    private void mockGetFilterEquipments(MockedStatic<FilterServiceUtils> filterServiceUtilsMockedStatic, Network network, UUID filterUuid, IdentifiableAttributes identifiableAttributes) {
        filterServiceUtilsMockedStatic.when(() -> FilterServiceUtils.getFilterEquipmentsFromUuid(eq(network), eq(filterUuid), any(FilterLoader.class)))
            .thenReturn(List.of(new FilterEquipments(filterUuid, List.of(identifiableAttributes), null)));
    }

    private void initMockFilters(Network network, MockedStatic<FilterServiceUtils> filtersUtilsMock) {
        // Generator
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_GENERATOR_1_UUID, new IdentifiableAttributes("ID1", IdentifiableType.GENERATOR, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_GENERATOR_2_UUID, new IdentifiableAttributes("ID2", IdentifiableType.GENERATOR, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_GENERATOR_1_UUID, new IdentifiableAttributes("VL1", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_GENERATOR_2_UUID, new IdentifiableAttributes("VL2", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_SUBSTATION_GENERATOR_1_UUID, new IdentifiableAttributes("SUBST1", IdentifiableType.SUBSTATION, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_SUBSTATION_GENERATOR_2_UUID, new IdentifiableAttributes("SUBST2", IdentifiableType.SUBSTATION, 100D));

        // Load
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_LOAD_1_UUID, new IdentifiableAttributes("ID1", IdentifiableType.LOAD, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_LOAD_2_UUID, new IdentifiableAttributes("ID2", IdentifiableType.LOAD, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_LOAD_1_UUID, new IdentifiableAttributes("VL1", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_LOAD_2_UUID, new IdentifiableAttributes("VL2", IdentifiableType.VOLTAGE_LEVEL, 100D));

        // Battery
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_BATTERY_1_UUID, new IdentifiableAttributes("ID1", IdentifiableType.BATTERY, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_BATTERY_2_UUID, new IdentifiableAttributes("ID2", IdentifiableType.BATTERY, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_BATTERY_1_UUID, new IdentifiableAttributes("VL1", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_BATTERY_2_UUID, new IdentifiableAttributes("VL2", IdentifiableType.VOLTAGE_LEVEL, 100D));

        // Shunt compensator
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_SHUNT_COMPENSATOR_1_UUID, new IdentifiableAttributes("ID1", IdentifiableType.SHUNT_COMPENSATOR, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_SHUNT_COMPENSATOR_2_UUID, new IdentifiableAttributes("ID2", IdentifiableType.SHUNT_COMPENSATOR, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_SHUNT_COMPENSATOR_1_UUID, new IdentifiableAttributes("VL1", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_SHUNT_COMPENSATOR_2_UUID, new IdentifiableAttributes("VL2", IdentifiableType.VOLTAGE_LEVEL, 100D));

        // Line
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_LINE_1_UUID, new IdentifiableAttributes("ID1", IdentifiableType.LINE, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_LINE_2_UUID, new IdentifiableAttributes("ID2", IdentifiableType.LINE, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_1_LINE_1_UUID, new IdentifiableAttributes("VL11", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_2_LINE_1_UUID, new IdentifiableAttributes("VL21", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_1_LINE_2_UUID, new IdentifiableAttributes("VL12", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_2_LINE_2_UUID, new IdentifiableAttributes("VL22", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_SUBSTATION_1_LINE_1_UUID, new IdentifiableAttributes("SUBST1", IdentifiableType.SUBSTATION, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_SUBSTATION_2_LINE_1_UUID, new IdentifiableAttributes("SUBST2", IdentifiableType.SUBSTATION, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_SUBSTATION_1_LINE_2_UUID, new IdentifiableAttributes("SUBST3", IdentifiableType.SUBSTATION, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_SUBSTATION_2_LINE_2_UUID, new IdentifiableAttributes("SUBST4", IdentifiableType.SUBSTATION, 100D));

        // Dangling Lines
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_DANGLING_LINE_1_UUID, new IdentifiableAttributes("ID1", IdentifiableType.DANGLING_LINE, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_DANGLING_LINE_2_UUID, new IdentifiableAttributes("ID2", IdentifiableType.DANGLING_LINE, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_DANGLING_LINE_1_UUID, new IdentifiableAttributes("VL1", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_DANGLING_LINE_2_UUID, new IdentifiableAttributes("VL2", IdentifiableType.VOLTAGE_LEVEL, 100D));

        // Hvdc Line
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_HVDC_LINE_1_UUID, new IdentifiableAttributes("ID1", IdentifiableType.HVDC_LINE, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_HVDC_LINE_2_UUID, new IdentifiableAttributes("ID2", IdentifiableType.HVDC_LINE, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_1_HVDC_LINE_1_UUID, new IdentifiableAttributes("VL11", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_2_HVDC_LINE_1_UUID, new IdentifiableAttributes("VL21", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_1_HVDC_LINE_2_UUID, new IdentifiableAttributes("VL12", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_2_HVDC_LINE_2_UUID, new IdentifiableAttributes("VL22", IdentifiableType.VOLTAGE_LEVEL, 100D));

        // Dangling Lines
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_DANGLING_LINE_1_UUID, new IdentifiableAttributes("ID1", IdentifiableType.DANGLING_LINE, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_DANGLING_LINE_2_UUID, new IdentifiableAttributes("ID2", IdentifiableType.DANGLING_LINE, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_DANGLING_LINE_1_UUID, new IdentifiableAttributes("VL1", IdentifiableType.VOLTAGE_LEVEL, 100D));
        mockGetFilterEquipments(filtersUtilsMock, network, FILTER_VOLTAGE_LEVEL_DANGLING_LINE_2_UUID, new IdentifiableAttributes("VL2", IdentifiableType.VOLTAGE_LEVEL, 100D));
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForDanglingLinesTest",
        "provideArgumentsForLineTest",
        "provideArgumentsForHvdcTest",
        "provideArgumentsForLineTest",
        "provideArgumentsForDanglingLinesTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, String value, Set<String> values, Identifiable<?> equipment, boolean expected) {
        try (MockedStatic<FilterServiceUtils> filterServiceUtilsMockedStatic = Mockito.mockStatic(FilterServiceUtils.class)) {
            initMockFilters(equipment.getNetwork(), filterServiceUtilsMockedStatic);
            FilterUuidExpertRule rule = FilterUuidExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
            assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
        }
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {
        Network network = Mockito.mock(Network.class);

        Generator gen1 = Mockito.mock(Generator.class);
        Mockito.when(gen1.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(gen1.getNetwork()).thenReturn(network);
        Generator gen2 = Mockito.mock(Generator.class);
        Mockito.when(gen2.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(gen2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(gen1.getId()).thenReturn("ID1");
        Mockito.when(gen2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Substation substation1 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Mockito.when(substation1.getId()).thenReturn("SUBST1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(gen1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Mockito.when(substation2.getId()).thenReturn("SUBST2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(gen2.getTerminal()).thenReturn(terminal2);
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));

        return Stream.of(
            // --- IS_PART_OF --- //
            // Common fields
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_GENERATOR_1_UUID.toString()), gen1, true),
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_GENERATOR_2_UUID.toString()), gen2, true),
            // VoltageLevel fields
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_GENERATOR_1_UUID.toString()), gen1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_GENERATOR_2_UUID.toString()), gen2, true),
            // Substation fields
            Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID, null, Set.of(FILTER_SUBSTATION_GENERATOR_1_UUID.toString()), gen1, true),
            Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID, null, Set.of(FILTER_SUBSTATION_GENERATOR_2_UUID.toString()), gen2, true),

            // --- IS_NOT_PART_OF --- //
            // Common fields
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_GENERATOR_1_UUID.toString()), gen2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_GENERATOR_2_UUID.toString()), gen1, true),
            // VoltageLevel fields
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_GENERATOR_2_UUID.toString()), gen1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_GENERATOR_1_UUID.toString()), gen2, true),
            // Substation fields
            Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID, null, Set.of(FILTER_SUBSTATION_GENERATOR_2_UUID.toString()), gen1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID, null, Set.of(FILTER_SUBSTATION_GENERATOR_1_UUID.toString()), gen2, true)
            );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {
        Network network = Mockito.mock(Network.class);

        Load load1 = Mockito.mock(Load.class);
        Mockito.when(load1.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load1.getNetwork()).thenReturn(network);
        Load load2 = Mockito.mock(Load.class);
        Mockito.when(load2.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(load1.getId()).thenReturn("ID1");
        Mockito.when(load2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(load1.getTerminal()).thenReturn(terminal1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(load2.getTerminal()).thenReturn(terminal2);

        return Stream.of(
            // --- IS_PART_OF --- //
            // Common fields
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_LOAD_1_UUID.toString()), load1, true),
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_LOAD_2_UUID.toString()), load2, true),
            // VoltageLevel fields
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_LOAD_1_UUID.toString()), load1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_LOAD_2_UUID.toString()), load2, true),

            // --- IS_NOT_PART_OF --- //
            // Common fields
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_LOAD_1_UUID.toString()), load2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_LOAD_2_UUID.toString()), load1, true),
            // VoltageLevel fields
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_LOAD_2_UUID.toString()), load1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_LOAD_1_UUID.toString()), load2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {
        Network network = Mockito.mock(Network.class);

        Battery battery1 = Mockito.mock(Battery.class);
        Mockito.when(battery1.getType()).thenReturn(IdentifiableType.BATTERY);
        Mockito.when(battery1.getNetwork()).thenReturn(network);
        Battery battery2 = Mockito.mock(Battery.class);
        Mockito.when(battery2.getType()).thenReturn(IdentifiableType.BATTERY);
        Mockito.when(battery2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(battery1.getId()).thenReturn("ID1");
        Mockito.when(battery2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(battery1.getTerminal()).thenReturn(terminal1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(battery2.getTerminal()).thenReturn(terminal2);

        return Stream.of(
            // --- IS_PART_OF --- //
            // Common fields
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_BATTERY_1_UUID.toString()), battery1, true),
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_BATTERY_2_UUID.toString()), battery2, true),
            // VoltageLevel fields
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_BATTERY_1_UUID.toString()), battery1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_BATTERY_2_UUID.toString()), battery2, true),

            // --- IS_NOT_PART_OF --- //
            // Common fields
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_BATTERY_1_UUID.toString()), battery2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_BATTERY_2_UUID.toString()), battery1, true),
            // VoltageLevel fields
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_BATTERY_2_UUID.toString()), battery1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_BATTERY_1_UUID.toString()), battery2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {
        Network network = Mockito.mock(Network.class);

        ShuntCompensator shuntCompensator1 = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator1.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        Mockito.when(shuntCompensator1.getNetwork()).thenReturn(network);
        ShuntCompensator shuntCompensator2 = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator2.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        Mockito.when(shuntCompensator2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(shuntCompensator1.getId()).thenReturn("ID1");
        Mockito.when(shuntCompensator2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(shuntCompensator1.getTerminal()).thenReturn(terminal1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(shuntCompensator2.getTerminal()).thenReturn(terminal2);

        return Stream.of(
            // --- IS_PART_OF --- //
            // Common fields
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_SHUNT_COMPENSATOR_1_UUID.toString()), shuntCompensator1, true),
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_SHUNT_COMPENSATOR_2_UUID.toString()), shuntCompensator2, true),
            // VoltageLevel fields
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_SHUNT_COMPENSATOR_1_UUID.toString()), shuntCompensator1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_SHUNT_COMPENSATOR_2_UUID.toString()), shuntCompensator2, true),

            // --- IS_NOT_PART_OF --- //
            // Common fields
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_SHUNT_COMPENSATOR_1_UUID.toString()), shuntCompensator2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_SHUNT_COMPENSATOR_2_UUID.toString()), shuntCompensator1, true),
            // VoltageLevel fields
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_SHUNT_COMPENSATOR_2_UUID.toString()), shuntCompensator1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_SHUNT_COMPENSATOR_1_UUID.toString()), shuntCompensator2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForLineTest() {
        Network network = Mockito.mock(Network.class);

        Line line1 = Mockito.mock(Line.class);
        Mockito.when(line1.getType()).thenReturn(IdentifiableType.LINE);
        Mockito.when(line1.getNetwork()).thenReturn(network);
        Line line2 = Mockito.mock(Line.class);
        Mockito.when(line2.getType()).thenReturn(IdentifiableType.LINE);
        Mockito.when(line2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(line1.getId()).thenReturn("ID1");
        Mockito.when(line2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1Line1 = Mockito.mock(VoltageLevel.class);
        Substation substation1Line1 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel1Line1.getId()).thenReturn("VL11");
        Mockito.when(substation1Line1.getId()).thenReturn("SUBST1");
        Terminal terminal1Line1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1Line1.getVoltageLevel()).thenReturn(voltageLevel1Line1);
        Mockito.when(line1.getTerminal(TwoSides.ONE)).thenReturn(terminal1Line1);
        Mockito.when(voltageLevel1Line1.getSubstation()).thenReturn(Optional.of(substation1Line1));

        VoltageLevel voltageLevel2Line1 = Mockito.mock(VoltageLevel.class);
        Substation substation2Line1 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel2Line1.getId()).thenReturn("VL21");
        Mockito.when(substation2Line1.getId()).thenReturn("SUBST2");
        Terminal terminal2Line1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2Line1.getVoltageLevel()).thenReturn(voltageLevel2Line1);
        Mockito.when(line1.getTerminal(TwoSides.TWO)).thenReturn(terminal2Line1);
        Mockito.when(voltageLevel2Line1.getSubstation()).thenReturn(Optional.of(substation2Line1));

        VoltageLevel voltageLevel1Line2 = Mockito.mock(VoltageLevel.class);
        Substation substation1Line2 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel1Line2.getId()).thenReturn("VL12");
        Mockito.when(substation1Line2.getId()).thenReturn("SUBST3");
        Terminal terminal1Line2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1Line2.getVoltageLevel()).thenReturn(voltageLevel1Line2);
        Mockito.when(line2.getTerminal(TwoSides.ONE)).thenReturn(terminal1Line2);
        Mockito.when(voltageLevel1Line2.getSubstation()).thenReturn(Optional.of(substation1Line2));

        VoltageLevel voltageLevel2Line2 = Mockito.mock(VoltageLevel.class);
        Substation substation2Line2 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel2Line2.getId()).thenReturn("VL22");
        Mockito.when(substation2Line2.getId()).thenReturn("SUBST4");
        Terminal terminal2Line2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2Line2.getVoltageLevel()).thenReturn(voltageLevel2Line2);
        Mockito.when(line2.getTerminal(TwoSides.TWO)).thenReturn(terminal2Line2);
        Mockito.when(voltageLevel2Line2.getSubstation()).thenReturn(Optional.of(substation2Line2));

        return Stream.of(
            // --- IS_PART_OF --- //
            // Common fields
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_LINE_1_UUID.toString()), line1, true),
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_LINE_2_UUID.toString()), line2, true),
            // VoltageLevel fields
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, null, Set.of(FILTER_VOLTAGE_LEVEL_1_LINE_1_UUID.toString()), line1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, null, Set.of(FILTER_VOLTAGE_LEVEL_2_LINE_1_UUID.toString()), line1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, null, Set.of(FILTER_VOLTAGE_LEVEL_1_LINE_2_UUID.toString()), line2, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, null, Set.of(FILTER_VOLTAGE_LEVEL_2_LINE_2_UUID.toString()), line2, true),
            // Substation fields
            Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID_1, null, Set.of(FILTER_SUBSTATION_1_LINE_1_UUID.toString()), line1, true),
            Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID_2, null, Set.of(FILTER_SUBSTATION_2_LINE_1_UUID.toString()), line1, true),
            Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID_1, null, Set.of(FILTER_SUBSTATION_1_LINE_2_UUID.toString()), line2, true),
            Arguments.of(IS_PART_OF, FieldType.SUBSTATION_ID_2, null, Set.of(FILTER_SUBSTATION_2_LINE_2_UUID.toString()), line2, true),

            // --- IS_NOT_PART_OF --- //
            // Common fields
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_LINE_1_UUID.toString()), line2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_LINE_2_UUID.toString()), line1, true),
            // VoltageLevel fields
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, null, Set.of(FILTER_VOLTAGE_LEVEL_1_LINE_1_UUID.toString()), line2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, null, Set.of(FILTER_VOLTAGE_LEVEL_2_LINE_1_UUID.toString()), line2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, null, Set.of(FILTER_VOLTAGE_LEVEL_1_LINE_2_UUID.toString()), line1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, null, Set.of(FILTER_VOLTAGE_LEVEL_2_LINE_2_UUID.toString()), line1, true),
            // Substation fields
            Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID_1, null, Set.of(FILTER_SUBSTATION_1_LINE_1_UUID.toString()), line2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID_2, null, Set.of(FILTER_SUBSTATION_2_LINE_1_UUID.toString()), line2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID_1, null, Set.of(FILTER_SUBSTATION_1_LINE_2_UUID.toString()), line1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.SUBSTATION_ID_2, null, Set.of(FILTER_SUBSTATION_2_LINE_2_UUID.toString()), line1, true)
            );
    }

    private static Stream<Arguments> provideArgumentsForHvdcTest() {
        Network network = Mockito.mock(Network.class);

        HvdcLine hvdcLine1 = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine1.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        Mockito.when(hvdcLine1.getNetwork()).thenReturn(network);
        HvdcLine hvdcLine2 = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine2.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        Mockito.when(hvdcLine2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(hvdcLine1.getId()).thenReturn("ID1");
        Mockito.when(hvdcLine2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1Line1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1Line1.getId()).thenReturn("VL11");
        Terminal terminal1Line1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1Line1.getVoltageLevel()).thenReturn(voltageLevel1Line1);
        HvdcConverterStation converterStation1 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation1.getTerminal()).thenReturn(terminal1Line1);
        Mockito.when(hvdcLine1.getConverterStation1()).thenReturn(converterStation1);
        VoltageLevel voltageLevel2Line1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2Line1.getId()).thenReturn("VL21");
        Terminal terminal2Line1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2Line1.getVoltageLevel()).thenReturn(voltageLevel2Line1);
        HvdcConverterStation converterStation2 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation2.getTerminal()).thenReturn(terminal2Line1);
        Mockito.when(hvdcLine1.getConverterStation2()).thenReturn(converterStation2);

        VoltageLevel voltageLevel1Line2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1Line2.getId()).thenReturn("VL12");
        Terminal terminal1Line2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1Line2.getVoltageLevel()).thenReturn(voltageLevel1Line2);
        HvdcConverterStation converterStation3 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation3.getTerminal()).thenReturn(terminal1Line2);
        Mockito.when(hvdcLine2.getConverterStation1()).thenReturn(converterStation3);
        VoltageLevel voltageLevel2Line2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2Line2.getId()).thenReturn("VL22");
        Terminal terminal2Line2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2Line2.getVoltageLevel()).thenReturn(voltageLevel2Line2);
        HvdcConverterStation converterStation4 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation4.getTerminal()).thenReturn(terminal2Line2);
        Mockito.when(hvdcLine2.getConverterStation2()).thenReturn(converterStation4);

        return Stream.of(
            // --- IS_PART_OF --- //
            // Common fields
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_HVDC_LINE_1_UUID.toString()), hvdcLine1, true),
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_HVDC_LINE_2_UUID.toString()), hvdcLine2, true),
            // VoltageLevel fields
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, null, Set.of(FILTER_VOLTAGE_LEVEL_1_HVDC_LINE_1_UUID.toString()), hvdcLine1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, null, Set.of(FILTER_VOLTAGE_LEVEL_2_HVDC_LINE_1_UUID.toString()), hvdcLine1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, null, Set.of(FILTER_VOLTAGE_LEVEL_1_HVDC_LINE_2_UUID.toString()), hvdcLine2, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, null, Set.of(FILTER_VOLTAGE_LEVEL_2_HVDC_LINE_2_UUID.toString()), hvdcLine2, true),

            // --- IS_NOT_PART_OF --- //
            // Common fields
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_HVDC_LINE_1_UUID.toString()), hvdcLine2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_HVDC_LINE_2_UUID.toString()), hvdcLine1, true),
            // VoltageLevel fields
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, null, Set.of(FILTER_VOLTAGE_LEVEL_1_HVDC_LINE_1_UUID.toString()), hvdcLine2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, null, Set.of(FILTER_VOLTAGE_LEVEL_2_HVDC_LINE_1_UUID.toString()), hvdcLine2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_1, null, Set.of(FILTER_VOLTAGE_LEVEL_1_HVDC_LINE_2_UUID.toString()), hvdcLine1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID_2, null, Set.of(FILTER_VOLTAGE_LEVEL_2_HVDC_LINE_2_UUID.toString()), hvdcLine1, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForDanglingLinesTest() {
        Network network = Mockito.mock(Network.class);

        DanglingLine danglingLine1 = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine1.getType()).thenReturn(IdentifiableType.DANGLING_LINE);
        Mockito.when(danglingLine1.getNetwork()).thenReturn(network);
        DanglingLine danglingLine2 = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine2.getType()).thenReturn(IdentifiableType.DANGLING_LINE);
        Mockito.when(danglingLine2.getNetwork()).thenReturn(network);

        // Common fields
        Mockito.when(danglingLine1.getId()).thenReturn("ID1");
        Mockito.when(danglingLine2.getId()).thenReturn("ID2");

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getId()).thenReturn("VL1");
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(danglingLine1.getTerminal()).thenReturn(terminal1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getId()).thenReturn("VL2");
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(danglingLine2.getTerminal()).thenReturn(terminal2);

        return Stream.of(
            // --- IS_PART_OF --- //
            // Common fields
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_DANGLING_LINE_1_UUID.toString()), danglingLine1, true),
            Arguments.of(IS_PART_OF, FieldType.ID, null, Set.of(FILTER_DANGLING_LINE_2_UUID.toString()), danglingLine2, true),
            // VoltageLevel fields
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_DANGLING_LINE_1_UUID.toString()), danglingLine1, true),
            Arguments.of(IS_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_DANGLING_LINE_2_UUID.toString()), danglingLine2, true),

            // --- IS_NOT_PART_OF --- //
            // Common fields
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_DANGLING_LINE_1_UUID.toString()), danglingLine2, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.ID, null, Set.of(FILTER_DANGLING_LINE_2_UUID.toString()), danglingLine1, true),
            // VoltageLevel fields
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_1_LINE_2_UUID.toString()), danglingLine1, true),
            Arguments.of(IS_NOT_PART_OF, FieldType.VOLTAGE_LEVEL_ID, null, Set.of(FILTER_VOLTAGE_LEVEL_GENERATOR_1_UUID.toString()), danglingLine2, true)
        );
    }
}
