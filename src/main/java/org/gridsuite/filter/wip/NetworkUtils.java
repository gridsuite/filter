/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import com.powsybl.iidm.network.VoltageLevel;
import org.gridsuite.filter.utils.EquipmentType;

import java.util.stream.Stream;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
final class NetworkUtils {

    private NetworkUtils() {
    }

    static Stream<Identifiable<?>> getEquipmentStream(Network network, EquipmentType equipmentType, TopologyKind topologyKind) {
        return switch (equipmentType) {
            case LINE -> network.getLineStream().map(line -> line);
            case BOUNDARY_LINE -> network.getBoundaryLineStream().map(boundaryLine -> boundaryLine);
            case HVDC_LINE -> network.getHvdcLineStream().map(hvdcLine -> hvdcLine);
            case SUBSTATION -> network.getSubstationStream().map(substation -> substation);
            case VOLTAGE_LEVEL -> network.getVoltageLevelStream().map(voltageLevel -> voltageLevel);
            case BUSBAR_SECTION -> network.getBusbarSectionStream().map(busbarSection -> busbarSection);
            case BUS -> getBusStream(network, topologyKind);
            case GENERATOR -> network.getGeneratorStream().map(generator -> generator);
            case BATTERY -> network.getBatteryStream().map(battery -> battery);
            case LOAD -> network.getLoadStream().map(load -> load);
            case SHUNT_COMPENSATOR -> network.getShuntCompensatorStream().map(sc -> sc);
            case STATIC_VAR_COMPENSATOR -> network.getStaticVarCompensatorStream().map(svc -> svc);
            case TWO_WINDINGS_TRANSFORMER -> network.getTwoWindingsTransformerStream().map(twoWindingsTransformer -> twoWindingsTransformer);
            case THREE_WINDINGS_TRANSFORMER -> network.getThreeWindingsTransformerStream().map(threeWindingsTransformer -> threeWindingsTransformer);
            case LCC_CONVERTER_STATION -> network.getLccConverterStationStream().map(lccConverterStation -> lccConverterStation);
            case VSC_CONVERTER_STATION -> network.getVscConverterStationStream().map(vscConverterStation -> vscConverterStation);
        };
    }

    private static Stream<Identifiable<?>> getBusStream(Network network, TopologyKind topologyKind) {
        return network.getVoltageLevelStream()
                .filter(vl -> topologyKind == null || vl.getTopologyKind() == topologyKind)
                .map(VoltageLevel::getBusBreakerView)
                .flatMap(VoltageLevel.BusBreakerView::getBusStream);
    }
}
