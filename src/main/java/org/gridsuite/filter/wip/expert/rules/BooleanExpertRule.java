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

import java.util.Set;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
public final class BooleanExpertRule extends AbstractExpertRule {

    private static final Set<OperatorType> SUPPORTED_OPERATORS = Set.of(OperatorType.EQUALS, OperatorType.NOT_EQUALS, OperatorType.EXISTS, OperatorType.NOT_EXISTS);

    private final FieldType fieldType;
    private final OperatorType operatorType;
    private final Boolean referenceValue;

    public static BooleanExpertRule of(FieldType fieldType, OperatorType operatorType, Boolean referenceValue) {
        if (fieldType == null || operatorType == null) {
            throw new PowsyblException("Invalid boolean expert rule, parameters must be non null");
        }
        if (!DataType.BOOLEAN.equals(fieldType.getDataType()) || !SUPPORTED_OPERATORS.contains(operatorType)) {
            throw new PowsyblException("Invalid boolean expert rule, fieldType must be BOOLEAN and operatorType must be one of " + SUPPORTED_OPERATORS);
        }

        if (OperatorType.EXISTS.equals(operatorType) || OperatorType.NOT_EXISTS.equals(operatorType)) {
            if (referenceValue != null) {
                throw new PowsyblException("Invalid boolean expert rule, referenceValue must be null for EXISTS and NOT_EXISTS operators");
            }
            return new BooleanExpertRule(fieldType, operatorType);
        } else {
            if (referenceValue == null) {
                throw new PowsyblException("Invalid boolean expert rule, referenceValue must be defined (not null)");
            }
            return new BooleanExpertRule(fieldType, operatorType, referenceValue);
        }
    }

    private BooleanExpertRule(FieldType fieldType, OperatorType operatorType, Boolean referenceValue) {
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.referenceValue = referenceValue;
    }

    private BooleanExpertRule(FieldType fieldType, OperatorType operatorType) {
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.referenceValue = Boolean.FALSE;
    }

    @Override
    public DataType getDataType() {
        return DataType.BOOLEAN;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        Boolean fieldValue = ExpertRuleUtils.getBooleanFieldValue(identifiable, fieldType);
        if (fieldValue == null) {
            return OperatorType.NOT_EXISTS.equals(operatorType);
        }

        return switch (operatorType) {
            case EQUALS -> referenceValue.equals(fieldValue);
            case NOT_EQUALS -> !referenceValue.equals(fieldValue);
            case EXISTS -> true;
            case NOT_EXISTS -> false;
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    protected OperatorType getOperatorType() {
        return operatorType;
    }
}
