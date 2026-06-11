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

import java.util.List;
import java.util.Objects;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PropertiesExpertRule implements ExpertRule {

    private FieldType field;
    private OperatorType operator;
    private String propertyName;
    private List<String> propertyValues;

    @Builder
    public PropertiesExpertRule(FieldType field, OperatorType operator, String propertyName, List<String> propertyValues) {
        this.field = Objects.requireNonNull(field);
        this.operator = Objects.requireNonNull(operator);
        this.propertyName = Objects.requireNonNull(propertyName);
        this.propertyValues = List.copyOf(Objects.requireNonNull(propertyValues));
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        String propertyValue = ExpertFilterUtils.getFieldValue(field, propertyName, identifiable);
        if (propertyValue == null) {
            return false;
        }

        return switch (operator) {
            case IN -> propertyValues.stream().anyMatch(propertyValue::equalsIgnoreCase);
            case NOT_IN -> propertyValues.stream().noneMatch(propertyValue::equalsIgnoreCase);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.PROPERTIES;
    }
}
