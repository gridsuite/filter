/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.IdentifiableType;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EquipmentTypeMapperTest {

    @Test
    void testMap() {
        Map<EquipmentType, IdentifiableType> expectedMappings = Map.ofEntries(
            Map.entry(EquipmentType.LINE, IdentifiableType.LINE),
            Map.entry(EquipmentType.GENERATOR, IdentifiableType.GENERATOR),
            Map.entry(EquipmentType.LOAD, IdentifiableType.LOAD),
            Map.entry(EquipmentType.SHUNT_COMPENSATOR, IdentifiableType.SHUNT_COMPENSATOR),
            Map.entry(EquipmentType.STATIC_VAR_COMPENSATOR, IdentifiableType.STATIC_VAR_COMPENSATOR),
            Map.entry(EquipmentType.BATTERY, IdentifiableType.BATTERY),
            Map.entry(EquipmentType.BUS, IdentifiableType.BUS),
            Map.entry(EquipmentType.BUSBAR_SECTION, IdentifiableType.BUSBAR_SECTION),
            Map.entry(EquipmentType.BOUNDARY_LINE, IdentifiableType.BOUNDARY_LINE),
            Map.entry(EquipmentType.LCC_CONVERTER_STATION, IdentifiableType.LINE_COMMUTATED_CONVERTER),
            Map.entry(EquipmentType.VSC_CONVERTER_STATION, IdentifiableType.VOLTAGE_SOURCE_CONVERTER),
            Map.entry(EquipmentType.TWO_WINDINGS_TRANSFORMER, IdentifiableType.TWO_WINDINGS_TRANSFORMER),
            Map.entry(EquipmentType.THREE_WINDINGS_TRANSFORMER, IdentifiableType.THREE_WINDINGS_TRANSFORMER),
            Map.entry(EquipmentType.HVDC_LINE, IdentifiableType.HVDC_LINE),
            Map.entry(EquipmentType.SUBSTATION, IdentifiableType.SUBSTATION),
            Map.entry(EquipmentType.VOLTAGE_LEVEL, IdentifiableType.VOLTAGE_LEVEL),
            Map.entry(EquipmentType.SWITCH, IdentifiableType.SWITCH)
        );

        assertEquals(EquipmentType.values().length, expectedMappings.size());
        expectedMappings.forEach((equipmentType, identifiableType) ->
            assertEquals(identifiableType, EquipmentTypeMapper.map(equipmentType), equipmentType.name()));
    }
}
