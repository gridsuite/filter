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
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.isVoltageRegulatorOn()).thenReturn(true);

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        StaticVarCompensator svar = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        DanglingLine dl = Mockito.mock(DanglingLine.class);
        Mockito.when(dl.getType()).thenReturn(IdentifiableType.DANGLING_LINE);

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        ThreeWindingsTransformer threeWindingsTransformer = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        HvdcLine hvdcLine = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);

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
                Arguments.of(EQUALS, FieldType.RATED_S, dl, PowsyblException.class),

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
        "provideArgumentsForDanglingLineTest",
        "provideArgumentsForThreeWindingTransformerTest",
        "provideArgumentsForHvdcLinesTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, Boolean value, Identifiable<?> equipment, boolean expected) {
        BooleanExpertRule rule = BooleanExpertRule.builder().operator(operator).field(field).value(value).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        //Generator fields
        Mockito.when(gen.isVoltageRegulatorOn()).thenReturn(true);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);

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

        Load gen = Mockito.mock(Load.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.LOAD);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);

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

        ShuntCompensator gen = Mockito.mock(ShuntCompensator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);

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

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(battery.getTerminal()).thenReturn(terminal);

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

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);
        // Terminal fields
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.isConnected()).thenReturn(true);
        Mockito.when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);

        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.isConnected()).thenReturn(true);
        Mockito.when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);

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

        HvdcLine hvdcLine = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        // Terminal fields
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.isConnected()).thenReturn(true);
        HvdcConverterStation converterStation1 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation1.getTerminal()).thenReturn(terminal1);
        Mockito.when(hvdcLine.getConverterStation1()).thenReturn(converterStation1);

        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.isConnected()).thenReturn(true);
        HvdcConverterStation converterStation2 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation2.getTerminal()).thenReturn(terminal2);
        Mockito.when(hvdcLine.getConverterStation2()).thenReturn(converterStation2);

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

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(twoWindingsTransformer.getTerminal1()).thenReturn(terminal);
        Mockito.when(twoWindingsTransformer.getTerminal2()).thenReturn(terminal);

        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.isRegulating()).thenReturn(true);
        Mockito.when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(true);
        Mockito.when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(twoWindingsTransformer.hasRatioTapChanger()).thenReturn(true);

        // null RatioTapChanger
        TwoWindingsTransformer twoWindingsTransformer2 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer2.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twoWindingsTransformer2.getRatioTapChanger()).thenReturn(null);
        Mockito.when(twoWindingsTransformer2.hasRatioTapChanger()).thenReturn(false);

        // PhaseTapChanger fields
        PhaseTapChanger phaseTapChanger = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger.isRegulating()).thenReturn(false);
        Mockito.when(twoWindingsTransformer.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        Mockito.when(twoWindingsTransformer.hasPhaseTapChanger()).thenReturn(true);

        // null PhaseTapChanger
        Mockito.when(twoWindingsTransformer2.getPhaseTapChanger()).thenReturn(null);
        Mockito.when(twoWindingsTransformer2.hasPhaseTapChanger()).thenReturn(false);

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

        ThreeWindingsTransformer threeWindingsTransformer = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        ThreeWindingsTransformer.Leg leg = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg.getTerminal()).thenReturn(terminal);
        Mockito.when(threeWindingsTransformer.getLeg1()).thenReturn(leg);
        Mockito.when(threeWindingsTransformer.getLeg2()).thenReturn(leg);
        Mockito.when(threeWindingsTransformer.getLeg3()).thenReturn(leg);

        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.isRegulating()).thenReturn(true);
        Mockito.when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(true);
        Mockito.when(leg.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(leg.hasRatioTapChanger()).thenReturn(true);

        // null RatioTapChanger
        ThreeWindingsTransformer threeWindingsTransformer2 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer2.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        ThreeWindingsTransformer.Leg leg2 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg2.getRatioTapChanger()).thenReturn(null);
        Mockito.when(leg2.hasRatioTapChanger()).thenReturn(false);

        // PhaseTapChanger fields
        PhaseTapChanger phaseTapChanger = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger.isRegulating()).thenReturn(false);
        Mockito.when(leg2.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        Mockito.when(leg2.hasPhaseTapChanger()).thenReturn(true);

        Mockito.when(threeWindingsTransformer2.getLeg1()).thenReturn(leg2);
        Mockito.when(threeWindingsTransformer2.getLeg2()).thenReturn(leg2);
        Mockito.when(threeWindingsTransformer2.getLeg3()).thenReturn(leg2);

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

        StaticVarCompensator svar = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);
        Mockito.when(svar.getId()).thenReturn("SVAR");
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(svar.getTerminal()).thenReturn(terminal);

        // Regulating terminal fields
        Terminal regulatingTerminal = Mockito.mock(Terminal.class);
        VoltageLevel distantVoltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(regulatingTerminal.getVoltageLevel()).thenReturn(distantVoltageLevel);
        BusbarSection regulatedBusBarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(regulatedBusBarSection.getId()).thenReturn("BBS");
        Mockito.when(regulatingTerminal.getConnectable()).thenReturn(regulatedBusBarSection);
        Mockito.when(svar.getRegulatingTerminal()).thenReturn(regulatingTerminal);

        StandbyAutomaton standbyAutomaton = Mockito.mock(StandbyAutomaton.class);
        Mockito.when(svar.getExtension(StandbyAutomaton.class)).thenReturn(standbyAutomaton);

        // for testing none EXISTS automaton and regulating terminal
        StaticVarCompensator svar1 = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar1.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        // configure a regulating terminal without connected equipment
        Terminal regulatingTerminal1 = Mockito.mock(Terminal.class);
        VoltageLevel distantVoltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(regulatingTerminal1.getVoltageLevel()).thenReturn(distantVoltageLevel1);
        Mockito.when(svar1.getRegulatingTerminal()).thenReturn(regulatingTerminal1);

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

    private static Stream<Arguments> provideArgumentsForDanglingLineTest() {

        DanglingLine danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getType()).thenReturn(IdentifiableType.DANGLING_LINE);
        //Generator fields
        Mockito.when(danglingLine.isPaired()).thenReturn(true);
        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.isConnected()).thenReturn(true);
        Mockito.when(danglingLine.getTerminal()).thenReturn(terminal);

        return Stream.of(
            // --- EQUALS--- //
            //Generator fields
            Arguments.of(EQUALS, FieldType.PAIRED, true, danglingLine, true),
            Arguments.of(EQUALS, FieldType.PAIRED, false, danglingLine, false),
            // Terminal fields
            Arguments.of(EQUALS, FieldType.CONNECTED, true, danglingLine, true),
            Arguments.of(EQUALS, FieldType.CONNECTED, false, danglingLine, false),

            // --- NOT_EQUALS--- //
            //Generator fields
            Arguments.of(NOT_EQUALS, FieldType.PAIRED, false, danglingLine, true),
            Arguments.of(NOT_EQUALS, FieldType.PAIRED, true, danglingLine, false),
            // Terminal fields
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED, false, danglingLine, true),
            Arguments.of(NOT_EQUALS, FieldType.CONNECTED, true, danglingLine, false)
        );
    }
}
