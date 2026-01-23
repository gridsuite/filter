/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model;

import com.powsybl.iidm.network.*;
import org.gridsuite.filter.utils.EquipmentType;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public final class FiltersUtils {
    private FiltersUtils() {
        throw new AssertionError("Utility class should not be instantiated");
    }

    public static List<Identifiable<?>> getIdentifiables(EquipmentType equipmentType, Network network, TopologyKind topologyKind) {
        return (List<Identifiable<?>>) switch (equipmentType) {
            case GENERATOR -> network.getGeneratorStream().toList();
            case LOAD -> network.getLoadStream().toList();
            case BATTERY -> network.getBatteryStream().toList();
            case STATIC_VAR_COMPENSATOR -> network.getStaticVarCompensatorStream().toList();
            case SHUNT_COMPENSATOR -> network.getShuntCompensatorStream().toList();
            case LCC_CONVERTER_STATION -> network.getLccConverterStationStream().toList();
            case VSC_CONVERTER_STATION -> network.getVscConverterStationStream().toList();
            case HVDC_LINE -> network.getHvdcLineStream().toList();
            case DANGLING_LINE -> network.getDanglingLineStream().toList();
            case LINE -> network.getLineStream().toList();
            case TWO_WINDINGS_TRANSFORMER -> network.getTwoWindingsTransformerStream().toList();
            case THREE_WINDINGS_TRANSFORMER -> network.getThreeWindingsTransformerStream().toList();
            case BUS -> getBusStream(network, topologyKind).toList();
            case BUSBAR_SECTION -> network.getBusbarSectionStream().toList();
            case VOLTAGE_LEVEL -> network.getVoltageLevelStream().toList();
            case SUBSTATION -> network.getSubstationStream().toList();
        };
    }

    private static Stream<Identifiable<?>> getBusStream(Network network, TopologyKind topologyKind) {
        Predicate<VoltageLevel> voltageLevelFilter = vl -> topologyKind == null || vl.getTopologyKind() == topologyKind;

        return network.getVoltageLevelStream()
            .filter(voltageLevelFilter)
            .map(VoltageLevel::getBusBreakerView)
            .flatMap(VoltageLevel.BusBreakerView::getBusStream);
    }
}
