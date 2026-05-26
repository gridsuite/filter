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
import org.apache.commons.lang3.Strings;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.expert.data.DataType;
import org.gridsuite.filter.wip.expert.data.FieldType;

import java.util.Collections;
import java.util.Set;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
public final class StringExpertRule extends AbstractExpertRule {

    private static final Set<OperatorType> SUPPORTED_OPERATORS = Set.of(OperatorType.IS, OperatorType.CONTAINS, OperatorType.BEGINS_WITH, OperatorType.ENDS_WITH, OperatorType.EXISTS, OperatorType.NOT_EXISTS, OperatorType.IN, OperatorType.NOT_IN);

    private final FieldType fieldType;
    private final OperatorType operatorType;
    private final String referenceValue;
    private final Set<String> referenceValues;

    public static StringExpertRule of(FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues) {
        if (fieldType == null || operatorType == null) {
            throw new PowsyblException("Invalid string expert rule, parameters must be non null");
        }
        if (!DataType.STRING.equals(fieldType.getDataType()) || !SUPPORTED_OPERATORS.contains(operatorType)) {
            throw new PowsyblException("Invalid string expert rule, fieldType must be STRING and operatorType must be one of " + SUPPORTED_OPERATORS);
        }

        if (OperatorType.EXISTS.equals(operatorType) || OperatorType.NOT_EXISTS.equals(operatorType)) {
            if (referenceValue != null || referenceValues != null) {
                throw new PowsyblException("Invalid string expert rule, referenceValue and referenceValues must be null for EXISTS and NOT_EXISTS operators");
            }
            return new StringExpertRule(fieldType, operatorType);
        }

        if (OperatorType.IN.equals(operatorType) || OperatorType.NOT_IN.equals(operatorType)) {
            if (referenceValues == null || referenceValues.isEmpty()) {
                throw new PowsyblException("Invalid string expert rule, referenceValues must be defined (not null and not empty)");
            }
            return new StringExpertRule(fieldType, operatorType, referenceValues);
        } else {
            if (referenceValue == null || referenceValue.isBlank()) {
                throw new PowsyblException("Invalid string expert rule, referenceValue must be defined (not null and not blank)");
            }
            return new StringExpertRule(fieldType, operatorType, referenceValue);
        }
    }

    private StringExpertRule(FieldType fieldType, OperatorType operatorType, String referenceValue) {
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.referenceValue = referenceValue;
        this.referenceValues = Collections.emptySet();
    }

    private StringExpertRule(FieldType fieldType, OperatorType operatorType, Set<String> referenceValues) {
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.referenceValues = referenceValues;
        this.referenceValue = "";
    }

    private StringExpertRule(FieldType fieldType, OperatorType operatorType) {
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.referenceValue = "";
        this.referenceValues = Collections.emptySet();
    }

    @Override
    public DataType getDataType() {
        return DataType.STRING;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String fieldValue = ExpertRuleUtils.getStringFieldValue(identifiable, null, fieldType);
        if (fieldValue == null || fieldValue.isEmpty()) {
            return operatorType.equals(OperatorType.NOT_EXISTS);
        }

        return switch (operatorType) {
            case IS -> Strings.CI.equals(fieldValue, referenceValue);
            case CONTAINS -> Strings.CI.contains(fieldValue, referenceValue);
            case BEGINS_WITH -> Strings.CI.startsWith(fieldValue, referenceValue);
            case ENDS_WITH -> Strings.CI.endsWith(fieldValue, referenceValue);
            case EXISTS -> true;
            case NOT_EXISTS -> false;
            case IN -> referenceValues.stream().anyMatch(fieldValue::equalsIgnoreCase);
            case NOT_IN -> referenceValues.stream().noneMatch(fieldValue::equalsIgnoreCase);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    protected OperatorType getOperatorType() {
        return operatorType;
    }
}
