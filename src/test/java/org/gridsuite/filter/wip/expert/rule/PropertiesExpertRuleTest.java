/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rule;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.expert.data.DataType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.gridsuite.filter.utils.expertfilter.OperatorType.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PropertiesExpertRuleTest {

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
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, voltageLevel, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, generator, "region", Set.of("north"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, load, "region", Set.of("north"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, shuntCompensator, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, line, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, battery, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, svar, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SERIE_REACTANCE, threeWindingsTransformer, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, hvdcLine, "region", Set.of("east"), PowsyblException.class),

                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, voltageLevel, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, generator, "region", Set.of("north"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, load, "region", Set.of("north"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, shuntCompensator, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, line, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, battery, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, svar, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SERIE_REACTANCE, threeWindingsTransformer, "region", Set.of("east"), PowsyblException.class),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, hvdcLine, "region", Set.of("east"), PowsyblException.class),

                // --- Test an unsupported operator for this rule type --- //
                Arguments.of(IS, FieldType.FREE_PROPERTIES, generator, "codeOI", Set.of("33"), PowsyblException.class),
                Arguments.of(CONTAINS, FieldType.SUBSTATION_PROPERTIES, generator, "cvgRegion", Set.of("LILLE"), PowsyblException.class)
        );
    }

    private static Stream<Arguments> provideArgumentsForSubstationTest() {

        Substation substation = Mockito.mock(Substation.class);
        Mockito.when(substation.getType()).thenReturn(IdentifiableType.SUBSTATION);
        Mockito.when(substation.getProperty("cvgRegion")).thenReturn("LILLE");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "cvgRegion", Set.of("Lille", "PARIS"), substation, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "cvgRegion", Set.of("Paris"), substation, false),

                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "cvgRegion", Set.of("Lille", "PARIS"), substation, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "cvgRegion", Set.of("Paris"), substation, true)
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
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "CodeOI", Set.of("11"), generator, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "CodeOI", Set.of("22"), generator, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Lille"), generator, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Paris"), generator, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), generator, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("11"), generator, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "CodeOI", Set.of("11"), generator, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "CodeOI", Set.of("22"), generator, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Lille"), generator, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Paris"), generator, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), generator, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("11"), generator, true)
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
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "region", Set.of("north"), line, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "region", Set.of("south"), line, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", Set.of("Lille"), line, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", Set.of("Paris"), line, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", Set.of("Paris"), line, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", Set.of("Lille"), line, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("22"), line, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("33"), line, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("33"), line, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("22"), line, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "region", Set.of("north"), line, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "region", Set.of("south"), line, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", Set.of("Lille"), line, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "regionCSV", Set.of("Paris"), line, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", Set.of("Paris"), line, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "regionCSV", Set.of("Lille"), line, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("22"), line, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("33"), line, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("33"), line, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("22"), line, true)
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
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", Set.of("propertyValueLoad"), load, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", Set.of("propertyValueLoad2"), load, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation"), load, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation2"), load, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("33"), load, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), load, false),
                // --- IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", Set.of("propertyValueLoad"), load, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameLoad", Set.of("propertyValueLoad2"), load, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation"), load, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation2"), load, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("33"), load, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), load, true)
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
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSC", Set.of("propertyValueSC"), shuntCompensator, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSC", Set.of("propertyValueSC1"), shuntCompensator, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation"), shuntCompensator, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation2"), shuntCompensator, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("33"), shuntCompensator, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), shuntCompensator, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSC", Set.of("propertyValueSC"), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSC", Set.of("propertyValueSC1"), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation"), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation2"), shuntCompensator, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("33"), shuntCompensator, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), shuntCompensator, true)
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
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT2"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Lille"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Paris"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("22"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("33"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("33"), twoWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("22"), twoWindingsTransformer, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Paris"), transformerWithNullSub, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT"), twoWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT2"), twoWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Lille"), twoWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Paris"), twoWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("22"), twoWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("33"), twoWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("33"), twoWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("22"), twoWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Paris"), transformerWithNullSub, false)
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
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", Set.of("propertyValueSVAR"), svar, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", Set.of("propertyValueSVAR2"), svar, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation"), svar, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation1"), svar, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("33"), svar, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), svar, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", Set.of("propertyValueSVAR"), svar, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameSVAR", Set.of("propertyValueSVAR2"), svar, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation"), svar, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation1"), svar, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("33"), svar, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), svar, true)
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
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT"), threeWindingsTransformer, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT2"), threeWindingsTransformer, false),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT3"), threeWindingsTransformer, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Lille"), threeWindingsTransformer, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Paris"), threeWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("22"), threeWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("33"), threeWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("33"), threeWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("22"), threeWindingsTransformer, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", Set.of("44"), threeWindingsTransformer, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", Set.of("22"), threeWindingsTransformer, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT"), threeWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT2"), threeWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameTWT", Set.of("propertyValueTWT3"), threeWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Lille"), threeWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "regionCSV", Set.of("Paris"), threeWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("22"), threeWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("33"), threeWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("33"), threeWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("22"), threeWindingsTransformer, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", Set.of("44"), threeWindingsTransformer, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_3, "CodeOI", Set.of("22"), threeWindingsTransformer, true)
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
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", Set.of("propertyValueHVDC"), hvdc, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", Set.of("propertyValueHVDC2"), hvdc, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", Set.of("propertyValueSubstation1"), hvdc, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", Set.of("propertyValueSubstation8"), hvdc, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", Set.of("PropertyValueSubstation2"), hvdc, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", Set.of("propertyValueSubstation1"), hvdc, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("33"), hvdc, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("22"), hvdc, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("42"), hvdc, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("21"), hvdc, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", Set.of("propertyValueHVDC"), hvdc, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameHVDC", Set.of("propertyValueHVDC2"), hvdc, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", Set.of("propertyValueSubstation1"), hvdc, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_1, "propertyNameSubstation", Set.of("propertyValueSubstation8"), hvdc, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", Set.of("PropertyValueSubstation2"), hvdc, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES_2, "propertyNameSubstation", Set.of("propertyValueSubstation1"), hvdc, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("33"), hvdc, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_1, "CodeOI", Set.of("22"), hvdc, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("42"), hvdc, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES_2, "CodeOI", Set.of("21"), hvdc, true)
        );
    }

    private static Stream<Arguments> provideArgumentsForBoundaryLineTest() {
        BoundaryLine boundaryLine = Mockito.mock(BoundaryLine.class);
        Mockito.when(boundaryLine.getType()).thenReturn(IdentifiableType.BOUNDARY_LINE);
        Mockito.when(boundaryLine.getProperty("propertyNameBL")).thenReturn("PropertyValueBL");

        Substation substation = Mockito.mock(Substation.class);
        VoltageLevel voltageLevel = Mockito.mock(VoltageLevel.class);
        Mockito.when(voltageLevel.getProperty("CodeOI")).thenReturn("33");
        Mockito.when(voltageLevel.getNullableSubstation()).thenReturn(substation);
        Terminal terminal = Mockito.mock(Terminal.class);
        Mockito.when(terminal.getVoltageLevel()).thenReturn(voltageLevel);
        Mockito.when(boundaryLine.getTerminal()).thenReturn(terminal);
        Mockito.when(substation.getProperty("propertyNameSubstation")).thenReturn("PropertyValueSubstation");

        return Stream.of(
                // --- IN --- //
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameBL", Set.of("propertyValueBL"), boundaryLine, true),
                Arguments.of(IN, FieldType.FREE_PROPERTIES, "propertyNameBL", Set.of("propertyValueBL2"), boundaryLine, false),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation"), boundaryLine, true),
                Arguments.of(IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation1"), boundaryLine, false),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("33"), boundaryLine, true),
                Arguments.of(IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), boundaryLine, false),
                // --- NOT_IN --- //
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameBL", Set.of("propertyValueBL"), boundaryLine, false),
                Arguments.of(NOT_IN, FieldType.FREE_PROPERTIES, "propertyNameBL", Set.of("propertyValueBL2"), boundaryLine, true),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation"), boundaryLine, false),
                Arguments.of(NOT_IN, FieldType.SUBSTATION_PROPERTIES, "propertyNameSubstation", Set.of("propertyValueSubstation1"), boundaryLine, true),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("33"), boundaryLine, false),
                Arguments.of(NOT_IN, FieldType.VOLTAGE_LEVEL_PROPERTIES, "CodeOI", Set.of("22"), boundaryLine, true)
        );
    }

    @Test
    void testGetDataTypeReturnsProperties() {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().build();

        assertThat(rule.getDataType()).isEqualTo(DataType.PROPERTIES);
    }

    @Test
    void testGetOperatorTypeReturnsExpectedOperatorType() {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operatorType(OperatorType.IN).build();

        assertThat(rule.getOperatorType()).isEqualTo(OperatorType.IN);
    }

    @ParameterizedTest
    @MethodSource({
        "provideArgumentsForTestWithException"
    })
    void testEvaluateRuleWithException(OperatorType operatorType, FieldType fieldType, Identifiable<?> equipment,
                                       String targetProperty, Set<String> referenceValues, Class<Throwable> expectedException) {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operatorType(operatorType).fieldType(fieldType).targetProperty(targetProperty).referenceValues(referenceValues).build();
        assertThrows(expectedException, () -> rule.evaluateRule(equipment));
    }

    @Test
    void testPropertiesValue() {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operatorType(IN).fieldType(FieldType.FREE_PROPERTIES).targetProperty("property")
                .referenceValues(Collections.singleton("value1")).build();
        assertEquals(Collections.singleton("value1"), rule.getReferenceValues());
        assertEquals("property", rule.getTargetProperty());
        assertEquals(FieldType.FREE_PROPERTIES, rule.getFieldType());
        assertEquals(IN, rule.getOperatorType());
        rule = PropertiesExpertRule.builder().operatorType(NOT_IN).fieldType(FieldType.FREE_PROPERTIES).targetProperty("property2")
                .referenceValues(Collections.singleton("value2")).build();
        assertEquals(Collections.singleton("value2"), rule.getReferenceValues());
        assertEquals("property2", rule.getTargetProperty());
        assertEquals(FieldType.FREE_PROPERTIES, rule.getFieldType());
        assertEquals(NOT_IN, rule.getOperatorType());
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
    })
    void testEvaluateRule(OperatorType operatorType, FieldType fieldType, String targetProperty, Set<String> referenceValues, Identifiable<?> equipment, boolean expected) {
        PropertiesExpertRule rule = PropertiesExpertRule.builder().operatorType(operatorType).fieldType(fieldType).targetProperty(targetProperty).referenceValues(referenceValues).build();
        assertEquals(expected, rule.evaluateRule(equipment));
    }
}
