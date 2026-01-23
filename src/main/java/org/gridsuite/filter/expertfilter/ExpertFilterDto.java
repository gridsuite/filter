/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.iidm.network.TopologyKind;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRuleDto;
import org.gridsuite.filter.model.Filter;
import org.gridsuite.filter.model.expertfilter.ExpertFilter;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

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
public class ExpertFilterDto extends AbstractFilterDto {

    private AbstractExpertRuleDto rules;

    @Setter
    private TopologyKind topologyKind;

    public ExpertFilterDto(UUID id, Date modificationDate, EquipmentType equipmentType, AbstractExpertRuleDto rules) {
        super(id, modificationDate, equipmentType);
        this.rules = rules;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.EXPERT;
    }

    @Override
    public Filter toModel() {
        return ExpertFilter.builder()
            .equipmentType(equipmentType)
            .rule(rules.toModel())
            .build();
    }
}
