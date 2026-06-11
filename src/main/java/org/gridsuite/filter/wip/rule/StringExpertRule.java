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
import java.util.List;
import java.util.Objects;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StringExpertRule implements ExpertRule {

    private FieldType field;
    private OperatorType operator;
    private String value;
    private List<String> values;

    @Builder
    public StringExpertRule(FieldType field, OperatorType operator, String value, List<String> values) {
        this.field = Objects.requireNonNull(field);
        this.operator = Objects.requireNonNull(operator);
        this.value = value != null ? value : "";
        this.values = values != null ? List.copyOf(values) : Collections.emptyList();
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String fieldValue = ExpertFilterUtils.getFieldValue(field, null, identifiable);
        if (fieldValue == null || fieldValue.isEmpty()) {
            return operator.equals(OperatorType.NOT_EXISTS);
        }

        return switch (operator) {
            case IS -> Strings.CI.equals(fieldValue, value);
            case CONTAINS -> Strings.CI.contains(fieldValue, value);
            case BEGINS_WITH -> Strings.CI.startsWith(fieldValue, value);
            case ENDS_WITH -> Strings.CI.endsWith(fieldValue, value);
            case EXISTS -> true;
            case NOT_EXISTS -> false;
            case IN -> values.stream().anyMatch(fieldValue::equalsIgnoreCase);
            case NOT_IN -> values.stream().noneMatch(fieldValue::equalsIgnoreCase);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.STRING;
    }
}
