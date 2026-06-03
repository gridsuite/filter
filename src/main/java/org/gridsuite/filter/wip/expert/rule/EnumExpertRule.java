/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rule;

import com.google.common.annotations.Beta;
import com.powsybl.iidm.network.Identifiable;
import lombok.*;
import lombok.experimental.SuperBuilder;
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
public final class EnumExpertRule extends AbstractExpertRule {

    private FieldType fieldType;
    private OperatorType operatorType;

    @Builder.Default
    private String referenceValue = "";

    @Builder.Default
    private Set<String> referenceValues = new HashSet<>();

    @Override
    public DataType getDataType() {
        return DataType.ENUM;
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
    protected OperatorType getOperatorType() {
        return operatorType;
    }
}
