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
import org.mockito.Mockito;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operator(IS_IN).field(FieldType.FREE_PROPERTIES).propertyName("property")
            .propertyValues(Collections.singletonList("value1")).build();
        assertEquals(Collections.singletonList("value1"), rule.getPropertyValues());
        assertEquals("property", rule.getStringValue());
        assertEquals(FieldType.FREE_PROPERTIES, rule.getField());
        assertEquals(IS_IN, rule.getOperator());
        rule = PropertiesExpertRule.builder().operator(IS_NOT_IN).field(FieldType.FREE_PROPERTIES).propertyName("property2")
            .propertyValues(Collections.singletonList("value2")).build();
        assertEquals(Collections.singletonList("value2"), rule.getPropertyValues());
        assertEquals("property2", rule.getStringValue());
        assertEquals(FieldType.FREE_PROPERTIES, rule.getField());
        assertEquals(IS_NOT_IN, rule.getOperator());
    }

    private static Stream<Arguments> provideArgumentsForTestWithException() {

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

        StaticVarCompensator svar = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        ThreeWindingsTransformer threeWindingsTransformer = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        HvdcLine hvdcLine = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);

        return Stream.of(
                // --- Test an unsupported field for some equipment --- //
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_1, voltageLevel, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_2, generator, "region", List.of("north"), PowsyblException.class),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_1, load, "region", List.of("north"), PowsyblException.class),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_1, shuntCompensator, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, line, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_2, battery, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_1, svar, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_IN, FieldType.SERIE_REACTANCE, threeWindingsTransformer, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, hvdcLine, "region", List.of("east"), PowsyblException.class),

                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, voltageLevel, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, generator, "region", List.of("north"), PowsyblException.class),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, load, "region", List.of("north"), PowsyblException.class),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, shuntCompensator, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, line, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, battery, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, svar, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_NOT_IN, FieldType.SERIE_REACTANCE, threeWindingsTransformer, "region", List.of("east"), PowsyblException.class),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, hvdcLine, "region", List.of("east"), PowsyblException.class),

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
        "provideArgumentsForDanglingLineTest",
        "provideArgumentsForThreeWindingTransformerTest",
        "provideArgumentsForHvdcLineTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, String propertyName, List<String> propertyValues, Identifiable<?> equipment, boolean expected) {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operator(operator).field(field).propertyName(propertyName).propertyValues(propertyValues).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForSubstationTest() {

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getType()).thenReturn(IdentifiableType.SUBSTATION);
        Mockito.when(substation.getProperty("cvgRegion")).thenReturn("LILLE");

        return Stream.of(
                // --- IS_IN --- //
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("Lille", "PARIS"), substation, true),
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("Paris"), substation, false),

                // --- IS_NOT_IN --- //
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("Lille", "PARIS"), substation, false),
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "cvgRegion", List.of("Paris"), substation, true)
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
                // --- IS_IN --- //
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("11"), generator, true),
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("22"), generator, false),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), generator, true),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), generator, false),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), generator, true),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("11"), generator, false),
                // --- IS_NOT_IN --- //
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("11"), generator, false),
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "CodeOI", List.of("22"), generator, true),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), generator, false),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), generator, true),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), generator, false),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("11"), generator, true)
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
                // --- IS_IN --- //
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "region", List.of("north"), line, true),
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "region", List.of("south"), line, false),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("Lille"), line, true),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("Paris"), line, false),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("Paris"), line, true),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("Lille"), line, false),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), line, true),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), line, false),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), line, true),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), line, false),
                // --- IS_NOT_IN --- //
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "region", List.of("north"), line, false),
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "region", List.of("south"), line, true),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("Lille"), line, false),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", List.of("Paris"), line, true),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("Paris"), line, false),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", List.of("Lille"), line, true),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), line, false),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), line, true),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), line, false),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), line, true)
   );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load.getProperty("propertyNameLoad")).thenReturn("PropertyValueLoad");

        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IS_IN --- //
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad"), load, true),
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad2"), load, false),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), load, true),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), load, false),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), load, true),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), load, false),
                // --- IS_IN --- //
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad"), load, false),
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", List.of("propertyValueLoad2"), load, true),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), load, false),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), load, true),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), load, false),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), load, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        Mockito.when(shuntCompensator.getProperty("propertyNameSC")).thenReturn("PropertyValueSC");

        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(shuntCompensator.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IS_IN --- //
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC"), shuntCompensator, true),
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC1"), shuntCompensator, false),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), shuntCompensator, true),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), shuntCompensator, false),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), shuntCompensator, true),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), shuntCompensator, false),
                // --- IS_NOT_IN --- //
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC"), shuntCompensator, false),
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSC", List.of("propertyValueSC1"), shuntCompensator, true),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), shuntCompensator, false),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation2"), shuntCompensator, true),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), shuntCompensator, false),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), shuntCompensator, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForTwoWindingTransformerTest() {

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twoWindingsTransformer.getProperty("propertyNameTWT")).thenReturn("PropertyValueTWT");

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

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(twoWindingsTransformer.getNullableSubstation()).thenReturn(substation);
        Mockito.when(substation.getProperty("regionCSV")).thenReturn("LILLE");

        TwoWindingsTransformer transformerWithNullSub = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(transformerWithNullSub.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(transformerWithNullSub.getNullableSubstation()).thenReturn(null);

        return Stream.of(
                // --- IS_IN --- //
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), twoWindingsTransformer, true),
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT2"), twoWindingsTransformer, false),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), twoWindingsTransformer, true),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), twoWindingsTransformer, false),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), twoWindingsTransformer, true),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), twoWindingsTransformer, false),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), twoWindingsTransformer, true),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), twoWindingsTransformer, false),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), transformerWithNullSub, false),
                // --- IS_NOT_IN --- //
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), twoWindingsTransformer, false),
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT2"), twoWindingsTransformer, true),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), twoWindingsTransformer, false),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), twoWindingsTransformer, true),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), twoWindingsTransformer, false),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), twoWindingsTransformer, true),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), twoWindingsTransformer, false),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), twoWindingsTransformer, true),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), transformerWithNullSub, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForStaticVarCompensatorTest() {
        StaticVarCompensator svar = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);
        Mockito.when(svar.getProperty("propertyNameSVAR")).thenReturn("PropertyValueSVAR");

        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(svar.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IS_IN --- //
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", List.of("propertyValueSVAR"), svar, true),
                Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", List.of("propertyValueSVAR2"), svar, false),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), svar, true),
                Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation1"), svar, false),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), svar, true),
                Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), svar, false),
                // --- IS_NOT_IN --- //
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", List.of("propertyValueSVAR"), svar, false),
                Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", List.of("propertyValueSVAR2"), svar, true),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), svar, false),
                Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation1"), svar, true),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), svar, false),
                Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), svar, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForThreeWindingTransformerTest() {

        ThreeWindingsTransformer threeWindingsTransformer = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        Mockito.when(threeWindingsTransformer.getProperty("propertyNameTWT")).thenReturn("PropertyValueTWT");

        Terminal terminal1 = Mockito.mock(Terminal.class);
        ThreeWindingsTransformer.Leg leg1 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg1.getTerminal()).thenReturn(terminal1);
        Mockito.when(threeWindingsTransformer.getLeg1()).thenReturn(leg1);
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel1.getProperty("CodeOI")).thenReturn("22");
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        Terminal terminal2 = Mockito.mock(Terminal.class);
        ThreeWindingsTransformer.Leg leg2 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg2.getTerminal()).thenReturn(terminal2);
        Mockito.when(threeWindingsTransformer.getLeg2()).thenReturn(leg2);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel2.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        Terminal terminal3 = Mockito.mock(Terminal.class);
        ThreeWindingsTransformer.Leg leg3 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg3.getTerminal()).thenReturn(terminal3);
        Mockito.when(threeWindingsTransformer.getLeg3()).thenReturn(leg3);
        VoltageLevel voltageLevel3 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel3.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel3.getProperty("CodeOI")).thenReturn("44");
        Mockito.when(terminal3.getVoltageLevel()).thenReturn(voltageLevel3);

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(threeWindingsTransformer.getNullableSubstation()).thenReturn(substation);
        Mockito.when(substation.getProperty("regionCSV")).thenReturn("LILLE");

        return Stream.of(
            // --- IS_IN --- //
            Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), threeWindingsTransformer, true),
            Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT2"), threeWindingsTransformer, false),
            Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT3"), threeWindingsTransformer, false),
            Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), threeWindingsTransformer, true),
            Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), threeWindingsTransformer, false),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), threeWindingsTransformer, true),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), threeWindingsTransformer, false),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), threeWindingsTransformer, true),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), threeWindingsTransformer, false),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", List.of("44"), threeWindingsTransformer, true),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", List.of("22"), threeWindingsTransformer, false),
            // --- IS_NOT_IN --- //
            Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT"), threeWindingsTransformer, false),
            Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT2"), threeWindingsTransformer, true),
            Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", List.of("propertyValueTWT3"), threeWindingsTransformer, true),
            Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Lille"), threeWindingsTransformer, false),
            Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", List.of("Paris"), threeWindingsTransformer, true),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), threeWindingsTransformer, false),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), threeWindingsTransformer, true),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("33"), threeWindingsTransformer, false),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("22"), threeWindingsTransformer, true),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", List.of("44"), threeWindingsTransformer, false),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", List.of("22"), threeWindingsTransformer, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForHvdcLineTest() {
        HvdcLine hvdc = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdc.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        Mockito.when(hvdc.getProperty("propertyNameHVDC")).thenReturn("PropertyValueHVDC");

        Substation substation1 = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(voltageLevel1.getNullableSubstation()).thenReturn(substation1);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        Substation substation2 = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getProperty("CodeOI")).thenReturn("42");
        Mockito.when(voltageLevel2.getNullableSubstation()).thenReturn(substation2);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        HvdcConverterStation converterStation1 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation1.getTerminal()).thenReturn(terminal1);
        Mockito.when(hvdc.getConverterStation1()).thenReturn(converterStation1);
        HvdcConverterStation converterStation2 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation2.getTerminal()).thenReturn(terminal2);
        Mockito.when(hvdc.getConverterStation2()).thenReturn(converterStation2);

        Mockito.when(substation1.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation1");
        Mockito.when(substation2.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation2");

        return Stream.of(
            // --- IS_IN --- //
            Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", List.of("propertyValueHVDC"), hvdc, true),
            Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", List.of("propertyValueHVDC2"), hvdc, false),
            Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", List.of("propertyValueSubstation1"), hvdc, true),
            Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", List.of("propertyValueSubstation8"), hvdc, false),
            Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", List.of("PropertyValueSubstation2"), hvdc, true),
            Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", List.of("propertyValueSubstation1"), hvdc, false),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), hvdc, true),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), hvdc, false),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("42"), hvdc, true),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("21"), hvdc, false),
            // --- IS_NOT_IN --- //
            Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", List.of("propertyValueHVDC"), hvdc, false),
            Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", List.of("propertyValueHVDC2"), hvdc, true),
            Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", List.of("propertyValueSubstation1"), hvdc, false),
            Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", List.of("propertyValueSubstation8"), hvdc, true),
            Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", List.of("PropertyValueSubstation2"), hvdc, false),
            Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", List.of("propertyValueSubstation1"), hvdc, true),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("33"), hvdc, false),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", List.of("22"), hvdc, true),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("42"), hvdc, false),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", List.of("21"), hvdc, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForDanglingLineTest() {
        DanglingLine danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getType()).thenReturn(IdentifiableType.DANGLING_LINE);
        Mockito.when(danglingLine.getProperty("propertyNameDL")).thenReturn("PropertyValueDL");

        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(danglingLine.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
            // --- IS_IN --- //
            Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameDL", List.of("propertyValueDL"), danglingLine, true),
            Arguments.of(IS_IN, FieldType.FREE_PROPERTIES, "propertyNameDL", List.of("propertyValueDL2"), danglingLine, false),
            Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), danglingLine, true),
            Arguments.of(IS_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation1"), danglingLine, false),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), danglingLine, true),
            Arguments.of(IS_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), danglingLine, false),
            // --- IS_NOT_IN --- //
            Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameDL", List.of("propertyValueDL"), danglingLine, false),
            Arguments.of(IS_NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameDL", List.of("propertyValueDL2"), danglingLine, true),
            Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation"), danglingLine, false),
            Arguments.of(IS_NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", List.of("propertyValueSubstation1"), danglingLine, true),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("33"), danglingLine, false),
            Arguments.of(IS_NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", List.of("22"), danglingLine, true)
        );
    }

}
