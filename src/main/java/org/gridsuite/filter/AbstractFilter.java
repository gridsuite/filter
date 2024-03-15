/**
 * Copyright (c) 2021, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.criteriafilter.CriteriaFilter;
import org.gridsuite.filter.expertfilter.ExpertFilter;
import org.gridsuite.filter.identifierlistfilter.FilterEquipments;
import org.gridsuite.filter.identifierlistfilter.IdentifiableAttributes;
import org.gridsuite.filter.identifierlistfilter.IdentifierListFilter;
import org.gridsuite.filter.scriptfilter.ScriptFilter;
import org.gridsuite.filter.utils.EquipmentType;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author Jacques Borsenberger <jacques.borsenberger at rte-france.com>
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    property = "type",
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    visible = true
)
@JsonSubTypes({//Below, we define the names and the binding classes.
    @JsonSubTypes.Type(value = CriteriaFilter.class, name = "CRITERIA"),
    @JsonSubTypes.Type(value = IdentifierListFilter.class, name = "IDENTIFIER_LIST"),
    @JsonSubTypes.Type(value = ExpertFilter.class, name = "EXPERT"),
    @JsonSubTypes.Type(value = ScriptFilter.class, name = "SCRIPT")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString
public abstract class AbstractFilter implements IFilterAttributes {

    UUID id;

    Date modificationDate;

    EquipmentType equipmentType;

    public FilterEquipments getFilterEquipments(List<IdentifiableAttributes> identifiableAttributes) {
        return FilterEquipments.builder()
                .filterId(id)
                .identifiableAttributes(identifiableAttributes)
                .build();
    }
}
