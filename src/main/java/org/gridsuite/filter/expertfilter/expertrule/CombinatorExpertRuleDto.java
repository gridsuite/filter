/**
 * Copyright (c) 2023, RTE (http://www.rte-france.com)
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
import org.gridsuite.filter.model.expertfilter.rules.CombinatorExpertRule;
import org.gridsuite.filter.model.expertfilter.rules.ExpertRule;
import org.gridsuite.filter.utils.expertfilter.DataType;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class CombinatorExpertRuleDto extends AbstractExpertRuleDto {
    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.COMBINATOR;
    }

    @Override
    public String getStringValue() {
        return null;
    }

    @Override
    public ExpertRule toModel() {
        return CombinatorExpertRule.builder()
            .combinator(getCombinator())
            .rules(getRules().stream().map(AbstractExpertRuleDto::toModel).toList())
            .build();
    }
}
