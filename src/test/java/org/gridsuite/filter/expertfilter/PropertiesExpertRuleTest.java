package org.gridsuite.filter.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.expertrule.PropertiesExpertRule;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertiesExpertRuleTest {
    private FilterLoader filterLoader;

    static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getProperty("cvgRegion")).thenReturn("LILLE");

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Terminal terminal = Mockito.mock(Terminal.class);

        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.getId()).thenReturn("GEN");
        Mockito.when(generator.getProperty("codeOI")).thenReturn("33");
        Mockito.when(generator.getProperty("north")).thenReturn("north");
        Mockito.when(generator.getTerminal()).thenReturn(terminal);

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        return Stream.of(
                // --- Test an unsupported field for some equipment --- //
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, voltageLevel, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, generator, "region", List.of("north"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, load, "region", List.of("north"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, shuntCompensator, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, line, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, battery, "region", List.of("east"), PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(IS, FieldType.FREE_PROPERTIES, generator, "codeOI", List.of("33"), PowsyblException.class),
                Arguments.of(CONTAINS, FieldType.SUBSTATION_PROPERTIES, generator, "cvgRegion", List.of("LILLE"), PowsyblException.class)
        );
    }

    private static Stream<Arguments> provideArgumentsForSubstationTest() {

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getType()).thenReturn(IdentifiableType.SUBSTATION);
        Mockito.when(substation.getProperty("cvgRegion")).thenReturn("LILLE");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("LILLE", "PARIS"), substation, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.getProperty("CodeOI")).thenReturn("11");

        Terminal terminal = Mockito.mock(Terminal.class);

        Mockito.when(generator.getTerminal()).thenReturn(terminal);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel.getProperty("CodeOI")).thenReturn("22");
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);

        Mockito.when(substation.getProperty("regionCSV")).thenReturn("LILLE");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("11"), generator, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("22"), generator, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("LILLE"), generator, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("PARIS"), generator, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), generator, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("11"), generator, false)
   );
    }

    private static Stream<Arguments> provideArgumentsForLineTest() {

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Terminal terminal2 = Mockito.mock(Terminal.class);

        Mockito.when(line.getTerminal1()).thenReturn(terminal1);
        Mockito.when(line.getTerminal2()).thenReturn(terminal2);
        Mockito.when(line.getProperty("region")).thenReturn("north");

        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel1.getProperty("CodeOI")).thenReturn("22");
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel2.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        Substation substation1 = Mockito.mock(Substation.class);
        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel1.getNullableSubstation()).thenReturn(substation1);
        Mockito.when(voltageLevel2.getNullableSubstation()).thenReturn(substation2);
        Mockito.when(substation1.getProperty("regionCSV")).thenReturn("LILLE");
        Mockito.when(substation2.getProperty("regionCSV")).thenReturn("PARIS");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "region", List.of("north"), line, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("LILLE"), line, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("PARIS"), line, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), line, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), line, true)
   );
    }

    @BeforeEach
    public void setUp() {
        filterLoader = uuids -> null;
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, String propertyName, List<String> propertyValues, Class expectedException) {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operator(operator).field(field).propertyName(propertyName).propertyValues(propertyValues).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load.getProperty("propertyNameLoad")).thenReturn("propertyValueLoad");

        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getProperty("propertyNameSubstation")).thenReturn("propertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad"), load, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), load, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), load, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        Mockito.when(shuntCompensator.getProperty("propertyNameSC")).thenReturn("propertyValueSC");

        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(shuntCompensator.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getProperty("propertyNameSubstation")).thenReturn("propertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC"), shuntCompensator, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), shuntCompensator, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), shuntCompensator, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForTwoWindingTransformerTest() {

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twoWindingsTransformer.getProperty("propertyNameTWT")).thenReturn("propertyValueTWT");

        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(twoWindingsTransformer.getTerminal1()).thenReturn(terminal1);
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel1.getProperty("CodeOI")).thenReturn("22");
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(twoWindingsTransformer.getTerminal2()).thenReturn(terminal2);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel2.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        Substation substation1 = Mockito.mock(Substation.class);
        Substation substation2 = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel1.getNullableSubstation()).thenReturn(substation1);
        Mockito.when(voltageLevel2.getNullableSubstation()).thenReturn(substation2);
        Mockito.when(substation1.getProperty("regionCSV")).thenReturn("LILLE");
        Mockito.when(substation2.getProperty("regionCSV")).thenReturn("PARIS");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT", "propertyValueTWT"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValue"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("LILLE"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("PARIS"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("PARIS"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("LILLE"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), twoWindingsTransformer, false)
        );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForSubstationTest",
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForTwoWindingTransformerTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForLineTest",
    })

    void testEvaluateRule(OperatorType operator, FieldType field, String propertyName, List<String> propertyValues, Identifiable<?> equipment, boolean expected) {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operator(operator).field(field).propertyName(propertyName).propertyValues(propertyValues).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }
}
