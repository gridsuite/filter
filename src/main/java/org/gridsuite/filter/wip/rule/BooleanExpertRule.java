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

import java.util.Objects;
import java.util.Optional;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Data
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BooleanExpertRule implements ExpertRule {

    private FieldType fieldType;
    private OperatorType operatorType;
    private Boolean referenceValue;

    @Builder
    public BooleanExpertRule(FieldType fieldType, OperatorType operatorType, Boolean referenceValue) {
        this.fieldType = Objects.requireNonNull(fieldType);
        this.operatorType = Objects.requireNonNull(operatorType);
        this.referenceValue = referenceValue;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String fieldValue = ExpertFilterUtils.getFieldValue(fieldType, null, identifiable);
        if (fieldValue == null) {
            return OperatorType.NOT_EXISTS.equals(operatorType);
        }

        boolean parsedFieldValue = Boolean.parseBoolean(fieldValue);
        boolean parsedReferenceValue = Optional.ofNullable(referenceValue).orElse(false);
        return switch (operatorType) {
            case EQUALS -> parsedReferenceValue == parsedFieldValue;
            case NOT_EQUALS -> parsedReferenceValue != parsedFieldValue;
            case EXISTS -> true;
            case NOT_EXISTS -> false;
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.BOOLEAN;
    }
}
