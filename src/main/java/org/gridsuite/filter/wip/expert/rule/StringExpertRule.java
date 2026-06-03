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
import org.apache.commons.lang3.Strings;
import org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.expert.data.DataType;

import java.util.HashSet;
import java.util.Set;

@Beta
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public final class StringExpertRule extends AbstractExpertRule {

    private FieldType fieldType;
    private OperatorType operatorType;

    @Builder.Default
    private String referenceValue = "";
    @Builder.Default
    @JsonDeserialize(as = HashSet.class)
    private Set<String> referenceValues = new HashSet<>();

    @Override
    public DataType getDataType() {
        return DataType.STRING;
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
    protected OperatorType getOperatorType() {
        return operatorType;
    }
}
