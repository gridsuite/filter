package org.gridsuite.filter.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.extensions.GeneratorStartup;
import com.powsybl.iidm.network.extensions.IdentifiableShortCircuit;
import com.powsybl.iidm.network.extensions.StandbyAutomaton;
import com.powsybl.iidm.network.impl.extensions.IdentifiableShortCircuitImpl;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.expertrule.NumberExpertRule;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;

class NumberExpertRuleTest {
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
        NumberExpertRule rule = NumberExpertRule.builder().operator(operator).field(field).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.getMinP()).thenReturn(-500.0);

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        TwoWindingsTransformer twoWindingTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        StaticVarCompensator svar = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        ThreeWindingsTransformer threeWindingTransformer = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

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
                Arguments.of(EQUALS, FieldType.P0, twoWindingTransformer, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, svar, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.P0, threeWindingTransformer, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, hvdcLine, PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(IS, FieldType.MIN_P, generator, PowsyblException.class)
        );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBusTest",
        "provideArgumentsForBusBarSectionTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForVoltageLevelTest",
        "provideArgumentsForLinesTest",
        "provideArgumentsForTwoWindingTransformerTest",
        "provideArgumentsForStaticVarCompensatorTest",
        "provideArgumentsForDanglingLineTest",
        "provideArgumentsForThreeWindingTransformerTest",
        "provideArgumentsForHvdcLinesTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, Double value, Set<Double> values, Identifiable<?> equipment, boolean expected) {
        NumberExpertRule rule = NumberExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen.getMinP()).thenReturn(-500.0);
        Mockito.when(gen.getMaxP()).thenReturn(100.0);
        Mockito.when(gen.getTargetV()).thenReturn(20.0);
        Mockito.when(gen.getTargetP()).thenReturn(30.0);
        Mockito.when(gen.getTargetQ()).thenReturn(40.0);
        Mockito.when(gen.getRatedS()).thenReturn(60.0);
        // GeneratorStartup extension fields
        GeneratorStartup genStartup = Mockito.mock(GeneratorStartup.class);
        Mockito.when(genStartup.getPlannedActivePowerSetpoint()).thenReturn(50.0);
        Mockito.when(genStartup.getMarginalCost()).thenReturn(50.0);
        Mockito.when(genStartup.getPlannedOutageRate()).thenReturn(50.0);
        Mockito.when(genStartup.getForcedOutageRate()).thenReturn(50.0);
        Mockito.when(gen.getExtension(any())).thenReturn(genStartup);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        Generator gen1 = Mockito.mock(Generator.class);
        Mockito.when(gen1.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen1.getMinP()).thenReturn(Double.NaN);
        Mockito.when(gen1.getMaxP()).thenReturn(Double.NaN);
        Mockito.when(gen1.getTargetV()).thenReturn(Double.NaN);
        Mockito.when(gen1.getTargetP()).thenReturn(Double.NaN);
        Mockito.when(gen1.getTargetQ()).thenReturn(Double.NaN);
        Mockito.when(gen1.getRatedS()).thenReturn(Double.NaN);
        // GeneratorStartup extension fields
        GeneratorStartup genStartup1 = Mockito.mock(GeneratorStartup.class);
        Mockito.when(genStartup1.getPlannedActivePowerSetpoint()).thenReturn(Double.NaN);
        Mockito.when(genStartup1.getMarginalCost()).thenReturn(Double.NaN);
        Mockito.when(genStartup1.getPlannedOutageRate()).thenReturn(Double.NaN);
        Mockito.when(genStartup1.getForcedOutageRate()).thenReturn(Double.NaN);
        Mockito.when(gen1.getExtension(any())).thenReturn(genStartup1);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(gen1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // Generator fields
                Arguments.of(EQUALS, FieldType.MIN_P, -500.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.MIN_P, -400.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.MAX_P, 100.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.MAX_P, 90.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.TARGET_V, 20.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_V, 10.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.TARGET_P, 30.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_P, 20.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 40.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 30.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.RATED_S, 60.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.RATED_S, 50.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.MARGINAL_COST, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.MARGINAL_COST, 40.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, false),
                Arguments.of(EQUALS, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, false),

                // --- GREATER_OR_EQUALS --- //
                // Generator fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -600.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -500.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -400.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 90.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 100.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 110.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_V, 10.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_V, 20.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_V, 30.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 20.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 30.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 40.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 30.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 50.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 60.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 70.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MARGINAL_COST, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MARGINAL_COST, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MARGINAL_COST, 60.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, false),

                // --- GREATER --- //
                // Generator fields
                Arguments.of(GREATER, FieldType.MIN_P, -600.0, null, gen, true),
                Arguments.of(GREATER, FieldType.MIN_P, -500.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MIN_P, -400.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MAX_P, 90.0, null, gen, true),
                Arguments.of(GREATER, FieldType.MAX_P, 100.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MAX_P, 110.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_V, 10.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_V, 20.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_V, 30.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_P, 20.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_P, 30.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_P, 40.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_Q, 30.0, null, gen, true),
                Arguments.of(GREATER, FieldType.TARGET_Q, 40.0, null, gen, false),
                Arguments.of(GREATER, FieldType.TARGET_Q, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.RATED_S, 50.0, null, gen, true),
                Arguments.of(GREATER, FieldType.RATED_S, 60.0, null, gen, false),
                Arguments.of(GREATER, FieldType.RATED_S, 70.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(GREATER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MARGINAL_COST, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.MARGINAL_COST, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.MARGINAL_COST, 60.0, null, gen, false),
                Arguments.of(GREATER, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, false),
                Arguments.of(GREATER, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, true),
                Arguments.of(GREATER, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, false),
                Arguments.of(GREATER, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, false),

                // --- LOWER_OR_EQUALS --- //
                // Generator fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -400.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -500.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -600.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 110.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 100.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 90.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_V, 30.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_V, 20.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_V, 10.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 40.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 30.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 20.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 40.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 30.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 70.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 50.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MARGINAL_COST, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MARGINAL_COST, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MARGINAL_COST, 40.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, false),

                // --- LOWER --- //
                // Generator fields
                Arguments.of(LOWER, FieldType.MIN_P, -400.0, null, gen, true),
                Arguments.of(LOWER, FieldType.MIN_P, -500.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MIN_P, -600.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MAX_P, 110.0, null, gen, true),
                Arguments.of(LOWER, FieldType.MAX_P, 100.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MAX_P, 90.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_V, 30.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_V, 20.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_V, 10.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_P, 40.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_P, 30.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_P, 20.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_Q, 50.0, null, gen, true),
                Arguments.of(LOWER, FieldType.TARGET_Q, 40.0, null, gen, false),
                Arguments.of(LOWER, FieldType.TARGET_Q, 30.0, null, gen, false),
                Arguments.of(LOWER, FieldType.RATED_S, 70.0, null, gen, true),
                Arguments.of(LOWER, FieldType.RATED_S, 60.0, null, gen, false),
                Arguments.of(LOWER, FieldType.RATED_S, 50.0, null, gen, false),
                // GeneratorStartup extension fields
                Arguments.of(LOWER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 50.0, null, gen, false),
                Arguments.of(LOWER, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, 40.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MARGINAL_COST, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.MARGINAL_COST, 50.0, null, gen, false),
                Arguments.of(LOWER, FieldType.MARGINAL_COST, 40.0, null, gen, false),
                Arguments.of(LOWER, FieldType.PLANNED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.PLANNED_OUTAGE_RATE, 50.0, null, gen, false),
                Arguments.of(LOWER, FieldType.PLANNED_OUTAGE_RATE, 40.0, null, gen, false),
                Arguments.of(LOWER, FieldType.FORCED_OUTAGE_RATE, 60.0, null, gen, true),
                Arguments.of(LOWER, FieldType.FORCED_OUTAGE_RATE, 50.0, null, gen, false),
                Arguments.of(LOWER, FieldType.FORCED_OUTAGE_RATE, 40.0, null, gen, false),
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, gen, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, gen, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, gen, false),

                // --- BETWEEN --- //
                // Generator fields
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-600.0, -400.0), gen, true),
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-450.0, -400.0), gen, false),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(90.0, 110.0), gen, true),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(105.0, 110.0), gen, false),
                Arguments.of(BETWEEN, FieldType.TARGET_V, null, Set.of(10.0, 30.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_V, null, Set.of(25.0, 30.0), gen, false),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(20.0, 40.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(35.0, 40.0), gen, false),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(30.0, 50.0), gen, true),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(45.0, 50.0), gen, false),
                Arguments.of(BETWEEN, FieldType.RATED_S, null, Set.of(50.0, 70.0), gen, true),
                Arguments.of(BETWEEN, FieldType.RATED_S, null, Set.of(65.0, 70.0), gen, false),
                // GeneratorStartup extension fields
                Arguments.of(BETWEEN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(55.0, 60.0), gen, false),
                Arguments.of(BETWEEN, FieldType.MARGINAL_COST, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.MARGINAL_COST, null, Set.of(55.0, 60.0), gen, false),
                Arguments.of(BETWEEN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(55.0, 60.0), gen, false),
                Arguments.of(BETWEEN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(BETWEEN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(55.0, 60.0), gen, false),
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), gen, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), gen, false),

                // --- EXISTS --- //
                // Generator fields
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.TARGET_V, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_V, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.RATED_S, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.RATED_S, null, null, gen1, false),
                // GeneratorStartup extension fields
                Arguments.of(EXISTS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.MARGINAL_COST, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.MARGINAL_COST, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.PLANNED_OUTAGE_RATE, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.PLANNED_OUTAGE_RATE, null, null, gen1, false),
                Arguments.of(EXISTS, FieldType.FORCED_OUTAGE_RATE, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.FORCED_OUTAGE_RATE, null, null, gen1, false),
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, gen, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, gen1, false),

                // --- NOT_EXISTS --- //
                // Generator fields
                Arguments.of(NOT_EXISTS, FieldType.MIN_P, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.MIN_P, null, null, gen1, true),
                Arguments.of(NOT_EXISTS, FieldType.MAX_P, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.MAX_P, null, null, gen1, true),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_V, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_V, null, null, gen1, true),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_P, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_P, null, null, gen1, true),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_Q, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_Q, null, null, gen1, true),
                Arguments.of(NOT_EXISTS, FieldType.RATED_S, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.RATED_S, null, null, gen1, true),
                // GeneratorStartup extension fields
                Arguments.of(NOT_EXISTS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, null, gen1, true),
                Arguments.of(NOT_EXISTS, FieldType.MARGINAL_COST, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.MARGINAL_COST, null, null, gen1, true),
                Arguments.of(NOT_EXISTS, FieldType.PLANNED_OUTAGE_RATE, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.PLANNED_OUTAGE_RATE, null, null, gen1, true),
                Arguments.of(NOT_EXISTS, FieldType.FORCED_OUTAGE_RATE, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.FORCED_OUTAGE_RATE, null, null, gen1, true),
                // VoltageLevel fields
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, gen, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, gen1, true),

                // --- IN --- //
                // Generator fields
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-600.0, -500.0, -400.0), gen, true),
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-600.0, -400.0), gen, false),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(90.0, 100.0, 110.0), gen, true),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(90.0, 110.0), gen, false),
                Arguments.of(IN, FieldType.TARGET_V, null, Set.of(10.0, 20.0, 30.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_V, null, Set.of(10.0, 30.0), gen, false),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(20.0, 30.0, 40.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(20.0, 40.0), gen, false),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(30.0, 40.0, 50.0), gen, true),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(30.0, 50.0), gen, false),
                Arguments.of(IN, FieldType.RATED_S, null, Set.of(50.0, 60.0, 70.0), gen, true),
                Arguments.of(IN, FieldType.RATED_S, null, Set.of(50.0, 70.0), gen, false),
                // GeneratorStartup extension fields
                Arguments.of(IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 60.0), gen, false),
                Arguments.of(IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 60.0), gen, false),
                Arguments.of(IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, false),
                Arguments.of(IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, true),
                Arguments.of(IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), gen, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), gen, false),

                // --- NOT_IN --- //
                // Generator fields
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-600.0, -400.0), gen, true),
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-600.0, -500.0, -400.0), gen, false),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(90.0, 110.0), gen, true),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(90.0, 100.0, 110.0), gen, false),
                Arguments.of(NOT_IN, FieldType.TARGET_V, null, Set.of(10.0, 30.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_V, null, Set.of(10.0, 20.0, 30.0), gen, false),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(20.0, 40.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(20.0, 30.0, 40.0), gen, false),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(30.0, 50.0), gen, true),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(30.0, 40.0, 50.0), gen, false),
                Arguments.of(NOT_IN, FieldType.RATED_S, null, Set.of(50.0, 70.0), gen, true),
                Arguments.of(NOT_IN, FieldType.RATED_S, null, Set.of(50.0, 60.0, 70.0), gen, false),
                // GeneratorStartup extension fields
                Arguments.of(NOT_IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.PLANNED_ACTIVE_POWER_SET_POINT, null, Set.of(40.0, 50.0, 60.0), gen, false),
                Arguments.of(NOT_IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.MARGINAL_COST, null, Set.of(40.0, 50.0, 60.0), gen, false),
                Arguments.of(NOT_IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.PLANNED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, false),
                Arguments.of(NOT_IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 60.0), gen, true),
                Arguments.of(NOT_IN, FieldType.FORCED_OUTAGE_RATE, null, Set.of(40.0, 50.0, 60.0), gen, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), gen, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load.getP0()).thenReturn(77.0);
        Mockito.when(load.getQ0()).thenReturn(277.0);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        Load load1 = Mockito.mock(Load.class);
        Mockito.when(load1.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load1.getP0()).thenReturn(Double.NaN);
        Mockito.when(load1.getQ0()).thenReturn(Double.NaN);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(load1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, false),
                // Load fields
                Arguments.of(EQUALS, FieldType.P0, 77.0, null, load, true),
                Arguments.of(EQUALS, FieldType.P0, 50.0, null, load, false),
                Arguments.of(EQUALS, FieldType.Q0, 277.0, null, load, true),
                Arguments.of(EQUALS, FieldType.Q0, 300.0, null, load, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, false),
                // Load fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 72.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 77.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 78.0, null, load, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 272.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 277.0, null, load, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 278.0, null, load, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, false),
                // Load fields
                Arguments.of(GREATER, FieldType.P0, 70.0, null, load, true),
                Arguments.of(GREATER, FieldType.P0, 77.0, null, load, false),
                Arguments.of(GREATER, FieldType.P0, 78.0, null, load, false),
                Arguments.of(GREATER, FieldType.Q0, 270.0, null, load, true),
                Arguments.of(GREATER, FieldType.Q0, 277.0, null, load, false),
                Arguments.of(GREATER, FieldType.Q0, 278.0, null, load, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, false),
                // Load fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 80.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 77.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 70.0, null, load, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 300.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 277.0, null, load, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 77.0, null, load, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, load, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, load, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, load, false),
                // Load fields
                Arguments.of(LOWER, FieldType.P0, 99.0, null, load, true),
                Arguments.of(LOWER, FieldType.P0, 77.0, null, load, false),
                Arguments.of(LOWER, FieldType.P0, 70.0, null, load, false),
                Arguments.of(LOWER, FieldType.Q0, 300.0, null, load, true),
                Arguments.of(LOWER, FieldType.Q0, 277.0, null, load, false),
                Arguments.of(LOWER, FieldType.Q0, 270.0, null, load, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), load, false),
                // Load fields
                Arguments.of(BETWEEN, FieldType.P0, null, Set.of(60.0, 80.0), load, true),
                Arguments.of(BETWEEN, FieldType.P0, null, Set.of(60.0, 70.0), load, false),
                Arguments.of(BETWEEN, FieldType.Q0, null, Set.of(270.0, 280.0), load, true),
                Arguments.of(BETWEEN, FieldType.Q0, null, Set.of(100.0, 260.0), load, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load1, false),
                // Load fields
                Arguments.of(EXISTS, FieldType.P0, null, null, load, true),
                Arguments.of(EXISTS, FieldType.P0, null, null, load1, false),
                Arguments.of(EXISTS, FieldType.Q0, null, null, load, true),
                Arguments.of(EXISTS, FieldType.Q0, null, null, load1, false),

                // --- NOT_EXISTS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, load1, true),
                // Load fields
                Arguments.of(NOT_EXISTS, FieldType.P0, null, null, load, false),
                Arguments.of(NOT_EXISTS, FieldType.P0, null, null, load1, true),
                Arguments.of(NOT_EXISTS, FieldType.Q0, null, null, load, false),
                Arguments.of(NOT_EXISTS, FieldType.Q0, null, null, load1, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), load, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, false),
                // Load fields
                Arguments.of(IN, FieldType.P0, null, Set.of(12.0, 77.0, 100.0), load, true),
                Arguments.of(IN, FieldType.P0, null, Set.of(12.0, 100.0), load, false),
                Arguments.of(IN, FieldType.Q0, null, Set.of(120.0, 277.0, 300.0), load, true),
                Arguments.of(IN, FieldType.Q0, null, Set.of(120.0, 300.0), load, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), load, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), load, false),
                // Load fields
                Arguments.of(NOT_IN, FieldType.P0, null, Set.of(120.0, 140.0), load, true),
                Arguments.of(NOT_IN, FieldType.P0, null, Set.of(70.0, 77.0, 140.0), load, false),
                Arguments.of(NOT_IN, FieldType.Q0, null, Set.of(120.0, 300.0), load, true),
                Arguments.of(NOT_IN, FieldType.Q0, null, Set.of(120.0, 277.0, 300.0), load, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusTest() {

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(bus.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        Bus bus1 = Mockito.mock(Bus.class);
        Mockito.when(bus1.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(bus1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, bus, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, bus, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, bus, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), bus, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), bus, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, bus, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, bus1, false),

                // --- NOT_EXISTS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, bus, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, bus1, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), bus, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), bus, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), bus, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), bus, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusBarSectionTest() {

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(busbarSection.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        BusbarSection busbarSection1 = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection1.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(busbarSection1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, busbarSection, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, busbarSection, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, busbarSection, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), busbarSection, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), busbarSection, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, busbarSection, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, busbarSection1, false),

                // --- NOT_EXISTS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, busbarSection, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, busbarSection1, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), busbarSection, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), busbarSection, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), busbarSection, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), busbarSection, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Mockito.when(shuntCompensator.getMaximumSectionCount()).thenReturn(3);
        Mockito.when(shuntCompensator.getSectionCount()).thenReturn(3);
        Mockito.when(shuntCompensator.getModel(ShuntCompensatorLinearModel.class)).thenReturn(new ShuntCompensatorLinearModel() {
            @Override
            public double getBPerSection() {
                return 1.;
            }

            @Override
            public ShuntCompensatorLinearModel setBPerSection(double v) {
                return null;
            }

            @Override
            public double getGPerSection() {
                return 0.;
            }

            @Override
            public ShuntCompensatorLinearModel setGPerSection(double v) {
                return null;
            }

            @Override
            public ShuntCompensatorLinearModel setMaximumSectionCount(int i) {
                return null;
            }
        });
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(shuntCompensator.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        ShuntCompensator shuntCompensator1 = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator1.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(shuntCompensator1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, false),
                // Shunt Compensator Fields
                Arguments.of(EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.SECTION_COUNT, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, false),
                Arguments.of(EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 4., null, shuntCompensator, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SECTION_COUNT, 2., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SECTION_COUNT, 4., null, shuntCompensator, false),

                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 506., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 508., null, shuntCompensator, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 506., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 508., null, shuntCompensator, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 4., null, shuntCompensator, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 4., null, shuntCompensator, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(GREATER, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.MAXIMUM_SECTION_COUNT, 4., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SECTION_COUNT, 2., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.SECTION_COUNT, 3., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SECTION_COUNT, 4., null, shuntCompensator, false),

                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, 506., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, 508., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 506., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 508., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, 4., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, true),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, false),
                Arguments.of(GREATER, FieldType.SWITCHED_ON_SUSCEPTANCE, 4., null, shuntCompensator, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 4., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SECTION_COUNT, 4., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SECTION_COUNT, 3., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SECTION_COUNT, 2., null, shuntCompensator, false),

                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 508., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, 506., null, shuntCompensator, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 508., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 506., null, shuntCompensator, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 4., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 4., null, shuntCompensator, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(LOWER, FieldType.MAXIMUM_SECTION_COUNT, 4., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.MAXIMUM_SECTION_COUNT, 3., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.MAXIMUM_SECTION_COUNT, 2., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SECTION_COUNT, 4., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.SECTION_COUNT, 3., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SECTION_COUNT, 2., null, shuntCompensator, false),

                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, 508., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, 507., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, 506., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 508., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 507., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, 506., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, 4., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, 3., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, 2., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_SUSCEPTANCE, 4., null, shuntCompensator, true),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_SUSCEPTANCE, 3., null, shuntCompensator, false),
                Arguments.of(LOWER, FieldType.SWITCHED_ON_SUSCEPTANCE, 2., null, shuntCompensator, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(BETWEEN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2.0, 4.0), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(3.5, 4.0), shuntCompensator, false),
                Arguments.of(BETWEEN, FieldType.SECTION_COUNT, null, Set.of(2.0, 4.0), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.SECTION_COUNT, null, Set.of(3.5, 4.0), shuntCompensator, false),

                Arguments.of(BETWEEN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(506., 508.), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(508., 509.), shuntCompensator, false),
                Arguments.of(BETWEEN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(506., 508.), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(508., 509.), shuntCompensator, false),
                Arguments.of(BETWEEN, FieldType.MAX_SUSCEPTANCE, null, Set.of(2., 4.), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.MAX_SUSCEPTANCE, null, Set.of(4., 6.), shuntCompensator, false),
                Arguments.of(BETWEEN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(2., 4.), shuntCompensator, true),
                Arguments.of(BETWEEN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(4., 6.), shuntCompensator, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, shuntCompensator, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, shuntCompensator1, false),

                // --- NOT_EXISTS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, shuntCompensator, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, shuntCompensator1, true),

                // Shunt Compensator Fields

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), shuntCompensator, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(IN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2., 3., 4.), shuntCompensator, true),
                Arguments.of(IN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2., 4.), shuntCompensator, false),
                Arguments.of(IN, FieldType.SECTION_COUNT, null, Set.of(2., 3., 4.), shuntCompensator, true),
                Arguments.of(IN, FieldType.SECTION_COUNT, null, Set.of(2., 4.), shuntCompensator, false),

                Arguments.of(IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(507., 508.), shuntCompensator, true),
                Arguments.of(IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(508., 509.), shuntCompensator, false),
                Arguments.of(IN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(507., 508.), shuntCompensator, true),
                Arguments.of(IN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(508., 509.), shuntCompensator, false),
                Arguments.of(IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(3., 4.), shuntCompensator, true),
                Arguments.of(IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(4., 6.), shuntCompensator, false),
                Arguments.of(IN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(3., 4.), shuntCompensator, true),
                Arguments.of(IN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(4., 6.), shuntCompensator, false),
                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), shuntCompensator, false),

                // Shunt Compensator Fields
                Arguments.of(NOT_IN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2., 4.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.MAXIMUM_SECTION_COUNT, null, Set.of(2., 3., 4.), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.SECTION_COUNT, null, Set.of(2., 4.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SECTION_COUNT, null, Set.of(2., 3., 4.), shuntCompensator, false),

                Arguments.of(NOT_IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(506., 508.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(507., 509.), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(506., 508.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SWITCHED_ON_Q_AT_NOMINAL_V, null, Set.of(507., 509.), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(6., 4.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(3., 6.), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(6., 4.), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SWITCHED_ON_SUSCEPTANCE, null, Set.of(3., 6.), shuntCompensator, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLinesTest() {

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);
        // Line Fields

        Mockito.when(line.getR()).thenReturn(150.);
        Mockito.when(line.getX()).thenReturn(50.);
        Mockito.when(line.getG1()).thenReturn(10.);
        Mockito.when(line.getG2()).thenReturn(5.);
        Mockito.when(line.getB1()).thenReturn(200.);
        Mockito.when(line.getB2()).thenReturn(250.);

        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(13.);

        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(17.);

        // for testing none EXISTS
        Line line1 = Mockito.mock(Line.class);
        Mockito.when(line1.getType()).thenReturn(IdentifiableType.LINE);
        // VoltageLevel fields
        VoltageLevel voltageLevelNone = Mockito.mock(VoltageLevel.class);
        Terminal terminalNone = Mockito.mock(Terminal.class);
        Mockito.when(terminalNone.getVoltageLevel()).thenReturn(voltageLevelNone);
        Mockito.when(line1.getTerminal(TwoSides.ONE)).thenReturn(terminalNone);
        Mockito.when(voltageLevelNone.getNominalV()).thenReturn(Double.NaN);

        VoltageLevel voltageLevelNone2 = Mockito.mock(VoltageLevel.class);
        Terminal terminalNone2 = Mockito.mock(Terminal.class);
        Mockito.when(terminalNone2.getVoltageLevel()).thenReturn(voltageLevelNone2);
        Mockito.when(line1.getTerminal(TwoSides.TWO)).thenReturn(terminalNone2);
        Mockito.when(voltageLevelNone2.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, false),

                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_2, 14., null, line, false),

                // Line fields
                Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 150., null, line, true),
                Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 149., null, line, false),
                Arguments.of(EQUALS, FieldType.SERIE_REACTANCE, 50., null, line, true),
                Arguments.of(EQUALS, FieldType.SERIE_REACTANCE, 49., null, line, false),
                Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, true),
                Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 14., null, line, false),
                Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, true),
                Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 14., null, line, false),
                Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, true),
                Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 14., null, line, false),
                Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, true),
                Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 14., null, line, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, false),

                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 18., null, line, false),

                // Line fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 149., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 150., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 151., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 49., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 50., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 51., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 9., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 14., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 4., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 14., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 199., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 201., null, line, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 249., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 251., null, line, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 12., null, line, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, false),

                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 16., null, line, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 18., null, line, false),

                // Line fields
                Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 149., null, line, true),
                Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 150., null, line, false),
                Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 151., null, line, false),
                Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 49., null, line, true),
                Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 50., null, line, false),
                Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 51., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_1, 9., null, line, true),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_1, 14., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_2, 4., null, line, true),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE_2, 14., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_1, 199., null, line, true),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_1, 201., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_2, 249., null, line, true),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, false),
                Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE_2, 251., null, line, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12., null, line, false),

                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 18., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12., null, line, false),

                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 151., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 150., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 149., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 51., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 50., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 49., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 11., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_1, 9., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 6., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE_2, 4., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 201., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_1, 199., null, line, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 251., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE_2, 249., null, line, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 14., null, line, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 13., null, line, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 12., null, line, false),

                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 18., null, line, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 17., null, line, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 12., null, line, false),

                Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 151., null, line, true),
                Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 150., null, line, false),
                Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 149., null, line, false),
                Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 51., null, line, true),
                Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 50., null, line, false),
                Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 49., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_1, 11., null, line, true),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_1, 10., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_1, 9., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_2, 6., null, line, true),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_2, 5., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE_2, 4., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_1, 201., null, line, true),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_1, 200., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_1, 199., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_2, 251., null, line, true),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_2, 250., null, line, false),
                Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE_2, 249., null, line, false),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 14.), line, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(13.5, 14.), line, false),

                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(16., 18.), line, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(17.5, 19.), line, false),

                Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 160.), line, true),
                Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 140.), line, false),
                Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE, null, Set.of(49., 51.), line, true),
                Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE, null, Set.of(12., 14.), line, false),
                Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(9., 14.), line, true),
                Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(12., 14.), line, false),
                Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(2., 6.), line, true),
                Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(12., 14.), line, false),
                Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 200.), line, true),
                Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(201., 204.), line, false),
                Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(245., 255.), line, true),
                Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(240., 249.), line, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, line, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, line1, false),

                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, line, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, line1, false),

                // --- NOT_EXISTS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, line1, true),

                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, line, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, line1, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 13., 14.), line, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 14.), line, false),

                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12., 17., 14.), line, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12., 14.), line, false),

                Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 150.), line, true),
                Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 140.), line, false),
                Arguments.of(IN, FieldType.SERIE_REACTANCE, null, Set.of(49., 50.), line, true),
                Arguments.of(IN, FieldType.SERIE_REACTANCE, null, Set.of(12., 14.), line, false),
                Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(9., 10.), line, true),
                Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(12., 14.), line, false),
                Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(2., 5., 14.), line, true),
                Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(2., 14.), line, false),
                Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 200.), line, true),
                Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 204.), line, false),
                Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(250., 240.), line, true),
                Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(240., 249.), line, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 14.), line, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12., 13., 14.), line, false),

                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12., 14.), line, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12., 17., 14.), line, false),

                Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 149.), line, true),
                Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(120., 140., 150.), line, false),
                Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, null, Set.of(49., 51.), line, true),
                Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, null, Set.of(50., 49., 51.), line, false),
                Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(9., 11.), line, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE_1, null, Set.of(9., 10., 11.), line, false),
                Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(4., 6.), line, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE_2, null, Set.of(4., 5., 6.), line, false),
                Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 201.), line, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE_1, null, Set.of(199., 200., 201.), line, false),
                Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(249., 251.), line, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE_2, null, Set.of(249., 250., 251.), line, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        Mockito.when(battery.getMinP()).thenReturn(-5.0);
        Mockito.when(battery.getMaxP()).thenReturn(5.0);
        Mockito.when(battery.getTargetP()).thenReturn(3.0);
        Mockito.when(battery.getTargetQ()).thenReturn(1.0);
        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(battery.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        Battery battery1 = Mockito.mock(Battery.class);

        Mockito.when(battery1.getType()).thenReturn(IdentifiableType.BATTERY);
        Mockito.when(battery1.getMinP()).thenReturn(Double.NaN);
        Mockito.when(battery1.getMaxP()).thenReturn(Double.NaN);
        Mockito.when(battery1.getTargetP()).thenReturn(Double.NaN);
        Mockito.when(battery1.getTargetQ()).thenReturn(Double.NaN);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(battery1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, false),
                //Battery fields
                Arguments.of(EQUALS, FieldType.MIN_P, -5.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.MIN_P, -4.0, null, battery, false),
                Arguments.of(EQUALS, FieldType.MAX_P, 5.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.MAX_P, 4.0, null, battery, false),
                Arguments.of(EQUALS, FieldType.TARGET_P, 3.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.TARGET_P, 4.0, null, battery, false),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 1.0, null, battery, true),
                Arguments.of(EQUALS, FieldType.TARGET_Q, 0.0, null, battery, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, false),
                //Battery fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -6.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -5.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_P, -3.0, null, battery, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 4.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 5.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 6.0, null, battery, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 2.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 3.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_P, 6.0, null, battery, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 0.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 1.0, null, battery, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.TARGET_Q, 2.0, null, battery, false),
                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, battery, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, false),
                //Battery fields
                Arguments.of(GREATER, FieldType.MIN_P, -6.0, null, battery, true),
                Arguments.of(GREATER, FieldType.MIN_P, -5.0, null, battery, false),
                Arguments.of(GREATER, FieldType.MIN_P, -4.0, null, battery, false),
                Arguments.of(GREATER, FieldType.MAX_P, 2.0, null, battery, true),
                Arguments.of(GREATER, FieldType.MAX_P, 5.0, null, battery, false),
                Arguments.of(GREATER, FieldType.MAX_P, 6.0, null, battery, false),
                Arguments.of(GREATER, FieldType.TARGET_P, 2.0, null, battery, true),
                Arguments.of(GREATER, FieldType.TARGET_P, 5.0, null, battery, false),
                Arguments.of(GREATER, FieldType.TARGET_P, 3.0, null, battery, false),
                Arguments.of(GREATER, FieldType.TARGET_Q, 0.0, null, battery, true),
                Arguments.of(GREATER, FieldType.TARGET_Q, 2.0, null, battery, false),
                Arguments.of(GREATER, FieldType.TARGET_Q, 3.0, null, battery, false),

                // --- LOWER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, battery, false),
                //Battery fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -4.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -5.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_P, -6.0, null, battery, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 7.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 5.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 2.0, null, battery, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 5.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 3.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_P, 2.0, null, battery, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 2.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 1.0, null, battery, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.TARGET_Q, 0.0, null, battery, false),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, battery, true),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, battery, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, battery, false),
                //Battery fields
                Arguments.of(LOWER, FieldType.MIN_P, -4.0, null, battery, true),
                Arguments.of(LOWER, FieldType.MIN_P, -5.0, null, battery, false),
                Arguments.of(LOWER, FieldType.MIN_P, -6.0, null, battery, false),
                Arguments.of(LOWER, FieldType.MAX_P, 7.0, null, battery, true),
                Arguments.of(LOWER, FieldType.MAX_P, 5.0, null, battery, false),
                Arguments.of(LOWER, FieldType.MAX_P, 2.0, null, battery, false),
                Arguments.of(LOWER, FieldType.TARGET_P, 5.0, null, battery, true),
                Arguments.of(LOWER, FieldType.TARGET_P, 3.0, null, battery, false),
                Arguments.of(LOWER, FieldType.TARGET_P, 2.0, null, battery, false),
                Arguments.of(LOWER, FieldType.TARGET_Q, 2.0, null, battery, true),
                Arguments.of(LOWER, FieldType.TARGET_Q, 1.0, null, battery, false),
                Arguments.of(LOWER, FieldType.TARGET_Q, 0.0, null, battery, false),
                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), battery, false),
                //Battery fields
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-3.0, -14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.MIN_P, null, Set.of(-12.0, -14.0), battery, false),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(2.0, 14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(12.0, 14.0), battery, false),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(2.0, 14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.TARGET_P, null, Set.of(12.0, 14.0), battery, false),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(0.0, 14.0), battery, true),
                Arguments.of(BETWEEN, FieldType.TARGET_Q, null, Set.of(2.0, 14.0), battery, false),
                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, battery1, false),
                //Battery fields
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.MIN_P, null, null, battery1, false),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.MAX_P, null, null, battery1, false),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.TARGET_P, null, null, battery1, false),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, battery, true),
                Arguments.of(EXISTS, FieldType.TARGET_Q, null, null, battery1, false),
                // --- NOT_EXISTS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, battery1, true),
                //Battery fields
                Arguments.of(NOT_EXISTS, FieldType.MIN_P, null, null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.MIN_P, null, null, battery1, true),
                Arguments.of(NOT_EXISTS, FieldType.MAX_P, null, null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.MAX_P, null, null, battery1, true),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_P, null, null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_P, null, null, battery1, true),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_Q, null, null, battery, false),
                Arguments.of(NOT_EXISTS, FieldType.TARGET_Q, null, null, battery1, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), battery, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), battery, false),
                //Battery fields
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-3.0, -5.0, -14.0), battery, true),
                Arguments.of(IN, FieldType.MIN_P, null, Set.of(-12.0, -6.0, -14.0), battery, false),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(2.0, 5.0, 14.0), battery, true),
                Arguments.of(IN, FieldType.MAX_P, null, Set.of(12.0, 6.0, 14.0), battery, false),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(2.0, 3.0, 14.0), battery, true),
                Arguments.of(IN, FieldType.TARGET_P, null, Set.of(12.0, 4.0, 14.0), battery, false),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(0.0, 1.0, 14.0), battery, true),
                Arguments.of(IN, FieldType.TARGET_Q, null, Set.of(2.0, 3.0, 14.0), battery, false),
                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), battery, false),
                //Battery fields
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-3.0, -14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.MIN_P, null, Set.of(-12.0, -5.0, -14.0), battery, false),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(2.0, 14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(12.0, 5.0, 14.0), battery, false),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(2.0, 14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.TARGET_P, null, Set.of(12.0, 3.0, 14.0), battery, false),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(0.0, 14.0), battery, true),
                Arguments.of(NOT_IN, FieldType.TARGET_Q, null, Set.of(2.0, 1.0, 14.0), battery, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForVoltageLevelTest() {

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getExtension(IdentifiableShortCircuit.class)).thenReturn(new IdentifiableShortCircuitImpl(voltageLevel, 1.0, 4.0));
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);
        Mockito.when(voltageLevel.getLowVoltageLimit()).thenReturn(40.0);
        Mockito.when(voltageLevel.getHighVoltageLimit()).thenReturn(400.0);

        // for testing none EXISTS
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);
        Mockito.when(voltageLevel1.getLowVoltageLimit()).thenReturn(Double.NaN);
        Mockito.when(voltageLevel1.getHighVoltageLimit()).thenReturn(Double.NaN);

        return Stream.of(
            // --- EQUALS --- //
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, false),
            Arguments.of(EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, false),
            Arguments.of(EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, false),
            Arguments.of(EQUALS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 1.0, null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 2.0, null, voltageLevel, false),
            Arguments.of(EQUALS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 4.0, null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 3.0, null, voltageLevel, false),

            // --- GREATER_OR_EQUALS --- //
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, voltageLevel, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 30.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 300.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 0.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 1.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 2.0, null, voltageLevel, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 3.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 4.0, null, voltageLevel, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 5.0, null, voltageLevel, false),

            // --- GREATER --- //
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, true),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.LOW_VOLTAGE_LIMIT, 30.0, null, voltageLevel, true),
            Arguments.of(GREATER, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_LIMIT, 300.0, null, voltageLevel, true),
            Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 0.0, null, voltageLevel, true),
            Arguments.of(GREATER, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 1.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 2.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 3.0, null, voltageLevel, true),
            Arguments.of(GREATER, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 4.0, null, voltageLevel, false),
            Arguments.of(GREATER, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 5.0, null, voltageLevel, false),

            // --- LOWER_OR_EQUALS --- //
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_LIMIT, 30.0, null, voltageLevel, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_LIMIT, 300.0, null, voltageLevel, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 2.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 1.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 0.0, null, voltageLevel, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 5.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 4.0, null, voltageLevel, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 3.0, null, voltageLevel, false),

            // --- LOWER --- //
            // voltageLevelerator fields
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, voltageLevel, true),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.LOW_VOLTAGE_LIMIT, 50.0, null, voltageLevel, true),
            Arguments.of(LOWER, FieldType.LOW_VOLTAGE_LIMIT, 40.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.LOW_VOLTAGE_LIMIT, 30.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_LIMIT, 500.0, null, voltageLevel, true),
            Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_LIMIT, 400.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_LIMIT, 300.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 2.0, null, voltageLevel, true),
            Arguments.of(LOWER, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 1.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, 1.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 5.0, null, voltageLevel, true),
            Arguments.of(LOWER, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 4.0, null, voltageLevel, false),
            Arguments.of(LOWER, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, 3.0, null, voltageLevel, false),

            // --- BETWEEN --- //
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), voltageLevel, true),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 50.0), voltageLevel, true),
            Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(50.0, 60.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(20.0, 30.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 500.0), voltageLevel, true),
            Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(500.0, 600.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(200.0, 300.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(0.0, 2.0), voltageLevel, true),
            Arguments.of(BETWEEN, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(2.0, 6.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(3.0, 4.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(3.0, 5.0), voltageLevel, true),
            Arguments.of(BETWEEN, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(5.0, 6.0), voltageLevel, false),
            Arguments.of(BETWEEN, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(2.0, 3.0), voltageLevel, false),

            // --- EXISTS --- //
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, voltageLevel, true),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, voltageLevel1, false),
            Arguments.of(EXISTS, FieldType.LOW_VOLTAGE_LIMIT, null, null, voltageLevel, true),
            Arguments.of(EXISTS, FieldType.LOW_VOLTAGE_LIMIT, null, null, voltageLevel1, false),
            Arguments.of(EXISTS, FieldType.HIGH_VOLTAGE_LIMIT, null, null, voltageLevel, true),
            Arguments.of(EXISTS, FieldType.HIGH_VOLTAGE_LIMIT, null, null, voltageLevel1, false),
            Arguments.of(EXISTS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, null, voltageLevel, true),
            Arguments.of(EXISTS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, null, voltageLevel1, false),
            Arguments.of(EXISTS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, null, voltageLevel, true),
            Arguments.of(EXISTS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, null, voltageLevel1, false),

            // --- NOT_EXISTS --- //
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, voltageLevel, false),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, voltageLevel1, true),
            Arguments.of(NOT_EXISTS, FieldType.LOW_VOLTAGE_LIMIT, null, null, voltageLevel, false),
            Arguments.of(NOT_EXISTS, FieldType.LOW_VOLTAGE_LIMIT, null, null, voltageLevel1, true),
            Arguments.of(NOT_EXISTS, FieldType.HIGH_VOLTAGE_LIMIT, null, null, voltageLevel, false),
            Arguments.of(NOT_EXISTS, FieldType.HIGH_VOLTAGE_LIMIT, null, null, voltageLevel1, true),
            Arguments.of(NOT_EXISTS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, null, voltageLevel, false),
            Arguments.of(NOT_EXISTS, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, null, voltageLevel1, true),
            Arguments.of(NOT_EXISTS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, null, voltageLevel, false),
            Arguments.of(NOT_EXISTS, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, null, voltageLevel1, true),

            // --- IN --- //
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), voltageLevel, true),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), voltageLevel, false),
            Arguments.of(IN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 40.0, 50.0), voltageLevel, true),
            Arguments.of(IN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 50.0), voltageLevel, false),
            Arguments.of(IN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 400.0, 500.0), voltageLevel, true),
            Arguments.of(IN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 500.0), voltageLevel, false),
            Arguments.of(IN, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(1.0, 4.0, 5.0), voltageLevel, true),
            Arguments.of(IN, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(3.0, 5.0), voltageLevel, false),
            Arguments.of(IN, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(1.0, 4.0, 5.0), voltageLevel, true),
            Arguments.of(IN, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(3.0, 5.0), voltageLevel, false),

            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), voltageLevel, false),
            Arguments.of(NOT_IN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 50.0), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.LOW_VOLTAGE_LIMIT, null, Set.of(30.0, 40.0, 50.0), voltageLevel, false),
            Arguments.of(NOT_IN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 500.0), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.HIGH_VOLTAGE_LIMIT, null, Set.of(300.0, 400.0, 500.0), voltageLevel, false),
            Arguments.of(NOT_IN, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(3.0, 5.0), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.LOW_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(1.0, 4.0, 5.0), voltageLevel, false),
            Arguments.of(NOT_IN, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(3.0, 5.0), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.HIGH_SHORT_CIRCUIT_CURRENT_LIMIT, null, Set.of(1.0, 4.0, 5.0), voltageLevel, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForTwoWindingTransformerTest() {

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twoWindingsTransformer.getR()).thenReturn(0.1);
        Mockito.when(twoWindingsTransformer.getX()).thenReturn(0.2);
        Mockito.when(twoWindingsTransformer.getG()).thenReturn(0.3);
        Mockito.when(twoWindingsTransformer.getB()).thenReturn(0.4);
        Mockito.when(twoWindingsTransformer.getRatedS()).thenReturn(100.0);
        Mockito.when(twoWindingsTransformer.getRatedU1()).thenReturn(50.);
        Mockito.when(twoWindingsTransformer.getRatedU2()).thenReturn(300.0);

        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(twoWindingsTransformer.getTerminal1()).thenReturn(terminal);
        Mockito.when(twoWindingsTransformer.getTerminal2()).thenReturn(terminal);
        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.getTargetV()).thenReturn(13.0);
        Mockito.when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        // PhaseTapChanger fields
        PhaseTapChanger phaseTapChanger = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger.getRegulationValue()).thenReturn(200.);
        Mockito.when(twoWindingsTransformer.getPhaseTapChanger()).thenReturn(phaseTapChanger);

        // for testing none EXISTS
        TwoWindingsTransformer twoWindingsTransformer1 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer1.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twoWindingsTransformer1.getR()).thenReturn(Double.NaN);
        Mockito.when(twoWindingsTransformer1.getX()).thenReturn(Double.NaN);
        Mockito.when(twoWindingsTransformer1.getG()).thenReturn(Double.NaN);
        Mockito.when(twoWindingsTransformer1.getB()).thenReturn(Double.NaN);
        Mockito.when(twoWindingsTransformer1.getRatedS()).thenReturn(Double.NaN);
        Mockito.when(twoWindingsTransformer1.getRatedU1()).thenReturn(Double.NaN);
        Mockito.when(twoWindingsTransformer1.getRatedU2()).thenReturn(Double.NaN);

        // Terminal fields
        Terminal terminal1 = Mockito.mock(Terminal.class);
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(twoWindingsTransformer1.getTerminal1()).thenReturn(terminal1);
        Mockito.when(twoWindingsTransformer1.getTerminal2()).thenReturn(terminal1);

        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger1 = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger1.getTargetV()).thenReturn(Double.NaN);
        Mockito.when(twoWindingsTransformer1.getRatioTapChanger()).thenReturn(ratioTapChanger1);

        // null RatioTapChanger
        TwoWindingsTransformer twoWindingsTransformer2 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer2.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twoWindingsTransformer2.getRatioTapChanger()).thenReturn(null);

        // PhaseTapChanger fields
        PhaseTapChanger phaseTapChanger1 = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger1.getRegulationValue()).thenReturn(Double.NaN);
        Mockito.when(twoWindingsTransformer1.getPhaseTapChanger()).thenReturn(phaseTapChanger1);

        // null PhaseTapChanger
        Mockito.when(twoWindingsTransformer2.getPhaseTapChanger()).thenReturn(null);

        return Stream.of(
            // --- EQUALS --- //
            // Terminal fields
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, twoWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(EQUALS, FieldType.RATIO_TARGET_V, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATIO_TARGET_V, 12.0, null, twoWindingsTransformer, false),
            // PhaseTapChanger fields
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_VALUE, 200., null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_VALUE, 250., null, twoWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 0.2, null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE, 0.2, null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE, 0.3, null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_CONDUCTANCE, 0.3, null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_CONDUCTANCE, 0.4, null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE, 0.4, null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE, 0.5, null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_S, 100.0, null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_S, 200.0, null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_1, 50., null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_1, 300., null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_2, 300., null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_2, 50., null, twoWindingsTransformer, false),

            // --- GREATER_OR_EQUALS --- //
            // Terminal
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14.0, null, twoWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 14.0, null, twoWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V, 12.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V, 14.0, null, twoWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.05, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.15, null, twoWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.15, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.2, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.25, null, twoWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE, 0.25, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE, 0.3, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE, 0.35, null, twoWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE, 0.35, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE, 0.4, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE, 0.45, null, twoWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 50.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 100.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S, 150.0, null, twoWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 49., null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 50., null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 51., null, twoWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 299., null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 300., null, twoWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 301., null, twoWindingsTransformer, false),

            // --- GREATER --- //
            // Terminal
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 14.0, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 14.0, null, twoWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V, 12.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V, 13.0, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V, 14.0, null, twoWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.05, null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.1, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.15, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 0.15, null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 0.2, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 0.25, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE, 0.25, null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE, 0.3, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE, 0.35, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE, 0.35, null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE, 0.4, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE, 0.45, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_S, 50.0, null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_S, 100.0, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_S, 150.0, null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_1, 49., null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_1, 50., null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_1, 51., null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_2, 299., null, twoWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_2, 300., null, twoWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_2, 301., null, twoWindingsTransformer, false),

            // --- LOWER_OR_EQUALS --- //
            // Terminal
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, twoWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 14.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, twoWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V, 14.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V, 13.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V, 12.0, null, twoWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.15, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.05, null, twoWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.25, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.2, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.15, null, twoWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE, 0.35, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE, 0.3, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE, 0.25, null, twoWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE, 0.45, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE, 0.4, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE, 0.35, null, twoWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 150.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 100.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S, 50.0, null, twoWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 51., null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 50., null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 49., null, twoWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 301., null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 300., null, twoWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 299., null, twoWindingsTransformer, false),

            // --- LOWER --- //
            // Terminal
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 14.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 14.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, twoWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V, 14.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V, 13.0, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V, 12.0, null, twoWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.15, null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.1, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.05, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 0.25, null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 0.2, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 0.15, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE, 0.35, null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE, 0.3, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE, 0.25, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE, 0.45, null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE, 0.4, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE, 0.35, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_S, 150.0, null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_S, 100.0, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_S, 50.0, null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_1, 51., null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_1, 50., null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_1, 49., null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_2, 301., null, twoWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_2, 300., null, twoWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_2, 299., null, twoWindingsTransformer, false),

            // --- BETWEEN --- //
            // Terminal
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(13.5, 14.0), twoWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(13.5, 14.0), twoWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(BETWEEN, FieldType.RATIO_TARGET_V, null, Set.of(12.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATIO_TARGET_V, null, Set.of(13.5, 14.0), twoWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.15), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(0.15, 0.25), twoWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE, null, Set.of(0.15, 0.25), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE, null, Set.of(0.25, 0.35), twoWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_CONDUCTANCE, null, Set.of(0.25, 0.35), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_CONDUCTANCE, null, Set.of(0.35, 0.45), twoWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_SUSCEPTANCE, null, Set.of(0.35, 0.45), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_SUSCEPTANCE, null, Set.of(0.45, 0.55), twoWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_S, null, Set.of(50.0, 150.0), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_S, null, Set.of(150.0, 250.0), twoWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_1, null, Set.of(48., 52.), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_1, null, Set.of(55., 70.), twoWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_2, null, Set.of(295., 305.), twoWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_2, null, Set.of(320., 350.), twoWindingsTransformer, false),

            // --- EXISTS --- //
            // Terminal
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, twoWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, twoWindingsTransformer1, false),
            // RatioTapChanger fields
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V, null, null, twoWindingsTransformer1, false),
            // TwoWindingsTransformer fields
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE, null, null, twoWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE, null, null, twoWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_CONDUCTANCE, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_CONDUCTANCE, null, null, twoWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE, null, null, twoWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_S, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_S, null, null, twoWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_1, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_1, null, null, twoWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_2, null, null, twoWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_2, null, null, twoWindingsTransformer1, false),

            // --- NOT_EXISTS --- //
            // Terminal
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, twoWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, twoWindingsTransformer1, true),
            // RatioTapChanger fields
            Arguments.of(NOT_EXISTS, FieldType.RATIO_TARGET_V, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATIO_TARGET_V, null, null, twoWindingsTransformer1, true),
            // TwoWindingsTransformer fields
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE, null, null, twoWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE, null, null, twoWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_CONDUCTANCE, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_CONDUCTANCE, null, null, twoWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE, null, null, twoWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_S, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_S, null, null, twoWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_1, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_1, null, null, twoWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_2, null, null, twoWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_2, null, null, twoWindingsTransformer1, true),

            // --- IN --- //
            // Terminal
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 13.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 13.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), twoWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(IN, FieldType.RATIO_TARGET_V, null, Set.of(12.0, 13.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATIO_TARGET_V, null, Set.of(12.0, 14.0), twoWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.1, 0.15), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.15), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.SERIE_REACTANCE, null, Set.of(0.15, 0.2, 0.25), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.SERIE_REACTANCE, null, Set.of(0.15, 0.25), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.MAGNETIZING_CONDUCTANCE, null, Set.of(0.25, 0.3, 0.35), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.MAGNETIZING_CONDUCTANCE, null, Set.of(0.25, 0.35), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.MAGNETIZING_SUSCEPTANCE, null, Set.of(0.35, 0.4, 0.45), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.MAGNETIZING_SUSCEPTANCE, null, Set.of(0.35, 0.45), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_S, null, Set.of(50.0, 100.0, 150.0), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_S, null, Set.of(50.0, 150.0), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_1, null, Set.of(49.0, 50.0, 51.0), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_1, null, Set.of(49.0, 51.0), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_2, null, Set.of(299., 300., 301.), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_2, null, Set.of(299., 301.), twoWindingsTransformer, false),

            // --- NOT_IN --- //
            // Terminal
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 13.0, 14.0), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 13.0, 14.0), twoWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(NOT_IN, FieldType.RATIO_TARGET_V, null, Set.of(12.0, 14.0), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATIO_TARGET_V, null, Set.of(12.0, 13.0, 14.0), twoWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.15), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.1, 0.15), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, null, Set.of(0.15, 0.25), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, null, Set.of(0.15, 0.2, 0.25), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_CONDUCTANCE, null, Set.of(0.25, 0.35), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_CONDUCTANCE, null, Set.of(0.25, 0.3, 0.35), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_SUSCEPTANCE, null, Set.of(0.35, 0.45), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_SUSCEPTANCE, null, Set.of(0.35, 0.4, 0.45), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_S, null, Set.of(50.0, 150.0), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_S, null, Set.of(50.0, 100.0, 150.0), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_1, null, Set.of(49., 51.), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_1, null, Set.of(49., 50., 51.), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_2, null, Set.of(299., 301.), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_2, null, Set.of(299., 300., 301.), twoWindingsTransformer, false),

            // null RatioTapChanger
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V, null, null, twoWindingsTransformer2, false),

            // null PhaseTapChanger
            Arguments.of(EXISTS, FieldType.PHASE_REGULATION_VALUE, null, null, twoWindingsTransformer2, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForThreeWindingTransformerTest() {
        // transfo
        ThreeWindingsTransformer threeWindingsTransformer = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        ThreeWindingsTransformer.Leg leg = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg.getR()).thenReturn(0.1);
        Mockito.when(leg.getX()).thenReturn(0.2);
        Mockito.when(leg.getG()).thenReturn(0.3);
        Mockito.when(leg.getB()).thenReturn(0.4);
        Mockito.when(leg.getRatedS()).thenReturn(100.0);
        Mockito.when(leg.getRatedU()).thenReturn(50.);

        // Terminal fields
        Terminal terminal = Mockito.mock(Terminal.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(leg.getTerminal()).thenReturn(terminal);
        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger.getTargetV()).thenReturn(13.0);
        Mockito.when(leg.getRatioTapChanger()).thenReturn(ratioTapChanger);
        // PhaseTapChanger fields
        PhaseTapChanger phaseTapChanger = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger.getRegulationValue()).thenReturn(200.);
        Mockito.when(leg.getPhaseTapChanger()).thenReturn(phaseTapChanger);

        Mockito.when(threeWindingsTransformer.getLeg1()).thenReturn(leg);
        Mockito.when(threeWindingsTransformer.getLeg2()).thenReturn(leg);
        Mockito.when(threeWindingsTransformer.getLeg3()).thenReturn(leg);

        // transfo 1
        // for testing none EXISTS
        ThreeWindingsTransformer threeWindingsTransformer1 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer1.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        ThreeWindingsTransformer.Leg leg1 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg1.getR()).thenReturn(Double.NaN);
        Mockito.when(leg1.getX()).thenReturn(Double.NaN);
        Mockito.when(leg1.getG()).thenReturn(Double.NaN);
        Mockito.when(leg1.getB()).thenReturn(Double.NaN);
        Mockito.when(leg1.getRatedS()).thenReturn(Double.NaN);
        Mockito.when(leg1.getRatedU()).thenReturn(Double.NaN);

        // Terminal fields
        Terminal terminal1 = Mockito.mock(Terminal.class);
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(leg1.getTerminal()).thenReturn(terminal1);

        // RatioTapChanger fields
        RatioTapChanger ratioTapChanger1 = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger1.getTargetV()).thenReturn(Double.NaN);
        Mockito.when(leg1.getRatioTapChanger()).thenReturn(ratioTapChanger1);

        // PhaseTapChanger fields
        PhaseTapChanger phaseTapChanger1 = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger1.getRegulationValue()).thenReturn(Double.NaN);
        Mockito.when(leg1.getPhaseTapChanger()).thenReturn(phaseTapChanger1);

        Mockito.when(threeWindingsTransformer1.getLeg1()).thenReturn(leg1);
        Mockito.when(threeWindingsTransformer1.getLeg2()).thenReturn(leg1);
        Mockito.when(threeWindingsTransformer1.getLeg3()).thenReturn(leg1);

        // transfo 2
        // null RatioTapChanger
        ThreeWindingsTransformer threeWindingsTransformer2 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer2.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        ThreeWindingsTransformer.Leg leg2 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg2.getRatioTapChanger()).thenReturn(null);

        // null PhaseTapChanger
        Mockito.when(leg2.getPhaseTapChanger()).thenReturn(null);
        Mockito.when(threeWindingsTransformer2.getLeg1()).thenReturn(leg2);
        Mockito.when(threeWindingsTransformer2.getLeg2()).thenReturn(leg2);
        Mockito.when(threeWindingsTransformer2.getLeg3()).thenReturn(leg2);

        return Stream.of(
            // --- EQUALS --- //
            // Terminal fields
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_3, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE_3, 12.0, null, threeWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(EQUALS, FieldType.RATIO_TARGET_V1, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATIO_TARGET_V2, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATIO_TARGET_V3, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATIO_TARGET_V1, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_TARGET_V2, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_TARGET_V3, 12.0, null, threeWindingsTransformer, false),
            // PhaseTapChanger fields
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_VALUE_1, 200., null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_VALUE_2, 200., null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_VALUE_3, 200., null, threeWindingsTransformer, true),

            // reprendre ici
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_VALUE_1, 250., null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_VALUE_2, 250., null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_VALUE_3, 250., null, threeWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE_1, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE_2, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE_3, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE_1, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE_2, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE_3, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE_1, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE_2, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE_3, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE_1, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE_2, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE_3, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.5, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.5, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.5, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_S1, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_S2, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_S3, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_S1, 200.0, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_S2, 200.0, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_S3, 200.0, null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_1, 50., null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_1, 300., null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_2, 50., null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_2, 300., null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_3, 50., null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.RATED_VOLTAGE_3, 300., null, threeWindingsTransformer, false),

            // --- GREATER_OR_EQUALS --- //
            // Terminal
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 14.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_3, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_3, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_3, 14.0, null, threeWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V1, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V2, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V3, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V1, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V2, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V3, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V1, 14.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V2, 14.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATIO_TARGET_V3, 14.0, null, threeWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_1, 0.05, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_2, 0.05, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_3, 0.05, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_1, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_2, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_3, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_1, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_2, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE_3, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_1, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_2, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_3, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_1, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_2, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_3, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_1, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_2, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE_3, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.45, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.45, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.45, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S1, 50.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S2, 50.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S3, 50.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S1, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S2, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S3, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S1, 150.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S2, 150.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_S3, 150.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 49., null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 50., null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 51., null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 49., null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 50., null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 51., null, threeWindingsTransformer, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_3, 49., null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_3, 50., null, threeWindingsTransformer, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.RATED_VOLTAGE_3, 51., null, threeWindingsTransformer, false),

            // --- GREATER --- //
            // Terminal
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_1, 14.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_2, 14.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_3, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_3, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE_3, 14.0, null, threeWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V1, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V1, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V1, 14.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V2, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V2, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V2, 14.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V3, 12.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V3, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATIO_TARGET_V3, 14.0, null, threeWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_1, 0.05, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_1, 0.1, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_1, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_2, 0.05, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_2, 0.1, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_2, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_3, 0.05, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_3, 0.1, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE_3, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_1, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_1, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_1, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_2, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_2, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_2, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_3, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_3, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE_3, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.45, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.45, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.45, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_S1, 50.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_S1, 100.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_S1, 150.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_S2, 50.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_S2, 100.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_S2, 150.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_S3, 50.0, null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_S3, 100.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_S3, 150.0, null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_1, 49., null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_1, 50., null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_1, 51., null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_2, 49., null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_2, 50., null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_2, 51., null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_3, 49., null, threeWindingsTransformer, true),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_3, 50., null, threeWindingsTransformer, false),
            Arguments.of(GREATER, FieldType.RATED_VOLTAGE_3, 51., null, threeWindingsTransformer, false),

            // --- LOWER_OR_EQUALS --- //
            // Terminal
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_3, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_3, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE_3, 12.0, null, threeWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V1, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V1, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V1, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V2, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V2, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V2, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V3, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V3, 13.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATIO_TARGET_V3, 12.0, null, threeWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_1, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_1, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_1, 0.05, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_2, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_2, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_2, 0.05, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_3, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_3, 0.1, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE_3, 0.05, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_1, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_1, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_1, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_2, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_2, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_2, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_3, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_3, 0.2, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE_3, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.3, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.45, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.45, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.45, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.4, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S1, 150.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S1, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S1, 50.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S2, 150.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S2, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S2, 50.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S3, 150.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S3, 100.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_S3, 50.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 51., null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 50., null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_1, 49., null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 51., null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 50., null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_2, 49., null, threeWindingsTransformer, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_3, 51., null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_3, 50., null, threeWindingsTransformer, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.RATED_VOLTAGE_3, 49., null, threeWindingsTransformer, false),

            // --- LOWER --- //
            // Terminal
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_1, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_2, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_3, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_3, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE_3, 12.0, null, threeWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V1, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V1, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V1, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V2, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V2, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V2, 12.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V3, 14.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V3, 13.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATIO_TARGET_V3, 12.0, null, threeWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_1, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_1, 0.1, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_1, 0.05, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_2, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_2, 0.1, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_2, 0.05, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_3, 0.15, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_3, 0.1, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE_3, 0.05, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_1, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_1, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_1, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_2, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_2, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_2, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_3, 0.25, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_3, 0.2, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE_3, 0.15, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_1, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_2, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.35, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.3, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_CONDUCTANCE_3, 0.25, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.45, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_1, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.45, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_2, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.45, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.4, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.MAGNETIZING_SUSCEPTANCE_3, 0.35, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_S1, 150.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_S1, 100.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_S1, 50.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_S2, 150.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_S2, 100.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_S2, 50.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_S3, 150.0, null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_S3, 100.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_S3, 50.0, null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_1, 51., null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_1, 50., null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_1, 49., null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_2, 51., null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_2, 50., null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_2, 49., null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_3, 51., null, threeWindingsTransformer, true),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_3, 50., null, threeWindingsTransformer, false),
            Arguments.of(LOWER, FieldType.RATED_VOLTAGE_3, 49., null, threeWindingsTransformer, false),

            // --- BETWEEN --- //
            // Terminal
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(13.5, 14.0), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(13.5, 14.0), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_3, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE_3, null, Set.of(13.5, 14.0), threeWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(BETWEEN, FieldType.RATIO_TARGET_V1, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATIO_TARGET_V1, null, Set.of(13.5, 14.0), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATIO_TARGET_V2, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATIO_TARGET_V2, null, Set.of(13.5, 14.0), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATIO_TARGET_V3, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATIO_TARGET_V3, null, Set.of(13.5, 14.0), threeWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE_1, null, Set.of(0.05, 0.15), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE_1, null, Set.of(0.15, 0.25), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE_2, null, Set.of(0.05, 0.15), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE_2, null, Set.of(0.15, 0.25), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE_3, null, Set.of(0.05, 0.15), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE_3, null, Set.of(0.15, 0.25), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE_1, null, Set.of(0.15, 0.25), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE_1, null, Set.of(0.25, 0.35), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE_2, null, Set.of(0.15, 0.25), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE_2, null, Set.of(0.25, 0.35), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE_3, null, Set.of(0.15, 0.25), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE_3, null, Set.of(0.25, 0.35), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_CONDUCTANCE_1, null, Set.of(0.25, 0.35), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_CONDUCTANCE_1, null, Set.of(0.35, 0.45), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_CONDUCTANCE_2, null, Set.of(0.25, 0.35), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_CONDUCTANCE_2, null, Set.of(0.35, 0.45), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_CONDUCTANCE_3, null, Set.of(0.25, 0.35), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_CONDUCTANCE_3, null, Set.of(0.35, 0.45), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, Set.of(0.35, 0.45), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, Set.of(0.45, 0.55), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, Set.of(0.35, 0.45), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, Set.of(0.45, 0.55), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, Set.of(0.35, 0.45), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, Set.of(0.45, 0.55), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_S1, null, Set.of(50.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_S1, null, Set.of(150.0, 250.0), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_S2, null, Set.of(50.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_S2, null, Set.of(150.0, 250.0), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_S3, null, Set.of(50.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_S3, null, Set.of(150.0, 250.0), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_1, null, Set.of(48., 52.), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_1, null, Set.of(55., 70.), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_2, null, Set.of(48., 52.), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_2, null, Set.of(55., 70.), threeWindingsTransformer, false),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_3, null, Set.of(48., 52.), threeWindingsTransformer, true),
            Arguments.of(BETWEEN, FieldType.RATED_VOLTAGE_3, null, Set.of(55., 70.), threeWindingsTransformer, false),

            // --- EXISTS --- //
            // Terminal
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_3, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE_3, null, null, threeWindingsTransformer1, false),
            // RatioTapChanger fields
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V1, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V1, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V2, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V2, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V3, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V3, null, null, threeWindingsTransformer1, false),
            // TwoWindingsTransformer fields
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE_1, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE_1, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE_2, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE_2, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE_3, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE_3, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE_1, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE_1, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE_2, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE_2, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE_3, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE_3, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_1, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_1, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_2, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_2, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_3, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_3, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_S1, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_S1, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_S2, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_S2, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_S3, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_S3, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_1, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_1, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_2, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_2, null, null, threeWindingsTransformer1, false),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_3, null, null, threeWindingsTransformer, true),
            Arguments.of(EXISTS, FieldType.RATED_VOLTAGE_3, null, null, threeWindingsTransformer1, false),

            // --- NOT_EXISTS --- //
            // Terminal
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_1, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_2, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_3, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE_3, null, null, threeWindingsTransformer1, true),
            // RatioTapChanger fields
            Arguments.of(NOT_EXISTS, FieldType.RATIO_TARGET_V1, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATIO_TARGET_V1, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATIO_TARGET_V2, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATIO_TARGET_V2, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATIO_TARGET_V3, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATIO_TARGET_V3, null, null, threeWindingsTransformer1, true),
            // TwoWindingsTransformer fields
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE_1, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE_1, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE_2, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE_2, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE_3, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE_3, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE_1, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE_1, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE_2, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE_2, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE_3, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE_3, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_1, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_1, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_2, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_2, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_3, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_CONDUCTANCE_3, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_S1, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_S1, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_S2, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_S2, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_S3, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_S3, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_1, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_1, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_2, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_2, null, null, threeWindingsTransformer1, true),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_3, null, null, threeWindingsTransformer, false),
            Arguments.of(NOT_EXISTS, FieldType.RATED_VOLTAGE_3, null, null, threeWindingsTransformer1, true),

            // --- IN --- //
            // Terminal
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_3, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE_3, null, Set.of(12.0, 14.0), threeWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(IN, FieldType.RATIO_TARGET_V1, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATIO_TARGET_V1, null, Set.of(12.0, 14.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATIO_TARGET_V2, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATIO_TARGET_V2, null, Set.of(12.0, 14.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATIO_TARGET_V3, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATIO_TARGET_V3, null, Set.of(12.0, 14.0), threeWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(IN, FieldType.SERIE_RESISTANCE_1, null, Set.of(0.05, 0.1, 0.15), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.SERIE_RESISTANCE_1, null, Set.of(0.05, 0.15), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.SERIE_RESISTANCE_2, null, Set.of(0.05, 0.1, 0.15), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.SERIE_RESISTANCE_2, null, Set.of(0.05, 0.15), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.SERIE_RESISTANCE_3, null, Set.of(0.05, 0.1, 0.15), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.SERIE_RESISTANCE_3, null, Set.of(0.05, 0.15), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.SERIE_REACTANCE_1, null, Set.of(0.15, 0.2, 0.25), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.SERIE_REACTANCE_1, null, Set.of(0.15, 0.25), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.SERIE_REACTANCE_2, null, Set.of(0.15, 0.2, 0.25), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.SERIE_REACTANCE_2, null, Set.of(0.15, 0.25), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.SERIE_REACTANCE_3, null, Set.of(0.15, 0.2, 0.25), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.SERIE_REACTANCE_3, null, Set.of(0.15, 0.25), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.MAGNETIZING_CONDUCTANCE_1, null, Set.of(0.25, 0.3, 0.35), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.MAGNETIZING_CONDUCTANCE_1, null, Set.of(0.25, 0.35), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.MAGNETIZING_CONDUCTANCE_2, null, Set.of(0.25, 0.3, 0.35), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.MAGNETIZING_CONDUCTANCE_2, null, Set.of(0.25, 0.35), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.MAGNETIZING_CONDUCTANCE_3, null, Set.of(0.25, 0.3, 0.35), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.MAGNETIZING_CONDUCTANCE_3, null, Set.of(0.25, 0.35), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, Set.of(0.35, 0.4, 0.45), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, Set.of(0.35, 0.45), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, Set.of(0.35, 0.4, 0.45), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, Set.of(0.35, 0.45), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, Set.of(0.35, 0.4, 0.45), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, Set.of(0.35, 0.45), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_S1, null, Set.of(50.0, 100.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_S1, null, Set.of(50.0, 150.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_S2, null, Set.of(50.0, 100.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_S2, null, Set.of(50.0, 150.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_S3, null, Set.of(50.0, 100.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_S3, null, Set.of(50.0, 150.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_1, null, Set.of(49.0, 50.0, 51.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_1, null, Set.of(49.0, 51.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_2, null, Set.of(49.0, 50.0, 51.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_2, null, Set.of(49.0, 51.0), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_3, null, Set.of(49.0, 50.0, 51.0), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.RATED_VOLTAGE_3, null, Set.of(49.0, 51.0), threeWindingsTransformer, false),

            // --- NOT_IN --- //
            // Terminal
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_1, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_2, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_3, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE_3, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, false),
            // RatioTapChanger fields
            Arguments.of(NOT_IN, FieldType.RATIO_TARGET_V1, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATIO_TARGET_V1, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATIO_TARGET_V2, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATIO_TARGET_V2, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATIO_TARGET_V3, null, Set.of(12.0, 14.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATIO_TARGET_V3, null, Set.of(12.0, 13.0, 14.0), threeWindingsTransformer, false),
            // TwoWindingsTransformer fields
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE_1, null, Set.of(0.05, 0.15), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE_1, null, Set.of(0.05, 0.1, 0.15), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE_2, null, Set.of(0.05, 0.15), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE_2, null, Set.of(0.05, 0.1, 0.15), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE_3, null, Set.of(0.05, 0.15), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE_3, null, Set.of(0.05, 0.1, 0.15), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE_1, null, Set.of(0.15, 0.25), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE_1, null, Set.of(0.15, 0.2, 0.25), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE_2, null, Set.of(0.15, 0.25), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE_2, null, Set.of(0.15, 0.2, 0.25), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE_3, null, Set.of(0.15, 0.25), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE_3, null, Set.of(0.15, 0.2, 0.25), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_CONDUCTANCE_1, null, Set.of(0.25, 0.35), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_CONDUCTANCE_1, null, Set.of(0.25, 0.3, 0.35), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_CONDUCTANCE_2, null, Set.of(0.25, 0.35), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_CONDUCTANCE_2, null, Set.of(0.25, 0.3, 0.35), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_CONDUCTANCE_3, null, Set.of(0.25, 0.35), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_CONDUCTANCE_3, null, Set.of(0.25, 0.3, 0.35), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, Set.of(0.35, 0.45), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_SUSCEPTANCE_1, null, Set.of(0.35, 0.4, 0.45), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, Set.of(0.35, 0.45), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_SUSCEPTANCE_2, null, Set.of(0.35, 0.4, 0.45), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, Set.of(0.35, 0.45), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.MAGNETIZING_SUSCEPTANCE_3, null, Set.of(0.35, 0.4, 0.45), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_S1, null, Set.of(50.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_S1, null, Set.of(50.0, 100.0, 150.0), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_S2, null, Set.of(50.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_S2, null, Set.of(50.0, 100.0, 150.0), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_S3, null, Set.of(50.0, 150.0), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_S3, null, Set.of(50.0, 100.0, 150.0), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_1, null, Set.of(49., 51.), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_1, null, Set.of(49., 50., 51.), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_2, null, Set.of(49., 51.), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_2, null, Set.of(49., 50., 51.), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_3, null, Set.of(49., 51.), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATED_VOLTAGE_3, null, Set.of(49., 50., 51.), threeWindingsTransformer, false),

            // null RatioTapChanger
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V1, null, null, threeWindingsTransformer2, false),
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V2, null, null, threeWindingsTransformer2, false),
            Arguments.of(EXISTS, FieldType.RATIO_TARGET_V3, null, null, threeWindingsTransformer2, false),

            // null PhaseTapChanger
            Arguments.of(EXISTS, FieldType.PHASE_REGULATION_VALUE_1, null, null, threeWindingsTransformer2, false),
            Arguments.of(EXISTS, FieldType.PHASE_REGULATION_VALUE_2, null, null, threeWindingsTransformer2, false),
            Arguments.of(EXISTS, FieldType.PHASE_REGULATION_VALUE_3, null, null, threeWindingsTransformer2, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForStaticVarCompensatorTest() {
        StaticVarCompensator svar = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        Mockito.when(svar.getBmin()).thenReturn(-1.0);
        Mockito.when(svar.getBmax()).thenReturn(-2.0);
        Mockito.when(svar.getVoltageSetpoint()).thenReturn(1.0);
        Mockito.when(svar.getReactivePowerSetpoint()).thenReturn(2.0);

        StandbyAutomaton standbyAutomaton = Mockito.mock(StandbyAutomaton.class);
        Mockito.when(standbyAutomaton.getLowVoltageSetpoint()).thenReturn(1.0);
        Mockito.when(standbyAutomaton.getHighVoltageSetpoint()).thenReturn(2.0);
        Mockito.when(standbyAutomaton.getLowVoltageThreshold()).thenReturn(1.0);
        Mockito.when(standbyAutomaton.getHighVoltageThreshold()).thenReturn(2.0);
        Mockito.when(standbyAutomaton.getB0()).thenReturn(-1.0);
        Mockito.when(svar.getExtension(StandbyAutomaton.class)).thenReturn(standbyAutomaton);

        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(svar.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        StaticVarCompensator svar1 = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar1.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(svar1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        Mockito.when(svar1.getBmin()).thenReturn(Double.NaN);
        Mockito.when(svar1.getBmax()).thenReturn(Double.NaN);
        Mockito.when(svar1.getVoltageSetpoint()).thenReturn(Double.NaN);
        Mockito.when(svar1.getReactivePowerSetpoint()).thenReturn(Double.NaN);

        StandbyAutomaton standbyAutomaton1 = Mockito.mock(StandbyAutomaton.class);
        Mockito.when(standbyAutomaton1.getLowVoltageSetpoint()).thenReturn(Double.NaN);
        Mockito.when(standbyAutomaton1.getHighVoltageSetpoint()).thenReturn(Double.NaN);
        Mockito.when(standbyAutomaton1.getLowVoltageThreshold()).thenReturn(Double.NaN);
        Mockito.when(standbyAutomaton1.getHighVoltageThreshold()).thenReturn(Double.NaN);
        Mockito.when(standbyAutomaton1.getB0()).thenReturn(Double.NaN);
        Mockito.when(svar1.getExtension(StandbyAutomaton.class)).thenReturn(standbyAutomaton1);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, svar, false),

                // Static Var Compensator fields
                Arguments.of(EQUALS, FieldType.MIN_Q_AT_NOMINAL_V, -169.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.MIN_Q_AT_NOMINAL_V, -170.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, -338.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, -339.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.MIN_SUSCEPTANCE, -1.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.MIN_SUSCEPTANCE, -2.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.MAX_SUSCEPTANCE, -2.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.MAX_SUSCEPTANCE, -1.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.VOLTAGE_SET_POINT, 1.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.VOLTAGE_SET_POINT, 2.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.REACTIVE_POWER_SET_POINT, 2.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.REACTIVE_POWER_SET_POINT, 1.0, null, svar, false),

                // StandbyAutomaton fields
                Arguments.of(EQUALS, FieldType.LOW_VOLTAGE_SET_POINT, 1.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.LOW_VOLTAGE_SET_POINT, 2.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.HIGH_VOLTAGE_SET_POINT, 2.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.HIGH_VOLTAGE_SET_POINT, 1.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.LOW_VOLTAGE_THRESHOLD, 1.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.LOW_VOLTAGE_THRESHOLD, 2.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.HIGH_VOLTAGE_THRESHOLD, 1.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.SUSCEPTANCE_FIX, -1.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.SUSCEPTANCE_FIX, -2.0, null, svar, false),
                Arguments.of(EQUALS, FieldType.FIX_Q_AT_NOMINAL_V, -169.0, null, svar, true),
                Arguments.of(EQUALS, FieldType.FIX_Q_AT_NOMINAL_V, -170.0, null, svar, false),

                // --- GREATER_OR_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, svar, false),

                // Static Var Compensator fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_Q_AT_NOMINAL_V, -169.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_Q_AT_NOMINAL_V, -170.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_Q_AT_NOMINAL_V, -168.0, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, -338.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, -339.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, -337.0, null, svar, false),

                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_SUSCEPTANCE, -1.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_SUSCEPTANCE, -1.1, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MIN_SUSCEPTANCE, -0.9, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, -2.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, -2.1, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, -1.9, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.VOLTAGE_SET_POINT, 1.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.VOLTAGE_SET_POINT, 0.9, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.VOLTAGE_SET_POINT, 1.1, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.REACTIVE_POWER_SET_POINT, 2.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.REACTIVE_POWER_SET_POINT, 1.9, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.REACTIVE_POWER_SET_POINT, 2.1, null, svar, false),

                // StandbyAutomaton fields
                Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_SET_POINT, 1.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_SET_POINT, 0.9, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_SET_POINT, 1.1, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_SET_POINT, 2.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_SET_POINT, 1.9, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_SET_POINT, 2.1, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_THRESHOLD, 1.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_THRESHOLD, 0.9, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.LOW_VOLTAGE_THRESHOLD, 1.1, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_THRESHOLD, 1.9, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.1, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SUSCEPTANCE_FIX, -1.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SUSCEPTANCE_FIX, -1.1, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.SUSCEPTANCE_FIX, -0.9, null, svar, false),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FIX_Q_AT_NOMINAL_V, -169.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FIX_Q_AT_NOMINAL_V, -170.0, null, svar, true),
                Arguments.of(GREATER_OR_EQUALS, FieldType.FIX_Q_AT_NOMINAL_V, -168.0, null, svar, false),

                // --- GREATER --- //
                // VoltageLevel fields
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, svar, false),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, svar, true),
                Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, svar, false),

                // Static Var Compensator fields
                Arguments.of(GREATER, FieldType.MIN_Q_AT_NOMINAL_V, -169.0, null, svar, false),
                Arguments.of(GREATER, FieldType.MIN_Q_AT_NOMINAL_V, -170.0, null, svar, true),
                Arguments.of(GREATER, FieldType.MIN_Q_AT_NOMINAL_V, -168.0, null, svar, false),
                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, -338.0, null, svar, false),
                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, -339.0, null, svar, true),
                Arguments.of(GREATER, FieldType.MAX_Q_AT_NOMINAL_V, -337.0, null, svar, false),

                Arguments.of(GREATER, FieldType.MIN_SUSCEPTANCE, -1.0, null, svar, false),
                Arguments.of(GREATER, FieldType.MIN_SUSCEPTANCE, -1.1, null, svar, true),
                Arguments.of(GREATER, FieldType.MIN_SUSCEPTANCE, -0.9, null, svar, false),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, -2.0, null, svar, false),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, -2.1, null, svar, true),
                Arguments.of(GREATER, FieldType.MAX_SUSCEPTANCE, -1.9, null, svar, false),
                Arguments.of(GREATER, FieldType.VOLTAGE_SET_POINT, 1.0, null, svar, false),
                Arguments.of(GREATER, FieldType.VOLTAGE_SET_POINT, 0.9, null, svar, true),
                Arguments.of(GREATER, FieldType.VOLTAGE_SET_POINT, 1.1, null, svar, false),
                Arguments.of(GREATER, FieldType.REACTIVE_POWER_SET_POINT, 2.0, null, svar, false),
                Arguments.of(GREATER, FieldType.REACTIVE_POWER_SET_POINT, 1.9, null, svar, true),
                Arguments.of(GREATER, FieldType.REACTIVE_POWER_SET_POINT, 2.1, null, svar, false),

                // StandbyAutomaton fields
                Arguments.of(GREATER, FieldType.LOW_VOLTAGE_SET_POINT, 1.0, null, svar, false),
                Arguments.of(GREATER, FieldType.LOW_VOLTAGE_SET_POINT, 0.9, null, svar, true),
                Arguments.of(GREATER, FieldType.LOW_VOLTAGE_SET_POINT, 1.1, null, svar, false),
                Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_SET_POINT, 2.0, null, svar, false),
                Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_SET_POINT, 1.9, null, svar, true),
                Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_SET_POINT, 2.1, null, svar, false),
                Arguments.of(GREATER, FieldType.LOW_VOLTAGE_THRESHOLD, 1.0, null, svar, false),
                Arguments.of(GREATER, FieldType.LOW_VOLTAGE_THRESHOLD, 0.9, null, svar, true),
                Arguments.of(GREATER, FieldType.LOW_VOLTAGE_THRESHOLD, 1.1, null, svar, false),
                Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.0, null, svar, false),
                Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_THRESHOLD, 1.9, null, svar, true),
                Arguments.of(GREATER, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.1, null, svar, false),
                Arguments.of(GREATER, FieldType.SUSCEPTANCE_FIX, -1.0, null, svar, false),
                Arguments.of(GREATER, FieldType.SUSCEPTANCE_FIX, -1.1, null, svar, true),
                Arguments.of(GREATER, FieldType.SUSCEPTANCE_FIX, -0.9, null, svar, false),
                Arguments.of(GREATER, FieldType.FIX_Q_AT_NOMINAL_V, -169.0, null, svar, false),
                Arguments.of(GREATER, FieldType.FIX_Q_AT_NOMINAL_V, -170.0, null, svar, true),
                Arguments.of(GREATER, FieldType.FIX_Q_AT_NOMINAL_V, -168.0, null, svar, false),

                // --- LOWER_OR_EQUALS --- //
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, svar, true),

                // Static Var Compensator fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_Q_AT_NOMINAL_V, -169.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_Q_AT_NOMINAL_V, -170.0, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_Q_AT_NOMINAL_V, -168.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, -338.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, -339.0, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_Q_AT_NOMINAL_V, -337.0, null, svar, true),

                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_SUSCEPTANCE, -1.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_SUSCEPTANCE, -1.1, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MIN_SUSCEPTANCE, -0.9, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, -2.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, -2.1, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_SUSCEPTANCE, -1.9, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.VOLTAGE_SET_POINT, 1.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.VOLTAGE_SET_POINT, 0.9, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.VOLTAGE_SET_POINT, 1.1, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.REACTIVE_POWER_SET_POINT, 2.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.REACTIVE_POWER_SET_POINT, 1.9, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.REACTIVE_POWER_SET_POINT, 2.1, null, svar, true),

                // StandbyAutomaton fields
                Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_SET_POINT, 1.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_SET_POINT, 0.9, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_SET_POINT, 1.1, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_SET_POINT, 2.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_SET_POINT, 1.9, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_SET_POINT, 2.1, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_THRESHOLD, 1.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_THRESHOLD, 0.9, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.LOW_VOLTAGE_THRESHOLD, 1.1, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_THRESHOLD, 1.9, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.1, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SUSCEPTANCE_FIX, -1.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SUSCEPTANCE_FIX, -1.1, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.SUSCEPTANCE_FIX, -0.9, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FIX_Q_AT_NOMINAL_V, -169.0, null, svar, true),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FIX_Q_AT_NOMINAL_V, -170.0, null, svar, false),
                Arguments.of(LOWER_OR_EQUALS, FieldType.FIX_Q_AT_NOMINAL_V, -168.0, null, svar, true),

                // --- LOWER --- //
                // VoltageLevel fields
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, svar, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, svar, false),
                Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, svar, true),

                // Static Var Compensator fields
                Arguments.of(LOWER, FieldType.MIN_Q_AT_NOMINAL_V, -169.0, null, svar, false),
                Arguments.of(LOWER, FieldType.MIN_Q_AT_NOMINAL_V, -170.0, null, svar, false),
                Arguments.of(LOWER, FieldType.MIN_Q_AT_NOMINAL_V, -168.0, null, svar, true),
                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, -338.0, null, svar, false),
                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, -339.0, null, svar, false),
                Arguments.of(LOWER, FieldType.MAX_Q_AT_NOMINAL_V, -337.0, null, svar, true),

                Arguments.of(LOWER, FieldType.MIN_SUSCEPTANCE, -1.0, null, svar, false),
                Arguments.of(LOWER, FieldType.MIN_SUSCEPTANCE, -1.1, null, svar, false),
                Arguments.of(LOWER, FieldType.MIN_SUSCEPTANCE, -0.9, null, svar, true),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, -2.0, null, svar, false),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, -2.1, null, svar, false),
                Arguments.of(LOWER, FieldType.MAX_SUSCEPTANCE, -1.9, null, svar, true),
                Arguments.of(LOWER, FieldType.VOLTAGE_SET_POINT, 1.0, null, svar, false),
                Arguments.of(LOWER, FieldType.VOLTAGE_SET_POINT, 0.9, null, svar, false),
                Arguments.of(LOWER, FieldType.VOLTAGE_SET_POINT, 1.1, null, svar, true),
                Arguments.of(LOWER, FieldType.REACTIVE_POWER_SET_POINT, 2.0, null, svar, false),
                Arguments.of(LOWER, FieldType.REACTIVE_POWER_SET_POINT, 1.9, null, svar, false),
                Arguments.of(LOWER, FieldType.REACTIVE_POWER_SET_POINT, 2.1, null, svar, true),

                // StandbyAutomaton fields
                Arguments.of(LOWER, FieldType.LOW_VOLTAGE_SET_POINT, 1.0, null, svar, false),
                Arguments.of(LOWER, FieldType.LOW_VOLTAGE_SET_POINT, 0.9, null, svar, false),
                Arguments.of(LOWER, FieldType.LOW_VOLTAGE_SET_POINT, 1.1, null, svar, true),
                Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_SET_POINT, 2.0, null, svar, false),
                Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_SET_POINT, 1.9, null, svar, false),
                Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_SET_POINT, 2.1, null, svar, true),
                Arguments.of(LOWER, FieldType.LOW_VOLTAGE_THRESHOLD, 1.0, null, svar, false),
                Arguments.of(LOWER, FieldType.LOW_VOLTAGE_THRESHOLD, 0.9, null, svar, false),
                Arguments.of(LOWER, FieldType.LOW_VOLTAGE_THRESHOLD, 1.1, null, svar, true),
                Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.0, null, svar, false),
                Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_THRESHOLD, 1.9, null, svar, false),
                Arguments.of(LOWER, FieldType.HIGH_VOLTAGE_THRESHOLD, 2.1, null, svar, true),
                Arguments.of(LOWER, FieldType.SUSCEPTANCE_FIX, -1.0, null, svar, false),
                Arguments.of(LOWER, FieldType.SUSCEPTANCE_FIX, -1.1, null, svar, false),
                Arguments.of(LOWER, FieldType.SUSCEPTANCE_FIX, -0.9, null, svar, true),
                Arguments.of(LOWER, FieldType.FIX_Q_AT_NOMINAL_V, -169.0, null, svar, false),
                Arguments.of(LOWER, FieldType.FIX_Q_AT_NOMINAL_V, -170.0, null, svar, false),
                Arguments.of(LOWER, FieldType.FIX_Q_AT_NOMINAL_V, -168.0, null, svar, true),

                // --- BETWEEN --- //
                // VoltageLevel fields
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), svar, true),
                Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), svar, false),

                // Static Var Compensator fields
                Arguments.of(BETWEEN, FieldType.MIN_Q_AT_NOMINAL_V, null, Set.of(-168.0, -170.0), svar, true),
                Arguments.of(BETWEEN, FieldType.MIN_Q_AT_NOMINAL_V, null, Set.of(-169.5, -170.0), svar, false),
                Arguments.of(BETWEEN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(-337.0, -339.0), svar, true),
                Arguments.of(BETWEEN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(-338.5, -339.0), svar, false),

                Arguments.of(BETWEEN, FieldType.MIN_SUSCEPTANCE, null, Set.of(-0.9, -1.1), svar, true),
                Arguments.of(BETWEEN, FieldType.MIN_SUSCEPTANCE, null, Set.of(-1.05, -1.1), svar, false),
                Arguments.of(BETWEEN, FieldType.MAX_SUSCEPTANCE, null, Set.of(-1.9, -2.1), svar, true),
                Arguments.of(BETWEEN, FieldType.MAX_SUSCEPTANCE, null, Set.of(-2.05, -2.1), svar, false),
                Arguments.of(BETWEEN, FieldType.VOLTAGE_SET_POINT, null, Set.of(0.9, 1.1), svar, true),
                Arguments.of(BETWEEN, FieldType.VOLTAGE_SET_POINT, null, Set.of(1.05, 1.1), svar, false),
                Arguments.of(BETWEEN, FieldType.REACTIVE_POWER_SET_POINT, null, Set.of(1.9, 2.1), svar, true),
                Arguments.of(BETWEEN, FieldType.REACTIVE_POWER_SET_POINT, null, Set.of(2.05, 2.1), svar, false),

                // StandbyAutomaton fields
                Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_SET_POINT, null, Set.of(0.9, 1.1), svar, true),
                Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_SET_POINT, null, Set.of(1.05, 1.1), svar, false),
                Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_SET_POINT, null, Set.of(1.9, 2.1), svar, true),
                Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_SET_POINT, null, Set.of(2.05, 2.1), svar, false),
                Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_THRESHOLD, null, Set.of(0.9, 1.1), svar, true),
                Arguments.of(BETWEEN, FieldType.LOW_VOLTAGE_THRESHOLD, null, Set.of(1.05, 1.1), svar, false),
                Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_THRESHOLD, null, Set.of(1.9, 2.1), svar, true),
                Arguments.of(BETWEEN, FieldType.HIGH_VOLTAGE_THRESHOLD, null, Set.of(2.05, 2.1), svar, false),
                Arguments.of(BETWEEN, FieldType.SUSCEPTANCE_FIX, null, Set.of(-0.9, -1.1), svar, true),
                Arguments.of(BETWEEN, FieldType.SUSCEPTANCE_FIX, null, Set.of(-1.05, -1.1), svar, false),
                Arguments.of(BETWEEN, FieldType.FIX_Q_AT_NOMINAL_V, null, Set.of(-168.0, -170.0), svar, true),
                Arguments.of(BETWEEN, FieldType.FIX_Q_AT_NOMINAL_V, null, Set.of(-169.5, -170.0), svar, false),

                // --- EXISTS --- //
                // VoltageLevel fields
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, svar1, false),

                // Static Var Compensator fields
                Arguments.of(EXISTS, FieldType.MIN_Q_AT_NOMINAL_V, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.MIN_Q_AT_NOMINAL_V, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.MAX_Q_AT_NOMINAL_V, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.MAX_Q_AT_NOMINAL_V, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.MIN_SUSCEPTANCE, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.MIN_SUSCEPTANCE, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.MAX_SUSCEPTANCE, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.MAX_SUSCEPTANCE, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.VOLTAGE_SET_POINT, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.VOLTAGE_SET_POINT, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.REACTIVE_POWER_SET_POINT, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.REACTIVE_POWER_SET_POINT, null, null, svar1, false),

                // StandbyAutomaton fields
                Arguments.of(EXISTS, FieldType.LOW_VOLTAGE_SET_POINT, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.LOW_VOLTAGE_SET_POINT, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.HIGH_VOLTAGE_SET_POINT, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.HIGH_VOLTAGE_SET_POINT, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.LOW_VOLTAGE_THRESHOLD, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.LOW_VOLTAGE_THRESHOLD, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.HIGH_VOLTAGE_THRESHOLD, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.HIGH_VOLTAGE_THRESHOLD, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.SUSCEPTANCE_FIX, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.SUSCEPTANCE_FIX, null, null, svar1, false),
                Arguments.of(EXISTS, FieldType.FIX_Q_AT_NOMINAL_V, null, null, svar, true),
                Arguments.of(EXISTS, FieldType.FIX_Q_AT_NOMINAL_V, null, null, svar1, false),

                // --- NOT_EXISTS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, svar1, true),

                // Static Var Compensator fields
                Arguments.of(NOT_EXISTS, FieldType.MIN_Q_AT_NOMINAL_V, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.MIN_Q_AT_NOMINAL_V, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.MAX_Q_AT_NOMINAL_V, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.MAX_Q_AT_NOMINAL_V, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.MIN_SUSCEPTANCE, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.MIN_SUSCEPTANCE, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.MAX_SUSCEPTANCE, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.MAX_SUSCEPTANCE, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_SET_POINT, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.VOLTAGE_SET_POINT, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.REACTIVE_POWER_SET_POINT, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.REACTIVE_POWER_SET_POINT, null, null, svar1, true),

                // StandbyAutomaton fields
                Arguments.of(NOT_EXISTS, FieldType.LOW_VOLTAGE_SET_POINT, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.LOW_VOLTAGE_SET_POINT, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.HIGH_VOLTAGE_SET_POINT, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.HIGH_VOLTAGE_SET_POINT, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.LOW_VOLTAGE_THRESHOLD, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.LOW_VOLTAGE_THRESHOLD, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.HIGH_VOLTAGE_THRESHOLD, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.HIGH_VOLTAGE_THRESHOLD, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.SUSCEPTANCE_FIX, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.SUSCEPTANCE_FIX, null, null, svar1, true),
                Arguments.of(NOT_EXISTS, FieldType.FIX_Q_AT_NOMINAL_V, null, null, svar, false),
                Arguments.of(NOT_EXISTS, FieldType.FIX_Q_AT_NOMINAL_V, null, null, svar1, true),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.0, 14.0), svar, true),
                Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), svar, false),

                // Static Var Compensator fields
                Arguments.of(IN, FieldType.MIN_Q_AT_NOMINAL_V, null, Set.of(-169.0, -170.0), svar, true),
                Arguments.of(IN, FieldType.MIN_Q_AT_NOMINAL_V, null, Set.of(-169.5, -170.0), svar, false),
                Arguments.of(IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(-338.0, -339.0), svar, true),
                Arguments.of(IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(-338.5, -339.0), svar, false),

                Arguments.of(IN, FieldType.MIN_SUSCEPTANCE, null, Set.of(-1.0, -1.1), svar, true),
                Arguments.of(IN, FieldType.MIN_SUSCEPTANCE, null, Set.of(-1.05, -1.1), svar, false),
                Arguments.of(IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(-2.0, -2.1), svar, true),
                Arguments.of(IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(-2.05, -2.1), svar, false),
                Arguments.of(IN, FieldType.VOLTAGE_SET_POINT, null, Set.of(1.0, 1.1), svar, true),
                Arguments.of(IN, FieldType.VOLTAGE_SET_POINT, null, Set.of(1.05, 1.1), svar, false),
                Arguments.of(IN, FieldType.REACTIVE_POWER_SET_POINT, null, Set.of(2.0, 2.1), svar, true),
                Arguments.of(IN, FieldType.REACTIVE_POWER_SET_POINT, null, Set.of(2.05, 2.1), svar, false),

                // StandbyAutomaton fields
                Arguments.of(IN, FieldType.LOW_VOLTAGE_SET_POINT, null, Set.of(1.0, 1.1), svar, true),
                Arguments.of(IN, FieldType.LOW_VOLTAGE_SET_POINT, null, Set.of(1.05, 1.1), svar, false),
                Arguments.of(IN, FieldType.HIGH_VOLTAGE_SET_POINT, null, Set.of(2.0, 2.1), svar, true),
                Arguments.of(IN, FieldType.HIGH_VOLTAGE_SET_POINT, null, Set.of(2.05, 2.1), svar, false),
                Arguments.of(IN, FieldType.LOW_VOLTAGE_THRESHOLD, null, Set.of(1.0, 1.1), svar, true),
                Arguments.of(IN, FieldType.LOW_VOLTAGE_THRESHOLD, null, Set.of(1.05, 1.1), svar, false),
                Arguments.of(IN, FieldType.HIGH_VOLTAGE_THRESHOLD, null, Set.of(2.0, 2.1), svar, true),
                Arguments.of(IN, FieldType.HIGH_VOLTAGE_THRESHOLD, null, Set.of(2.05, 2.1), svar, false),
                Arguments.of(IN, FieldType.SUSCEPTANCE_FIX, null, Set.of(-1.0, -1.1), svar, true),
                Arguments.of(IN, FieldType.SUSCEPTANCE_FIX, null, Set.of(-1.05, -1.1), svar, false),
                Arguments.of(IN, FieldType.FIX_Q_AT_NOMINAL_V, null, Set.of(-169.0, -170.0), svar, true),
                Arguments.of(IN, FieldType.FIX_Q_AT_NOMINAL_V, null, Set.of(-169.5, -170.0), svar, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), svar, true),
                Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), svar, false),

                // Static Var Compensator fields
                Arguments.of(NOT_IN, FieldType.MIN_Q_AT_NOMINAL_V, null, Set.of(-168.0, -170.0), svar, true),
                Arguments.of(NOT_IN, FieldType.MIN_Q_AT_NOMINAL_V, null, Set.of(-169.0, -170.0), svar, false),
                Arguments.of(NOT_IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(-337.0, -339.0), svar, true),
                Arguments.of(NOT_IN, FieldType.MAX_Q_AT_NOMINAL_V, null, Set.of(-338.0, -339.0), svar, false),

                Arguments.of(NOT_IN, FieldType.MIN_SUSCEPTANCE, null, Set.of(-0.9, -1.1), svar, true),
                Arguments.of(NOT_IN, FieldType.MIN_SUSCEPTANCE, null, Set.of(-1.0, -1.1), svar, false),
                Arguments.of(NOT_IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(-1.9, -2.1), svar, true),
                Arguments.of(NOT_IN, FieldType.MAX_SUSCEPTANCE, null, Set.of(-2.0, -2.1), svar, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_SET_POINT, null, Set.of(0.9, 1.1), svar, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_SET_POINT, null, Set.of(1.0, 1.1), svar, false),
                Arguments.of(NOT_IN, FieldType.REACTIVE_POWER_SET_POINT, null, Set.of(1.9, 2.1), svar, true),
                Arguments.of(NOT_IN, FieldType.REACTIVE_POWER_SET_POINT, null, Set.of(2.0, 2.1), svar, false),

                // StandbyAutomaton fields
                Arguments.of(NOT_IN, FieldType.LOW_VOLTAGE_SET_POINT, null, Set.of(0.9, 1.1), svar, true),
                Arguments.of(NOT_IN, FieldType.LOW_VOLTAGE_SET_POINT, null, Set.of(1.0, 1.1), svar, false),
                Arguments.of(NOT_IN, FieldType.HIGH_VOLTAGE_SET_POINT, null, Set.of(1.9, 2.1), svar, true),
                Arguments.of(NOT_IN, FieldType.HIGH_VOLTAGE_SET_POINT, null, Set.of(2.0, 2.1), svar, false),
                Arguments.of(NOT_IN, FieldType.LOW_VOLTAGE_THRESHOLD, null, Set.of(0.9, 1.1), svar, true),
                Arguments.of(NOT_IN, FieldType.LOW_VOLTAGE_THRESHOLD, null, Set.of(1.0, 1.1), svar, false),
                Arguments.of(NOT_IN, FieldType.HIGH_VOLTAGE_THRESHOLD, null, Set.of(1.9, 2.1), svar, true),
                Arguments.of(NOT_IN, FieldType.HIGH_VOLTAGE_THRESHOLD, null, Set.of(2.0, 2.1), svar, false),
                Arguments.of(NOT_IN, FieldType.SUSCEPTANCE_FIX, null, Set.of(-0.9, -1.1), svar, true),
                Arguments.of(NOT_IN, FieldType.SUSCEPTANCE_FIX, null, Set.of(-1.0, -1.1), svar, false),
                Arguments.of(NOT_IN, FieldType.FIX_Q_AT_NOMINAL_V, null, Set.of(-168.0, -170.0), svar, true),
                Arguments.of(NOT_IN, FieldType.FIX_Q_AT_NOMINAL_V, null, Set.of(-169.0, -170.0), svar, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForDanglingLineTest() {
        DanglingLine danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getType()).thenReturn(IdentifiableType.DANGLING_LINE);
        Mockito.when(danglingLine.getR()).thenReturn(0.1);
        Mockito.when(danglingLine.getX()).thenReturn(0.2);
        Mockito.when(danglingLine.getB()).thenReturn(0.3);
        Mockito.when(danglingLine.getG()).thenReturn(0.4);
        Mockito.when(danglingLine.getP0()).thenReturn(100.0);
        Mockito.when(danglingLine.getQ0()).thenReturn(50.0);

        // VoltageLevel fields
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(danglingLine.getTerminal()).thenReturn(terminal);
        Mockito.when(voltageLevel.getNominalV()).thenReturn(13.0);

        // for testing none EXISTS
        DanglingLine danglingLine1 = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine1.getType()).thenReturn(IdentifiableType.DANGLING_LINE);
        Mockito.when(danglingLine1.getB()).thenReturn(Double.NaN);
        Mockito.when(danglingLine1.getR()).thenReturn(Double.NaN);
        Mockito.when(danglingLine1.getG()).thenReturn(Double.NaN);
        Mockito.when(danglingLine1.getX()).thenReturn(Double.NaN);
        Mockito.when(danglingLine1.getP0()).thenReturn(Double.NaN);
        Mockito.when(danglingLine1.getQ0()).thenReturn(Double.NaN);
        // VoltageLevel fields
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(danglingLine1.getTerminal()).thenReturn(terminal1);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(Double.NaN);

        return Stream.of(
            // --- EQUALS --- //
            // VoltageLevel fields
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, danglingLine, true),
            Arguments.of(EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, danglingLine, false),
            // Dangling Line fields
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, danglingLine, true),
            Arguments.of(EQUALS, FieldType.SERIE_REACTANCE, 0.2, null, danglingLine, true),
            Arguments.of(EQUALS, FieldType.SHUNT_SUSCEPTANCE, 0.3, null, danglingLine, true),
            Arguments.of(EQUALS, FieldType.SHUNT_CONDUCTANCE, 0.4, null, danglingLine, true),
            Arguments.of(EQUALS, FieldType.P0, 100.0, null, danglingLine, true),
            Arguments.of(EQUALS, FieldType.Q0, 50.0, null, danglingLine, true),

            // --- GREATER_OR_EQUALS --- //
            // VoltageLevel fields
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, danglingLine, false),
            // Dangling Line fields
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.05, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.15, null, danglingLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.1, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.2, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.3, null, danglingLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE, 0.2, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE, 0.3, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE, 0.4, null, danglingLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE, 0.3, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE, 0.4, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE, 0.5, null, danglingLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 50.0, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 100.0, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.P0, 200.0, null, danglingLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 25.0, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 50.0, null, danglingLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.Q0, 100.0, null, danglingLine, false),

            // --- GREATER --- //
            // VoltageLevel fields
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 12.0, null, danglingLine, true),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 13.0, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.NOMINAL_VOLTAGE, 14.0, null, danglingLine, false),
            // Dangling Line fields
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.05, null, danglingLine, true),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.1, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.15, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 0.1, null, danglingLine, true),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 0.2, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.SERIE_REACTANCE, 0.3, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE, 0.2, null, danglingLine, true),
            Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE, 0.3, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.SHUNT_SUSCEPTANCE, 0.4, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE, 0.3, null, danglingLine, true),
            Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE, 0.4, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.SHUNT_CONDUCTANCE, 0.5, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.P0, 50.0, null, danglingLine, true),
            Arguments.of(GREATER, FieldType.P0, 100.0, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.P0, 200.0, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.Q0, 25.0, null, danglingLine, true),
            Arguments.of(GREATER, FieldType.Q0, 50.0, null, danglingLine, false),
            Arguments.of(GREATER, FieldType.Q0, 100.0, null, danglingLine, false),

            // --- LOWER_OR_EQUALS --- //
            // VoltageLevel fields
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 14.0, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 13.0, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.NOMINAL_VOLTAGE, 12.0, null, danglingLine, false),
            // Dangling Line fields
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.05, null, danglingLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.15, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.1, null, danglingLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.2, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_REACTANCE, 0.3, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE, 0.2, null, danglingLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE, 0.3, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_SUSCEPTANCE, 0.4, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE, 0.3, null, danglingLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE, 0.4, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SHUNT_CONDUCTANCE, 0.5, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 50.0, null, danglingLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 100.0, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.P0, 200.0, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 25.0, null, danglingLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 50.0, null, danglingLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.Q0, 100.0, null, danglingLine, true),

            // --- LOWER --- //
            // VoltageLevel fields
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 14.0, null, danglingLine, true),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 13.0, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.NOMINAL_VOLTAGE, 12.0, null, danglingLine, false),
            // Dangling Line fields
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.05, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.1, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.15, null, danglingLine, true),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 0.1, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 0.2, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.SERIE_REACTANCE, 0.3, null, danglingLine, true),
            Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE, 0.2, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE, 0.3, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.SHUNT_SUSCEPTANCE, 0.4, null, danglingLine, true),
            Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE, 0.3, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE, 0.4, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.SHUNT_CONDUCTANCE, 0.5, null, danglingLine, true),
            Arguments.of(LOWER, FieldType.P0, 50.0, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.P0, 100.0, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.P0, 200.0, null, danglingLine, true),
            Arguments.of(LOWER, FieldType.Q0, 25.0, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.Q0, 50.0, null, danglingLine, false),
            Arguments.of(LOWER, FieldType.Q0, 100.0, null, danglingLine, true),
            // --- BETWEEN --- //
            // VoltageLevel fields
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), danglingLine, true),
            Arguments.of(BETWEEN, FieldType.NOMINAL_VOLTAGE, null, Set.of(13.5, 14.0), danglingLine, false),
            // Dangling Line fields
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.15), danglingLine, true),
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(0.2, 0.3), danglingLine, false),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE, null, Set.of(0.15, 0.25), danglingLine, true),
            Arguments.of(BETWEEN, FieldType.SERIE_REACTANCE, null, Set.of(0.3, 0.4), danglingLine, false),
            Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE, null, Set.of(0.25, 0.35), danglingLine, true),
            Arguments.of(BETWEEN, FieldType.SHUNT_SUSCEPTANCE, null, Set.of(0.4, 0.5), danglingLine, false),
            Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE, null, Set.of(0.35, 0.45), danglingLine, true),
            Arguments.of(BETWEEN, FieldType.SHUNT_CONDUCTANCE, null, Set.of(0.5, 0.6), danglingLine, false),
            Arguments.of(BETWEEN, FieldType.P0, null, Set.of(50.0, 150.0), danglingLine, true),
            Arguments.of(BETWEEN, FieldType.P0, null, Set.of(120.0, 180.0), danglingLine, false),
            Arguments.of(BETWEEN, FieldType.Q0, null, Set.of(25.0, 75.0), danglingLine, true),
            Arguments.of(BETWEEN, FieldType.Q0, null, Set.of(120.0, 180.0), danglingLine, false),
            // --- EXISTS --- //
            // VoltageLevel fields
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, danglingLine, true),
            Arguments.of(EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, danglingLine1, false),
            //Battery fields
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE, null, null, danglingLine, true),
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE, null, null, danglingLine1, false),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE, null, null, danglingLine, true),
            Arguments.of(EXISTS, FieldType.SERIE_REACTANCE, null, null, danglingLine1, false),
            Arguments.of(EXISTS, FieldType.SHUNT_SUSCEPTANCE, null, null, danglingLine, true),
            Arguments.of(EXISTS, FieldType.SHUNT_SUSCEPTANCE, null, null, danglingLine1, false),
            Arguments.of(EXISTS, FieldType.SHUNT_CONDUCTANCE, null, null, danglingLine, true),
            Arguments.of(EXISTS, FieldType.SHUNT_CONDUCTANCE, null, null, danglingLine1, false),
            Arguments.of(EXISTS, FieldType.P0, null, null, danglingLine, true),
            Arguments.of(EXISTS, FieldType.P0, null, null, danglingLine1, false),
            Arguments.of(EXISTS, FieldType.Q0, null, null, danglingLine, true),
            Arguments.of(EXISTS, FieldType.Q0, null, null, danglingLine1, false),
            // --- NOT_EXISTS --- //
            // VoltageLevel fields
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, danglingLine, false),
            Arguments.of(NOT_EXISTS, FieldType.NOMINAL_VOLTAGE, null, null, danglingLine1, true),
            // Dangling Line fields
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE, null, null, danglingLine, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE, null, null, danglingLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE, null, null, danglingLine, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_REACTANCE, null, null, danglingLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.SHUNT_SUSCEPTANCE, null, null, danglingLine, false),
            Arguments.of(NOT_EXISTS, FieldType.SHUNT_SUSCEPTANCE, null, null, danglingLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.SHUNT_CONDUCTANCE, null, null, danglingLine, false),
            Arguments.of(NOT_EXISTS, FieldType.SHUNT_CONDUCTANCE, null, null, danglingLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.P0, null, null, danglingLine, false),
            Arguments.of(NOT_EXISTS, FieldType.P0, null, null, danglingLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.Q0, null, null, danglingLine, false),
            Arguments.of(NOT_EXISTS, FieldType.Q0, null, null, danglingLine1, true),

            // --- IN --- //
            // VoltageLevel fields
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), danglingLine, true),
            Arguments.of(IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), danglingLine, false),
            // Dangling Line
            Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.1, 0.15), danglingLine, true),
            Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.2, 0.3), danglingLine, false),
            Arguments.of(IN, FieldType.SERIE_REACTANCE, null, Set.of(0.15, 0.2, 0.25), danglingLine, true),
            Arguments.of(IN, FieldType.SERIE_REACTANCE, null, Set.of(0.3, 0.4), danglingLine, false),
            Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE, null, Set.of(0.25, 0.3, 0.35), danglingLine, true),
            Arguments.of(IN, FieldType.SHUNT_SUSCEPTANCE, null, Set.of(0.4, 0.5), danglingLine, false),
            Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE, null, Set.of(0.35, 0.4, 0.45), danglingLine, true),
            Arguments.of(IN, FieldType.SHUNT_CONDUCTANCE, null, Set.of(0.5, 0.6), danglingLine, false),
            Arguments.of(IN, FieldType.P0, null, Set.of(50.0, 100.0, 150.0), danglingLine, true),
            Arguments.of(IN, FieldType.P0, null, Set.of(120.0, 180.0), danglingLine, false),
            Arguments.of(IN, FieldType.Q0, null, Set.of(25.0, 50.0, 75.0), danglingLine, true),
            Arguments.of(IN, FieldType.Q0, null, Set.of(120.0, 180.0), danglingLine, false),
            // --- NOT_IN --- //
            // VoltageLevel fields
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 14.0), danglingLine, true),
            Arguments.of(NOT_IN, FieldType.NOMINAL_VOLTAGE, null, Set.of(12.0, 13.0, 14.0), danglingLine, false),
            // Dangling Line
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.1, 0.15), danglingLine, false),
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.2, 0.3), danglingLine, true),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, null, Set.of(0.15, 0.2, 0.25), danglingLine, false),
            Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, null, Set.of(0.3, 0.4), danglingLine, true),
            Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE, null, Set.of(0.25, 0.3, 0.35), danglingLine, false),
            Arguments.of(NOT_IN, FieldType.SHUNT_SUSCEPTANCE, null, Set.of(0.4, 0.5), danglingLine, true),
            Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE, null, Set.of(0.35, 0.4, 0.45), danglingLine, false),
            Arguments.of(NOT_IN, FieldType.SHUNT_CONDUCTANCE, null, Set.of(0.5, 0.6), danglingLine, true),
            Arguments.of(NOT_IN, FieldType.P0, null, Set.of(50.0, 100.0, 150.0), danglingLine, false),
            Arguments.of(NOT_IN, FieldType.P0, null, Set.of(120.0, 180.0), danglingLine, true),
            Arguments.of(NOT_IN, FieldType.Q0, null, Set.of(25.0, 50.0, 75.0), danglingLine, false),
            Arguments.of(NOT_IN, FieldType.Q0, null, Set.of(120.0, 180.0), danglingLine, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForHvdcLinesTest() {
        HvdcLine hvdcLine = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        Mockito.when(hvdcLine.getR()).thenReturn(0.1);
        Mockito.when(hvdcLine.getMaxP()).thenReturn(200.0);
        Mockito.when(hvdcLine.getActivePowerSetpoint()).thenReturn(100.0);
        Mockito.when(hvdcLine.getNominalV()).thenReturn(400.0);

        // Terminal fields
        Terminal terminal1 = Mockito.mock(Terminal.class);
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel1.getNominalV()).thenReturn(13.0);
        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);

        Terminal terminal2 = Mockito.mock(Terminal.class);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel2.getNominalV()).thenReturn(13.0);
        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);

        // Converter Stations fields
        HvdcConverterStation converterStation1 = Mockito.mock(HvdcConverterStation.class);
        HvdcConverterStation converterStation2 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation1.getTerminal()).thenReturn(terminal1);
        Mockito.when(converterStation2.getTerminal()).thenReturn(terminal2);
        Mockito.when(hvdcLine.getConverterStation1()).thenReturn(converterStation1);
        Mockito.when(hvdcLine.getConverterStation2()).thenReturn(converterStation2);

        // for testing none EXISTS
        HvdcLine hvdcLine1 = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine1.getType()).thenReturn(IdentifiableType.HVDC_LINE);
        Mockito.when(hvdcLine1.getR()).thenReturn(Double.NaN);
        Mockito.when(hvdcLine1.getMaxP()).thenReturn(Double.NaN);
        Mockito.when(hvdcLine1.getActivePowerSetpoint()).thenReturn(Double.NaN);
        Mockito.when(hvdcLine1.getNominalV()).thenReturn(Double.NaN);

        // Terminal fields
        Terminal terminal3 = Mockito.mock(Terminal.class);
        VoltageLevel voltageLevel3 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel3.getNominalV()).thenReturn(Double.NaN);
        Mockito.when(terminal3.getVoltageLevel()).thenReturn(voltageLevel3);
        Terminal terminal4 = Mockito.mock(Terminal.class);
        VoltageLevel voltageLevel4 = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel4.getNominalV()).thenReturn(Double.NaN);
        Mockito.when(terminal4.getVoltageLevel()).thenReturn(voltageLevel4);

        // None Converter Stations fields
        HvdcConverterStation converterStation3 = Mockito.mock(HvdcConverterStation.class);
        HvdcConverterStation converterStation4 = Mockito.mock(HvdcConverterStation.class);
        Mockito.when(converterStation3.getTerminal()).thenReturn(terminal3);
        Mockito.when(converterStation4.getTerminal()).thenReturn(terminal4);
        Mockito.when(hvdcLine1.getConverterStation1()).thenReturn(converterStation3);
        Mockito.when(hvdcLine1.getConverterStation2()).thenReturn(converterStation4);

        return Stream.of(
            // --- EQUALS --- //
            // Terminal fields
            Arguments.of(EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 13.0, null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 12.0, null, hvdcLine, false),
            Arguments.of(EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 13.0, null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 12.0, null, hvdcLine, false),
            // HvdcLine fields
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.SERIE_RESISTANCE, 0.2, null, hvdcLine, false),
            Arguments.of(EQUALS, FieldType.MAX_P, 200.0, null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.MAX_P, 300.0, null, hvdcLine, false),
            Arguments.of(EQUALS, FieldType.ACTIVE_POWER_SET_POINT, 100.0, null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.ACTIVE_POWER_SET_POINT, 200.0, null, hvdcLine, false),
            Arguments.of(EQUALS, FieldType.DC_NOMINAL_VOLTAGE, 400.0, null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.DC_NOMINAL_VOLTAGE, 200.0, null, hvdcLine, false),

            // --- GREATER_OR_EQUALS --- //
            // Terminal
            Arguments.of(GREATER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 12.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 13.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 14.0, null, hvdcLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 12.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 13.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 14.0, null, hvdcLine, false),
            // HvdcLine fields
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.05, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.15, null, hvdcLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 150.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 200.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.MAX_P, 300.0, null, hvdcLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.ACTIVE_POWER_SET_POINT, 50.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.ACTIVE_POWER_SET_POINT, 100.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.ACTIVE_POWER_SET_POINT, 150.0, null, hvdcLine, false),
            Arguments.of(GREATER_OR_EQUALS, FieldType.DC_NOMINAL_VOLTAGE, 300.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.DC_NOMINAL_VOLTAGE, 400.0, null, hvdcLine, true),
            Arguments.of(GREATER_OR_EQUALS, FieldType.DC_NOMINAL_VOLTAGE, 500.0, null, hvdcLine, false),
            // --- GREATER --- //
            // Terminal
            Arguments.of(GREATER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 12.0, null, hvdcLine, true),
            Arguments.of(GREATER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 13.0, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 14.0, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 12.0, null, hvdcLine, true),
            Arguments.of(GREATER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 13.0, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 14.0, null, hvdcLine, false),
            // HvdcLine fields
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.05, null, hvdcLine, true),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.1, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.SERIE_RESISTANCE, 0.15, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.MAX_P, 150.0, null, hvdcLine, true),
            Arguments.of(GREATER, FieldType.MAX_P, 200.0, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.MAX_P, 300.0, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.ACTIVE_POWER_SET_POINT, 50.0, null, hvdcLine, true),
            Arguments.of(GREATER, FieldType.ACTIVE_POWER_SET_POINT, 100.0, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.ACTIVE_POWER_SET_POINT, 150.0, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.DC_NOMINAL_VOLTAGE, 300.0, null, hvdcLine, true),
            Arguments.of(GREATER, FieldType.DC_NOMINAL_VOLTAGE, 400.0, null, hvdcLine, false),
            Arguments.of(GREATER, FieldType.DC_NOMINAL_VOLTAGE, 500.0, null, hvdcLine, false),

            // --- LOWER_OR_EQUALS --- //
            // Terminal
            Arguments.of(LOWER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 12.0, null, hvdcLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 13.0, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 14.0, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 12.0, null, hvdcLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 13.0, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 14.0, null, hvdcLine, true),
            // HvdcLine fields
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.05, null, hvdcLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.1, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.SERIE_RESISTANCE, 0.15, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 150.0, null, hvdcLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 200.0, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.MAX_P, 300.0, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.ACTIVE_POWER_SET_POINT, 50.0, null, hvdcLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.ACTIVE_POWER_SET_POINT, 100.0, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.ACTIVE_POWER_SET_POINT, 150.0, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.DC_NOMINAL_VOLTAGE, 300.0, null, hvdcLine, false),
            Arguments.of(LOWER_OR_EQUALS, FieldType.DC_NOMINAL_VOLTAGE, 400.0, null, hvdcLine, true),
            Arguments.of(LOWER_OR_EQUALS, FieldType.DC_NOMINAL_VOLTAGE, 500.0, null, hvdcLine, true),

            // --- LOWER --- //
            // Terminal
            Arguments.of(LOWER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 12.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 13.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, 14.0, null, hvdcLine, true),
            Arguments.of(LOWER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 12.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 13.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, 14.0, null, hvdcLine, true),
            // HvdcLine fields
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.05, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.1, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.SERIE_RESISTANCE, 0.15, null, hvdcLine, true),
            Arguments.of(LOWER, FieldType.MAX_P, 150.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.MAX_P, 200.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.MAX_P, 300.0, null, hvdcLine, true),
            Arguments.of(LOWER, FieldType.ACTIVE_POWER_SET_POINT, 50.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.ACTIVE_POWER_SET_POINT, 100.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.ACTIVE_POWER_SET_POINT, 150.0, null, hvdcLine, true),
            Arguments.of(LOWER, FieldType.DC_NOMINAL_VOLTAGE, 300.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.DC_NOMINAL_VOLTAGE, 400.0, null, hvdcLine, false),
            Arguments.of(LOWER, FieldType.DC_NOMINAL_VOLTAGE, 500.0, null, hvdcLine, true),

            // --- BETWEEN --- //
            // Terminal
            Arguments.of(BETWEEN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), hvdcLine, true),
            Arguments.of(BETWEEN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, Set.of(13.5, 14.0), hvdcLine, false),
            Arguments.of(BETWEEN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), hvdcLine, true),
            Arguments.of(BETWEEN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, Set.of(13.5, 14.0), hvdcLine, false),
            // HvdcLine fields
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.15), hvdcLine, true),
            Arguments.of(BETWEEN, FieldType.SERIE_RESISTANCE, null, Set.of(0.15, 0.25), hvdcLine, false),
            Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(150.0, 250.0), hvdcLine, true),
            Arguments.of(BETWEEN, FieldType.MAX_P, null, Set.of(225.0, 300.0), hvdcLine, false),
            Arguments.of(BETWEEN, FieldType.ACTIVE_POWER_SET_POINT, null, Set.of(50.0, 150.0), hvdcLine, true),
            Arguments.of(BETWEEN, FieldType.ACTIVE_POWER_SET_POINT, null, Set.of(120.0, 140.0), hvdcLine, false),
            Arguments.of(BETWEEN, FieldType.DC_NOMINAL_VOLTAGE, null, Set.of(300.0, 500.0), hvdcLine, true),
            Arguments.of(BETWEEN, FieldType.DC_NOMINAL_VOLTAGE, null, Set.of(450.0, 600.0), hvdcLine, false),

            // --- EXISTS --- //
            // Terminal
            Arguments.of(EXISTS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, null, hvdcLine, true),
            Arguments.of(EXISTS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, null, hvdcLine1, false),
            Arguments.of(EXISTS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, null, hvdcLine, true),
            Arguments.of(EXISTS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, null, hvdcLine1, false),
            // HvdcLine fields
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE, null, null, hvdcLine, true),
            Arguments.of(EXISTS, FieldType.SERIE_RESISTANCE, null, null, hvdcLine1, false),
            Arguments.of(EXISTS, FieldType.MAX_P, null, null, hvdcLine, true),
            Arguments.of(EXISTS, FieldType.MAX_P, null, null, hvdcLine1, false),
            Arguments.of(EXISTS, FieldType.ACTIVE_POWER_SET_POINT, null, null, hvdcLine, true),
            Arguments.of(EXISTS, FieldType.ACTIVE_POWER_SET_POINT, null, null, hvdcLine1, false),
            Arguments.of(EXISTS, FieldType.DC_NOMINAL_VOLTAGE, null, null, hvdcLine, true),
            Arguments.of(EXISTS, FieldType.DC_NOMINAL_VOLTAGE, null, null, hvdcLine1, false),

            // --- NOT_EXISTS --- //
            // Terminal
            Arguments.of(NOT_EXISTS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, null, hvdcLine, false),
            Arguments.of(NOT_EXISTS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, null, hvdcLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, null, hvdcLine, false),
            Arguments.of(NOT_EXISTS, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, null, hvdcLine1, true),
            // HvdcLine fields
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE, null, null, hvdcLine, false),
            Arguments.of(NOT_EXISTS, FieldType.SERIE_RESISTANCE, null, null, hvdcLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.MAX_P, null, null, hvdcLine, false),
            Arguments.of(NOT_EXISTS, FieldType.MAX_P, null, null, hvdcLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.ACTIVE_POWER_SET_POINT, null, null, hvdcLine, false),
            Arguments.of(NOT_EXISTS, FieldType.ACTIVE_POWER_SET_POINT, null, null, hvdcLine1, true),
            Arguments.of(NOT_EXISTS, FieldType.DC_NOMINAL_VOLTAGE, null, null, hvdcLine, false),
            Arguments.of(NOT_EXISTS, FieldType.DC_NOMINAL_VOLTAGE, null, null, hvdcLine1, true),

            // --- IN --- //
            // Terminal
            Arguments.of(IN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, Set.of(12.0, 13.0, 14.0), hvdcLine, true),
            Arguments.of(IN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), hvdcLine, false),
            Arguments.of(IN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, Set.of(12.0, 13.0, 14.0), hvdcLine, true),
            Arguments.of(IN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), hvdcLine, false),
            // HvdcLine fields
            Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.1, 0.15), hvdcLine, true),
            Arguments.of(IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.15), hvdcLine, false),
            Arguments.of(IN, FieldType.MAX_P, null, Set.of(50.0, 200.0, 300.0), hvdcLine, true),
            Arguments.of(IN, FieldType.MAX_P, null, Set.of(100.0, 150.0), hvdcLine, false),
            Arguments.of(IN, FieldType.ACTIVE_POWER_SET_POINT, null, Set.of(50.0, 100.0, 300.0), hvdcLine, true),
            Arguments.of(IN, FieldType.ACTIVE_POWER_SET_POINT, null, Set.of(200.0, 150.0), hvdcLine, false),
            Arguments.of(IN, FieldType.DC_NOMINAL_VOLTAGE, null, Set.of(100.0, 400.0, 300.0), hvdcLine, true),
            Arguments.of(IN, FieldType.DC_NOMINAL_VOLTAGE, null, Set.of(200.0, 150.0), hvdcLine, false),

            // --- NOT_IN --- //
            // Terminal
            Arguments.of(NOT_IN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, Set.of(12.0, 14.0), hvdcLine, true),
            Arguments.of(NOT_IN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_1, null, Set.of(12.0, 13.0, 14.0), hvdcLine, false),
            Arguments.of(NOT_IN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, Set.of(12.0, 14.0), hvdcLine, true),
            Arguments.of(NOT_IN, FieldType.CONVERTER_STATION_NOMINAL_VOLTAGE_2, null, Set.of(12.0, 13.0, 14.0), hvdcLine, false),
            // HvdcLine fields
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.15), hvdcLine, true),
            Arguments.of(NOT_IN, FieldType.SERIE_RESISTANCE, null, Set.of(0.05, 0.1, 0.15), hvdcLine, false),
            Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(50.0, 200.0, 300.0), hvdcLine, false),
            Arguments.of(NOT_IN, FieldType.MAX_P, null, Set.of(100.0, 150.0), hvdcLine, true),
            Arguments.of(NOT_IN, FieldType.ACTIVE_POWER_SET_POINT, null, Set.of(50.0, 100.0, 300.0), hvdcLine, false),
            Arguments.of(NOT_IN, FieldType.ACTIVE_POWER_SET_POINT, null, Set.of(200.0, 150.0), hvdcLine, true),
            Arguments.of(NOT_IN, FieldType.DC_NOMINAL_VOLTAGE, null, Set.of(100.0, 200.0, 300.0), hvdcLine, true),
            Arguments.of(NOT_IN, FieldType.DC_NOMINAL_VOLTAGE, null, Set.of(400.0, 150.0), hvdcLine, false)
        );

    }
}
