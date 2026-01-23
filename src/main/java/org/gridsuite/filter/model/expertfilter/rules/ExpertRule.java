package org.gridsuite.filter.model.expertfilter.rules;

import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;

public interface ExpertRule {

    void init(Network network);

    boolean evaluate(Identifiable<?> identifiable);
}
