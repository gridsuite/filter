/*
 * Copyright (c) 2026, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 * SPDX-License-Identifier: MPL-2.0
 */

package org.gridsuite.filter.wip.expert.rules;

import com.google.common.annotations.Beta;
import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.Network;
import org.gridsuite.filter.utils.expertfilter.OperatorType;
import org.gridsuite.filter.wip.Filter;
import org.gridsuite.filter.wip.expert.data.DataType;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Kamil MARUT {@literal <kamil.marut at rte-france.com>}
 */
@Beta
public final class FilterExpertRule extends AbstractExpertRule {

    private static final Set<OperatorType> SUPPORTED_OPERATORS = Set.of(OperatorType.IS_PART_OF, OperatorType.IS_NOT_PART_OF);

    private final OperatorType operatorType;
    private final Set<Filter> referenceFilters;

    private String networkIdCache;
    private Set<String> filterEvaluationCache;

    public static FilterExpertRule of(OperatorType operatorType, Set<Filter> referenceFilters) {
        if (operatorType == null || referenceFilters == null || referenceFilters.isEmpty()) {
            throw new PowsyblException("Invalid filter expert rule, all parameters must be non null and reference filters must not be empty");
        }
        if (!SUPPORTED_OPERATORS.contains(operatorType)) {
            throw new PowsyblException("Invalid filter expert rule, operatorType must be one of " + SUPPORTED_OPERATORS);
        }

        return new FilterExpertRule(operatorType, referenceFilters);
    }

    private FilterExpertRule(OperatorType operatorType, Set<Filter> referenceFilters) {
        this.operatorType = operatorType;
        this.referenceFilters = Set.copyOf(referenceFilters);
        this.networkIdCache = "";
        this.filterEvaluationCache = Collections.emptySet();
    }

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
