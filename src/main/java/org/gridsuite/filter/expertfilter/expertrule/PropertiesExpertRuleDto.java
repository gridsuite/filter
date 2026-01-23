/**
 * Copyright (c) 2024, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.model.expertfilter.rules.ExpertRule;
import org.gridsuite.filter.model.expertfilter.rules.PropertiesExpertRule;
import org.gridsuite.filter.utils.expertfilter.DataType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author maissa SOUISSI <maissa.souissi at rte-france.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class PropertiesExpertRuleDto extends AbstractExpertRuleDto {
    private String propertyName;

    @JsonDeserialize(as = ArrayList.class)
    private List<String> propertyValues;

    @Override
    public DataType getDataType() {
        return DataType.PROPERTIES;
    }

    @Override
    public String getStringValue() {
        return this.getPropertyName();
    }

    @Override
    public ExpertRule toModel() {
        return PropertiesExpertRule.builder()
            .propertyName(propertyName)
            .field(getField())
            .operator(getOperator())
            .value(getPropertyValues())
            .build();
    }
}
