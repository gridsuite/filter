/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
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
import org.gridsuite.filter.AbstractFilterDto;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.model.Filter;
import org.gridsuite.filter.model.expertfilter.rules.ExpertRule;
import org.gridsuite.filter.model.expertfilter.rules.FilterExpertRule;
import org.gridsuite.filter.model.expertfilter.rules.StringExpertRule;
import org.gridsuite.filter.model.expertfilter.rules.StringListExpertRule;
import org.gridsuite.filter.utils.expertfilter.DataType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.isMultipleCriteriaOperator;

/**
 * @author Franck Lecuyer <franck.lecuyer at rte-france.com>
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class FilterUuidExpertRuleDto extends StringExpertRuleDto {
    private FilterLoader filterLoader;

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.FILTER_UUID;
    }

    @Override
    public ExpertRule toModel() {
        List<Filter> filters = filterLoader.getFilters(
            getValues().stream()
                .map(UUID::fromString)
                .toList()
        ).stream().map(AbstractFilterDto::toModel).toList();
        return FilterExpertRule.builder().filters(filters).operator(getOperator()).build();
    }
}
