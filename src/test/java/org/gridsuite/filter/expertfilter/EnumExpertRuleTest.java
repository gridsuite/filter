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

import java.util.HashMap;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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

        Network network = mock(Network.class);
        when(network.getType()).thenReturn(IdentifiableType.NETWORK);

        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);

        Generator generator = mock(Generator.class);
        when(generator.getType()).thenReturn(IdentifiableType.GENERATOR);
        when(generator.getEnergySource()).thenReturn(EnergySource.HYDRO);

        Load load = mock(Load.class);
        when(load.getType()).thenReturn(IdentifiableType.LOAD);

        ShuntCompensator shuntCompensator = mock(ShuntCompensator.class);
        when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        Bus bus = mock(Bus.class);
        when(bus.getType()).thenReturn(IdentifiableType.BUS);

        BusbarSection busbarSection = mock(BusbarSection.class);
        when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);

        Battery battery = mock(Battery.class);
        when(battery.getType()).thenReturn(IdentifiableType.BATTERY);

        Substation substation = mock(Substation.class);
        when(substation.getType()).thenReturn(IdentifiableType.SUBSTATION);

        TwoWindingsTransformer twoWindingsTransformer = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        RatioTapChanger ratioTapChanger = mock(RatioTapChanger.class);
        when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        when(ratioTapChanger.getRegulationMode()).thenReturn(RatioTapChanger.RegulationMode.VOLTAGE);

        PhaseTapChanger phaseTapChanger = mock(PhaseTapChanger.class);
        when(twoWindingsTransformer.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        when(phaseTapChanger.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        StaticVarCompensator svar = mock(StaticVarCompensator.class);
        when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);

        HvdcLine hvdcLine = mock(HvdcLine.class);
        when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);

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
        "provideArgumentsForBoundaryLineTest",
        "provideArgumentsForThreeWindingTransformerTest",
        "provideArgumentsForHvdcLineTest",
    })
    void testEvaluateRule(OperatorType operator, FieldType field, String value, Set<String> values, Identifiable<?> equipment, boolean expected) {
        EnumExpertRule rule = EnumExpertRule.builder().operator(operator).field(field).value(value).values(values).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForGeneratorTest() {

        Generator gen = mock(Generator.class);
        when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        // VoltageLevel fields
        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(gen.getTerminal()).thenReturn(terminal);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

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

        Load load = mock(Load.class);
        when(load.getType()).thenReturn(IdentifiableType.LOAD);
        when(load.getLoadType()).thenReturn(LoadType.AUXILIARY);

        // VoltageLevel fields
        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(load.getTerminal()).thenReturn(terminal);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

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

        Bus bus = mock(Bus.class);
        when(bus.getType()).thenReturn(IdentifiableType.BUS);
        // VoltageLevel fields
        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        when(bus.getVoltageLevel()).thenReturn(voltageLevel);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

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

        BusbarSection busbarSection = mock(BusbarSection.class);
        when(busbarSection.getType()).thenReturn(IdentifiableType.BUSBAR_SECTION);
        // VoltageLevel fields
        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(busbarSection.getTerminal()).thenReturn(terminal);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

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

        ShuntCompensator shuntCompensator = mock(ShuntCompensator.class);
        when(shuntCompensator.getType()).thenReturn(IdentifiableType.SHUNT_COMPENSATOR);

        when(shuntCompensator.getModel(ShuntCompensatorLinearModel.class)).thenReturn(new ShuntCompensatorLinearModel() {
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

            @Override
            public String getProperty(String key, String defaultValue) {
                return null;
            }

            @Override
            public String getProperty(String key) {
                return null;
            }

            @Override
            public boolean hasProperty() {
                return false;
            }

            @Override
            public boolean hasProperty(String key) {
                return false;
            }

            @Override
            public boolean removeProperty(String key) {
                return false;
            }

            @Override
            public String setProperty(String key, String value) {
                return null;
            }

            @Override
            public Set<String> getPropertyNames() {
                return null;
            }
        });

        // VoltageLevel fields
        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(shuntCompensator.getTerminal()).thenReturn(terminal);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

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

        Battery battery = mock(Battery.class);
        when(battery.getType()).thenReturn(IdentifiableType.BATTERY);
        // VoltageLevel fields
        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(battery.getTerminal()).thenReturn(terminal);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

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

    private static Stream<Arguments> provideArgumentsForBoundaryLineTest() {

        BoundaryLine boundaryLine = mock(BoundaryLine.class);
        when(boundaryLine.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);
        // VoltageLevel fields
        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(boundaryLine.getTerminal()).thenReturn(terminal);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

        return Stream.of(
            // --- EQUALS --- //
            // VoltageLevel fields
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.FR.name(), null, boundaryLine, true),
            Arguments.of(EQUALS, FieldType.COUNTRY, Country.DE.name(), null, boundaryLine, false),

            // --- NOT_EQUALS --- //
            // VoltageLevel fields
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.DE.name(), null, boundaryLine, true),
            Arguments.of(NOT_EQUALS, FieldType.COUNTRY, Country.FR.name(), null, boundaryLine, false),

            // --- IN --- //
            // VoltageLevel fields
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), boundaryLine, true),
            Arguments.of(IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), boundaryLine, false),

            // --- NOT_IN --- //
            // VoltageLevel fields
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), boundaryLine, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), boundaryLine, false)
        );
    }

    private static Stream<Arguments> provideArgumentsForVoltageLevelTest() {

        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getType()).thenReturn(IdentifiableType.VOLTAGE_LEVEL);
        Substation substation = mock(Substation.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

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

        Substation substation = mock(Substation.class);
        when(substation.getType()).thenReturn(IdentifiableType.SUBSTATION);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));

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

        Line line = mock(Line.class);
        when(line.getType()).thenReturn(IdentifiableType.LINE);
        // VoltageLevel fields
        Substation substation1 = mock(Substation.class);
        Substation substation2 = mock(Substation.class);
        VoltageLevel voltageLevel1 = mock(VoltageLevel.class);
        VoltageLevel voltageLevel2 = mock(VoltageLevel.class);
        Terminal terminal1 = mock(Terminal.class);
        Terminal terminal2 = mock(Terminal.class);

        when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));
        when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));

        when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        when(line.getTerminal(TwoSides.ONE)).thenReturn(terminal1);
        when(substation1.getCountry()).thenReturn(Optional.of(Country.FR));

        when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        when(line.getTerminal(TwoSides.TWO)).thenReturn(terminal2);
        when(substation2.getCountry()).thenReturn(Optional.of(Country.SM));

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

        TwoWindingsTransformer twoWindingsTransformer = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);

        RatioTapChanger ratioTapChanger = mock(RatioTapChanger.class);
        when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        when(ratioTapChanger.isRegulating()).thenReturn(true);
        when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(false);
        when(ratioTapChanger.getRegulationValue()).thenReturn(225.);

        TwoWindingsTransformer twoWindingsTransformer2 = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer2.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        when(twoWindingsTransformer2.getRatioTapChanger()).thenReturn(null);

        when(twoWindingsTransformer.getRatioTapChanger()).thenReturn(ratioTapChanger);
        PhaseTapChanger phaseTapChanger = mock(PhaseTapChanger.class);
        when(twoWindingsTransformer.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        when(phaseTapChanger.isRegulating()).thenReturn(true);
        when(phaseTapChanger.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
        when(phaseTapChanger.getRegulationValue()).thenReturn(100.);

        when(twoWindingsTransformer2.getPhaseTapChanger()).thenReturn(null);

        Substation substation = mock(Substation.class);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));
        when(twoWindingsTransformer.getSubstation()).thenReturn(Optional.of(substation));

        TwoWindingsTransformer twoWindingsTransformer3 = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer3.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        RatioTapChanger ratioTapChanger3 = mock(RatioTapChanger.class);
        when(twoWindingsTransformer3.getRatioTapChanger()).thenReturn(ratioTapChanger3);
        when(ratioTapChanger3.isRegulating()).thenReturn(false);
        when(ratioTapChanger3.hasLoadTapChangingCapabilities()).thenReturn(true);

        TwoWindingsTransformer twoWindingsTransformer4 = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer4.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        RatioTapChanger ratioTapChanger4 = mock(RatioTapChanger.class);
        when(twoWindingsTransformer4.getRatioTapChanger()).thenReturn(ratioTapChanger4);
        when(ratioTapChanger4.isRegulating()).thenReturn(true);
        when(ratioTapChanger4.hasLoadTapChangingCapabilities()).thenReturn(true);

        TwoWindingsTransformer twoWindingsTransformer5 = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer5.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger5 = mock(PhaseTapChanger.class);
        when(twoWindingsTransformer5.getPhaseTapChanger()).thenReturn(phaseTapChanger5);
        when(phaseTapChanger5.isRegulating()).thenReturn(false);
        when(phaseTapChanger5.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        TwoWindingsTransformer twoWindingsTransformer6 = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer6.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger6 = mock(PhaseTapChanger.class);
        when(twoWindingsTransformer6.getPhaseTapChanger()).thenReturn(phaseTapChanger6);
        when(phaseTapChanger6.isRegulating()).thenReturn(false);
        when(phaseTapChanger6.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);

        TwoWindingsTransformer twoWindingsTransformer7 = mock(TwoWindingsTransformer.class);
        when(twoWindingsTransformer7.getType()).thenReturn(IdentifiableType.TWO_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger7 = mock(PhaseTapChanger.class);
        when(twoWindingsTransformer7.getPhaseTapChanger()).thenReturn(phaseTapChanger7);
        when(phaseTapChanger7.isRegulating()).thenReturn(true);
        when(phaseTapChanger7.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

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
            Arguments.of(IN, FieldType.RATIO_REGULATION_MODE, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name(), RatioRegulationModeType.FIXED_RATIO.name()), twoWindingsTransformer,
                    false),
            Arguments.of(IN, FieldType.PHASE_REGULATION_MODE, null, Set.of(PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name()),
                    twoWindingsTransformer, true),

            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), twoWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATIO_REGULATION_MODE, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name()), twoWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.PHASE_REGULATION_MODE, null, Set.of(PhaseTapChanger.RegulationMode.CURRENT_LIMITER.name(), PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL.name()),
                    twoWindingsTransformer, false)
            );
    }

    private static Stream<Arguments> provideArgumentsForThreeWindingTransformerTest() {

        // transfo 1
        ThreeWindingsTransformer threeWindingsTransformer = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        // Ratio Tap Changer
        RatioTapChanger ratioTapChanger = mock(RatioTapChanger.class);
        ThreeWindingsTransformer.Leg leg = mock(ThreeWindingsTransformer.Leg.class);
        when(ratioTapChanger.isRegulating()).thenReturn(true);
        when(ratioTapChanger.hasLoadTapChangingCapabilities()).thenReturn(false);
        when(ratioTapChanger.getRegulationValue()).thenReturn(225.);
        when(leg.getRatioTapChanger()).thenReturn(ratioTapChanger);

        // Phase Tap Changer
        PhaseTapChanger phaseTapChanger = mock(PhaseTapChanger.class);
        when(leg.getPhaseTapChanger()).thenReturn(phaseTapChanger);
        when(phaseTapChanger.isRegulating()).thenReturn(true);
        when(phaseTapChanger.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);
        when(phaseTapChanger.getRegulationValue()).thenReturn(100.);

        when(threeWindingsTransformer.getLeg1()).thenReturn(leg);
        when(threeWindingsTransformer.getLeg2()).thenReturn(leg);
        when(threeWindingsTransformer.getLeg3()).thenReturn(leg);

        Substation substation = mock(Substation.class);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));
        when(threeWindingsTransformer.getSubstation()).thenReturn(Optional.of(substation));

        // transfo 2
        ThreeWindingsTransformer threeWindingsTransformer2 = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer2.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        ThreeWindingsTransformer.Leg leg2 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg2.getRatioTapChanger()).thenReturn(null);
        when(leg2.getPhaseTapChanger()).thenReturn(null);

        when(threeWindingsTransformer2.getLeg1()).thenReturn(leg2);
        when(threeWindingsTransformer2.getLeg2()).thenReturn(leg2);
        when(threeWindingsTransformer2.getLeg3()).thenReturn(leg2);

        // transfo 3
        ThreeWindingsTransformer threeWindingsTransformer3 = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer3.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);

        // Ratio Tap Changer
        RatioTapChanger ratioTapChanger3 = mock(RatioTapChanger.class);
        when(ratioTapChanger3.isRegulating()).thenReturn(false);
        when(ratioTapChanger3.hasLoadTapChangingCapabilities()).thenReturn(true);
        ThreeWindingsTransformer.Leg leg3 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg3.getRatioTapChanger()).thenReturn(ratioTapChanger3);
        when(threeWindingsTransformer3.getLeg1()).thenReturn(leg3);
        when(threeWindingsTransformer3.getLeg2()).thenReturn(leg3);
        when(threeWindingsTransformer3.getLeg3()).thenReturn(leg3);

        // transfo 4
        ThreeWindingsTransformer threeWindingsTransformer4 = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer4.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        RatioTapChanger ratioTapChanger4 = mock(RatioTapChanger.class);
        when(ratioTapChanger4.isRegulating()).thenReturn(true);
        when(ratioTapChanger4.hasLoadTapChangingCapabilities()).thenReturn(true);

        ThreeWindingsTransformer.Leg leg4 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg4.getRatioTapChanger()).thenReturn(ratioTapChanger4);
        when(threeWindingsTransformer4.getLeg1()).thenReturn(leg4);
        when(threeWindingsTransformer4.getLeg2()).thenReturn(leg4);
        when(threeWindingsTransformer4.getLeg3()).thenReturn(leg4);

        // transfo 5
        ThreeWindingsTransformer threeWindingsTransformer5 = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer5.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger5 = mock(PhaseTapChanger.class);
        when(phaseTapChanger5.isRegulating()).thenReturn(false);
        when(phaseTapChanger5.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        ThreeWindingsTransformer.Leg leg5 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg5.getPhaseTapChanger()).thenReturn(phaseTapChanger5);
        when(threeWindingsTransformer5.getLeg1()).thenReturn(leg5);
        when(threeWindingsTransformer5.getLeg2()).thenReturn(leg5);
        when(threeWindingsTransformer5.getLeg3()).thenReturn(leg5);

        // transfo 6
        ThreeWindingsTransformer threeWindingsTransformer6 = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer6.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger6 = mock(PhaseTapChanger.class);
        when(phaseTapChanger6.isRegulating()).thenReturn(false);
        when(phaseTapChanger6.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.ACTIVE_POWER_CONTROL);

        ThreeWindingsTransformer.Leg leg6 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg6.getPhaseTapChanger()).thenReturn(phaseTapChanger6);
        when(threeWindingsTransformer6.getLeg1()).thenReturn(leg6);
        when(threeWindingsTransformer6.getLeg2()).thenReturn(leg6);
        when(threeWindingsTransformer6.getLeg3()).thenReturn(leg6);

        // transfo 7
        ThreeWindingsTransformer threeWindingsTransformer7 = mock(ThreeWindingsTransformer.class);
        when(threeWindingsTransformer7.getType()).thenReturn(IdentifiableType.THREE_WINDINGS_TRANSFORMER);
        PhaseTapChanger phaseTapChanger7 = mock(PhaseTapChanger.class);
        when(phaseTapChanger7.isRegulating()).thenReturn(true);
        when(phaseTapChanger7.getRegulationMode()).thenReturn(PhaseTapChanger.RegulationMode.CURRENT_LIMITER);

        ThreeWindingsTransformer.Leg leg7 = mock(ThreeWindingsTransformer.Leg.class);
        when(leg7.getPhaseTapChanger()).thenReturn(phaseTapChanger7);
        when(threeWindingsTransformer7.getLeg1()).thenReturn(leg7);
        when(threeWindingsTransformer7.getLeg2()).thenReturn(leg7);
        when(threeWindingsTransformer7.getLeg3()).thenReturn(leg7);

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
            Arguments.of(IN, FieldType.RATIO_REGULATION_MODE_1, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name(), RatioRegulationModeType.FIXED_RATIO.name()), threeWindingsTransformer,
                    false),
            Arguments.of(IN, FieldType.RATIO_REGULATION_MODE_2, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name(), RatioRegulationModeType.FIXED_RATIO.name()), threeWindingsTransformer,
                    false),
            Arguments.of(IN, FieldType.RATIO_REGULATION_MODE_3, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name(), RatioRegulationModeType.FIXED_RATIO.name()), threeWindingsTransformer,
                    false),

            // --- NOT_IN --- //
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.BE.name(), Country.DE.name()), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.COUNTRY, null, Set.of(Country.FR.name(), Country.DE.name()), threeWindingsTransformer, false),
            Arguments.of(NOT_IN, FieldType.RATIO_REGULATION_MODE_1, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name()), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATIO_REGULATION_MODE_2, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name()), threeWindingsTransformer, true),
            Arguments.of(NOT_IN, FieldType.RATIO_REGULATION_MODE_3, null, Set.of(RatioRegulationModeType.VOLTAGE_REGULATION.name()), threeWindingsTransformer, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForStaticVarCompensatorTest() {

        StaticVarCompensator svar = mock(StaticVarCompensator.class);
        when(svar.getType()).thenReturn(IdentifiableType.STATIC_VAR_COMPENSATOR);
        when(svar.getId()).thenReturn("SVAR");
        when(svar.getRegulationMode()).thenReturn(StaticVarCompensator.RegulationMode.VOLTAGE);

        // VoltageLevel fields
        Substation substation = mock(Substation.class);
        VoltageLevel voltageLevel = mock(VoltageLevel.class);
        when(voltageLevel.getSubstation()).thenReturn(Optional.of(substation));
        Terminal terminal = mock(Terminal.class);
        when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        when(svar.getTerminal()).thenReturn(terminal);
        when(substation.getCountry()).thenReturn(Optional.of(Country.FR));
        // Regulating terminal fields
        Terminal regulatingTerminal = mock(Terminal.class);
        VoltageLevel distantVoltageLevel = mock(VoltageLevel.class);
        when(distantVoltageLevel.getId()).thenReturn("VL_2");
        when(regulatingTerminal.getVoltageLevel()).thenReturn(distantVoltageLevel);
        BusbarSection regulatedBusBarSection = mock(BusbarSection.class);
        when(regulatedBusBarSection.getId()).thenReturn("BBS");
        when(regulatingTerminal.getConnectable()).thenReturn(regulatedBusBarSection);
        when(svar.getRegulatingTerminal()).thenReturn(regulatingTerminal);

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

        HvdcLine hvdcLine = mock(HvdcLine.class);
        when(hvdcLine.getType()).thenReturn(IdentifiableType.HVDC_LINE);

        // VoltageLevel fields
        Substation substation1 = mock(Substation.class);
        Substation substation2 = mock(Substation.class);
        VoltageLevel voltageLevel1 = mock(VoltageLevel.class);
        VoltageLevel voltageLevel2 = mock(VoltageLevel.class);
        Terminal terminal1 = mock(Terminal.class);
        Terminal terminal2 = mock(Terminal.class);
        HvdcConverterStation converterStation1 = mock(HvdcConverterStation.class);
        HvdcConverterStation converterStation2 = mock(HvdcConverterStation.class);

        when(voltageLevel1.getSubstation()).thenReturn(Optional.of(substation1));
        when(voltageLevel2.getSubstation()).thenReturn(Optional.of(substation2));

        when(terminal1.getVoltageLevel()).thenReturn(voltageLevel1);
        when(converterStation1.getTerminal()).thenReturn(terminal1);
        when(substation1.getCountry()).thenReturn(Optional.of(Country.FR));
        when(hvdcLine.getConverterStation1()).thenReturn(converterStation1);

        when(terminal2.getVoltageLevel()).thenReturn(voltageLevel2);
        when(converterStation2.getTerminal()).thenReturn(terminal2);
        when(substation2.getCountry()).thenReturn(Optional.of(Country.SM));
        when(hvdcLine.getConverterStation2()).thenReturn(converterStation2);

        when(hvdcLine.getConvertersMode()).thenReturn(HvdcLine.ConvertersMode.SIDE_1_INVERTER_SIDE_2_RECTIFIER);

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
