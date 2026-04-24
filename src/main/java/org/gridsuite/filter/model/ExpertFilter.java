package org.gridsuite.filter.model;

import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.utils.FiltersUtils;

import java.util.ArrayList;
import java.util.List;

public class ExpertFilter extends AbstractFilter {

    private final AbstractExpertRule rule;

    public ExpertFilter(EquipmentType equipmentType, AbstractExpertRule rule) {
        super(equipmentType);
        this.rule = rule;
    }

    @Override
    public FilterEquipments getEquipments(Network network, TopologyKind topologyKind) {
        rule.init(network);
        List<String> notFound = new ArrayList<>();
        List<String> found = new ArrayList<>();
        FiltersUtils.getIdentifiablesLoader(network, equipmentType, topologyKind)
                .forEach(identifiable -> {
                    if (rule.evaluateRule(identifiable)) {
                        found.add(identifiable.getId());
                    } else {
                        notFound.add(identifiable.getId());
                    }
                });
        return new FilterEquipments(found, notFound);
    }

    @Override
    public FilterType getType() {
        return FilterType.EXPERT;
    }
}
