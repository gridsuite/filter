package org.gridsuite.filter.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.expertrule.BooleanExpertRule;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class BooleanExpertRuleTest {
    private FilterLoader filterLoader;

    @BeforeEach
    void setUp() {
        filterLoader = uuids -> null;
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, Class<Throwable> expectedException) {
        BooleanExpertRule rule = BooleanExpertRule.builder().operator(operator).field(field).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = mock(Network.class);
        when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = mock(Generator.class);
        when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        when(generator.isVoltageRegulatorOn()).thenReturn(true);

        Load load = mock(Load.class);
        when(load.getType()).thenReturn(IdentifiableType.LOAD);

        ShuntCompensator shuntCompensator = mock(ShuntCompensator.class);
        when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Bus bus = mock(Bus.class);
        when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = mock(BusbarSection.class);
        when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        StaticVarCompensator svar = mock(StaticVarCompensator.class);
        when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        BoundaryLine bl = mock(BoundaryLine.class);
        when(bl.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);

        TwoWindingsTransformer twoWindingsTransformer = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        ThreeWindingsTransformer threeWindingsTransformer = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        HvdcLine hvdcLine = mock(HvdcLine.class);
        when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);

        return Stream.of(
                // --- Test an unsupported field for each equipment --- //
                Arguments.of(EQUALS, FieldType.RATED_S, network, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, voltageLevel, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.P0, generator, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, load, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, shuntCompensator, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, bus, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, busbarSection, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, svar, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.CONNECTED, twoWindingsTransformer, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, threeWindingsTransformer, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, hvdcLine, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, bl, PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(IS, FieldType.VOLTAGE_REGULATOR_ON, generator, PowsyblException.class)
        );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForLinesTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForTwoWindingTransformerTest",
        "provideArgumentsForStaticVarCompensatorTest",
        "provideArgumentsForBoundaryLineTest",
        "provideArgumentsForThreeWindingTransformerTest",
        "provideArgumentsForHvdcLinesTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, Boolean value, Identifiable<?> equipment, boolean expected) {
        BooleanExpertRule rule = BooleanExpertRule.builder().operator(operator).field(field).value(value).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = mock(Generator.class);
        when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        //Generator fields
        when(gen.isVoltageRegulatorOn()).thenReturn(true);
        // Terminal fields
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(gen.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- EQUALS--- //
                //Generator fields
                Arguments.of(EQUALS, FieldType.VOLTAGE_REGULATOR_ON, true, gen, true),
                Arguments.of(EQUALS, FieldType.VOLTAGE_REGULATOR_ON, false, gen, false),
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, gen, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, gen, false),

                // --- NOT_EQUALS--- //
                //Generator fields
                Arguments.of(NOT_EQUALS, FieldType.VOLTAGE_REGULATOR_ON, false, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.VOLTAGE_REGULATOR_ON, true, gen, false),
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load gen = mock(Load.class);
        when(gen.getType()).thenReturn(IdentifiableType.LOAD);
        // Terminal fields
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(gen.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, gen, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, gen, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {

        ShuntCompensator gen = mock(ShuntCompensator.class);
        when(gen.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        // Terminal fields
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(gen.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, gen, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, gen, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {

        Battery battery = mock(Battery.class);
        when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        // Terminal fields
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(battery.getTerminal()).thenReturn(terminal);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, battery, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, battery, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, battery, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, battery, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLinesTest() {

        Line line = mock(Line.class);
        when(line.getType()).thenReturn(IdentifiableType.LINE);
        // Terminal fields
        Terminal terminal1 = mock(Terminal.class);
        when(terminal1.isConnected()).thenReturn(true);
        when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);

        Terminal terminal2 = mock(Terminal.class);
        when(terminal2.isConnected()).thenReturn(true);
        when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED_1, true, line, true),
                Arguments.of(EQUALS, FieldType.CONNECTED_1, false, line, false),
                Arguments.of(EQUALS, FieldType.CONNECTED_2, true, line, true),
                Arguments.of(EQUALS, FieldType.CONNECTED_2, false, line, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, false, line, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, true, line, false),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, false, line, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, true, line, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForHvdcLinesTest() {

        HvdcLine hvdcLine = mock(HvdcLine.class);
        when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        // Terminal fields
        Terminal terminal1 = mock(Terminal.class);
        when(terminal1.isConnected()).thenReturn(true);
        HvdcConverterStation converterStation1 = mock(HvdcConverterStation.class);
        when(converterStation1.getTerminal()).thenReturn(terminal1);
        when(hvdcLine.getConverterStation1()).thenReturn(converterStation1);

        Terminal terminal2 = mock(Terminal.class);
        when(terminal2.isConnected()).thenReturn(true);
        HvdcConverterStation converterStation2 = mock(HvdcConverterStation.class);
        when(converterStation2.getTerminal()).thenReturn(terminal2);
        when(hvdcLine.getConverterStation2()).thenReturn(converterStation2);

        return Stream.of(
            // --- EQUALS--- //
            // Terminal fields
            Arguments.of(EQUALS, FieldType.CONNECTED_1, true, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.CONNECTED_1, false, hvdcLine, false),
            Arguments.of(EQUALS, FieldType.CONNECTED_2, true, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.CONNECTED_2, false, hvdcLine, false),

            // --- NOT_EQUALS--- //
            // Terminal fields
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, false, hvdcLine, true),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, true, hvdcLine, false),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, false, hvdcLine, true),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, true, hvdcLine, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForTwoWindingTransformerTest() {

        TwoWindingsTransformer twoWindingsTransformer = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        // Terminal fields
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(twoWindingsTransformer.getTerminal1()).thenReturn(terminal);
        when(twoWindingsTransformer.getTerminal2()).thenReturn(terminal);

        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger = mock(RatioTapChanger.class);
        when(ratioTapChanger.isRegulating()).thenReturn(true);
        when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(true);
        when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        when(twoWindingsTransformer.hasRatioTapChanger()).thenReturn(true);

        // null RatioTapChanger
        TwoWindingsTransformer twoWindingsTransformer2 = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer2.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        when(twoWindingsTransformer2.getRatioTapChanger()).thenReturn(null);
        when(twoWindingsTransformer2.hasRatioTapChanger()).thenReturn(false);

        // PhaseTapChanger fields
        PhaseTapChanger phaseTapChanger = mock(PhaseTapChanger.class);
        when(phaseTapChanger.isRegulating()).thenReturn(false);
        when(twoWindingsTransformer.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        when(twoWindingsTransformer.hasPhaseTapChanger()).thenReturn(true);

        // null PhaseTapChanger
        when(twoWindingsTransformer2.getPhaseTapChanger()).thenReturn(null);
        when(twoWindingsTransformer2.hasPhaseTapChanger()).thenReturn(false);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED_1, true, twoWindingsTransformer, true),
                Arguments.of(EQUALS, FieldType.CONNECTED_1, false, twoWindingsTransformer, false),
                Arguments.of(EQUALS, FieldType.CONNECTED_2, true, twoWindingsTransformer, true),
                Arguments.of(EQUALS, FieldType.CONNECTED_2, false, twoWindingsTransformer, false),

                // RatioTapChanger fields
                Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, true, twoWindingsTransformer, true),
                Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, false, twoWindingsTransformer, false),
                Arguments.of(EQUALS, FieldType.HAS_RATIO_TAP_CHANGER, true, twoWindingsTransformer, true),

                // PhaseTapChanger fields
                Arguments.of(EQUALS, FieldType.HAS_PHASE_TAP_CHANGER, true, twoWindingsTransformer, true),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, false, twoWindingsTransformer, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, true, twoWindingsTransformer, false),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, false, twoWindingsTransformer, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, true, twoWindingsTransformer, false),

                // RatioTapChanger fields
                Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, false, twoWindingsTransformer, true),
                Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES, true, twoWindingsTransformer, false),
                Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER, true, twoWindingsTransformer, false),

                // null RatioTapChanger
                Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER, false, twoWindingsTransformer2, false),

                // PhaseTapChanger fields
                Arguments.of(NOT_EQUALS, FieldType.HAS_PHASE_TAP_CHANGER, true, twoWindingsTransformer, false),

                // null PhaseTapChanger
                Arguments.of(NOT_EQUALS, FieldType.HAS_PHASE_TAP_CHANGER, false, twoWindingsTransformer2, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForThreeWindingTransformerTest() {

        ThreeWindingsTransformer threeWindingsTransformer = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        // Terminal fields
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        ThreeWindingsTransformer.Leg leg = mock(ThreeWindingsTransformer.Leg.class);
        when(leg.getTerminal()).thenReturn(terminal);
        when(threeWindingsTransformer.getLeg1()).thenReturn(leg);
        when(threeWindingsTransformer.getLeg2()).thenReturn(leg);
        when(threeWindingsTransformer.getLeg3()).thenReturn(leg);

        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger = mock(RatioTapChanger.class);
        when(ratioTapChanger.isRegulating()).thenReturn(true);
        when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(true);
        when(leg.getRatioTapChanger()).thenReturn(ratioTapChanger);
        when(leg.hasRatioTapChanger()).thenReturn(true);

        // null RatioTapChanger
        ThreeWindingsTransformer threeWindingsTransformer2 = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer2.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        ThreeWindingsTransformer.Leg leg2 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg2.getRatioTapChanger()).thenReturn(null);
        when(leg2.hasRatioTapChanger()).thenReturn(false);

        // PhaseTapChanger fields
        PhaseTapChanger phaseTapChanger = mock(PhaseTapChanger.class);
        when(phaseTapChanger.isRegulating()).thenReturn(false);
        when(leg2.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        when(leg2.hasPhaseTapChanger()).thenReturn(true);

        when(threeWindingsTransformer2.getLeg1()).thenReturn(leg2);
        when(threeWindingsTransformer2.getLeg2()).thenReturn(leg2);
        when(threeWindingsTransformer2.getLeg3()).thenReturn(leg2);

        return Stream.of(
            // --- EQUALS--- //
            // Terminal fields
            Arguments.of(EQUALS, FieldType.CONNECTED_1, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.CONNECTED_1, false, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.CONNECTED_2, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.CONNECTED_2, false, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.CONNECTED_3, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.CONNECTED_3, false, threeWindingsTransformer, false),

            // RatioTapChanger fields
            Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, false, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_2, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_2, false, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_3, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_3, false, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_1, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_1, false, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_2, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_2, false, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_3, true, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_3, false, threeWindingsTransformer, false),

            // PhaseTapChanger fields
            Arguments.of(EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_1, true, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_2, true, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_3, true, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_1, true, threeWindingsTransformer2, true),
            Arguments.of(EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_2, true, threeWindingsTransformer2, true),
            Arguments.of(EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_3, true, threeWindingsTransformer2, true),

            // --- NOT_EQUALS--- //
            // Terminal fields
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, false, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_1, true, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, false, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_2, true, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_3, false, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED_3, true, threeWindingsTransformer, false),

            // RatioTapChanger fields
            Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, false, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, true, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_2, false, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_2, true, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_3, false, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.LOAD_TAP_CHANGING_CAPABILITIES_3, true, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_1, true, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_1, false, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_2, true, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_2, false, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_3, true, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_3, false, threeWindingsTransformer, true),

            // null RatioTapChanger
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_1, false, threeWindingsTransformer2, false),
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_2, false, threeWindingsTransformer2, false),
            Arguments.of(NOT_EQUALS, FieldType.HAS_RATIO_TAP_CHANGER_3, false, threeWindingsTransformer2, false),

            // PhaseTapChanger fields
            Arguments.of(NOT_EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_1, true, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_2, true, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_3, true, threeWindingsTransformer, true),

            // null PhaseTapChanger
            Arguments.of(NOT_EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_1, false, threeWindingsTransformer2, true),
            Arguments.of(NOT_EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_2, false, threeWindingsTransformer2, true),
            Arguments.of(NOT_EQUALS, FieldType.HAS_PHASE_TAP_CHANGER_3, false, threeWindingsTransformer2, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForStaticVarCompensatorTest() {

        StaticVarCompensator svar = mock(StaticVarCompensator.class);
        when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);
        when(svar.getId()).thenReturn("SVAR");
        // Terminal fields
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(svar.getTerminal()).thenReturn(terminal);

        // Regulating terminal fields
        Terminal regulatingTerminal = mock(Terminal.class);
        VoltageLevel distantVoltageLevel = mock(VoltageLevel.class);
        when(regulatingTerminal.getVoltageLevel()).thenReturn(distantVoltageLevel);
        BusbarSection regulatedBusBarSection = mock(BusbarSection.class);
        when(regulatedBusBarSection.getId()).thenReturn("BBS");
        when(regulatingTerminal.getConnectable()).thenReturn(regulatedBusBarSection);
        when(svar.getRegulatingTerminal()).thenReturn(regulatingTerminal);

        StandbyAutomaton standbyAutomaton = mock(StandbyAutomaton.class);
        when(svar.getExtension(StandbyAutomaton.class)).thenReturn(standbyAutomaton);

        // for testing none EXISTS automaton and regulating terminal
        StaticVarCompensator svar1 = mock(StaticVarCompensator.class);
        when(svar1.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        // configure a regulating terminal without connected equipment
        Terminal regulatingTerminal1 = mock(Terminal.class);
        VoltageLevel distantVoltageLevel1 = mock(VoltageLevel.class);
        when(regulatingTerminal1.getVoltageLevel()).thenReturn(distantVoltageLevel1);
        when(svar1.getRegulatingTerminal()).thenReturn(regulatingTerminal1);

        return Stream.of(
                // --- EQUALS--- //
                // Terminal fields
                Arguments.of(EQUALS, FieldType.CONNECTED, true, svar, true),
                Arguments.of(EQUALS, FieldType.CONNECTED, false, svar, false),

                // --- NOT_EQUALS--- //
                // Terminal fields
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, svar, true),
                Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, svar, false),

                // --- EXISTS--- //
                Arguments.of(EXISTS, FieldType.REMOTE_REGULATED_TERMINAL, null, svar, true),
                Arguments.of(EXISTS, FieldType.REMOTE_REGULATED_TERMINAL, null, svar1, false),
                Arguments.of(EXISTS, FieldType.AUTOMATE, null, svar, true),
                Arguments.of(EXISTS, FieldType.AUTOMATE, null, svar1, false),

                // --- NOT_EXISTS--- //
                Arguments.of(NOT_EXISTS, FieldType.REMOTE_REGULATED_TERMINAL, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.REMOTE_REGULATED_TERMINAL, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.AUTOMATE, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.AUTOMATE, null, svar1, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBoundaryLineTest() {

        BoundaryLine boundaryLine = mock(BoundaryLine.class);
        when(boundaryLine.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);
        //Generator fields
        when(boundaryLine.isPaired()).thenReturn(true);
        // Terminal fields
        Terminal terminal = mock(Terminal.class);
        when(terminal.isConnected()).thenReturn(true);
        when(boundaryLine.getTerminal()).thenReturn(terminal);

        return Stream.of(
            // --- EQUALS--- //
            //Generator fields
            Arguments.of(EQUALS, FieldType.PAIRED, true, boundaryLine, true),
            Arguments.of(EQUALS, FieldType.PAIRED, false, boundaryLine, false),
            // Terminal fields
            Arguments.of(EQUALS, FieldType.CONNECTED, true, boundaryLine, true),
            Arguments.of(EQUALS, FieldType.CONNECTED, false, boundaryLine, false),

            // --- NOT_EQUALS--- //
            //Generator fields
            Arguments.of(NOT_EQUALS, FieldType.PAIRED, false, boundaryLine, true),
            Arguments.of(NOT_EQUALS, FieldType.PAIRED, true, boundaryLine, false),
            // Terminal fields
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, boundaryLine, true),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, boundaryLine, false)
        );
    }
}
