/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.wip.rule.ExpertRule;
import org.gridsuite.filter.wip.rule.FilterExpertRule;

import java.util.UUID;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class FilterUuidExpertRule extends StringExpertRule {
    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.FILTER_UUID;
    }

    @Override
    public ExpertRule toRule(FilterLoader filterLoader) {
        return FilterExpertRule.builder()
                .field(getField())
                .operator(getOperator())
                .filters(filterLoader.getFilters(getValues().stream().map(UUID::fromString).toList()).stream()
                        .map(af -> af.toFilter(filterLoader))
                        .toList())
                .build();
    }
}
