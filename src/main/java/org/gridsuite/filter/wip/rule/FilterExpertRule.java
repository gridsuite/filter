/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.rule;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.annotations.Beta;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import lombok.*;
import org.gridsuite.filter.utils.expertfilter.ExpertFilterUtils;
import org.gridsuite.filter.utils.expertfilter.FieldType;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.data.DataType;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
@Getter
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FilterExpertRule implements ExpertRule {

    private FieldType field;
    private OperatorType operator;
    private List<Filter> filters;

    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private boolean cachedFilterEvaluation = false;
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private final Set<String> filterEvaluationCache = new HashSet<>();

    @Builder
    public FilterExpertRule(FieldType field, OperatorType operator, List<Filter> filters) {
        this.field = Objects.requireNonNull(field);
        this.operator = Objects.requireNonNull(operator);
        this.filters = List.copyOf(Objects.requireNonNull(filters));
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        Network network = identifiable.getNetwork();
        String targetId = ExpertFilterUtils.getFieldValue(field, null, identifiable);

        return switch (operator) {
            case IS_PART_OF -> evaluateIsPartOfOperator(network, targetId);
            case IS_NOT_PART_OF -> !evaluateIsPartOfOperator(network, targetId);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.FILTER;
    }

    @Override
    public void clearCache() {
        filterEvaluationCache.clear();
        cachedFilterEvaluation = false;
    }

    private boolean evaluateIsPartOfOperator(Network network, String targetId) {
        if (!cachedFilterEvaluation) {
            Set<String> filterEvaluation = filters.stream()
                    .map(filter -> filter.evaluate(network))
                    .flatMap(List::stream)
                    .map(Identifiable::getId)
                    .collect(Collectors.toSet());

            filterEvaluationCache.addAll(filterEvaluation);
            cachedFilterEvaluation = true;
        }

        return filterEvaluationCache.contains(targetId);
    }
}
