package org.gridsuite.filter.wip;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import org.gridsuite.filter.utils.FilterType;

import java.util.List;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public interface Filter {

    List<Identifiable<?>> evaluate(Network network);

    List<Identifiable<?>> evaluate(Network network, TopologyKind topologyKind);

    FilterType getFilterType();
}
