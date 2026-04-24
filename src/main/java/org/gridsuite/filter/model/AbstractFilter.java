package org.gridsuite.filter.model;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

public abstract class AbstractFilter {

    protected EquipmentType equipmentType;

    public AbstractFilter(EquipmentType equipmentType) {
        this.equipmentType = equipmentType;
    }

    abstract public FilterEquipments getEquipments(Network network, TopologyKind topologyKind);

    public FilterEquipments getEquipments(Network network) {
        return getEquipments(network, TopologyKind.BUS_BREAKER);
    }

    abstract public FilterType getType();
}
