/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.iidm.network.TopologyKind;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.expertfilter.expertrule.AbstractExpertRule;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

import java.util.Date;
import java.util.UUID;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@Setter
@Schema(description = "Expert Filters", allOf = AbstractFilter.class)
@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ExpertFilter extends AbstractFilter {

    @Schema(description = "Rules")
    private AbstractExpertRule rules;

    @Setter
    @Schema(description = "TopologyKind is an optional information used when filtering bus")
    private TopologyKind topologyKind;

    public ExpertFilter(UUID id, Date modificationDate, EquipmentType equipmentType, AbstractExpertRule rules) {
        super(id, modificationDate, equipmentType);
        this.rules = rules;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.EXPERT;
    }
}
