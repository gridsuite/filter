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
    LINE,
    GENERATOR,
    LOAD,
    SHUNT_COMPENSATOR,
    STATIC_VAR_COMPENSATOR,
    BATTERY,
    BUS,
    BUSBAR_SECTION,
    DANGLING_LINE,
    LCC_CONVERTER_STATION,
    VSC_CONVERTER_STATION,
    TWO_WINDINGS_TRANSFORMER,
    THREE_WINDINGS_TRANSFORMER,
    HVDC_LINE,
    SUBSTATION,
    VOLTAGE_LEVEL
}
