package org.gridsuite.filter.model;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.utils.FiltersUtils;

import java.util.ArrayList;
import java.util.List;

public class IdentifierListFilter extends AbstractFilter {

    private final List<String> equipmentIds;

    public IdentifierListFilter(
            EquipmentType equipmentType,
            List<String> equipmentIds
    ) {
        super(equipmentType);
        this.equipmentIds = equipmentIds;
    }

    @Override
    public FilterEquipments getEquipments(Network network, TopologyKind topologyKind) {
        List<String> notFound = new ArrayList<>();
        List<String> found = new ArrayList<>();
        FiltersUtils.getIdentifiablesLoader(network, equipmentType, topologyKind)
                .forEach(identifiable -> {
                    if (equipmentIds.contains(identifiable.getId())) {
                        found.add(identifiable.getId());
                    } else {
                        notFound.add(identifiable.getId());
                    }
                });
        return new FilterEquipments(found, notFound);
    }

    @Override
    public FilterType getType() {
        return FilterType.IDENTIFIER_LIST;
    }
}
