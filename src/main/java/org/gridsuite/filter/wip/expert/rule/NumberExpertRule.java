/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rule;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.annotations.Beta;
import com.powsybl.iidm.network.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.expert.data.DataType;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Beta
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public final class NumberExpertRule extends AbstractExpertRule {

    private FieldType fieldType;
    private OperatorType operatorType;

    @Builder.Default
    private Double referenceValue = Double.NaN;

    @Builder.Default
    @JsonDeserialize(as = HashSet.class)
    private Set<Double> referenceValues = new HashSet<>(Set.of(Double.NaN));

    public static Double getNumberValue(String value) {
        return value == null ? Double.NaN : Double.parseDouble(value);
    }

    @Override
    public DataType getDataType() {
        return DataType.NUMBER;
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
    protected OperatorType getOperatorType() {
        return operatorType;
    }

    private boolean evaluateBetweenOperator(Double fieldValue) {
        Double lowerLimit = Collections.min(referenceValues);
        Double upperLimit = Collections.max(referenceValues);
        return fieldValue.compareTo(lowerLimit) >= 0 && fieldValue.compareTo(upperLimit) <= 0;
    }
}
