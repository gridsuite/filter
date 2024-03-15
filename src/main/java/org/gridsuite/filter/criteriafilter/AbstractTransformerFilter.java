/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.criteriafilter;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.springframework.util.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@ToString(callSuper = true)
@Schema(description = "Transformer Filters", allOf = CriteriaFilter.class)
public abstract class AbstractTransformerFilter extends AbstractEquipmentFilterForm {

    @Schema(description = "SubstationName")
    private String substationName;

    @Schema(description = "Countries")
    private SortedSet<String> countries;

    @Schema(description = "Substation free properties")
    // LinkedHashMap to keep order too
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> substationFreeProperties;

    @Schema(description = "Free properties")
    @JsonDeserialize(as = LinkedHashMap.class)
    private Map<String, List<String>> freeProperties;

    @Override
    public boolean isEmpty() {
        return super.isEmpty()
                && substationName == null
                && CollectionUtils.isEmpty(countries)
                && CollectionUtils.isEmpty(substationFreeProperties);
    }
}
