/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rules;

import com.google.common.annotations.Beta;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.expert.data.DataType;
import org.gridsuite.filter.wip.expert.data.FieldType;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
public final class NumberExpertRule extends AbstractExpertRule {

    private static final Set<OperatorType> SUPPORTED_OPERATORS = Set.of(OperatorType.EQUALS, OperatorType.GREATER_OR_EQUALS, OperatorType.GREATER, OperatorType.LOWER_OR_EQUALS, OperatorType.LOWER, OperatorType.BETWEEN, OperatorType.EXISTS, OperatorType.NOT_EXISTS, OperatorType.IN, OperatorType.NOT_IN);

    private final FieldType fieldType;
    private final OperatorType operatorType;
    private final Double referenceValue;
    private final Set<Double> referenceValues;

    public static NumberExpertRule of(FieldType fieldType, OperatorType operatorType, Double referenceValue, Set<Double> referenceValues) {
        if (fieldType == null || operatorType == null) {
            throw new PowsyblException("Invalid number expert rule, parameters must be non null");
        }
        if (!DataType.NUMBER.equals(fieldType.getDataType()) || !SUPPORTED_OPERATORS.contains(operatorType)) {
            throw new PowsyblException("Invalid number expert rule, fieldType must be NUMBER and operatorType must be one of " + SUPPORTED_OPERATORS);
        }

        if (OperatorType.EXISTS.equals(operatorType) || OperatorType.NOT_EXISTS.equals(operatorType)) {
            if (referenceValue != null || referenceValues != null) {
                throw new PowsyblException("Invalid number expert rule, referenceValue and referenceValues must be null for EXISTS and NOT_EXISTS operators");
            }
            return new NumberExpertRule(fieldType, operatorType);
        }

        if (OperatorType.BETWEEN.equals(operatorType) || OperatorType.IN.equals(operatorType) || OperatorType.NOT_IN.equals(operatorType)) {
            if (referenceValues == null || referenceValues.isEmpty()) {
                throw new PowsyblException("Invalid number expert rule, referenceValues must be defined (not null and not empty)");
            }
            return new NumberExpertRule(fieldType, operatorType, referenceValues);
        } else {
            if (referenceValue == null) {
                throw new PowsyblException("Invalid number expert rule, referenceValue must be defined (not null)");
            }
            return new NumberExpertRule(fieldType, operatorType, referenceValue);
        }
    }

    private NumberExpertRule(FieldType fieldType, OperatorType operatorType, Double referenceValue) {
        this.fieldType = Objects.requireNonNull(fieldType);
        this.operatorType = Objects.requireNonNull(operatorType);
        this.referenceValue = referenceValue;
        this.referenceValues = Collections.emptySet();
    }

    private NumberExpertRule(FieldType fieldType, OperatorType operatorType, Set<Double> referenceValues) {
        this.fieldType = Objects.requireNonNull(fieldType);
        this.operatorType = Objects.requireNonNull(operatorType);
        this.referenceValues = Set.copyOf(referenceValues);
        this.referenceValue = Double.NaN;
    }

    private NumberExpertRule(FieldType fieldType, OperatorType operatorType) {
        this.fieldType = Objects.requireNonNull(fieldType);
        this.operatorType = Objects.requireNonNull(operatorType);
        this.referenceValue = Double.NaN;
        this.referenceValues = Collections.emptySet();
    }

    @Override
    public DataType getDataType() {
        return DataType.NUMBER;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        Double fieldValue = ExpertRuleUtils.getDoubleFieldValue(identifiable, fieldType);
        if (fieldValue == null || fieldValue.isNaN()) {
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
