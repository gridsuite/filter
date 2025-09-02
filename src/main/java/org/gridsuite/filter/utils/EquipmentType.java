/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.utils;

/**
 * @author Etienne Homer <etienne.homer at rte-france.com>
 */
public enum EquipmentType {
    BATTERY,
    BUS,
    BUSBAR_SECTION,
    DANGLING_LINE,
    GENERATOR,
    HVDC_LINE,
    LCC_CONVERTER_STATION,
    LINE,
    LOAD,
    SHUNT_COMPENSATOR,
    STATIC_VAR_COMPENSATOR,
    SUBSTATION,
    THREE_WINDINGS_TRANSFORMER,
    TWO_WINDINGS_TRANSFORMER,
    VOLTAGE_LEVEL,
    VSC_CONVERTER_STATION
}
