/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.identifierlistfilter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.iidm.network.Network;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.model.Filter;
import org.gridsuite.filter.model.FilterEquipments;
import org.gridsuite.filter.model.IdentifierListFilter;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterType;

import java.util.*;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IdentifierListFilterDto extends AbstractFilterDto {

    private List<IdentifierListFilterEquipmentAttributes> filterEquipmentsAttributes;

    public IdentifierListFilterDto(UUID id,
                                   Date modificationDate,
                                   EquipmentType equipmentType,
                                   List<IdentifierListFilterEquipmentAttributes> filterEquipmentsAttributes) {
        super(id, modificationDate, equipmentType);
        this.filterEquipmentsAttributes = filterEquipmentsAttributes;
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Override
    public FilterType getType() {
        return FilterType.IDENTIFIER_LIST;
    }

    @Override
    public Filter toModel() {
        return IdentifierListFilter.builder()
            .equipmentType(equipmentType)
            .equipmentIds(filterEquipmentsAttributes.stream().map(IdentifierListFilterEquipmentAttributes::getEquipmentID).toList())
            .build();
    }

    @Override
    public FilteredIdentifiables toFilteredIdentifiables(Network network) {
        FilterEquipments filteredEquipments = toModel().evaluate(network);
        return null;
    }

    public Double getDistributionKey(String equipmentId) {
        return filterEquipmentsAttributes.stream()
            .filter(attribute -> attribute.getEquipmentID().equals(equipmentId))
            .findFirst()
            .map(IdentifierListFilterEquipmentAttributes::getDistributionKey)
            .orElse(null);
    }
}
