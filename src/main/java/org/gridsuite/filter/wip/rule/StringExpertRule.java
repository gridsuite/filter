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
import org.apache.commons.lang3.Strings;
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
public final class StringExpertRule implements ExpertRule {

    private FieldType fieldType;
    private OperatorType operatorType;
    private Set<String> referenceValues;
    private String referenceValue;

    @Builder
    public StringExpertRule(FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues) {
        this.fieldType = Objects.requireNonNull(fieldType);
        this.operatorType = Objects.requireNonNull(operatorType);
        this.referenceValue = referenceValue != null ? referenceValue : "";
        this.referenceValues = referenceValues != null ? referenceValues : Collections.emptySet();
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String fieldValue = ExpertFilterUtils.getFieldValue(fieldType, null, identifiable);
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.STRING;
    }
}
