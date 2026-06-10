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
import java.util.Objects;
import java.util.Set;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Data
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class NumberExpertRule implements ExpertRule {

    private FieldType fieldType;
    private OperatorType operatorType;
    private Double referenceValue;
    private Set<Double> referenceValues;

    public static Double getNumberValue(String value) {
        return value == null ? Double.NaN : Double.parseDouble(value);
    }

    @Builder
    public NumberExpertRule(FieldType fieldType, OperatorType operatorType, Double referenceValue, Set<Double> referenceValues) {
        this.fieldType = Objects.requireNonNull(fieldType);
        this.operatorType = Objects.requireNonNull(operatorType);
        this.referenceValue = referenceValue != null ? referenceValue : Double.NaN;
        this.referenceValues = referenceValues != null ? Set.copyOf(referenceValues) : Collections.singleton(Double.NaN);
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        Double fieldValue = getNumberValue(ExpertFilterUtils.getFieldValue(fieldType, null, identifiable));
        if (fieldValue.isNaN()) {
            return OperatorType.NOT_EXISTS.equals(operatorType);
        }

        return switch (operatorType) {
            case EQUALS -> fieldValue.equals(referenceValue);
            case GREATER_OR_EQUALS -> fieldValue.compareTo(referenceValue) >= 0;
            case GREATER -> fieldValue.compareTo(referenceValue) > 0;
            case LOWER_OR_EQUALS -> fieldValue.compareTo(referenceValue) <= 0;
            case LOWER -> fieldValue.compareTo(referenceValue) < 0;
            case BETWEEN -> evaluateBetweenOperator(fieldValue);
            case EXISTS -> true;
            case NOT_EXISTS -> false;
            case IN -> referenceValues.contains(fieldValue);
            case NOT_IN -> !referenceValues.contains(fieldValue);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.NUMBER;
    }

    private boolean evaluateBetweenOperator(Double fieldValue) {
        Double lowerLimit = Collections.min(referenceValues);
        Double upperLimit = Collections.max(referenceValues);
        return fieldValue.compareTo(lowerLimit) >= 0 && fieldValue.compareTo(upperLimit) <= 0;
    }
}
