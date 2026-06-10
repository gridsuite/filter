/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.rule;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.Beta;
import com.powsybl.iidm.network.Identifiable;
import lombok.*;
import org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.data.DataType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Data
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberExpertRule implements ExpertRule {

    private FieldType field;
    private OperatorType operator;
    private Double value;
    private List<Double> values;

    public static Double getNumberValue(String value) {
        return value == null ? Double.NaN : Double.parseDouble(value);
    }

    @Builder
    public NumberExpertRule(FieldType field, OperatorType operator, Double value, List<Double> values) {
        this.field = Objects.requireNonNull(field);
        this.operator = Objects.requireNonNull(operator);
        this.value = value != null ? value : Double.NaN;
        this.values = values != null ? List.copyOf(values) : List.of(Double.NaN);
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        Double fieldValue = getNumberValue(ExpertFilterUtils.getFieldValue(field, null, identifiable));
        if (fieldValue.isNaN()) {
            return OperatorType.NOT_EXISTS.equals(operator);
        }

        return switch (operator) {
            case EQUALS -> fieldValue.equals(value);
            case GREATER_OR_EQUALS -> fieldValue.compareTo(value) >= 0;
            case GREATER -> fieldValue.compareTo(value) > 0;
            case LOWER_OR_EQUALS -> fieldValue.compareTo(value) <= 0;
            case LOWER -> fieldValue.compareTo(value) < 0;
            case BETWEEN -> evaluateBetweenOperator(fieldValue);
            case EXISTS -> true;
            case NOT_EXISTS -> false;
            case IN -> values.contains(fieldValue);
            case NOT_IN -> !values.contains(fieldValue);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.NUMBER;
    }

    private boolean evaluateBetweenOperator(Double fieldValue) {
        Double lowerLimit = Collections.min(values);
        Double upperLimit = Collections.max(values);
        return fieldValue.compareTo(lowerLimit) >= 0 && fieldValue.compareTo(upperLimit) <= 0;
    }
}
