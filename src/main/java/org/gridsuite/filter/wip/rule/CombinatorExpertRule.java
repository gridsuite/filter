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
import lombok.*;
import org.gridsuite.filter.utils.expertfilter.CombinatorType;
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
public final class CombinatorExpertRule implements ExpertRule {

    private CombinatorType combinator;
    private List<ExpertRule> rules;

    @Builder
    public CombinatorExpertRule(CombinatorType combinator, List<ExpertRule> rules) {
        this.combinator = Objects.requireNonNull(combinator);
        this.rules = List.copyOf(Objects.requireNonNull(rules));
    }

    @Override
    public boolean evaluateRule(Identifiable<?> identifiable) {
        return switch (combinator) {
            case AND -> rules.stream().allMatch(rule -> rule.evaluateRule(identifiable));
            case OR -> rules.stream().anyMatch(rule -> rule.evaluateRule(identifiable));
        };
    }

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public DataType getDataType() {
        return DataType.COMBINATOR;
    }

    @Override
    @JsonIgnore
    public OperatorType getOperator() {
        throw new UnsupportedOperationException("Operator type not applicable for combinator rules");
    }

    @Override
    public void clearCache() {
        rules.forEach(ExpertRule::clearCache);
    }
}
