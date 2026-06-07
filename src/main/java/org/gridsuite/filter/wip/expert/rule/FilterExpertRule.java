/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.annotations.Beta;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.expert.data.DataType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Beta
@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder
public final class FilterExpertRule extends AbstractCachingExpertRule {

    private FieldType fieldType;
    private OperatorType operatorType;

    @Builder.Default
    private Set<Filter> referenceFilters = new HashSet<>();

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @Builder.Default
    private Set<String> filterEvaluationCache = new HashSet<>();

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private boolean cachedFilterEvaluation = false;

    @Override
    public DataType getDataType() {
        return DataType.FILTER;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        Network network = identifiable.getNetwork();
        String targetId = ExpertFilterUtils.getFieldValue(fieldType, null, identifiable);

        return switch (operatorType) {
            case IS_PART_OF -> evaluateIsPartOfOperator(network, targetId);
            case IS_NOT_PART_OF -> !evaluateIsPartOfOperator(network, targetId);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    public void clearCache() {
        filterEvaluationCache.clear();
        cachedFilterEvaluation = false;
    }

    @Override
    protected OperatorType getOperatorType() {
        return operatorType;
    }

    private boolean evaluateIsPartOfOperator(Network network, String targetId) {
        if (cachedFilterEvaluation) {
            return filterEvaluationCache.contains(targetId);
        }

        Set<String> filterEvaluation = referenceFilters.stream()
                .map(filter -> filter.evaluate(network))
                .flatMap(List::stream)
                .map(Identifiable::getId)
                .collect(Collectors.toSet());

        filterEvaluationCache.addAll(filterEvaluation);
        cachedFilterEvaluation = true;
        return filterEvaluation.contains(targetId);
    }
}
