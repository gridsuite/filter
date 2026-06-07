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
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
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
public final class CombinatorExpertRule extends AbstractCachingExpertRule {

    @NonNull
    private CombinatorType combinatorType;

    @Builder.Default
    @JsonDeserialize(as = HashSet.class)
    private Set<ExpertRule> subRules = new HashSet<>();

    @Override
    public DataType getDataType() {
        return DataType.COMBINATOR;
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        return switch (combinatorType) {
            case AND -> subRules.stream().allMatch(rule -> rule.evaluateRule(identifiable));
            case OR -> subRules.stream().anyMatch(rule -> rule.evaluateRule(identifiable));
        };
    }

    @Override
    public void clearCache() {
        subRules.stream()
                .filter(rule -> rule instanceof AbstractCachingExpertRule)
                .map(rule -> (AbstractCachingExpertRule) rule)
                .forEach(AbstractCachingExpertRule::clearCache);
    }

    @Override
    protected OperatorType getOperatorType() {
        throw new UnsupportedOperationException("Operator type not applicable for combinator rules");
    }
}
