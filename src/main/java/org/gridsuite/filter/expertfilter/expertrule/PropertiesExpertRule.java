/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.wip.rule.ExpertRule;

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
public class PropertiesExpertRule extends AbstractExpertRule {
    private String propertyName;

    @JsonDeserialize(as = ArrayList.class)
    private List<String> propertyValues;

    @Override
    public DataType getDataType() {
        return DataType.PROPERTIES;
    }

    @Override
    public ExpertRule toRule(FilterLoader filterLoader) {
        return org.gridsuite.filter.wip.rule.PropertiesExpertRule.builder()
                .field(getField())
                .operator(getOperator())
                .propertyName(propertyName)
                .propertyValues(propertyValues)
                .build();
    }

    @Override
    public String getStringValue() {
        return this.getPropertyName();
    }
}
