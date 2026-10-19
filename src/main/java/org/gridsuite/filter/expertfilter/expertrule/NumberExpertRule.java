/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.gridsuite.filter.expertfilter.expertrule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.FilterLoader;
import org.gridsuite.filter.utils.expertfilter.DataType;
import org.gridsuite.filter.wip.rule.ExpertRule;

import java.util.*;
import java.util.stream.Collectors;

import static org.gridsuite.filter.utils.expertfilter.OperatorType.isMultipleCriteriaOperator;

/**
 * @author Antoine Bouhours <antoine.bouhours at rte-france.com>
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public class NumberExpertRule extends AbstractExpertRule {
    private Double value;

    @JsonDeserialize(as = HashSet.class)
    private Set<Double> values;

    public static Double getNumberValue(String value) {
        return value == null ? Double.NaN : Double.parseDouble(value);
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.NUMBER;
    }

    @Override
    public ExpertRule toRule(FilterLoader filterLoader) {
        return org.gridsuite.filter.wip.rule.NumberExpertRule.builder()
                .field(getField())
                .operator(getOperator())
                .value(getValue())
                .values(getValues() == null ? null : getValues().stream().toList())
                .build();
    }

    @Override
    public String getStringValue() {
        if (isMultipleCriteriaOperator(this.getOperator())) { // multiple values
            return this.getValues().stream().map(String::valueOf).collect(Collectors.joining(","));
        } else { // single value or absence
            return this.getValue() != null ? String.valueOf(this.getValue()) : null;
        }
    }
}
