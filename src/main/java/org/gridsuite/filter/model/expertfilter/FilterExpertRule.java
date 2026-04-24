/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.model.AbstractExpertRule;
import org.gridsuite.filter.model.AbstractFilter;
import org.gridsuite.filter.model.FilterEquipments;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
public class FilterExpertRule extends AbstractExpertRule {

    private final OperatorType operatorType;

    private final AbstractFilter filter;

    private FilterEquipments filterEquipments;

    public FilterExpertRule(OperatorType operatorType, AbstractFilter filter) {
        this.operatorType = operatorType;
        this.filter = filter;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        return switch (operatorType) {
            case OperatorType.IS_PART_OF -> filterEquipments.foundEquipments()
                    .contains(identifiable.getId());
            case OperatorType.IS_NOT_PART_OF -> !filterEquipments.foundEquipments()
                    .contains(identifiable.getId());
            default -> throw new PowsyblException(operatorType + " operator not supported with filter rule data type");
        };
    }

    @Override
    public void init(Network network) {
        filterEquipments = filter.getEquipments(network);
    }
}
