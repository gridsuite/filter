package org.gridsuite.filter.wip.identifier;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.wip.AbstractFilter;
import org.gridsuite.filter.wip.data.EquipmentType;
import org.gridsuite.filter.wip.data.FilterType;

import java.util.Set;
import java.util.stream.Stream;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public class IdentifierListFilter extends AbstractFilter {

    private final Set<String> equipmentIds;

    public IdentifierListFilter(EquipmentType equipmentType, Set<String> equipmentIds) {
        super(equipmentType);
        this.equipmentIds = equipmentIds;
    }

    @Override
    public FilterType getFilterType() {
        return FilterType.IDENTIFIER_LIST;
    }

    @Override
    protected boolean evaluateFilterRule(Identifiable<?> identifiable) {
        return equipmentIds.contains(identifiable.getId());
    }

    @Override
    protected Stream<Identifiable<?>> getBusStream(Network network) {
        // return empty stream because IdentifierListFilter does not handle this Identifiable type
        return Stream.empty();
    }
}
