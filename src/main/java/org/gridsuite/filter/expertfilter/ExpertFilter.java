/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter;

import com.powsybl.iidm.network.TopologyKind;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.wip.Filter;

import java.util.Date;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ExpertFilter extends AbstractFilter {

    private AbstractExpertRule rules;

    @Setter
    private TopologyKind topologyKind;

    public ExpertFilter(UUID id, Date modificationDate, EquipmentType equipmentType, AbstractExpertRule rules) {
        super(id, modificationDate, equipmentType);
        this.rules = rules;
    }

    @Override
    public FilterType getType() {
        return FilterType.EXPERT;
    }

    @Override
    public Filter toFilter(FilterLoader filterLoader) {
        return org.gridsuite.filter.wip.ExpertFilter.builder()
                .equipmentType(getEquipmentType())
                .rule(rules.toRule(filterLoader))
                .build();
    }
}
