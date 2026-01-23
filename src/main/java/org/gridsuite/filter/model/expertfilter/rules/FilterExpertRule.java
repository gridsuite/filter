/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter.rules;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.model.Filter;
import org.gridsuite.filter.model.FilterEquipments;
import org.gridsuite.filter.utils.expertfilter.OperatorType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode
@ToString(callSuper = true)
@SuperBuilder
public class FilterExpertRule implements ExpertRule {
    private List<Filter> filters;

    private OperatorType operator;

    private Set<String> equipmentsIds;

    @Override
    public void init(Network network) {
        equipmentsIds = filters.stream()
            .flatMap(f -> f.evaluate(network).getFoundEquipments().stream())
            .collect(Collectors.toSet());
    }

    @Override
    public boolean evaluate(Identifiable<?> identifiable) {
        return switch (getOperator()) {
            case IS_PART_OF -> equipmentsIds.contains(identifiable.getId());
            case IS_NOT_PART_OF -> !equipmentsIds.contains(identifiable.getId());
            default -> throw new PowsyblException(this.getOperator() + " operator not supported with filter rule data type");
        };
    }
}
