package org.gridsuite.filter.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.expertrule.EnumExpertRule;
import org.gridsuite.filter.utils.RegulationType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.utils.expertfilter.RatioRegulationModeType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EnumExpertRuleTest {
    private FilterLoader filterLoader;

    @BeforeEach
    void setUp() {
        filterLoader = uuids -> null;
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operator, FieldType field, Identifiable<?> equipment, String value, Set<String> values, Class<Throwable> expectedException) {
        EnumExpertRule rule = EnumExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForTestWithException() {

        Network network = Mockito.mock(Network.class);
        Mockito.when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = Mockito.mock(Generator.class);
        Mockito.when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        Mockito.when(generator.getEnergySource()).thenReturn(EnergySource.HYDRO);

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getType()).thenReturn(IdentifiableType.SUBSTATION);

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(ratioTapChanger.getRegulationMode()).thenReturn(RatioTapChanger.RegulationMode.VOLTAGE);

        PhaseTapChanger phaseTapChanger = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(twoWindingsTransformer.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        Mockito.when(phaseTapChanger.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        StaticVarCompensator svar = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        HvdcLine hvdcLine = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);

        return Stream.of(
                // --- Test an unsupported field for each equipment --- //
                Arguments.of(EQUALS, FieldType.RATED_S, network, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, voltageLevel, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.P0, generator, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, load, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, shuntCompensator, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, bus, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, busbarSection, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, battery, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, substation, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.P0, twoWindingsTransformer, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, svar, null, null, PowsyblException.class),
                Arguments.of(EQUALS, FieldType.RATED_S, hvdcLine, null, null, PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(IS, FieldType.ENERGY_SOURCE, generator, null, null, PowsyblException.class),
                Arguments.of(IS, FieldType.RATIO_REGULATION_MODE, twoWindingsTransformer, null, null, PowsyblException.class),
                Arguments.of(IS, FieldType.PHASE_REGULATION_MODE, twoWindingsTransformer, null, null, PowsyblException.class),

                // --- Test an unsupported equipment type for field type RATIO_REGULATION_MODE --- //
                Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE, battery, null, null, PowsyblException.class),

                // --- Test an unsupported equipment type for field type PHASE_REGULATION_MODE --- //
                Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE, shuntCompensator, null, null, PowsyblException.class)
                );
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForGeneratorTest",
        "provideArgumentsForLoadTest",
        "provideArgumentsForBusTest",
        "provideArgumentsForShuntCompensatorTest",
        "provideArgumentsForBusBarSectionTest",
        "provideArgumentsForBatteryTest",
        "provideArgumentsForVoltageLevelTest",
        "provideArgumentsForSubstationTest",
        "provideArgumentsForLinesTest",
        "provideArgumentsForTwoWindingTransformerTest",
        "provideArgumentsForStaticVarCompensatorTest",
        "provideArgumentsForDanglingLineTest",
        "provideArgumentsForThreeWindingTransformerTest",
        "provideArgumentsForHvdcLineTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, String value, Set<String> values, Identifiable<?> equipment, boolean expected) {
        EnumExpertRule rule = EnumExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(gen.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // Generator fields
                Arguments.of(EQUALS, FieldType.ENERGY_SOURCE, EnergySource.HYDRO.name(), null, gen, true),
                Arguments.of(EQUALS, FieldType.ENERGY_SOURCE, EnergySource.THERMAL.name(), null, gen, false),
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, gen, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, gen, false),

                // --- NOT_EQUALS --- //
                // Generator fields
                Arguments.of(NOT_EQUALS, FieldType.ENERGY_SOURCE, EnergySource.THERMAL.name(), null, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.ENERGY_SOURCE, EnergySource.HYDRO.name(), null, gen, false),
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, gen, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, gen, false),

                // --- IN --- //
                // Generator fields
                Arguments.of(IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.HYDRO.name(), EnergySource.THERMAL.name()), gen, true),
                Arguments.of(IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.NUCLEAR.name(), EnergySource.THERMAL.name()), gen, false),
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), gen, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), gen, false),

                // --- NOT_IN --- //
                // Generator fields
                Arguments.of(NOT_IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.NUCLEAR.name(), EnergySource.THERMAL.name()), gen, true),
                Arguments.of(NOT_IN, FieldType.ENERGY_SOURCE, null, Set.of(EnergySource.HYDRO.name(), EnergySource.THERMAL.name()), gen, false),
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), gen, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), gen, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLoadTest() {

        Load load = Mockito.mock(Load.class);
        Mockito.when(load.getType()).thenReturn(IdentifiableType.LOAD);
        Mockito.when(load.getLoadType()).thenReturn(LoadType.AUXILIARY);

        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(load.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, load, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, load, false),
                Arguments.of(EQUALS, FieldType.LOAD_TYPE, LoadType.AUXILIARY.name(), null, load, true),
                Arguments.of(EQUALS, FieldType.LOAD_TYPE, LoadType.UNDEFINED.name(), null, load, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, load, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, load, false),
                Arguments.of(NOT_EQUALS, FieldType.LOAD_TYPE, LoadType.AUXILIARY.name(), null, load, false),
                Arguments.of(NOT_EQUALS, FieldType.LOAD_TYPE, LoadType.FICTITIOUS.name(), null, load, true),

                // --- IN --- //
                 // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), load, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), load, false),
                Arguments.of(IN, FieldType.LOAD_TYPE, null, Set.of(LoadType.UNDEFINED.name(), LoadType.AUXILIARY.name()), load, true),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), load, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), load, false),
                Arguments.of(NOT_IN, FieldType.LOAD_TYPE, null, Set.of(LoadType.UNDEFINED.name(), LoadType.FICTITIOUS.name()), load, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusTest() {

        Bus bus = Mockito.mock(Bus.class);
        Mockito.when(bus.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(bus.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, bus, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, bus, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, bus, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, bus, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), bus, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), bus, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), bus, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), bus, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBusBarSectionTest() {

        BusbarSection busbarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(busbarSection.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, busbarSection, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, busbarSection, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, busbarSection, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, busbarSection, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), busbarSection, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), busbarSection, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), busbarSection, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), busbarSection, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForShuntCompensatorTest() {

        ShuntCompensator shuntCompensator = Mockito.mock(ShuntCompensator.class);
        Mockito.when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Mockito.when(shuntCompensator.getModel(ShuntCompensatorLinearModel.class)).thenReturn(new ShuntCompensatorLinearModel() {
            @Override
            public double getBPerSection() {
                return -1.;
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
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(shuntCompensator.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, shuntCompensator, false),

                // Shunt Compensator fields
                Arguments.of(EQUALS, FieldType.SHUNT_COMPENSATOR_TYPE, "REACTOR", null, shuntCompensator, true),
                Arguments.of(EQUALS, FieldType.SHUNT_COMPENSATOR_TYPE, "CAPACITOR", null, shuntCompensator, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, shuntCompensator, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, shuntCompensator, false),

                // Shunt Compensator fields
                Arguments.of(NOT_EQUALS, FieldType.SHUNT_COMPENSATOR_TYPE, "CAPACITOR", null, shuntCompensator, true),
                Arguments.of(NOT_EQUALS, FieldType.SHUNT_COMPENSATOR_TYPE, "REACTOR", null, shuntCompensator, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), shuntCompensator, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), shuntCompensator, false),

                // Shunt Compensator fields
                Arguments.of(IN, FieldType.SHUNT_COMPENSATOR_TYPE, null, Set.of("REACTOR"), shuntCompensator, true),
                Arguments.of(IN, FieldType.SHUNT_COMPENSATOR_TYPE, null, Set.of("CAPACITOR"), shuntCompensator, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), shuntCompensator, false),

                // Shunt Compensator fields
                Arguments.of(NOT_IN, FieldType.SHUNT_COMPENSATOR_TYPE, null, Set.of("CAPACITOR"), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SHUNT_COMPENSATOR_TYPE, null, Set.of("REACTOR"), shuntCompensator, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForBatteryTest() {

        Battery battery = Mockito.mock(Battery.class);
        Mockito.when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(battery.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, battery, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, battery, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, battery, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, battery, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), battery, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), battery, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), battery, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), battery, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForDanglingLineTest() {

        DanglingLine danglingLine = Mockito.mock(DanglingLine.class);
        Mockito.when(danglingLine.getType()).thenReturn(IdentifiableType.DANGLING_LINE);
        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(danglingLine.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
            // --- EQUALS --- //
            // VoltageLevel fields
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, danglingLine, true),
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, danglingLine, false),

            // --- NOT_EQUALS --- //
            // VoltageLevel fields
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, danglingLine, true),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, danglingLine, false),

            // --- IN --- //
            // VoltageLevel fields
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), danglingLine, true),
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), danglingLine, false),

            // --- NOT_IN --- //
            // VoltageLevel fields
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), danglingLine, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), danglingLine, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForVoltageLevelTest() {

        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
            // --- EQUALS --- //
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, voltageLevel, true),
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, voltageLevel, false),

            // --- NOT_EQUALS --- //
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, voltageLevel, true),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, voltageLevel, false),

            // --- IN --- //
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), voltageLevel, true),
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), voltageLevel, false),

            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), voltageLevel, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), voltageLevel, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForSubstationTest() {

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getType()).thenReturn(IdentifiableType.SUBSTATION);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
            // --- EQUALS --- //
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, substation, true),
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, substation, false),

            // --- NOT_EQUALS --- //
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, substation, true),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, substation, false),

            // --- IN --- //
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), substation, true),
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), substation, false),

            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), substation, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), substation, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForLinesTest() {

        Line line = Mockito.mock(Line.class);
        Mockito.when(line.getType()).thenReturn(IdentifiableType.LINE);
        // VoltageLevel fields
        Substation substation1 = Mockito.mock(Substation.class);
        Substation substation2 = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Terminal terminal2 = Mockito.mock(Terminal.class);

        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));

        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        Mockito.when(substation1.getCountry()).thenReturn(Optional.of(Country.FR));

        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        Mockito.when(substation2.getCountry()).thenReturn(Optional.of(Country.SM));

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY_1, Country.FR.name(), null, line, true),
                Arguments.of(EQUALS, FieldType.COUNTRY_1, Country.DE.name(), null, line, false),
                Arguments.of(EQUALS, FieldType.COUNTRY_2, Country.SM.name(), null, line, true),
                Arguments.of(EQUALS, FieldType.COUNTRY_2, Country.LI.name(), null, line, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY_1, Country.DE.name(), null, line, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY_1, Country.FR.name(), null, line, false),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY_2, Country.LI.name(), null, line, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY_2, Country.SM.name(), null, line, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY_1, null, Set.of(Country.FR.name(), Country.DE.name()), line, true),
                Arguments.of(IN, FieldType.COUNTRY_1, null, Set.of(Country.BE.name(), Country.DE.name()), line, false),
                Arguments.of(IN, FieldType.COUNTRY_2, null, Set.of(Country.SM.name(), Country.FO.name()), line, true),
                Arguments.of(IN, FieldType.COUNTRY_2, null, Set.of(Country.LI.name(), Country.MC.name()), line, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY_1, null, Set.of(Country.BE.name(), Country.DE.name()), line, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY_1, null, Set.of(Country.FR.name(), Country.DE.name()), line, false),
                Arguments.of(NOT_IN, FieldType.COUNTRY_2, null, Set.of(Country.LI.name(), Country.MC.name()), line, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY_2, null, Set.of(Country.SM.name(), Country.FO.name()), line, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForTwoWindingTransformerTest() {

        TwoWindingsTransformer twoWindingsTransformer = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        Mockito.when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        Mockito.when(ratioTapChanger.isRegulating()).thenReturn(true);
        Mockito.when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(false);
        Mockito.when(ratioTapChanger.getRegulationValue()).thenReturn(225.);

        TwoWindingsTransformer twoWindingsTransformer2 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer2.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        Mockito.when(twoWindingsTransformer2.getRatioTapChanger()).thenReturn(null);

        Mockito.when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        PhaseTapChanger phaseTapChanger = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(twoWindingsTransformer.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        Mockito.when(phaseTapChanger.isRegulating()).thenReturn(true);
        Mockito.when(phaseTapChanger.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
        Mockito.when(phaseTapChanger.getRegulationValue()).thenReturn(100.);

        Mockito.when(twoWindingsTransformer2.getPhaseTapChanger()).thenReturn(null);

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));
        Mockito.when(twoWindingsTransformer.getSubstation()).thenReturn(Optional.of(substation));

        TwoWindingsTransformer twoWindingsTransformer3 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer3.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        RatioTapChanger ratioTapChanger3 = Mockito.mock(RatioTapChanger.class);
        Mockito.when(twoWindingsTransformer3.getRatioTapChanger()).thenReturn(ratioTapChanger3);
        Mockito.when(ratioTapChanger3.isRegulating()).thenReturn(false);
        Mockito.when(ratioTapChanger3.hasLoadTapChangingCapabilities()).thenReturn(true);

        TwoWindingsTransformer twoWindingsTransformer4 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer4.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        RatioTapChanger ratioTapChanger4 = Mockito.mock(RatioTapChanger.class);
        Mockito.when(twoWindingsTransformer4.getRatioTapChanger()).thenReturn(ratioTapChanger4);
        Mockito.when(ratioTapChanger4.isRegulating()).thenReturn(true);
        Mockito.when(ratioTapChanger4.hasLoadTapChangingCapabilities()).thenReturn(true);

        TwoWindingsTransformer twoWindingsTransformer5 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer5.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger5 = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(twoWindingsTransformer5.getPhaseTapChanger()).thenReturn(phaseTapChanger5);
        Mockito.when(phaseTapChanger5.isRegulating()).thenReturn(false);
        Mockito.when(phaseTapChanger5.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        TwoWindingsTransformer twoWindingsTransformer6 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer6.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger6 = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(twoWindingsTransformer6.getPhaseTapChanger()).thenReturn(phaseTapChanger6);
        Mockito.when(phaseTapChanger6.isRegulating()).thenReturn(false);
        Mockito.when(phaseTapChanger6.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);

        TwoWindingsTransformer twoWindingsTransformer7 = Mockito.mock(TwoWindingsTransformer.class);
        Mockito.when(twoWindingsTransformer7.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger7 = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(twoWindingsTransformer7.getPhaseTapChanger()).thenReturn(phaseTapChanger7);
        Mockito.when(phaseTapChanger7.isRegulating()).thenReturn(true);
        Mockito.when(phaseTapChanger7.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        return Stream.of(
            // --- EQUALS --- //
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE, RatioRegulationModeType.FIXED_RATIO.name(), null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE, RatioRegulationModeType.FIXED_RATIO.name(), null, twoWindingsTransformer3, true),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE, RatioRegulationModeType.FIXED_RATIO.name(), null, twoWindingsTransformer4, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, twoWindingsTransformer2, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, twoWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, twoWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, twoWindingsTransformer2, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, twoWindingsTransformer5, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, twoWindingsTransformer6, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, twoWindingsTransformer7, false),

            // --- NOT_EQUALS --- //
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, twoWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, twoWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATION_MODE, RatioRegulationModeType.FIXED_RATIO.name(), null, twoWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATION_MODE, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, twoWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, twoWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.PHASE_REGULATION_MODE, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, twoWindingsTransformer, true),

            // --- IN --- //
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), twoWindingsTransformer, true),
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATIO_REGULATION_MODE, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name(), RatioRegulationModeType.FIXED_RATIO.name()), twoWindingsTransformer, false),
            Arguments.of(IN, FieldType.PHASE_REGULATION_MODE, null, Set.of(PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name()), twoWindingsTransformer, true),

            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATIO_REGULATION_MODE, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name()), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.PHASE_REGULATION_MODE, null, Set.of(PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name()), twoWindingsTransformer, false)
            );
    }

    private static Stream<Arguments> provideArgumentsForThreeWindingTransformerTest() {

        // transfo 1
        ThreeWindingsTransformer threeWindingsTransformer = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        // Ratio Tap Changer
        RatioTapChanger ratioTapChanger = Mockito.mock(RatioTapChanger.class);
        ThreeWindingsTransformer.Leg leg = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(ratioTapChanger.isRegulating()).thenReturn(true);
        Mockito.when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(false);
        Mockito.when(ratioTapChanger.getRegulationValue()).thenReturn(225.);
        Mockito.when(leg.getRatioTapChanger()).thenReturn(ratioTapChanger);

        // Phase Tap Changer
        PhaseTapChanger phaseTapChanger = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(leg.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        Mockito.when(phaseTapChanger.isRegulating()).thenReturn(true);
        Mockito.when(phaseTapChanger.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
        Mockito.when(phaseTapChanger.getRegulationValue()).thenReturn(100.);

        Mockito.when(threeWindingsTransformer.getLeg1()).thenReturn(leg);
        Mockito.when(threeWindingsTransformer.getLeg2()).thenReturn(leg);
        Mockito.when(threeWindingsTransformer.getLeg3()).thenReturn(leg);

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));
        Mockito.when(threeWindingsTransformer.getSubstation()).thenReturn(Optional.of(substation));

        // transfo 2
        ThreeWindingsTransformer threeWindingsTransformer2 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer2.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        ThreeWindingsTransformer.Leg leg2 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg2.getRatioTapChanger()).thenReturn(null);
        Mockito.when(leg2.getPhaseTapChanger()).thenReturn(null);

        Mockito.when(threeWindingsTransformer2.getLeg1()).thenReturn(leg2);
        Mockito.when(threeWindingsTransformer2.getLeg2()).thenReturn(leg2);
        Mockito.when(threeWindingsTransformer2.getLeg3()).thenReturn(leg2);

        // transfo 3
        ThreeWindingsTransformer threeWindingsTransformer3 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer3.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        // Ratio Tap Changer
        RatioTapChanger ratioTapChanger3 = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger3.isRegulating()).thenReturn(false);
        Mockito.when(ratioTapChanger3.hasLoadTapChangingCapabilities()).thenReturn(true);
        ThreeWindingsTransformer.Leg leg3 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg3.getRatioTapChanger()).thenReturn(ratioTapChanger3);
        Mockito.when(threeWindingsTransformer3.getLeg1()).thenReturn(leg3);
        Mockito.when(threeWindingsTransformer3.getLeg2()).thenReturn(leg3);
        Mockito.when(threeWindingsTransformer3.getLeg3()).thenReturn(leg3);

        // transfo 4
        ThreeWindingsTransformer threeWindingsTransformer4 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer4.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        RatioTapChanger ratioTapChanger4 = Mockito.mock(RatioTapChanger.class);
        Mockito.when(ratioTapChanger4.isRegulating()).thenReturn(true);
        Mockito.when(ratioTapChanger4.hasLoadTapChangingCapabilities()).thenReturn(true);

        ThreeWindingsTransformer.Leg leg4 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg4.getRatioTapChanger()).thenReturn(ratioTapChanger4);
        Mockito.when(threeWindingsTransformer4.getLeg1()).thenReturn(leg4);
        Mockito.when(threeWindingsTransformer4.getLeg2()).thenReturn(leg4);
        Mockito.when(threeWindingsTransformer4.getLeg3()).thenReturn(leg4);

        // transfo 5
        ThreeWindingsTransformer threeWindingsTransformer5 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer5.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger5 = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger5.isRegulating()).thenReturn(false);
        Mockito.when(phaseTapChanger5.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        ThreeWindingsTransformer.Leg leg5 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg5.getPhaseTapChanger()).thenReturn(phaseTapChanger5);
        Mockito.when(threeWindingsTransformer5.getLeg1()).thenReturn(leg5);
        Mockito.when(threeWindingsTransformer5.getLeg2()).thenReturn(leg5);
        Mockito.when(threeWindingsTransformer5.getLeg3()).thenReturn(leg5);

        // transfo 6
        ThreeWindingsTransformer threeWindingsTransformer6 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer6.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger6 = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger6.isRegulating()).thenReturn(false);
        Mockito.when(phaseTapChanger6.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);

        ThreeWindingsTransformer.Leg leg6 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg6.getPhaseTapChanger()).thenReturn(phaseTapChanger6);
        Mockito.when(threeWindingsTransformer6.getLeg1()).thenReturn(leg6);
        Mockito.when(threeWindingsTransformer6.getLeg2()).thenReturn(leg6);
        Mockito.when(threeWindingsTransformer6.getLeg3()).thenReturn(leg6);

        // transfo 7
        ThreeWindingsTransformer threeWindingsTransformer7 = Mockito.mock(ThreeWindingsTransformer.class);
        Mockito.when(threeWindingsTransformer7.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger7 = Mockito.mock(PhaseTapChanger.class);
        Mockito.when(phaseTapChanger7.isRegulating()).thenReturn(true);
        Mockito.when(phaseTapChanger7.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        ThreeWindingsTransformer.Leg leg7 = Mockito.mock(ThreeWindingsTransformer.Leg.class);
        Mockito.when(leg7.getPhaseTapChanger()).thenReturn(phaseTapChanger7);
        Mockito.when(threeWindingsTransformer7.getLeg1()).thenReturn(leg7);
        Mockito.when(threeWindingsTransformer7.getLeg2()).thenReturn(leg7);
        Mockito.when(threeWindingsTransformer7.getLeg3()).thenReturn(leg7);

        return Stream.of(
            // --- EQUALS --- //
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_1, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_2, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_3, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_1, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_2, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_3, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_1, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer3, true),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_2, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer3, true),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_3, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer3, true),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_1, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer4, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_2, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer4, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_3, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer4, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_1, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer2, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_2, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer2, false),
            Arguments.of(EQUALS, FieldType.RATIO_REGULATION_MODE_3, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer2, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_1, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_2, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_3, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer, true),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_1, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_2, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_3, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_1, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer2, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_2, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer2, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_3, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer2, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_1, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer5, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_2, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer5, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_3, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer5, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_1, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer7, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_2, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer7, false),
            Arguments.of(EQUALS, FieldType.PHASE_REGULATION_MODE_3, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer7, false),

            // --- NOT_EQUALS --- //
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATION_MODE_1, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATION_MODE_2, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATION_MODE_3, RatioRegulationModeType.FIXED_RATIO.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATION_MODE_1, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATION_MODE_2, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.RATIO_REGULATION_MODE_3, RatioRegulationModeType.VOLTAGE_REGULATION.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.PHASE_REGULATION_MODE_1, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.PHASE_REGULATION_MODE_2, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.PHASE_REGULATION_MODE_3, PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), null, threeWindingsTransformer, false),
            Arguments.of(NOT_EQUALS, FieldType.PHASE_REGULATION_MODE_1, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.PHASE_REGULATION_MODE_2, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer, true),
            Arguments.of(NOT_EQUALS, FieldType.PHASE_REGULATION_MODE_3, PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name(), null, threeWindingsTransformer, true),

            // --- IN --- //
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), threeWindingsTransformer, true),
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATIO_REGULATION_MODE_1, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name(), RatioRegulationModeType.FIXED_RATIO.name()), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATIO_REGULATION_MODE_2, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name(), RatioRegulationModeType.FIXED_RATIO.name()), threeWindingsTransformer, false),
            Arguments.of(IN, FieldType.RATIO_REGULATION_MODE_3, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name(), RatioRegulationModeType.FIXED_RATIO.name()), threeWindingsTransformer, false),

            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATIO_REGULATION_MODE_1, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name()), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATIO_REGULATION_MODE_2, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name()), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATIO_REGULATION_MODE_3, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name()), threeWindingsTransformer, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForStaticVarCompensatorTest() {

        StaticVarCompensator svar = Mockito.mock(StaticVarCompensator.class);
        Mockito.when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);
        Mockito.when(svar.getId()).thenReturn("SVAR");
        Mockito.when(svar.getRegulationMode()).thenReturn(StaticVarCompensator.RegulationMode.VOLTAGE);

        // VoltageLevel fields
        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(svar.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getCountry()).thenReturn(Optional.of(Country.FR));
        // Regulating terminal fields
        Terminal regulatingTerminal = Mockito.mock(Terminal.class);
        VoltageLevel distantVoltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(distantVoltageLevel.getId()).thenReturn("VL_2");
        Mockito.when(regulatingTerminal.getVoltageLevel()).thenReturn(distantVoltageLevel);
        BusbarSection regulatedBusBarSection = Mockito.mock(BusbarSection.class);
        Mockito.when(regulatedBusBarSection.getId()).thenReturn("BBS");
        Mockito.when(regulatingTerminal.getConnectable()).thenReturn(regulatedBusBarSection);
        Mockito.when(svar.getRegulatingTerminal()).thenReturn(regulatingTerminal);

        return Stream.of(
                // --- EQUALS --- //
                // VoltageLevel fields
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, svar, true),
                Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, svar, false),

                // Static Var Compensator fields
                Arguments.of(EQUALS, FieldType.SVAR_REGULATION_MODE, StaticVarCompensator.RegulationMode.VOLTAGE.name(), null, svar, true),
                Arguments.of(EQUALS, FieldType.SVAR_REGULATION_MODE, StaticVarCompensator.RegulationMode.REACTIVE_POWER.name(), null, svar, false),
                Arguments.of(EQUALS, FieldType.REGULATION_TYPE, RegulationType.DISTANT.name(), null, svar, true),
                Arguments.of(EQUALS, FieldType.REGULATION_TYPE, RegulationType.LOCAL.name(), null, svar, false),

                // --- NOT_EQUALS --- //
                // VoltageLevel fields
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, svar, true),
                Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, svar, false),

                // Static Var Compensator fields
                Arguments.of(NOT_EQUALS, FieldType.SVAR_REGULATION_MODE, StaticVarCompensator.RegulationMode.VOLTAGE.name(), null, svar, false),
                Arguments.of(NOT_EQUALS, FieldType.SVAR_REGULATION_MODE, StaticVarCompensator.RegulationMode.REACTIVE_POWER.name(), null, svar, true),
                Arguments.of(NOT_EQUALS, FieldType.REGULATION_TYPE, RegulationType.LOCAL.name(), null, svar, true),
                Arguments.of(NOT_EQUALS, FieldType.REGULATION_TYPE, RegulationType.DISTANT.name(), null, svar, false),

                // --- IN --- //
                // VoltageLevel fields
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), svar, true),
                Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), svar, false),

                // Static Var Compensator fields
                Arguments.of(IN, FieldType.SVAR_REGULATION_MODE, null, Set.of(StaticVarCompensator.RegulationMode.VOLTAGE.name()), svar, true),
                Arguments.of(IN, FieldType.SVAR_REGULATION_MODE, null, Set.of(StaticVarCompensator.RegulationMode.REACTIVE_POWER.name()), svar, false),
                Arguments.of(IN, FieldType.REGULATION_TYPE, null, Set.of(RegulationType.DISTANT.name()), svar, true),
                Arguments.of(IN, FieldType.REGULATION_TYPE, null, Set.of(RegulationType.LOCAL.name()), svar, false),

                // --- NOT_IN --- //
                // VoltageLevel fields
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), svar, true),
                Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), svar, false),

                // Static Var Compensator fields
                Arguments.of(NOT_IN, FieldType.SVAR_REGULATION_MODE, null, Set.of(StaticVarCompensator.RegulationMode.REACTIVE_POWER.name()), svar, true),
                Arguments.of(NOT_IN, FieldType.SVAR_REGULATION_MODE, null, Set.of(StaticVarCompensator.RegulationMode.VOLTAGE.name()), svar, false),
                Arguments.of(NOT_IN, FieldType.REGULATION_TYPE, null, Set.of(RegulationType.LOCAL.name()), svar, true),
                Arguments.of(NOT_IN, FieldType.REGULATION_TYPE, null, Set.of(RegulationType.DISTANT.name()), svar, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForHvdcLineTest() {

        HvdcLine hvdcLine = Mockito.mock(HvdcLine.class);
        Mockito.when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);

        // VoltageLevel fields
        Substation substation1 = Mockito.mock(Substation.class);
        Substation substation2 = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel1 = Mockito.mock(VoltageLevel.class);
        VoltageLevel voltageLevel2 = Mockito.mock(VoltageLevel.class);
        Terminal terminal1 = Mockito.mock(Terminal.class);
        Terminal terminal2 = Mockito.mock(Terminal.class);
        HvdcConverterStation converterStation1 = Mockito.mock(HvdcConverterStation.class);
        HvdcConverterStation converterStation2 = Mockito.mock(HvdcConverterStation.class);

        Mockito.when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));
        Mockito.when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));

        Mockito.when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        Mockito.when(converterStation1.getTerminal()).thenReturn(terminal1);
        Mockito.when(substation1.getCountry()).thenReturn(Optional.of(Country.FR));
        Mockito.when(hvdcLine.getConverterStation1()).thenReturn(converterStation1);

        Mockito.when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        Mockito.when(converterStation2.getTerminal()).thenReturn(terminal2);
        Mockito.when(substation2.getCountry()).thenReturn(Optional.of(Country.SM));
        Mockito.when(hvdcLine.getConverterStation2()).thenReturn(converterStation2);

        Mockito.when(hvdcLine.getConvertersMode()).thenReturn(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER);

        return Stream.of(
            // --- EQUALS --- //
            // VoltageLevel fields
            Arguments.of(EQUALS, FieldType.COUNTRY_1, Country.FR.name(), null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.COUNTRY_1, Country.DE.name(), null, hvdcLine, false),
            Arguments.of(EQUALS, FieldType.COUNTRY_2, Country.SM.name(), null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.COUNTRY_2, Country.LI.name(), null, hvdcLine, false),
            Arguments.of(EQUALS, FieldType.CONVERTERS_MODE, HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER.name(), null, hvdcLine, true),
            Arguments.of(EQUALS, FieldType.CONVERTERS_MODE, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER.name(), null, hvdcLine, false),

            // --- NOT_EQUALS --- //
            // VoltageLevel fields
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY_1, Country.DE.name(), null, hvdcLine, true),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY_1, Country.FR.name(), null, hvdcLine, false),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY_2, Country.LI.name(), null, hvdcLine, true),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY_2, Country.SM.name(), null, hvdcLine, false),
            Arguments.of(NOT_EQUALS, FieldType.CONVERTERS_MODE, HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER.name(), null, hvdcLine, false),
            Arguments.of(NOT_EQUALS, FieldType.CONVERTERS_MODE, HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER.name(), null, hvdcLine, true),

            // --- IN --- //
            // VoltageLevel fields
            Arguments.of(IN, FieldType.COUNTRY_1, null, Set.of(Country.FR.name(), Country.DE.name()), hvdcLine, true),
            Arguments.of(IN, FieldType.COUNTRY_1, null, Set.of(Country.BE.name(), Country.DE.name()), hvdcLine, false),
            Arguments.of(IN, FieldType.COUNTRY_2, null, Set.of(Country.SM.name(), Country.FO.name()), hvdcLine, true),
            Arguments.of(IN, FieldType.COUNTRY_2, null, Set.of(Country.LI.name(), Country.MC.name()), hvdcLine, false),
            Arguments.of(IN, FieldType.CONVERTERS_MODE, null, Set.of(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER.name()), hvdcLine, true),
            Arguments.of(IN, FieldType.CONVERTERS_MODE, null, Set.of(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER.name()), hvdcLine, false),

            // --- NOT_IN --- //
            // VoltageLevel fields
            Arguments.of(NOT_IN, FieldType.COUNTRY_1, null, Set.of(Country.BE.name(), Country.DE.name()), hvdcLine, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY_1, null, Set.of(Country.FR.name(), Country.DE.name()), hvdcLine, false),
            Arguments.of(NOT_IN, FieldType.COUNTRY_2, null, Set.of(Country.LI.name(), Country.MC.name()), hvdcLine, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY_2, null, Set.of(Country.SM.name(), Country.FO.name()), hvdcLine, false),
            Arguments.of(NOT_IN, FieldType.CONVERTERS_MODE, null, Set.of(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER.name()), hvdcLine, false),
            Arguments.of(NOT_IN, FieldType.CONVERTERS_MODE, null, Set.of(HvdcLine.ConvertersMode.SIDE_1_RECTIFIER_SIDE_2_INVERTER.name()), hvdcLine, true)
        );
    }
}
