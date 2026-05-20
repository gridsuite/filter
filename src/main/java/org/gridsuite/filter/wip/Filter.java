package org.gridsuite.filter.wip;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.wip.data.EquipmentType;
import org.gridsuite.filter.wip.data.FilterType;

import java.util.List;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
public interface Filter {

    EquipmentType getEquipmentType();

    FilterType getFilterType();

    List<Identifiable<?>> evaluate(Network network);
}
