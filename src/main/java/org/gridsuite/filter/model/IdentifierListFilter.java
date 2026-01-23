/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import com.powsybl.iidm.network.TopologyKind;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

import java.util.*;

import static org.gridsuite.filter.model.FiltersUtils.getIdentifiables;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IdentifierListFilter extends AbstractFilter {

    private List<String> equipmentIds;

    public IdentifierListFilter(EquipmentType equipmentType, List<String> equipmentIds) {
        super(equipmentType);
        this.equipmentIds = equipmentIds;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.IDENTIFIER_LIST;
    }

    @Override
    public FilterEquipments evaluate(Network network, TopologyKind topologyKind) {
        // we keep the same order of the equipments in the filter
        List<String> foundEquipments = new ArrayList<>();
        List<String> notFoundEquipments = new ArrayList<>();
        List<String> networkIdentifiables = getIdentifiables(equipmentType, network, topologyKind)
            .stream()
            .map(Identifiable::getId)
            .toList();
        equipmentIds.forEach(id -> {
            if (networkIdentifiables.contains(id)) {
                foundEquipments.add(id);
            } else {
                notFoundEquipments.add(id);
            }
        });

        return new FilterEquipments(foundEquipments, notFoundEquipments);
    }
}
