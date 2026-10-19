/**
 * Copyright (c) 2022, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.gridsuite.filter.identifierlistfilter;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.powsybl.iidm.network.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.AbstractFilter;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.EquipmentType;
import org.gridsuite.filter.utils.FilterServiceUtils;
import org.gridsuite.filter.utils.FilterType;
import org.gridsuite.filter.wip.Filter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Seddik Yengui <seddik.yengui at rte-france.com>
 */

@SuperBuilder
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class IdentifierListFilter extends AbstractFilter {

    private List<IdentifierListFilterEquipmentAttributes> filterEquipmentsAttributes;

    public IdentifierListFilter(UUID id,
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
    public FilterEquipments toFilterEquipments(List<Identifiable<?>> identifiables) {
        // we keep the same order of the equipments in the filter
        List<String> notFound = new ArrayList<>();
        List<IdentifiableAttributes> orderedIdentifiableAttributes = filterEquipmentsAttributes.stream()
                .map(f -> identifiables.stream()
                        .filter(attribute -> Objects.equals(attribute.getId(), f.getEquipmentID()))
                        .findFirst()
                        .orElseGet(() -> {
                            notFound.add(f.getEquipmentID());
                            return null;
                        }))
                .filter(Objects::nonNull)
                .map(FilterServiceUtils::map)
                .toList();

        return FilterEquipments.builder()
                .filterId(getId())
                .identifiableAttributes(orderedIdentifiableAttributes)
                .notFoundEquipments(notFound)
                .build();
    }

    @Override
    public Filter toFilter(FilterLoader filterLoader) {
        return org.gridsuite.filter.wip.IdentifierListFilter.builder()
                .equipmentType(getEquipmentType())
                .equipmentIds(filterEquipmentsAttributes.stream()
                        .map(IdentifierListFilterEquipmentAttributes::getEquipmentID)
                        .collect(Collectors.toSet()))
                .build();
    }
}
