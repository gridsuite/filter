/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.utils;

import com.powsybl.iidm.network.IdentifiableType;

/**
 * @author Joris Mancini <joris.mancini_externe at rte-france.com>
 */
public final class EquipmentTypeMapper {

    private EquipmentTypeMapper() {
        // Sould not be instantiated
    }

    public static IdentifiableType map(EquipmentType equipmentType) {
        return switch (equipmentType) {
            case LINE -> IdentifiableType.LINE;
            case GENERATOR -> IdentifiableType.GENERATOR;
            case LOAD -> IdentifiableType.LOAD;
            case SHUNT_COMPENSATOR -> IdentifiableType.SHUNT_COMPENSATOR;
            case STATIC_VAR_COMPENSATOR -> IdentifiableType.STATIC_VAR_COMPENSATOR;
            case BATTERY -> IdentifiableType.BATTERY;
            case BUS -> IdentifiableType.BUS;
            case BUSBAR_SECTION -> IdentifiableType.BUSBAR_SECTION;
            case BOUNDARY_LINE -> IdentifiableType.BOUNDARY_LINE;
            case LCC_CONVERTER_STATION -> IdentifiableType.LINE_COMMUTATED_CONVERTER;
            case VSC_CONVERTER_STATION -> IdentifiableType.VOLTAGE_SOURCE_CONVERTER;
            case TWO_WINDINGS_TRANSFORMER -> IdentifiableType.TWO_WINDINGS_TRANSFORMER;
            case THREE_WINDINGS_TRANSFORMER -> IdentifiableType.THREE_WINDINGS_TRANSFORMER;
            case HVDC_LINE -> IdentifiableType.HVDC_LINE;
            case SUBSTATION -> IdentifiableType.SUBSTATION;
            case VOLTAGE_LEVEL -> IdentifiableType.VOLTAGE_LEVEL;
        };
    }
}
