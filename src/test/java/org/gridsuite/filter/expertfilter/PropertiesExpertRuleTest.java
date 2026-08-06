package org.gridsuite.filter.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.expertrule.PropertiesExpertRule;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class PropertiesExpertRuleTest {
    private FilterLoader filterLoader;

    @BeforeEach
    void setUp() {
        filterLoader = uuids -> null;
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, String propertyName, List<String> propertyValues, Class<Throwable> expectedException) {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operator(operator).field(field).propertyName(propertyName).propertyValues(propertyValues).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    @Test
    void testPropertiesValue() {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operator(IN).field(FieldType.FREE_PROPERTIES).propertyName("property")
            .propertyValues(Collections.singletonList("value1")).build();
        assertEquals(Collections.singletonList("value1"), rule.getPropertyValues());
        assertEquals("property", rule.getStringValue());
        assertEquals(FieldType.FREE_PROPERTIES, rule.getField());
        assertEquals(IN, rule.getOperator());
        rule = PropertiesExpertRule.builder().operator(NOT_IN).field(FieldType.FREE_PROPERTIES).propertyName("property2")
            .propertyValues(Collections.singletonList("value2")).build();
        assertEquals(Collections.singletonList("value2"), rule.getPropertyValues());
        assertEquals("property2", rule.getStringValue());
        assertEquals(FieldType.FREE_PROPERTIES, rule.getField());
        assertEquals(NOT_IN, rule.getOperator());
    }

    private static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = mock(Network.class);
        when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        Substation substation = mock(Substation.class);
        when(substation.getProperty("cvgRegion")).thenReturn("LILLE");

        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Terminal terminal = mock(Terminal.class);

        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);

        Line line = mock(Line.class);
        when(line.getType()).thenReturn(IdentifiableType.LINE);

        Generator generator = mock(Generator.class);
        when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        when(generator.getId()).thenReturn("GEN");
        when(generator.getProperty("codeOI")).thenReturn("33");
        when(generator.getProperty("north")).thenReturn("north");
        when(generator.getTerminal()).thenReturn(terminal);

        Load load = mock(Load.class);
        when(load.getType()).thenReturn(IdentifiableType.LOAD);

        ShuntCompensator shuntCompensator = mock(ShuntCompensator.class);
        when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Battery battery = mock(Battery.class);
        when(battery.getType()).thenReturn(IdentifiableType.BATTERY);

        TwoWindingsTransformer twoWindingsTransformer = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        StaticVarCompensator svar = mock(StaticVarCompensator.class);
        when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        ThreeWindingsTransformer threeWindingsTransformer = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        HvdcLine hvdcLine = mock(HvdcLine.class);
        when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);

        return Stream.of(
                // --- Test an unsupported field for some equipment --- //
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, voltageLevel, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, generator, "region", List.of("north"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, load, "region", List.of("north"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, shuntCompensator, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, line, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, battery, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, svar, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SERIE_REACTANCE, threeWindingsTransformer, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, hvdcLine, "region", List.of("east"), PowsyblException.class),

                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, voltageLevel, "region", List.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, generator, "region", List.of("north"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, load, "region", List.of("north"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, shuntCompensator, "region", List.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, line, "region", List.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, battery, "region", List.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, svar, "region", List.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, threeWindingsTransformer, "region", List.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, hvdcLine, "region", List.of("east"), PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(IS, FieldType.FREE_PROPERTIES, generator, "codeOI", List.of("33"), PowsyblException.class),
                Arguments.of(CONTAINS, FieldType.SUBSTATION_PROPERTIES, generator, "cvgRegion", List.of("LILLE"), PowsyblException.class)
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
        "provideArgumentsForStaticVarCompensatorTest",
        "provideArgumentsForBoundaryLineTest",
        "provideArgumentsForThreeWindingTransformerTest",
        "provideArgumentsForHvdcLineTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForVoltageLevelTest",
        "provideArgumentsForHvdcConverterStationTest",
        "provideArgumentsForNullSubstationTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, String propertyName, List<String> propertyValues, Identifiable<?> equipment, boolean expected) {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operator(operator).field(field).propertyName(propertyName).propertyValues(propertyValues).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    // Every equipment below is connected to a voltage level with no substation
    // (getNullableSubstation() returns null, a valid IIDM case). Before the fix
    // in ExpertFilterUtils, evaluating SUBSTATION_PROPERTIES on any of them
    // threw a NullPointerException instead of returning false/true.
    private static Stream<Arguments> provideArgumentsForNullSubstationTest() {

        VoltageLevel voltageLevelNoSubstation = mock(VoltageLevel.class);
        when(voltageLevelNoSubstation.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevelNoSubstation.getNullableSubstation()).thenReturn(null);

        Terminal terminalNoSubstation = mock(Terminal.class);
        when(terminalNoSubstation.getVoltageLevel()).thenReturn(voltageLevelNoSubstation);

        Line line = mock(Line.class);
        when(line.getType()).thenReturn(IdentifiableType.LINE);
        when(line.getTerminal1()).thenReturn(terminalNoSubstation);
        when(line.getTerminal2()).thenReturn(terminalNoSubstation);

        Load load = mock(Load.class);
        when(load.getType()).thenReturn(IdentifiableType.LOAD);
        when(load.getTerminal()).thenReturn(terminalNoSubstation);

        Generator generator = mock(Generator.class);
        when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        when(generator.getTerminal()).thenReturn(terminalNoSubstation);

        Battery battery = mock(Battery.class);
        when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        when(battery.getTerminal()).thenReturn(terminalNoSubstation);

        ShuntCompensator shuntCompensator = mock(ShuntCompensator.class);
        when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        when(shuntCompensator.getTerminal()).thenReturn(terminalNoSubstation);

        StaticVarCompensator svar = mock(StaticVarCompensator.class);
        when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);
        when(svar.getTerminal()).thenReturn(terminalNoSubstation);

        BoundaryLine boundaryLine = mock(BoundaryLine.class);
        when(boundaryLine.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);
        when(boundaryLine.getTerminal()).thenReturn(terminalNoSubstation);

        HvdcLine hvdcLine = mock(HvdcLine.class);
        when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        HvdcConverterStation converterStation1 = mock(HvdcConverterStation.class);
        when(converterStation1.getTerminal()).thenReturn(terminalNoSubstation);
        when(hvdcLine.getConverterStation1()).thenReturn(converterStation1);
        HvdcConverterStation converterStation2 = mock(HvdcConverterStation.class);
        when(converterStation2.getTerminal()).thenReturn(terminalNoSubstation);
        when(hvdcLine.getConverterStation2()).thenReturn(converterStation2);

        HvdcConverterStation standaloneConverterStation = mock(HvdcConverterStation.class);
        when(standaloneConverterStation.getType()).thenReturn(IdentifiableType.HVDC_CONVERTER_STATION);
        when(standaloneConverterStation.getTerminal()).thenReturn(terminalNoSubstation);

        return Stream.of(
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, voltageLevelNoSubstation, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, voltageLevelNoSubstation, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "region", null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "region", null, line, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "region", null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "region", null, line, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, load, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, load, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, generator, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, generator, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, battery, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, shuntCompensator, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, shuntCompensator, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, svar, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, boundaryLine, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, boundaryLine, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "region", null, hvdcLine, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "region", null, hvdcLine, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "region", null, hvdcLine, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "region", null, hvdcLine, true),

                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, standaloneConverterStation, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "region", null, standaloneConverterStation, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForSubstationTest() {

        Substation substation = mock(Substation.class);
        when(substation.getType()).thenReturn(IdentifiableType.SUBSTATION);
        when(substation.getProperty("cvgRegion")).thenReturn("LILLE");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("Lille", "PARIS"), substation, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("Paris"), substation, false),

                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("Lille", "PARIS"), substation, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("Paris"), substation, true),

                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "cvgRegion", null, substation, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "test", null, substation, false),

                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "cvgRegion", null, substation, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "test", null, substation, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator generator = mock(Generator.class);
        when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        when(generator.getProperty("CodeOI")).thenReturn("11");

        Terminal terminal = mock(Terminal.class);

        when(generator.getTerminal()).thenReturn(terminal);

        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel.getProperty("CodeOI")).thenReturn("22");
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);

        Substation substation = mock(Substation.class);
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);

        when(substation.getProperty("regionCSV")).thenReturn("LILLE");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("11"), generator, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("22"), generator, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), generator, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), generator, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), generator, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("11"), generator, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("11"), generator, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("22"), generator, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), generator, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), generator, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), generator, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("11"), generator, true),

                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "CodeOI", null, generator, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "test", null, generator, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "regionCSV", null, generator, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "test", null, generator, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, generator, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "test", null, generator, false),

                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "CodeOI", null, generator, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "test", null, generator, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "regionCSV", null, generator, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "test", null, generator, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, generator, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "test", null, generator, true)
   );
    }

    private static Stream<Arguments> provideArgumentsForLineTest() {

        Line line = mock(Line.class);
        when(line.getType()).thenReturn(IdentifiableType.LINE);
        Terminal terminal1 = mock(Terminal.class);
        Terminal terminal2 = mock(Terminal.class);

        when(line.getTerminal1()).thenReturn(terminal1);
        when(line.getTerminal2()).thenReturn(terminal2);
        when(line.getProperty("region")).thenReturn("north");

        VoltageLevel voltageLevel1 = mock(VoltageLevel.class);
        when(voltageLevel1.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel1.getProperty("CodeOI")).thenReturn("22");
        when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        VoltageLevel voltageLevel2 = mock(VoltageLevel.class);
        when(voltageLevel2.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel2.getProperty("CodeOI")).thenReturn("33");
        when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        Substation substation1 = mock(Substation.class);
        Substation substation2 = mock(Substation.class);
        when(voltageLevel1.getNullableSubstation()).thenReturn(substation1);
        when(voltageLevel2.getNullableSubstation()).thenReturn(substation2);
        when(substation1.getProperty("regionCSV")).thenReturn("LILLE");
        when(substation2.getProperty("regionCSV")).thenReturn("PARIS");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "region", List.of("north"), line, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "region", List.of("south"), line, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("Lille"), line, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("Paris"), line, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("Paris"), line, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("Lille"), line, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), line, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), line, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), line, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), line, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "region", List.of("north"), line, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "region", List.of("south"), line, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("Lille"), line, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("Paris"), line, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("Paris"), line, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("Lille"), line, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), line, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), line, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), line, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), line, true),
                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "region", null, line, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, line, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", null, line, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "b", null, line, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", null, line, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "c", null, line, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", null, line, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "d", null, line, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", null, line, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "e", null, line, false),
                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "region", null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, line, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "b", null, line, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "c", null, line, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "d", null, line, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "e", null, line, true)
   );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = mock(Load.class);
        when(load.getType()).thenReturn(IdentifiableType.LOAD);
        when(load.getProperty("propertyNameLoad")).thenReturn("PropertyValueLoad");

        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(load.getTerminal()).thenReturn(terminal);
        when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad"), load, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad2"), load, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), load, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), load, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), load, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), load, false),
                // --- IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad"), load, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad2"), load, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), load, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), load, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), load, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), load, true),
                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameLoad", null, load, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, load, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, load, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, load, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, load, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "c", null, load, false),
                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameLoad", null, load, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, load, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, load, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, load, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, load, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "c", null, load, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {

        ShuntCompensator shuntCompensator = mock(ShuntCompensator.class);
        when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        when(shuntCompensator.getProperty("propertyNameSC")).thenReturn("PropertyValueSC");

        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(shuntCompensator.getTerminal()).thenReturn(terminal);
        when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC"), shuntCompensator, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC1"), shuntCompensator, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), shuntCompensator, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), shuntCompensator, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), shuntCompensator, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), shuntCompensator, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC"), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC1"), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), shuntCompensator, true),
                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameSC", null, shuntCompensator, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, shuntCompensator, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, shuntCompensator, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, shuntCompensator, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, shuntCompensator, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "c", null, shuntCompensator, false),
                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameSC", null, shuntCompensator, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, shuntCompensator, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, shuntCompensator, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, shuntCompensator, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, shuntCompensator, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "c", null, shuntCompensator, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForTwoWindingTransformerTest() {

        TwoWindingsTransformer twoWindingsTransformer = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        when(twoWindingsTransformer.getProperty("propertyNameTWT")).thenReturn("PropertyValueTWT");

        Terminal terminal1 = mock(Terminal.class);
        when(twoWindingsTransformer.getTerminal1()).thenReturn(terminal1);
        VoltageLevel voltageLevel1 = mock(VoltageLevel.class);
        when(voltageLevel1.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel1.getProperty("CodeOI")).thenReturn("22");
        when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        Terminal terminal2 = mock(Terminal.class);
        when(twoWindingsTransformer.getTerminal2()).thenReturn(terminal2);
        VoltageLevel voltageLevel2 = mock(VoltageLevel.class);
        when(voltageLevel2.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel2.getProperty("CodeOI")).thenReturn("33");
        when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        Substation substation = mock(Substation.class);
        when(twoWindingsTransformer.getNullableSubstation()).thenReturn(substation);
        when(substation.getProperty("regionCSV")).thenReturn("LILLE");

        TwoWindingsTransformer transformerWithNullSub = mock(TwoWindingsTransformer.class);
        when(transformerWithNullSub.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        when(transformerWithNullSub.getNullableSubstation()).thenReturn(null);

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT2"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), transformerWithNullSub, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), twoWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT2"), twoWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), twoWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), twoWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), twoWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), twoWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), twoWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), twoWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), transformerWithNullSub, false),
                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameTWT", null, twoWindingsTransformer, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, twoWindingsTransformer, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "regionCSV", null, twoWindingsTransformer, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, twoWindingsTransformer, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", null, twoWindingsTransformer, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "c", null, twoWindingsTransformer, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", null, twoWindingsTransformer, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "d", null, twoWindingsTransformer, false),
                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameTWT", null, twoWindingsTransformer, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, twoWindingsTransformer, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "regionCSV", null, twoWindingsTransformer, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, twoWindingsTransformer, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", null, twoWindingsTransformer, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "c", null, twoWindingsTransformer, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", null, twoWindingsTransformer, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "d", null, twoWindingsTransformer, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForStaticVarCompensatorTest() {
        StaticVarCompensator svar = mock(StaticVarCompensator.class);
        when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);
        when(svar.getProperty("propertyNameSVAR")).thenReturn("PropertyValueSVAR");

        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(svar.getTerminal()).thenReturn(terminal);
        when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", List.of("propertyValueSVAR"), svar, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", List.of("propertyValueSVAR2"), svar, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), svar, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation1"), svar, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), svar, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), svar, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", List.of("propertyValueSVAR"), svar, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", List.of("propertyValueSVAR2"), svar, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), svar, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation1"), svar, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), svar, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), svar, true),
                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameSVAR", null, svar, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, svar, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, svar, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, svar, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, svar, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "c", null, svar, false),
                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameSVAR", null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, svar, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, svar, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "c", null, svar, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForThreeWindingTransformerTest() {

        ThreeWindingsTransformer threeWindingsTransformer = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        when(threeWindingsTransformer.getProperty("propertyNameTWT")).thenReturn("PropertyValueTWT");

        Terminal terminal1 = mock(Terminal.class);
        ThreeWindingsTransformer.Leg leg1 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg1.getTerminal()).thenReturn(terminal1);
        when(threeWindingsTransformer.getLeg1()).thenReturn(leg1);
        VoltageLevel voltageLevel1 = mock(VoltageLevel.class);
        when(voltageLevel1.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel1.getProperty("CodeOI")).thenReturn("22");
        when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        Terminal terminal2 = mock(Terminal.class);
        ThreeWindingsTransformer.Leg leg2 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg2.getTerminal()).thenReturn(terminal2);
        when(threeWindingsTransformer.getLeg2()).thenReturn(leg2);
        VoltageLevel voltageLevel2 = mock(VoltageLevel.class);
        when(voltageLevel2.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel2.getProperty("CodeOI")).thenReturn("33");
        when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        Terminal terminal3 = mock(Terminal.class);
        ThreeWindingsTransformer.Leg leg3 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg3.getTerminal()).thenReturn(terminal3);
        when(threeWindingsTransformer.getLeg3()).thenReturn(leg3);
        VoltageLevel voltageLevel3 = mock(VoltageLevel.class);
        when(voltageLevel3.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel3.getProperty("CodeOI")).thenReturn("44");
        when(terminal3.getVoltageLevel()).thenReturn(voltageLevel3);

        Substation substation = mock(Substation.class);
        when(threeWindingsTransformer.getNullableSubstation()).thenReturn(substation);
        when(substation.getProperty("regionCSV")).thenReturn("LILLE");

        return Stream.of(
            // --- IN --- //
            Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT2"), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT3"), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", List.of("44"), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", List.of("22"), threeWindingsTransformer, false),
            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT2"), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT3"), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", List.of("44"), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", List.of("22"), threeWindingsTransformer, true),
            // --- EXISTS --- //
            Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameTWT", null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "test", null, threeWindingsTransformer, false),
            Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "regionCSV", null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "a", null, threeWindingsTransformer, false),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "b", null, threeWindingsTransformer, false),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "c", null, threeWindingsTransformer, false),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "d", null, threeWindingsTransformer, false),
            // --- NOT_EXISTS --- //
            Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameTWT", null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "test", null, threeWindingsTransformer, true),
            Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "regionCSV", null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "a", null, threeWindingsTransformer, true),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "b", null, threeWindingsTransformer, true),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "c", null, threeWindingsTransformer, true),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "d", null, threeWindingsTransformer, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForHvdcLineTest() {
        HvdcLine hvdc = mock(HvdcLine.class);
        when(hvdc.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        when(hvdc.getProperty("propertyNameHVDC")).thenReturn("PropertyValueHVDC");

        Substation substation1 = mock(Substation.class);
        VoltageLevel voltageLevel1 = mock(VoltageLevel.class);
        when(voltageLevel1.getProperty("CodeOI")).thenReturn("33");
        when(voltageLevel1.getNullableSubstation()).thenReturn(substation1);
        Terminal terminal1 = mock(Terminal.class);
        when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        Substation substation2 = mock(Substation.class);
        VoltageLevel voltageLevel2 = mock(VoltageLevel.class);
        when(voltageLevel2.getProperty("CodeOI")).thenReturn("42");
        when(voltageLevel2.getNullableSubstation()).thenReturn(substation2);
        Terminal terminal2 = mock(Terminal.class);
        when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        HvdcConverterStation converterStation1 = mock(HvdcConverterStation.class);
        when(converterStation1.getTerminal()).thenReturn(terminal1);
        when(hvdc.getConverterStation1()).thenReturn(converterStation1);
        HvdcConverterStation converterStation2 = mock(HvdcConverterStation.class);
        when(converterStation2.getTerminal()).thenReturn(terminal2);
        when(hvdc.getConverterStation2()).thenReturn(converterStation2);

        when(substation1.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation1");
        when(substation2.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation2");

        return Stream.of(
            // --- IN --- //
            Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", List.of("propertyValueHVDC"), hvdc, true),
            Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", List.of("propertyValueHVDC2"), hvdc, false),
            Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", List.of("propertyValueSubstation1"), hvdc, true),
            Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", List.of("propertyValueSubstation8"), hvdc, false),
            Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", List.of("PropertyValueSubstation2"), hvdc, true),
            Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", List.of("propertyValueSubstation1"), hvdc, false),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), hvdc, true),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), hvdc, false),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("42"), hvdc, true),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("21"), hvdc, false),
            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", List.of("propertyValueHVDC"), hvdc, false),
            Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", List.of("propertyValueHVDC2"), hvdc, true),
            Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", List.of("propertyValueSubstation1"), hvdc, false),
            Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", List.of("propertyValueSubstation8"), hvdc, true),
            Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", List.of("PropertyValueSubstation2"), hvdc, false),
            Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", List.of("propertyValueSubstation1"), hvdc, true),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), hvdc, false),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), hvdc, true),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("42"), hvdc, false),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("21"), hvdc, true),
            // --- EXISTS --- //
            Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameHVDC", null, hvdc, true),
            Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, hvdc, false),
            Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", null, hvdc, true),
            Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "b", null, hvdc, false),
            Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", null, hvdc, true),
            Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "c", null, hvdc, false),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", null, hvdc, true),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "g", null, hvdc, false),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", null, hvdc, true),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "h", null, hvdc, false),
            // --- NOT_EXISTS --- //
            Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameHVDC", null, hvdc, false),
            Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, hvdc, true),
            Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", null, hvdc, false),
            Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_1, "b", null, hvdc, true),
            Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", null, hvdc, false),
            Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES_2, "c", null, hvdc, true),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", null, hvdc, false),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "g", null, hvdc, true),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", null, hvdc, false),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "h", null, hvdc, true)
            );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {

        Battery battery = mock(Battery.class);
        when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        when(battery.getProperty("propertyNameBattery")).thenReturn("PropertyValueBattery");

        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(battery.getTerminal()).thenReturn(terminal);
        when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameBattery", List.of("propertyValueBattery"), battery, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameBattery", List.of("propertyValueBattery2"), battery, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), battery, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), battery, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), battery, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), battery, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameBattery", List.of("propertyValueBattery"), battery, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameBattery", List.of("propertyValueBattery2"), battery, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), battery, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), battery, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), battery, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), battery, true),
                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameBattery", null, battery, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, battery, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, battery, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, battery, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, battery, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "c", null, battery, false),
                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameBattery", null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, battery, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, battery, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "c", null, battery, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForVoltageLevelTest() {

        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        when(voltageLevel.getProperty("propertyNameVL")).thenReturn("PropertyValueVL");

        Substation substation = mock(Substation.class);
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameVL", List.of("propertyValueVL"), voltageLevel, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameVL", List.of("propertyValueVL2"), voltageLevel, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), voltageLevel, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), voltageLevel, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameVL", List.of("propertyValueVL"), voltageLevel, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameVL", List.of("propertyValueVL2"), voltageLevel, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), voltageLevel, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), voltageLevel, true),
                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameVL", null, voltageLevel, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, voltageLevel, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, voltageLevel, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, voltageLevel, false),
                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameVL", null, voltageLevel, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, voltageLevel, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, voltageLevel, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, voltageLevel, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForHvdcConverterStationTest() {

        HvdcConverterStation converterStation = mock(HvdcConverterStation.class);
        when(converterStation.getType()).thenReturn(IdentifiableType.HVDC_CONVERTER_STATION);
        when(converterStation.getProperty("propertyNameHCS")).thenReturn("PropertyValueHCS");

        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(converterStation.getTerminal()).thenReturn(terminal);
        when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameHCS", List.of("propertyValueHCS"), converterStation, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameHCS", List.of("propertyValueHCS2"), converterStation, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), converterStation, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), converterStation, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameHCS", List.of("propertyValueHCS"), converterStation, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameHCS", List.of("propertyValueHCS2"), converterStation, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), converterStation, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), converterStation, true),
                // --- EXISTS --- //
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameHCS", null, converterStation, true),
                Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "a", null, converterStation, false),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, converterStation, true),
                Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, converterStation, false),
                // --- NOT_EXISTS --- //
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameHCS", null, converterStation, false),
                Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "a", null, converterStation, true),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, converterStation, false),
                Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "b", null, converterStation, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBoundaryLineTest() {
        BoundaryLine boundaryLine = mock(BoundaryLine.class);
        when(boundaryLine.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);
        when(boundaryLine.getProperty("propertyNameBL")).thenReturn("PropertyValueBL");

        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(boundaryLine.getTerminal()).thenReturn(terminal);
        when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
            // --- IN --- //
            Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameBL", List.of("propertyValueBL"), boundaryLine, true),
            Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameBL", List.of("propertyValueBL2"), boundaryLine, false),
            Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), boundaryLine, true),
            Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation1"), boundaryLine, false),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), boundaryLine, true),
            Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), boundaryLine, false),
            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameBL", List.of("propertyValueBL"), boundaryLine, false),
            Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameBL", List.of("propertyValueBL2"), boundaryLine, true),
            Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), boundaryLine, false),
            Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation1"), boundaryLine, true),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), boundaryLine, false),
            Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), boundaryLine, true),
            // --- EXISTS --- //
            Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "propertyNameBL", null, boundaryLine, true),
            Arguments.of(EXISTS, FieldType.FREE_PROPERTIES, "test", null, boundaryLine, false),
            Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, boundaryLine, true),
            Arguments.of(EXISTS, FieldType.SUBSTATION_PROPERTIES, "test", null, boundaryLine, false),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, boundaryLine, true),
            Arguments.of(EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "test", null, boundaryLine, false),
            // --- NOT_EXISTS --- //
            Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "propertyNameBL", null, boundaryLine, false),
            Arguments.of(NOT_EXISTS, FieldType.FREE_PROPERTIES, "test", null, boundaryLine, true),
            Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", null, boundaryLine, false),
            Arguments.of(NOT_EXISTS, FieldType.SUBSTATION_PROPERTIES, "test", null, boundaryLine, true),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", null, boundaryLine, false),
            Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_LEVEL_PROPERTIES, "test", null, boundaryLine, true)
        );
    }

}
