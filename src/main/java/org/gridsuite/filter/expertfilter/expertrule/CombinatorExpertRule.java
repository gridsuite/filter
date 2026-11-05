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

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class CombinatorExpertRule extends AbstractExpertRule {
    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.COMBINATOR;
    }

    @Override
    public ExpertRule toRule(FilterLoader filterLoader) {
        return org.gridsuite.filter.wip.rule.CombinatorExpertRule.builder()
                .combinator(getCombinator())
                .rules(getRules().stream().map(r -> r.toRule(filterLoader)).toList())
                .build();
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
