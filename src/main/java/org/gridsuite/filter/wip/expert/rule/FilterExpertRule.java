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
import com.powsybl.iidm.network.Network;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.expert.data.DataType;

import java.util.Collections;
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
public final class FilterExpertRule extends AbstractExpertRule {

    private OperatorType operatorType;
    @Builder.Default
    private Set<Filter> referenceFilters = Collections.emptySet();
    @Builder.Default
    private String networkIdCache = "";
    @Builder.Default
    @JsonDeserialize(as = HashSet.class)
    private Set<String> filterEvaluationCache = Collections.emptySet();

    @Override
    public DataType getDataType() {
        return DataType.FILTER;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        return switch (operatorType) {
            case IS_PART_OF -> evaluateIsPartOfOperator(identifiable);
            case IS_NOT_PART_OF -> !evaluateIsPartOfOperator(identifiable);
            default -> throw unsupportedOperatorException();
        };
    }

    @Override
    protected OperatorType getOperatorType() {
        return operatorType;
    }

    private boolean evaluateIsPartOfOperator(Identifiable<?> identifiable) {
        Network network = identifiable.getNetwork();
        if (networkIdCache.equals(network.getId())) {
            return filterEvaluationCache.contains(identifiable.getId());
        }

        Set<String> filterEvaluation = referenceFilters.stream()
                .map(filter -> filter.evaluate(identifiable.getNetwork()))
                .flatMap(List::stream)
                .map(Identifiable::getId)
                .collect(Collectors.toSet());

        networkIdCache = network.getId();
        filterEvaluationCache = filterEvaluation;
        return filterEvaluation.contains(identifiable.getId());
    }
}
