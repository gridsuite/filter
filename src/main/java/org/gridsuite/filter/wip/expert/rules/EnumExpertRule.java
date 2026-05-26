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
import java.util.Set;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
public final class EnumExpertRule extends AbstractExpertRule {

    private static final Set<OperatorType> SUPPORTED_OPERATORS = Set.of(OperatorType.EQUALS, OperatorType.NOT_EQUALS, OperatorType.IN, OperatorType.NOT_IN);

    private final FieldType fieldType;
    private final OperatorType operatorType;
    private final String referenceValue;
    private final Set<String> referenceValues;

    public static EnumExpertRule of(FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues) {
        if (fieldType == null || operatorType == null) {
            throw new PowsyblException("Invalid enum expert rule, parameters must be non null");
        }
        if (!DataType.ENUM.equals(fieldType.getDataType()) || !SUPPORTED_OPERATORS.contains(operatorType)) {
            throw new PowsyblException("Invalid enum expert rule, fieldType must be ENUM and operatorType must be one of " + SUPPORTED_OPERATORS);
        }

        if (OperatorType.IN.equals(operatorType) || OperatorType.NOT_IN.equals(operatorType)) {
            if (referenceValues == null || referenceValues.isEmpty()) {
                throw new PowsyblException("Invalid enum expert rule, referenceValues must be defined (not null and not empty)");
            }
            return new EnumExpertRule(fieldType, operatorType, referenceValues);
        } else {
            if (referenceValue == null || referenceValue.isBlank()) {
                throw new PowsyblException("Invalid enum expert rule, referenceValue must be defined (not null and not blank)");
            }
            return new EnumExpertRule(fieldType, operatorType, referenceValue);
        }
    }

    private EnumExpertRule(FieldType fieldType, OperatorType operatorType, String referenceValue) {
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.referenceValue = referenceValue;
        this.referenceValues = Collections.emptySet();
    }

    private EnumExpertRule(FieldType fieldType, OperatorType operatorType, Set<String> referenceValues) {
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.referenceValues = Set.copyOf(referenceValues);
        this.referenceValue = "";
    }

    @Override
    public DataType getDataType() {
        return DataType.ENUM;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String fieldValue = ExpertRuleUtils.getStringFieldValue(identifiable, null, fieldType);
        if (fieldValue == null) {
            return false;
        }

        return switch (operatorType) {
            case EQUALS -> fieldValue.equals(referenceValue);
            case NOT_EQUALS -> !fieldValue.equals(referenceValue);
            case IN -> referenceValues.contains(fieldValue);
            case NOT_IN -> !referenceValues.contains(fieldValue);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    protected OperatorType getOperatorType() {
        return operatorType;
    }
}
