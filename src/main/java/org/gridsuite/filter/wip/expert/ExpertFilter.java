package org.gridsuite.filter.wip.expert;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.wip.AbstractFilter;
import org.gridsuite.filter.wip.data.EquipmentType;
import org.gridsuite.filter.wip.data.FilterType;
import org.gridsuite.filter.wip.expert.rules.AbstractExpertRule;

import java.util.stream.Stream;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public class ExpertFilter extends AbstractFilter {

    private final AbstractExpertRule rule;

    public ExpertFilter(EquipmentType equipmentType) {
        super(equipmentType);
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.EXPERT;
    }

    @Override
    protected boolean evaluateFilterRule(Identifiable<?> identifiable) {
        return false;
    }

    @Override
    protected Stream<Identifiable<?>> getBusStream(Network network) {
        return Stream.empty();
    }
}
