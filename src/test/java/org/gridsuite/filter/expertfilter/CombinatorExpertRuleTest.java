package org.gridsuite.filter.expertfilter;

import com.powsybl.iidm.network.EnergySource;
import com.powsybl.iidm.network.Generator;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.IdentifiableType;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.expertrule.*;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CombinatorExpertRuleTest {
    private FilterLoader filterLoader;

    @BeforeEach
    void setUp() {
        filterLoader = uuids -> null;
    }

    @Test
    void testStringValue() {
        CombinatorExpertRule rule = CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(null).build();
        assertNull(rule.getStringValue());
        assertEquals(DataType.COMBINATOR, rule.getDataType());
    }

    @ParameterizedTest
    @MethodSource({"provideArgumentsForTest"})
    void testEvaluateRule(CombinatorType combinatorType, List<AbstractExpertRule> rules, Identifiable<?> equipment, boolean expected) {
        CombinatorExpertRule rule = CombinatorExpertRule.builder().combinator(combinatorType).rules(rules).build();
        assertEquals(expected, rule.evaluateRule(equipment, filterLoader, new HashMap<>()));
    }

    private static Stream<Arguments> provideArgumentsForTest() {
        Generator gen = Mockito.mock(Generator.class);
        Mockito.when(gen.getType()).thenReturn(IdentifiableType.GENERATOR);
        // Generator fields
        Mockito.when(gen.getEnergySource()).thenReturn(EnergySource.HYDRO);
        Mockito.when(gen.getId()).thenReturn("GEN");
        Mockito.when(gen.getMinP()).thenReturn(-500.0);
        Mockito.when(gen.isVoltageRegulatorOn()).thenReturn(true);

        return Stream.of(
                // --- Single rule AND --- //
                Arguments.of(CombinatorType.AND, List.of(
                    EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build()
                ), gen, true),
                Arguments.of(CombinatorType.AND, List.of(
                    EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build()
                ), gen, false),
                // --- Rule tree AND --- //
                Arguments.of(CombinatorType.AND, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build(),
                        CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(
                                StringExpertRule.builder().operator(OperatorType.IS).field(FieldType.ID).value("GEN").build(),
                                NumberExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.MIN_P).value(-500.0).build()
                            )
                        ).build()
                ), gen, true),
                Arguments.of(CombinatorType.AND, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build(),
                        CombinatorExpertRule.builder().combinator(CombinatorType.AND).rules(List.of(
                                StringExpertRule.builder().operator(OperatorType.IS).field(FieldType.ID).value("GEN").build(),
                                NumberExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.MIN_P).value(-400.0).build()
                            )
                        ).build()
                ), gen, false),
                // --- Single rule OR --- //
                Arguments.of(CombinatorType.OR, List.of(
                    EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.HYDRO.name()).build()
                ), gen, true),
                Arguments.of(CombinatorType.OR, List.of(
                    EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build()
                ), gen, false),
                // --- Rule tree OR --- //
                Arguments.of(CombinatorType.OR, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build(),
                        CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(
                                StringExpertRule.builder().operator(OperatorType.IS).field(FieldType.ID).value("GEN").build(),
                                BooleanExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.VOLTAGE_REGULATOR_ON).value(false).build()
                            )
                        ).build()
                ), gen, true),
                Arguments.of(CombinatorType.OR, List.of(
                        EnumExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.ENERGY_SOURCE).value(EnergySource.THERMAL.name()).build(),
                        CombinatorExpertRule.builder().combinator(CombinatorType.OR).rules(List.of(
                                StringExpertRule.builder().operator(OperatorType.IS).field(FieldType.ID).value("GEN_2").build(),
                                BooleanExpertRule.builder().operator(OperatorType.EQUALS).field(FieldType.VOLTAGE_REGULATOR_ON).value(false).build()
                            )
                        ).build()
                ), gen, false)
        );
    }
}
