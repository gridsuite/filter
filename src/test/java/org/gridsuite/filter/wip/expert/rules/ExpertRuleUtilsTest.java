/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rules;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.wip.TestNetworkUtils;
import org.gridsuite.filter.wip.expert.data.FieldType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
class ExpertRuleUtilsTest {

    private static Stream<Arguments> provideEquipmentsWithBooleanFields() {
        return Stream.of(
                Arguments.of(EquipmentType.LINE, "LINE_1", FieldType.CONNECTED_1, true),
                Arguments.of(EquipmentType.LINE, "LINE_1", FieldType.CONNECTED_2, true),
                Arguments.of(EquipmentType.LINE, "LINE_2", FieldType.CONNECTED_1, true),
                Arguments.of(EquipmentType.LINE, "LINE_2", FieldType.CONNECTED_2, true),
                Arguments.of(EquipmentType.GENERATOR, "GENERATOR_1", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.GENERATOR, "GENERATOR_1", FieldType.VOLTAGE_REGULATOR_ON, true),
                Arguments.of(EquipmentType.GENERATOR, "GENERATOR_2", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.GENERATOR, "GENERATOR_2", FieldType.VOLTAGE_REGULATOR_ON, true),
                Arguments.of(EquipmentType.LOAD, "LOAD_1", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.LOAD, "LOAD_2", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.SHUNT_COMPENSATOR, "SHUNT_COMPENSATOR_1", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.SHUNT_COMPENSATOR, "SHUNT_COMPENSATOR_2", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.BATTERY, "BATTERY_1", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.BATTERY, "BATTERY_2", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", FieldType.CONNECTED_1, true),
                Arguments.of(EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", FieldType.CONNECTED_2, true),
                Arguments.of(EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", FieldType.LOAD_TAP_CHANGING_CAPABILITIES, true),
                Arguments.of(EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", FieldType.HAS_PHASE_TAP_CHANGER, false),
                Arguments.of(EquipmentType.TWO_WINDINGS_TRANSFORMER, "TWO_WINDINGS_TRANSFORMER_1", FieldType.HAS_RATIO_TAP_CHANGER, true),
                Arguments.of(EquipmentType.STATIC_VAR_COMPENSATOR, "STATIC_VAR_COMPENSATOR_1", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.STATIC_VAR_COMPENSATOR, "STATIC_VAR_COMPENSATOR_1", FieldType.REMOTE_REGULATED_TERMINAL, false),
                Arguments.of(EquipmentType.STATIC_VAR_COMPENSATOR, "STATIC_VAR_COMPENSATOR_1", FieldType.AUTOMATE, false),
                Arguments.of(EquipmentType.BOUNDARY_LINE, "BOUNDARY_LINE_1", FieldType.CONNECTED, true),
                Arguments.of(EquipmentType.BOUNDARY_LINE, "BOUNDARY_LINE_1", FieldType.PAIRED, false),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.CONNECTED_1, true),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.LOAD_TAP_CHANGING_CAPABILITIES_1, null),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.HAS_PHASE_TAP_CHANGER_1, false),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.HAS_RATIO_TAP_CHANGER_1, false),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.CONNECTED_2, true),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.LOAD_TAP_CHANGING_CAPABILITIES_2, true),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.HAS_PHASE_TAP_CHANGER_2, false),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.HAS_RATIO_TAP_CHANGER_2, true),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.CONNECTED_3, true),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.LOAD_TAP_CHANGING_CAPABILITIES_3, true),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.HAS_PHASE_TAP_CHANGER_3, false),
                Arguments.of(EquipmentType.THREE_WINDINGS_TRANSFORMER, "THREE_WINDINGS_TRANSFORMER_1", FieldType.HAS_RATIO_TAP_CHANGER_3, true),
                Arguments.of(EquipmentType.HVDC_LINE, "HVDC_LINE_1", FieldType.CONNECTED_1, true),
                Arguments.of(EquipmentType.HVDC_LINE, "HVDC_LINE_1", FieldType.CONNECTED_2, true)
        );
    }

    @Test
    void testGetIdentifiableBooleanFieldValueThrowsExceptionWhenUnsupportedIdentifiable() {
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(EquipmentType.VOLTAGE_LEVEL, "VOLTAGE_LEVEL_1");

        assertThatThrownBy(() -> ExpertRuleUtils.getBooleanFieldValue(identifiable, FieldType.VOLTAGE_LEVEL_ID)).isInstanceOf(PowsyblException.class);
    }

    @ParameterizedTest
    @MethodSource("provideEquipmentsWithBooleanFields")
    void testGetExistingIdentifiableBooleanFieldValueReturnsExpected(EquipmentType equipmentType, String equipmentId, FieldType fieldType, Boolean expectedValue) {
        Identifiable<?> identifiable = TestNetworkUtils.getEquipmentFromTestNetwork(equipmentType, equipmentId);

        assertThat(ExpertRuleUtils.getBooleanFieldValue(identifiable, fieldType)).isEqualTo(expectedValue);
    }
}
