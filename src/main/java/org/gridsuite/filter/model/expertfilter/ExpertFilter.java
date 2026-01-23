/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.model.expertfilter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.model.AbstractFilter;
import org.gridsuite.filter.model.FilterEquipments;
import org.gridsuite.filter.model.expertfilter.rules.ExpertRule;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

import java.util.ArrayList;
import java.util.List;

import static org.gridsuite.filter.model.FiltersUtils.getIdentifiables;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExpertFilter extends AbstractFilter {

    private ExpertRule rule;

    public ExpertFilter(EquipmentType equipmentType, ExpertRule rule) {
        super(equipmentType);
        this.rule = rule;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.EXPERT;
    }

    @Override
    public FilterEquipments evaluate(Network network, TopologyKind topologyKind) {
        rule.init(network);
        // we keep the same order of the equipments in the filter
        List<String> foundEquipments = new ArrayList<>();
        List<String> notFoundEquipments = new ArrayList<>();
        List<Identifiable<?>> networkIdentifiables = getIdentifiables(equipmentType, network, topologyKind);
        networkIdentifiables.forEach(identifiable -> {
            if (rule.evaluate(identifiable)) {
                foundEquipments.add(identifiable.getId());
            } else {
                notFoundEquipments.add(identifiable.getId());
            }
        });

        return new FilterEquipments(foundEquipments, notFoundEquipments);
    }
}
