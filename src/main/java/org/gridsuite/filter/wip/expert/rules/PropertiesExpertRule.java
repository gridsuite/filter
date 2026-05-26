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
public final class PropertiesExpertRule extends AbstractExpertRule {

    private static final Set<OperatorType> SUPPORTED_OPERATORS = Set.of(OperatorType.IN, OperatorType.NOT_IN);

    private final FieldType fieldType;
    private final OperatorType operatorType;
    private final String targetProperty;
    private final Set<String> referenceValues;

    public static PropertiesExpertRule of(FieldType fieldType, OperatorType operatorType, String targetProperty, Set<String> referenceValues) {
        if (fieldType == null || operatorType == null || targetProperty == null || referenceValues == null || referenceValues.isEmpty()) {
            throw new PowsyblException("Invalid properties expert rule, parameters must be non null and referenceValues must not be empty");
        }
        if (!DataType.PROPERTIES.equals(fieldType.getDataType()) || !SUPPORTED_OPERATORS.contains(operatorType)) {
            throw new PowsyblException("Invalid properties expert rule, fieldType must be PROPERTIES and operatorType must be one of " + SUPPORTED_OPERATORS);
        }
        return new PropertiesExpertRule(fieldType, operatorType, targetProperty, referenceValues);
    }

    private PropertiesExpertRule(FieldType fieldType, OperatorType operatorType, String targetProperty, Set<String> referenceValues) {
        this.fieldType = fieldType;
        this.operatorType = operatorType;
        this.targetProperty = targetProperty;
        this.referenceValues = Set.copyOf(referenceValues);
    }

    @Override
    public DataType getDataType() {
        return DataType.PROPERTIES;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String propertyValue = ExpertRuleUtils.getStringFieldValue(identifiable, targetProperty, fieldType);
        if (propertyValue == null) {
            return false;
        }

        return switch (operatorType) {
            case IN -> referenceValues.stream().anyMatch(propertyValue::equalsIgnoreCase);
            case NOT_IN -> referenceValues.stream().noneMatch(propertyValue::equalsIgnoreCase);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    protected OperatorType getOperatorType() {
        return operatorType;
    }
}
