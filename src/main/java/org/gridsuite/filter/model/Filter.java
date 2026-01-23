package org.gridsuite.filter.model;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

public interface Filter {

    EquipmentType getEquipmentType();

    FilterType getType();

    FilterEquipments evaluate(Network network, TopologyKind topologyKind);

    default FilterEquipments evaluate(Network network) {
        return evaluate(network, TopologyKind.BUS_BREAKER);
    }
}
