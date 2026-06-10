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
public final class EnumExpertRule implements ExpertRule {

    private FieldType fieldType;
    private OperatorType operatorType;
    private String referenceValue;
    private Set<String> referenceValues;

    @Builder
    public EnumExpertRule(FieldType fieldType, OperatorType operatorType, String referenceValue, Set<String> referenceValues) {
        this.fieldType = Objects.requireNonNull(fieldType);
        this.operatorType = Objects.requireNonNull(operatorType);
        this.referenceValue = referenceValue != null ? referenceValue : "";
        this.referenceValues = referenceValues != null ? Set.copyOf(referenceValues) : Collections.emptySet();
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String fieldValue = ExpertFilterUtils.getFieldValue(fieldType, null, identifiable);
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
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.ENUM;
    }
}
